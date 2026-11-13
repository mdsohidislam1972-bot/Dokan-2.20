const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const firebaseCompatPath = require.resolve("firebase/compat/app", {
  paths: ["/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules"],
});
const firebase = require(firebaseCompatPath).default || require(firebaseCompatPath);
require(require.resolve("firebase/compat/firestore", {
  paths: ["/usr/local/lib/node_modules/@firebase/rules-unit-testing/node_modules"],
}));
const FieldValue = firebase.firestore.FieldValue;

const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

const OWNER_UID = "owner_uid_1";
const STAFF_UID = "staff_uid_2";
const THIRD_UID = "third_uid_3";
const STORE_ID = "store_test_1";
const VALID_CODE = "K7M9P2XQ";

const LIMITED_PERMS = {
  sell: true,
  view_stock: true,
  add_edit_products: false,
  see_buying_price_and_profit: false,
  history: false,
  customers_and_baki: true,
  receive_payment: false,
  suppliers_and_purchases: false,
  pay_supplier: false,
  reports: false,
  cash_and_bank: false,
  expenses_and_other_income: false,
  notes: true,
};

let testEnv;

function dbFor(context) {
  return context.firestore();
}

function fieldValueFor() {
  return FieldValue;
}

async function seedStoreAndInvite({
  code = VALID_CODE,
  status = "active",
  expiresAt = Date.now() + 24 * 60 * 60 * 1000,
  extraMemberUids = [],
} = {}) {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = dbFor(ctx);
    await adminDb.collection("stores").doc(STORE_ID).set({
      name: "Test Dokan",
      phone: "+8801700000000",
      address: "Dhaka",
      ownerId: OWNER_UID,
      memberUids: [OWNER_UID, ...extraMemberUids],
    });
    await adminDb.collection("stores").doc(STORE_ID).collection("products").doc("prod_1").set({
      name: "Rice",
      stock: 10,
      sellPrice: 100,
    });
    await adminDb.collection("invites").doc(code).set({
      code,
      storeId: STORE_ID,
      storeName: "Test Dokan",
      staffName: "Rahim Staff",
      roleLabel: "Sales",
      permissionLevel: "Limited",
      permissions: LIMITED_PERMS,
      createdByUid: OWNER_UID,
      createdByName: "Owner",
      status,
      createdAt: Date.now() - 1000,
      expiresAt,
      usedByUid: "",
      usedByEmail: "",
      usedAt: 0,
    });
  });
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated: cannot get invite or read store", async () => {
  await seedStoreAndInvite();
  const unauthDb = dbFor(testEnv.unauthenticatedContext());
  await assertFails(unauthDb.collection("invites").doc(VALID_CODE).get());
  await assertFails(unauthDb.collection("stores").doc(STORE_ID).get());
});

test("Non-member: can get single invite by code, CANNOT list invites, CANNOT read store or products", async () => {
  await seedStoreAndInvite();
  const staffDb = dbFor(testEnv.authenticatedContext(STAFF_UID));

  const snap = await assertSucceeds(staffDb.collection("invites").doc(VALID_CODE).get());
  assert.equal(snap.exists, true);
  assert.equal(snap.data().code, VALID_CODE);

  await assertFails(staffDb.collection("invites").get());
  await assertFails(staffDb.collection("invites").where("storeId", "==", STORE_ID).get());
  await assertFails(staffDb.collection("stores").doc(STORE_ID).get());
  await assertFails(staffDb.collection("stores").doc(STORE_ID).collection("products").doc("prod_1").get());
  await assertFails(staffDb.collection("stores").doc(STORE_ID).collection("meta").doc("counters").get());
});

