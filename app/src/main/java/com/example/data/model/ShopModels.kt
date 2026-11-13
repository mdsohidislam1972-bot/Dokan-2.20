package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val email: String = "",
    val storeId: String = "",
    val role: String = "owner",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object StorePermissions {
    const val SELL = "sell"
    const val VIEW_STOCK = "view_stock"
    const val ADD_EDIT_PRODUCTS = "add_edit_products"
    const val SEE_BUYING_PRICE_AND_PROFIT = "see_buying_price_and_profit"
    const val HISTORY = "history"
    const val CUSTOMERS_AND_BAKI = "customers_and_baki"
    const val RECEIVE_PAYMENT = "receive_payment"
    const val SUPPLIERS_AND_PURCHASES = "suppliers_and_purchases"
    const val PAY_SUPPLIER = "pay_supplier"
    const val REPORTS = "reports"
    const val CASH_AND_BANK = "cash_and_bank"
    const val EXPENSES_AND_OTHER_INCOME = "expenses_and_other_income"
    const val NOTES = "notes"

    val ALL_KEYS = listOf(
        SELL,
        VIEW_STOCK,
        ADD_EDIT_PRODUCTS,
        SEE_BUYING_PRICE_AND_PROFIT,
        HISTORY,
        CUSTOMERS_AND_BAKI,
        RECEIVE_PAYMENT,
        SUPPLIERS_AND_PURCHASES,
        PAY_SUPPLIER,
        REPORTS,
        CASH_AND_BANK,
        EXPENSES_AND_OTHER_INCOME,
        NOTES
    )

    fun defaultOwnerPermissions(): Map<String, Boolean> = ALL_KEYS.associateWith { true }

    fun forLevel(level: String): Map<String, Boolean> = when (level.lowercase()) {
        "full" -> ALL_KEYS.associateWith { true }
        "half" -> mapOf(
            SELL to true,
            VIEW_STOCK to true,
            ADD_EDIT_PRODUCTS to true,
            SEE_BUYING_PRICE_AND_PROFIT to false,
            HISTORY to true,
            CUSTOMERS_AND_BAKI to true,
            RECEIVE_PAYMENT to true,
            SUPPLIERS_AND_PURCHASES to true,
            PAY_SUPPLIER to false,
            REPORTS to false,
            CASH_AND_BANK to false,
            EXPENSES_AND_OTHER_INCOME to true,
            NOTES to true
        )
        "limited" -> mapOf(
            SELL to true,
            VIEW_STOCK to true,
            ADD_EDIT_PRODUCTS to false,
            SEE_BUYING_PRICE_AND_PROFIT to false,
            HISTORY to false,
            CUSTOMERS_AND_BAKI to true,
            RECEIVE_PAYMENT to false,
            SUPPLIERS_AND_PURCHASES to false,
            PAY_SUPPLIER to false,
            REPORTS to false,
            CASH_AND_BANK to false,
            EXPENSES_AND_OTHER_INCOME to false,
            NOTES to true
        )
        else -> ALL_KEYS.associateWith { false }
    }
}

@IgnoreExtraProperties
data class StoreMember(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "owner", // "owner", "manager", "staff"
    val roleLabel: String = "Owner", // "Owner", "Partner", "Manager", "Accountant", "Sales", "Stock", "Entry"
    val permissionLevel: String = "Full", // "Full", "Half", "Limited", "Selected"
    val permissions: Map<String, Boolean> = StorePermissions.defaultOwnerPermissions(),
    val status: String = "Active", // "Active", "Pending", "Removed"
    val joinedTime: Long = System.currentTimeMillis(),
    val invitedBy: String = "",
    val inviteCode: String = ""
)

object InviteCodeConfig {
    const val CODE_LENGTH = 8
    // Uppercase letters & digits without confusing characters (O, 0, I, 1, L)
    const val ALLOWED_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    const val EXAMPLE_CODE = "K7M9P2XQ"
    private val secureRandom = java.security.SecureRandom()

    fun generate(): String {
        return buildString(CODE_LENGTH) {
            repeat(CODE_LENGTH) {
                append(ALLOWED_CHARS[secureRandom.nextInt(ALLOWED_CHARS.length)])
            }
        }
    }

    fun normalize(raw: String): String {
        return raw.trim().replace(Regex("\\s+"), "").uppercase()
    }

