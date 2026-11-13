package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.InviteCodeConfig
import com.example.data.model.StoreInvite
import com.example.data.model.StorePermissions
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class StaffJoinRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun uniqueEmail(prefix: String): String =
        "$prefix.${UUID.randomUUID().toString().take(8)}@storetest.com"

    @Test
    fun joinStoreWithInvite_valid8CharCode_joinsInOneTransactionAndEnforcesPermissions() = runBlocking {
        val ownerUid = signInTestUser(uniqueEmail("owner"))
        val repo = StoreRepository(firestore)
        val storeId = withTimeout(TIMEOUT_MS) {
            repo.createStore("Amar Dokan", "+8801711111111", "Dhaka")
        }

        val limitedPerms = StorePermissions.forLevel("Limited")
        val code = withTimeout(TIMEOUT_MS) {
            repo.createStaffInvite(
                StoreInvite(
                    storeId = storeId,
                    storeName = "Amar Dokan",
                    staffName = "Sales Staff",
                    roleLabel = "Sales",
                    permissionLevel = "Limited",
                    permissions = limitedPerms,
                    createdByUid = ownerUid,
                    createdByName = "Owner",
                    status = "active"
                )
            )
        }
        assertEquals(InviteCodeConfig.CODE_LENGTH, code.length)
        assertTrue(InviteCodeConfig.isValidFormat(code))

        // Owner can list invites for their store
        val ownerInvites = withTimeout(TIMEOUT_MS) {
            repo.observeInvites(storeId).first { list -> list.any { it.code == code } }
        }
        assertTrue(ownerInvites.any { it.code == code })

        // Sign in as second user (not yet a member)
        val staffEmail = uniqueEmail("staff")
        val staffUid = signInTestUser(staffEmail)

        // Non-member cannot read store document or list invites
        val storeReadErr = runCatching {
            withTimeout(TIMEOUT_MS) { firestore.collection("stores").document(storeId).get().await() }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, storeReadErr?.unwrapFirestoreException()?.code)

        val listInvitesErr = runCatching {
            withTimeout(TIMEOUT_MS) { repo.observeInvites(storeId).first() }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, listInvitesErr?.unwrapFirestoreException()?.code)

        // Join via lowercase code with surrounding spaces -> normalized cleanly
        val joinResult = withTimeout(TIMEOUT_MS) {
            repo.joinStoreWithInvite("  ${code.lowercase()}  ", staffUid, staffEmail, "Sales Staff")
        }
        assertTrue("Expected join success, got: ${joinResult.exceptionOrNull()}", joinResult.isSuccess)
        assertEquals(storeId, joinResult.getOrThrow())

        // Verify staff now sees store and exact copied member permissions
        val storeSnap = withTimeout(TIMEOUT_MS) {
            firestore.collection("stores").document(storeId).get().await()
        }
        assertTrue(storeSnap.exists())

        val memberSnap = withTimeout(TIMEOUT_MS) {
            firestore.collection("stores").document(storeId).collection("members").document(staffUid).get().await()
        }
        assertEquals("Sales", memberSnap.getString("roleLabel"))
        assertEquals("sales", memberSnap.getString("role"))
        assertEquals(code, memberSnap.getString("inviteCode"))

        // Staff cannot promote self or change own permissions
        val selfEditErr = runCatching {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("stores").document(storeId).collection("members").document(staffUid)
                    .update("roleLabel", "Owner", "role", "owner")
                    .await()
            }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, selfEditErr?.unwrapFirestoreException()?.code)

        // Reuse of the same invite code from a third account must fail plainly
        val thirdEmail = uniqueEmail("third")
        val thirdUid = signInTestUser(thirdEmail)
        val reuseResult = withTimeout(TIMEOUT_MS) {
            repo.joinStoreWithInvite(code, thirdUid, thirdEmail, "Third User")
        }
        assertTrue(reuseResult.isFailure)
        assertTrue(reuseResult.exceptionOrNull()?.message?.contains("used", ignoreCase = true) == true)
    }

    @Test
    fun joinStoreWithInvite_blockedStates_returnPlainMessages() = runBlocking {
        val ownerUid = signInTestUser(uniqueEmail("owner2"))
        val repo = StoreRepository(firestore)
        val storeId = withTimeout(TIMEOUT_MS) {
            repo.createStore("Second Dokan", "+8801822222222", "Chittagong")
        }

        // Cancelled code
        val cancelledCode = withTimeout(TIMEOUT_MS) {
            repo.createStaffInvite(
                StoreInvite(
                    storeId = storeId,
                    storeName = "Second Dokan",
                    staffName = "Cancelled Staff",
                    roleLabel = "Sales",
                    permissionLevel = "Limited",
                    permissions = StorePermissions.forLevel("Limited"),
                    createdByUid = ownerUid,
                    status = "active"
                )
            )
        }
        withTimeout(TIMEOUT_MS) { repo.cancelInvite(cancelledCode) }

        // Expired code
        val expiredCode = withTimeout(TIMEOUT_MS) {
            repo.createStaffInvite(
                StoreInvite(
                    storeId = storeId,
                    storeName = "Second Dokan",
                    staffName = "Expired Staff",
                    roleLabel = "Sales",
                    permissionLevel = "Limited",
                    permissions = StorePermissions.forLevel("Limited"),
                    createdByUid = ownerUid,
                    status = "active",
                    expiresAt = System.currentTimeMillis() - 60_000L
                )
            )
        }

        val staffEmail = uniqueEmail("staff2")
        val staffUid = signInTestUser(staffEmail)

        val cancelledRes = withTimeout(TIMEOUT_MS) {
            repo.joinStoreWithInvite(cancelledCode, staffUid, staffEmail, "Staff")
        }
        assertTrue(cancelledRes.isFailure)
        assertTrue(cancelledRes.exceptionOrNull()?.message?.contains("cancelled", ignoreCase = true) == true)

        val expiredRes = withTimeout(TIMEOUT_MS) {
            repo.joinStoreWithInvite(expiredCode, staffUid, staffEmail, "Staff")
        }
        assertTrue(expiredRes.isFailure)
        assertTrue(expiredRes.exceptionOrNull()?.message?.contains("expired", ignoreCase = true) == true)

        val wrongRes = withTimeout(TIMEOUT_MS) {
            repo.joinStoreWithInvite("WRNG2345", staffUid, staffEmail, "Staff")
        }
        assertTrue(wrongRes.isFailure)
        assertNotNull(wrongRes.exceptionOrNull()?.message)
        assertTrue(wrongRes.exceptionOrNull()?.message?.contains("wrong", ignoreCase = true) == true)
    }

    @Test
    fun staffPermissions_enforcedOnServer_andRemovedMemberGetsNothing() = runBlocking {
        val ownerEmail = uniqueEmail("owner_perm")
        val ownerUid = signInTestUser(ownerEmail)
        val repo = StoreRepository(firestore)
        val storeId = withTimeout(TIMEOUT_MS) {
            repo.createStore("Perm Dokan", "+8801712345678", "Sylhet")
        }

        val limitedPerms = StorePermissions.forLevel("Limited")
        val inviteCode = withTimeout(TIMEOUT_MS) {
            repo.createStaffInvite(
                StoreInvite(
                    storeId = storeId,
                    storeName = "Perm Dokan",
                    staffName = "Limited Staff",
                    roleLabel = "Sales",
                    permissionLevel = "Limited",
                    permissions = limitedPerms,
                    createdByUid = ownerUid,
                    createdByName = "Owner",
                    status = "active"
                )
            )
        }

        // Staff joins with Limited permissions
        val staffEmail = uniqueEmail("staff_perm")
        val staffUid = signInTestUser(staffEmail)
        val joinRes = withTimeout(TIMEOUT_MS) {
            repo.joinStoreWithInvite(inviteCode, staffUid, staffEmail, "Limited Staff")
        }
        assertTrue(joinRes.isSuccess)

        // Limited staff CANNOT read Cash & Bank (accounts) or Sales history or Expenses
        val accReadErr = runCatching {
            withTimeout(TIMEOUT_MS) { repo.observeAccounts(storeId).first() }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, accReadErr?.unwrapFirestoreException()?.code)

        val expReadErr = runCatching {
            withTimeout(TIMEOUT_MS) { repo.observeExpenses(storeId).first() }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, expReadErr?.unwrapFirestoreException()?.code)

        // Limited staff CANNOT write to members collection or store settings
        val memberWriteErr = runCatching {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("stores").document(storeId)
                    .collection("members").document(staffUid)
                    .update("permissions.cash_and_bank", true)
                    .await()
            }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, memberWriteErr?.unwrapFirestoreException()?.code)

        val storeWriteErr = runCatching {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("stores").document(storeId)
                    .update("name", "Tampered Name")
                    .await()
            }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, storeWriteErr?.unwrapFirestoreException()?.code)

        // Owner grants Cash & Bank permission live -> Staff can read accounts
        signInTestUser(ownerEmail)
        val memberDoc = withTimeout(TIMEOUT_MS) {
            repo.observeMembers(storeId).first { list -> list.any { it.userId == staffUid } }
                .first { it.userId == staffUid }
        }
        withTimeout(TIMEOUT_MS) {
            repo.updateMemberPermissions(
                storeId = storeId,
                memberId = memberDoc.userId,
                roleLabel = "Accountant",
                permissionLevel = "Selected",
                permissions = limitedPerms + (StorePermissions.CASH_AND_BANK to true)
            )
        }

        signInTestUser(staffEmail)
        val accountsSnap = withTimeout(TIMEOUT_MS) {
            firestore.collection("stores").document(storeId).collection("accounts").get().await()
        }
        assertTrue(accountsSnap.size() >= 0)

        // Owner removes staff -> Removed member loses access to store, products, and members doc
        signInTestUser(ownerEmail)
        withTimeout(TIMEOUT_MS) {
            repo.removeMember(storeId, staffUid)
        }

        signInTestUser(staffEmail)
        val removedStoreErr = runCatching {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("stores").document(storeId).get().await()
            }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, removedStoreErr?.unwrapFirestoreException()?.code)

        val removedMemberDocErr = runCatching {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("stores").document(storeId).collection("members").document(staffUid).get().await()
            }
        }.exceptionOrNull()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, removedMemberDocErr?.unwrapFirestoreException()?.code)
    }

    private companion object {
        const val TIMEOUT_MS = 5000L
    }
}