test("Owner: can create 8-char invite and query invites for their store", async () => {
  await seedStoreAndInvite();
  const ownerDb = dbFor(testEnv.authenticatedContext(OWNER_UID));

  await assertSucceeds(
    ownerDb.collection("invites").doc("W9K2P3XR").set({
      code: "W9K2P3XR",
      storeId: STORE_ID,
      storeName: "Test Dokan",
      staffName: "Karim",
      roleLabel: "Manager",
      permissionLevel: "Half",
      permissions: LIMITED_PERMS,
      createdByUid: OWNER_UID,
      createdByName: "Owner",
      status: "active",
      createdAt: Date.now(),
      expiresAt: Date.now() + 86400000,
      usedByUid: "",
      usedByEmail: "",
      usedAt: 0,
    })
  );

  const listSnap = await assertSucceeds(
    ownerDb.collection("invites").where("storeId", "==", STORE_ID).get()
  );
  assert.equal(listSnap.size, 2);
});

test("Staff join: atomic transaction succeeds, copies role/permissions, then staff can read store", async () => {
  await seedStoreAndInvite();
  const staffDb = dbFor(testEnv.authenticatedContext(STAFF_UID, { email: "staff@example.com" }));
  const FV = fieldValueFor(staffDb);

  await assertSucceeds(
    staffDb.runTransaction(async (tx) => {
      const inviteRef = staffDb.collection("invites").doc(VALID_CODE);
      const inviteSnap = await tx.get(inviteRef);
      assert.equal(inviteSnap.exists, true);
      const inv = inviteSnap.data();

      const storeRef = staffDb.collection("stores").doc(inv.storeId);
      const memberRef = storeRef.collection("members").doc(STAFF_UID);
      const userRef = staffDb.collection("users").doc(STAFF_UID);

      tx.update(inviteRef, {
        status: "used",
        usedByUid: STAFF_UID,
        usedByEmail: "staff@example.com",
        usedAt: Date.now(),
        updatedAt: FV.serverTimestamp(),
      });
      tx.set(memberRef, {
        userId: STAFF_UID,
        displayName: inv.staffName,
        email: "staff@example.com",
        role: inv.roleLabel.toLowerCase(),
        roleLabel: inv.roleLabel,
        permissionLevel: inv.permissionLevel,
        permissions: inv.permissions,
        status: "Active",
        joinedTime: Date.now(),
        invitedBy: inv.createdByUid,
        inviteCode: VALID_CODE,
      });
      tx.update(storeRef, {
        memberUids: FV.arrayUnion(STAFF_UID),
        updatedAt: FV.serverTimestamp(),
      });
      tx.set(
        userRef,
        {
          email: "staff@example.com",
          storeId: inv.storeId,
          role: inv.roleLabel,
          updatedAt: FV.serverTimestamp(),
        },
        { merge: true }
      );
    })
  );

  // Staff is now a member: can read store & products, create notes, CANNOT add/edit products or reports or edit own permissions
  const storeSnap = await assertSucceeds(staffDb.collection("stores").doc(STORE_ID).get());
  assert.ok(storeSnap.data().memberUids.includes(STAFF_UID));
  await assertSucceeds(staffDb.collection("stores").doc(STORE_ID).collection("products").doc("prod_1").get());
  await assertSucceeds(
    staffDb.collection("stores").doc(STORE_ID).collection("notes").doc("n1").set({ title: "Shift note", text: "OK" })
  );
  await assertFails(
    staffDb.collection("stores").doc(STORE_ID).collection("products").doc("prod_1").update({ sellPrice: 999 })
  );
  await assertFails(
    staffDb.collection("stores").doc(STORE_ID).collection("members").doc(STAFF_UID).update({
      roleLabel: "Owner",
      role: "owner",
    })
  );
  await assertFails(
    staffDb.collection("stores").doc(STORE_ID).collection("members").doc(STAFF_UID).update({
      "permissions.reports": true,
    })
  );
});