    fun sanitizeInput(raw: String): String {
        return raw.filter { it.isLetterOrDigit() }.uppercase().take(CODE_LENGTH)
    }

    fun isValidFormat(raw: String): Boolean {
        val normalized = normalize(raw)
        return normalized.length == CODE_LENGTH && normalized.all { it in ALLOWED_CHARS }
    }
}

@IgnoreExtraProperties
data class StoreInvite(
    @DocumentId val id: String = "",
    val code: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val staffName: String = "",
    val roleLabel: String = "Sales",
    val permissionLevel: String = "Limited",
    val permissions: Map<String, Boolean> = emptyMap(),
    val createdByUid: String = "",
    val createdByName: String = "",
    val status: String = "active", // "active", "used", "cancelled"
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L), // 24 hours
    val usedByUid: String = "",
    val usedByEmail: String = "",
    val usedAt: Long = 0L
)

@IgnoreExtraProperties
data class Store(
    @DocumentId val id: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val ownerId: String = "",
    val memberUids: List<String> = emptyList(),
    val members: List<StoreMember> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val name: String = "",
    val nameBn: String = "",
    val barcode: String = "",
    val category: String = "General",
    val unit: String = "pcs",
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val stock: Long = 0L,
    val lowStockLimit: Long = 5L,
    val photoUri: String = "",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SaleItem(
    val productId: String = "",
    val name: String = "",
    val qty: Long = 0L,
    val unit: String = "pcs",
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val discount: Double = 0.0,
    val lineTotal: Double = 0.0,
    val profit: Double = 0.0
)

@IgnoreExtraProperties
data class Sale(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val invoiceNo: String = "",
    val items: List<SaleItem> = emptyList(),
    val totalItems: Long = 0L,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val profit: Double = 0.0,
    val paymentType: String = "cash", // "cash" or "baki"
    val customerId: String = "",
    val customerName: String = "Cash Sale",
    val customerPhone: String = "",
    val accountId: String = "cash_in_hand",
    val accountName: String = "Cash in Hand",
    val notes: String = "",
    val soldBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Customer(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val name: String = "",
    val mobile: String = "",
    val address: String = "",
    val notes: String = "",
    val currentBalance: Double = 0.0,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Payment(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val paymentNo: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerMobile: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "cash",
    val accountId: String = "cash_in_hand",
    val accountName: String = "Cash in Hand",
    val paymentDate: Long = 0L,
    val note: String = "",
    val receivedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Supplier(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val name: String = "",
    val mobile: String = "",
    val company: String = "",
    val address: String = "",
    val balance: Double = 0.0,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PurchaseItem(
    val productId: String = "",
    val name: String = "",
    val qty: Long = 0L,
    val unit: String = "pcs",
    val buyPrice: Double = 0.0,
    val lineTotal: Double = 0.0
)

@IgnoreExtraProperties
data class Purchase(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val purchaseNo: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val supplierMobile: String = "",
    val items: List<PurchaseItem> = emptyList(),
    val totalItems: Long = 0L,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentType: String = "paid",
    val accountId: String = "cash_in_hand",
    val accountName: String = "Cash in Hand",
    val note: String = "",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PurchaseReturn(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val returnNo: String = "",
    val purchaseId: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val amount: Double = 0.0,
    val reason: String = "",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HealthFixLog(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val problemType: String = "",
    val description: String = "",
    val entityId: String = "",
    val entityCollection: String = "",
    val fixDetails: String = "",
    val fixedBy: String = "",
    @ServerTimestamp val fixedAt: Timestamp? = null
)

data class CompletedSaleReceipt(
    val invoiceNo: String = "",
    val storeName: String = "",
    val storePhone: String = "",
    val storeAddress: String = "",
    val items: List<SaleItem> = emptyList(),
    val totalItems: Long = 0L,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paymentType: String = "cash",
    val customerName: String = "Cash Sale",
    val customerPhone: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun formatTextReceipt(): String {
        val sName = storeName.ifBlank { "StoreOS Dokan" }
        val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.US).format(java.util.Date(timestamp))
        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine(sName.uppercase())
        if (storePhone.isNotBlank()) sb.appendLine("Phone: $storePhone")
        if (storeAddress.isNotBlank()) sb.appendLine("Address: $storeAddress")
        sb.appendLine("================================")
        sb.appendLine("Invoice : #$invoiceNo")
        sb.appendLine("Date    : $dateStr")
        sb.appendLine("Customer: $customerName")
        if (customerPhone.isNotBlank()) sb.appendLine("Mobile  : $customerPhone")
        sb.appendLine("--------------------------------")
        sb.appendLine("ITEMS:")
        items.forEach { item ->
            sb.appendLine("• ${item.name}")
            val qtyRate = "  ${item.qty} ${item.unit} x ৳${"%.2f".format(item.sellPrice)}"
            val lineTot = "৳${"%.2f".format(item.lineTotal)}"
            val spaces = " ".repeat((32 - qtyRate.length - lineTot.length).coerceAtLeast(1))
            sb.appendLine("$qtyRate$spaces$lineTot")
        }
        sb.appendLine("--------------------------------")
        val subStr = "Subtotal:".padEnd(20) + "৳${"%.2f".format(subtotal)}"
        sb.appendLine(subStr)
        if (discount > 0.0) {
            val discStr = "Discount:".padEnd(20) + "-৳${"%.2f".format(discount)}"
            sb.appendLine(discStr)
        }
        val grandStr = "TOTAL:".padEnd(20) + "৳${"%.2f".format(grandTotal)}"
        sb.appendLine(grandStr)
        sb.appendLine("Paid Via: ${paymentType.uppercase()}")
        if (notes.isNotBlank()) {
            sb.appendLine("Note: $notes")
        }
        sb.appendLine("================================")
        sb.appendLine("   Thank you for your visit!   ")
        sb.appendLine("================================")
        return sb.toString()
    }
}

@IgnoreExtraProperties
data class Account(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val name: String = "",
    val type: String = "Cash", // "Cash", "Bank", "Wallet"
    val accountNumber: String = "", // Account number or ID
    val openingBalance: Double = 0.0,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AccountAdjustment(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val accountId: String = "",
    val accountName: String = "",
    val oldBalance: Double = 0.0,
    val newBalance: Double = 0.0,
    val difference: Double = 0.0,
    val note: String = "",
    val adjustedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val amount: Double = 0.0,
    val category: String = "Other",
    val accountId: String = "cash_in_hand",
    val accountName: String = "Cash in Hand",
    val date: Long = 0L,
    val note: String = "",
    val receiptUri: String = "",
    val createdBy: String = "",
    val isDeleted: Boolean = false,
    val deletedNote: String = "",
    val deletedBy: String = "",
    @ServerTimestamp val deletedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class OtherIncome(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val amount: Double = 0.0,
    val category: String = "Other",
    val accountId: String = "cash_in_hand",
    val accountName: String = "Cash in Hand",
    val date: Long = 0L,
    val note: String = "",
    val createdBy: String = "",
    val isDeleted: Boolean = false,
    val deletedNote: String = "",
    val deletedBy: String = "",
    @ServerTimestamp val deletedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SupplierPayment(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val paymentNo: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val amount: Double = 0.0,
    val accountId: String = "cash_in_hand",
    val accountName: String = "Cash in Hand",
    val paymentDate: Long = 0L,
    val note: String = "",
    val paidBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoreCategory(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val type: String = "expense", // "expense" or "income"
    val name: String = "",
    val isArchived: Boolean = false,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StoreNote(
    @DocumentId val id: String = "",
    val storeId: String = "",
    val title: String = "",
    val text: String = "",
    val date: Long = 0L,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class AccountStatementEntry(
    val id: String,
    val timestamp: Long,
    val type: String, // "SALE", "PAYMENT_IN", "PURCHASE", "PAYMENT_OUT", "EXPENSE", "OTHER_INCOME", "ADJUSTMENT"
    val referenceNo: String,
    val partyOrCategory: String,
    val moneyIn: Double,
    val moneyOut: Double,
    val runningBalance: Double,
    val note: String
)

data class AccountStatementReport(
    val account: Account,
    val period: com.example.ui.viewmodel.HistoryPeriod,
    val startDate: Long,
    val endDate: Long,
    val openingBalance: Double,
    val entries: List<AccountStatementEntry>,
    val totalMoneyIn: Double,
    val totalMoneyOut: Double,
    val closingBalance: Double
)