test("Blocked joins: reuse fails, expired fails, cancelled fails, forged role/permissions fail", async () => {
  // 1. Expired code fails
  await seedStoreAndInvite({ code: "EXP12345", expiresAt: Date.now() - 5000 });
  const staffDb = dbFor(testEnv.authenticatedContext(STAFF_UID, { email: "staff@example.com" }));
  const FV = fieldValueFor(staffDb);

  await assertFails(
    staffDb.runTransaction(async (tx) => {
      tx.update(staffDb.collection("invites").doc("EXP12345"), {
        status: "used",
        usedByUid: STAFF_UID,
        usedByEmail: "staff@example.com",
        usedAt: Date.now(),
      });
      tx.set(staffDb.collection("stores").doc(STORE_ID).collection("members").doc(STAFF_UID), {
        userId: STAFF_UID,
        displayName: "Rahim Staff",
        email: "staff@example.com",
        role: "sales",
        roleLabel: "Sales",
        permissionLevel: "Limited",
        permissions: LIMITED_PERMS,
        status: "Active",
        joinedTime: Date.now(),
        invitedBy: OWNER_UID,
        inviteCode: "EXP12345",
      });
      tx.update(staffDb.collection("stores").doc(STORE_ID), { memberUids: FV.arrayUnion(STAFF_UID) });
    })
  );

  // 2. Cancelled code fails
  await seedStoreAndInvite({ code: "CAN12345", status: "cancelled" });
  await assertFails(
    staffDb.runTransaction(async (tx) => {
      tx.update(staffDb.collection("invites").doc("CAN12345"), {
        status: "used",
        usedByUid: STAFF_UID,
        usedByEmail: "staff@example.com",
        usedAt: Date.now(),
      });
      tx.set(staffDb.collection("stores").doc(STORE_ID).collection("members").doc(STAFF_UID), {
        userId: STAFF_UID,
        displayName: "Rahim Staff",
        email: "staff@example.com",
        role: "sales",
        roleLabel: "Sales",
        permissionLevel: "Limited",
        permissions: LIMITED_PERMS,
        status: "Active",
        joinedTime: Date.now(),
        invitedBy: OWNER_UID,
        inviteCode: "CAN12345",
      });
      tx.update(staffDb.collection("stores").doc(STORE_ID), { memberUids: FV.arrayUnion(STAFF_UID) });
    })
  );

  // 3. Used code fails
  await seedStoreAndInvite({ code: "USE12345", status: "used" });
  const thirdDb = dbFor(testEnv.authenticatedContext(THIRD_UID, { email: "third@example.com" }));
  const FV3 = fieldValueFor(thirdDb);
  await assertFails(
    thirdDb.runTransaction(async (tx) => {
      tx.update(thirdDb.collection("invites").doc("USE12345"), {
        status: "used",
        usedByUid: THIRD_UID,
        usedByEmail: "third@example.com",
        usedAt: Date.now(),
      });
      tx.set(thirdDb.collection("stores").doc(STORE_ID).collection("members").doc(THIRD_UID), {
        userId: THIRD_UID,
        displayName: "Attacker",
        email: "third@example.com",
        role: "sales",
        roleLabel: "Sales",
        permissionLevel: "Limited",
        permissions: LIMITED_PERMS,
        status: "Active",
        joinedTime: Date.now(),
        invitedBy: OWNER_UID,
        inviteCode: "USE12345",
      });
      tx.update(thirdDb.collection("stores").doc(STORE_ID), { memberUids: FV3.arrayUnion(THIRD_UID) });
    })
  );

  // 4. Forged permissions or Owner role fails
  await seedStoreAndInvite({ code: "FRG23456", status: "active" });
  await assertFails(
    thirdDb.runTransaction(async (tx) => {
      tx.update(thirdDb.collection("invites").doc("FRG23456"), {
        status: "used",
        usedByUid: THIRD_UID,
        usedByEmail: "third@example.com",
        usedAt: Date.now(),
      });
      tx.set(thirdDb.collection("stores").doc(STORE_ID).collection("members").doc(THIRD_UID), {
        userId: THIRD_UID,
        displayName: "Forged",
        email: "third@example.com",
        role: "owner",
        roleLabel: "Owner",
        permissionLevel: "Full",
        permissions: { ...LIMITED_PERMS, reports: true },
        status: "Active",
        joinedTime: Date.now(),
        invitedBy: OWNER_UID,
        inviteCode: "FRG23456",
      });
      tx.update(thirdDb.collection("stores").doc(STORE_ID), { memberUids: FV3.arrayUnion(THIRD_UID) });
    })
  );
});

test("Limited staff: blocked from unpermitted reads/writes, allowed permitted sell/stock/customers/notes", async () => {
  await seedStoreAndInvite({ extraMemberUids: [STAFF_UID] });
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = dbFor(ctx);
    const storeRef = adminDb.collection("stores").doc(STORE_ID);
    await storeRef.collection("members").doc(STAFF_UID).set({
      userId: STAFF_UID,
      displayName: "Rahim Staff",
      email: "staff@example.com",
      role: "sales",
      roleLabel: "Sales",
      permissionLevel: "Limited",
      permissions: LIMITED_PERMS,
      status: "Active",
      joinedTime: Date.now(),
      invitedBy: OWNER_UID,
      inviteCode: VALID_CODE,
    });
    await storeRef.collection("accounts").doc("cash_in_hand").set({
      storeId: STORE_ID,
      name: "Cash in Hand",
      type: "cash",
      openingBalance: 1000,
      isDefault: true,
      isArchived: false,
    });
    await storeRef.collection("expenses").doc("exp_1").set({ storeId: STORE_ID, amount: 200, category: "Tea" });
    await storeRef.collection("otherIncomes").doc("inc_1").set({ storeId: STORE_ID, amount: 150, category: "Commission" });
    await storeRef.collection("suppliers").doc("sup_1").set({ storeId: STORE_ID, name: "Wholesaler", balance: 500 });
    await storeRef.collection("purchases").doc("pur_1").set({ storeId: STORE_ID, totalAmount: 500 });
    await storeRef.collection("sales").doc("sale_1").set({ storeId: STORE_ID, grandTotal: 100, soldBy: OWNER_UID });
    await storeRef.collection("payments").doc("pay_1").set({ storeId: STORE_ID, amount: 50 });
    await storeRef.collection("customers").doc("cust_1").set({ storeId: STORE_ID, name: "Karim", currentBalance: 300 });
  });

  const staffDb = dbFor(testEnv.authenticatedContext(STAFF_UID));
  const storeRef = staffDb.collection("stores").doc(STORE_ID);

  // Blocked reads for Limited staff
  await assertFails(storeRef.collection("accounts").get());
  await assertFails(storeRef.collection("accountAdjustments").get());
  await assertFails(storeRef.collection("expenses").get());
  await assertFails(storeRef.collection("otherIncomes").get());
  await assertFails(storeRef.collection("suppliers").get());
  await assertFails(storeRef.collection("purchases").get());
  await assertFails(storeRef.collection("purchaseReturns").get());
  await assertFails(storeRef.collection("supplierPayments").get());
  await assertFails(storeRef.collection("sales").get());
  await assertFails(storeRef.collection("payments").get());
  await assertFails(storeRef.collection("healthFixLogs").get());

  // Blocked writes for Limited staff
  await assertFails(storeRef.collection("accounts").doc("acc_2").set({ name: "bKash" }));
  await assertFails(storeRef.collection("expenses").doc("exp_2").set({ amount: 50 }));
  await assertFails(storeRef.collection("payments").doc("pay_2").set({ amount: 50 }));
  await assertFails(storeRef.collection("products").doc("prod_1").update({ sellPrice: 200 }));
  await assertFails(storeRef.collection("products").doc("prod_2").set({ name: "Oil", stock: 5 }));
  await assertFails(storeRef.update({ name: "Hacked Store" }));
  await assertFails(storeRef.collection("members").doc(OWNER_UID).update({ roleLabel: "Staff", role: "staff" }));

  // Permitted reads & writes for Limited staff (sell, stock deduction, customers, notes)
  await assertSucceeds(storeRef.collection("products").get());
  await assertSucceeds(storeRef.collection("customers").get());
  await assertSucceeds(storeRef.collection("products").doc("prod_1").update({ stock: 8 }));
  await assertSucceeds(storeRef.collection("sales").doc("sale_staff").set({ storeId: STORE_ID, grandTotal: 200, soldBy: STAFF_UID }));
  await assertSucceeds(storeRef.collection("notes").doc("note_1").set({ storeId: STORE_ID, title: "Morning note" }));
});

test("Owner-only & Removed member: live permission upgrade works, owner protected, removed gets nothing", async () => {
  await seedStoreAndInvite({ extraMemberUids: [STAFF_UID] });
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const adminDb = dbFor(ctx);
    const storeRef = adminDb.collection("stores").doc(STORE_ID);
    await storeRef.collection("members").doc(OWNER_UID).set({
      userId: OWNER_UID,
      displayName: "Owner",
      role: "owner",
      roleLabel: "Owner",
      permissionLevel: "Full",
      permissions: { ...LIMITED_PERMS, cash_and_bank: true, reports: true },
      status: "Active",
    });
    await storeRef.collection("members").doc(STAFF_UID).set({
      userId: STAFF_UID,
      displayName: "Rahim Staff",
      role: "sales",
      roleLabel: "Sales",
      permissionLevel: "Limited",
      permissions: LIMITED_PERMS,
      status: "Active",
    });
    await storeRef.collection("accounts").doc("cash_in_hand").set({
      storeId: STORE_ID,
      name: "Cash in Hand",
      isDefault: true,
      isArchived: false,
    });
  });

  const ownerDb = dbFor(testEnv.authenticatedContext(OWNER_UID));
  const staffDb = dbFor(testEnv.authenticatedContext(STAFF_UID));
  const FV = fieldValueFor(ownerDb);

  // Initially denied accounts
  await assertFails(staffDb.collection("stores").doc(STORE_ID).collection("accounts").get());

  // Owner grants cash_and_bank live
  await assertSucceeds(
    ownerDb.collection("stores").doc(STORE_ID).collection("members").doc(STAFF_UID).update({
      permissionLevel: "Selected",
      permissions: { ...LIMITED_PERMS, cash_and_bank: true },
    })
  );

  // Staff can now read & create accounts, STILL cannot adjust balance or archive account
  await assertSucceeds(staffDb.collection("stores").doc(STORE_ID).collection("accounts").get());
  await assertSucceeds(
    staffDb.collection("stores").doc(STORE_ID).collection("accounts").doc("bkash_1").set({
      storeId: STORE_ID,
      name: "bKash",
      type: "mobile",
      isArchived: false,
    })
  );
  await assertFails(
    staffDb.collection("stores").doc(STORE_ID).collection("accounts").doc("bkash_1").update({ isArchived: true })
  );
  await assertFails(
    staffDb.collection("stores").doc(STORE_ID).collection("accountAdjustments").doc("adj_1").set({ amount: 500 })
  );

  // Owner cannot demote or remove self
  await assertFails(
    ownerDb.collection("stores").doc(STORE_ID).collection("members").doc(OWNER_UID).update({ status: "Removed" })
  );
  await assertFails(
    ownerDb.collection("stores").doc(STORE_ID).collection("members").doc(OWNER_UID).update({ roleLabel: "Sales", role: "sales" })
  );

  // Owner removes staff member -> Removed member gets ZERO reads or writes everywhere
  await assertSucceeds(
    ownerDb.collection("stores").doc(STORE_ID).collection("members").doc(STAFF_UID).update({ status: "Removed" })
  );
  await assertSucceeds(
    ownerDb.collection("stores").doc(STORE_ID).update({ memberUids: FV.arrayRemove(STAFF_UID) })
  );

  await assertFails(staffDb.collection("stores").doc(STORE_ID).get());
  await assertFails(staffDb.collection("stores").doc(STORE_ID).collection("members").doc(STAFF_UID).get());
  await assertFails(staffDb.collection("stores").doc(STORE_ID).collection("products").get());
  await assertFails(staffDb.collection("stores").doc(STORE_ID).collection("notes").get());
  await assertFails(staffDb.collection("stores").doc(STORE_ID).collection("sales").doc("s2").set({ soldBy: STAFF_UID }));
});

