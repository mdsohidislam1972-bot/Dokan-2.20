package com.example.util

enum class AppLanguage(val code: String, val label: String) {
    EN("en", "English"),
    BN("bn", "বাংলা")
}

object ShopLocale {
    const val CURRENCY = "৳"

    fun formatMoney(amount: Double, lang: AppLanguage): String {
        val formatted = String.format(java.util.Locale.US, "%.2f", amount)
        val text = if (formatted.endsWith(".00")) {
            String.format(java.util.Locale.US, "%.0f", amount)
        } else {
            formatted
        }
        return if (lang == AppLanguage.BN) {
            toBengaliDigits(text) + " " + CURRENCY
        } else {
            CURRENCY + " " + text
        }
    }

    fun formatNumber(number: Long, lang: AppLanguage): String {
        return if (lang == AppLanguage.BN) {
            toBengaliDigits(number.toString())
        } else {
            number.toString()
        }
    }

    private fun toBengaliDigits(input: String): String {
        val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun get(key: String, lang: AppLanguage): String {
        val map = if (lang == AppLanguage.BN) banglaStrings else englishStrings
        return map[key] ?: englishStrings[key] ?: key
    }

    private val englishStrings = mapOf(
        "app_title" to "StoreOS",
        "app_subtitle" to "Multi-Device Retail System",
        // Auth
        "sign_in" to "Sign In",
        "sign_up" to "Sign Up",
        "forgot_password" to "Forgot Password",
        "sign_in_with_google" to "Sign in with Google",
        "or_email" to "Or use email and password",
        "email" to "Email Address",
        "password" to "Password",
        "confirm_password" to "Confirm Password",
        "log_out" to "Log Out",
        "send_reset_link" to "Send Reset Link",
        "reset_sent" to "Reset email sent. Check your inbox.",
        "passwords_do_not_match" to "Passwords do not match",
        "fill_all_fields" to "Please fill in all fields",
        "login_failed" to "Authentication failed",
        "need_account" to "Don't have an account? Sign Up",
        "already_have_account" to "Already have an account? Sign In",
        // Onboarding
        "welcome_title" to "Setup Your Store",
        "welcome_subtitle" to "Enter store details to start selling across all devices",
        "store_name" to "Store Name",
        "store_phone" to "Mobile Number",
        "store_address" to "Address / Location",
        "create_store_btn" to "Create & Launch Store",
        "owner_badge" to "Owner",
        // Dashboard / Home
        "dashboard" to "Dashboard",
        "catalog" to "Catalog",
        "pos" to "POS",
        "checklist" to "Checklist",
        "today_sales" to "Today's Sales",
        "net_profit" to "Net Profit",
        "orders_count" to "Orders Completed",
        "items_sold" to "Items Sold",
        "stock_value" to "Stock Value (Cost)",
        "cash_balance" to "Cash Balance",
        "quick_actions" to "Quick Actions",
        "new_sale_action" to "New Sale (POS)",
        "add_item_action" to "Add New Product",
        "sync_status_online" to "Cloud Sync Active",
        // Products / Catalog
        "products_title" to "Inventory Catalog",
        "add_product" to "Add Product",
        "search_products" to "Search product or barcode...",
        "product_name" to "Product Name",
        "barcode" to "Barcode / Code",
        "category" to "Category",
        "buy_price" to "Cost Price (Buy)",
        "sell_price" to "Selling Price (Sale)",
        "initial_stock" to "Stock Quantity",
        "save_product" to "Save Product",
        "stock" to "Stock",
        "low_stock" to "Low Stock",
        "out_of_stock" to "Out of Stock",
        "no_products" to "No products in store yet. Tap below to add your first item.",
        // POS / Sales
        "pos_title" to "Point of Sale",
        "select_items" to "Add items to customer invoice",
        "cart_empty" to "Cart is empty. Tap items to add.",
        "qty" to "Qty",
        "discount" to "Discount",
        "line_total" to "Total",
        "subtotal" to "Subtotal",
        "total_discount" to "Discount",
        "grand_total" to "Grand Total",
        "complete_sale" to "Complete Sale & Print",
        "sale_completed" to "Sale recorded successfully!",
        "invoice" to "Invoice",
        "sale_date" to "Date",
        "sales_history" to "Recent Sales",
        // Rules & Checklist
        "checklist_title" to "Multi-Device Test Checklist",
        "step_phone_a" to "1. Sign in on Phone A (Owner)",
        "step_phone_b" to "2. Sign in on Phone B with same account",
        "step_add_sync" to "3. Add product on A and observe on B",
        "step_persistence" to "4. Log out and back in without losing data",
        "step_offline" to "5. Test offline sales cache & restart persistence",
        "console_steps_title" to "Firebase Console Setup Guide",
        "rule_desc" to "Security Rules Text & Activation"
    )

    private val banglaStrings = mapOf(
        "app_title" to "স্টোর ওএস",
        "app_subtitle" to "মাল্টি-ডিভাইস দোকান ব্যবস্থাপনা",
        // Auth
        "sign_in" to "সাইন ইন",
        "sign_up" to "অ্যাকাউন্ট খুলুন",
        "forgot_password" to "পাসওয়ার্ড ভুলে গেছেন?",
        "sign_in_with_google" to "গুগল দিয়ে সাইন ইন করুন",
        "or_email" to "অথবা ইমেইল ও পাসওয়ার্ড ব্যবহার করুন",
        "email" to "ইমেইল ঠিকানা",
        "password" to "পাসওয়ার্ড",
        "confirm_password" to "পাসওয়ার্ড নিশ্চিত করুন",
        "log_out" to "লগ আউট",
        "send_reset_link" to "রিসেট লিংক পাঠান",
        "reset_sent" to "পাসওয়ার্ড রিসেট ইমেইল পাঠানো হয়েছে।",
        "passwords_do_not_match" to "পাসওয়ার্ড দুটি মিলছে না",
        "fill_all_fields" to "দয়া করে সব ঘর পূরণ করুন",
        "login_failed" to "সাইন ইন ব্যর্থ হয়েছে",
        "need_account" to "অ্যাকাউন্ট নেই? নতুন অ্যাকাউন্ট খুলুন",
        "already_have_account" to "আগে থেকেই অ্যাকাউন্ট আছে? সাইন ইন করুন",
        // Onboarding
        "welcome_title" to "আপনার দোকান সেটআপ করুন",
        "welcome_subtitle" to "যেকোনো ফোন থেকে একই দোকান চালাতে তথ্য দিন",
        "store_name" to "দোকানের নাম",
        "store_phone" to "মোবাইল নম্বর",
        "store_address" to "দোকানের ঠিকানা / অবস্থান",
        "create_store_btn" to "দোকান তৈরি করে শুরু করুন",
        "owner_badge" to "মালিক",
        // Dashboard / Home
        "dashboard" to "ড্যাশবোর্ড",
        "catalog" to "পণ্য তালিকা",
        "pos" to "বিক্রি / পিওএস",
        "checklist" to "চেকলিস্ট",
        "today_sales" to "আজকের বিক্রি",
        "net_profit" to "মোট লাভ",
        "orders_count" to "অর্ডার সংখ্যা",
        "items_sold" to "বিক্রিত পণ্য",
        "stock_value" to "মজুদ পণ্যের মূল্য (ক্রয়)",
        "cash_balance" to "নগদ স্থিতি",
        "quick_actions" to "দ্রুত অ্যাকশন",
        "new_sale_action" to "নতুন বিক্রি করুন",
        "add_item_action" to "নতুন পণ্য যোগ করুন",
        "sync_status_online" to "ক্লাউড সিঙ্ক চালু আছে",
        // Products / Catalog
        "products_title" to "দোকানের পণ্য তালিকা",
        "add_product" to "পণ্য যোগ করুন",
        "search_products" to "নাম বা বারকোড দিয়ে খুঁজুন...",
        "product_name" to "পণ্যের নাম",
        "barcode" to "বারকোড / কোড",
        "category" to "ক্যাটাগরি",
        "buy_price" to "ক্রয় মূল্য",
        "sell_price" to "বিক্রয় মূল্য",
        "initial_stock" to "মজুদ পরিমাণ",
        "save_product" to "পণ্য সংরক্ষণ করুন",
        "stock" to "মজুদ",
        "low_stock" to "মজুদ কম",
        "out_of_stock" to "মজুদ শেষ",
        "no_products" to "এখনো কোনো পণ্য নেই। পণ্য যোগ করতে নিচে চাপুন।",
        // POS / Sales
        "pos_title" to "পয়েন্ট অব সেল (বিক্রি)",
        "select_items" to "ইনভয়েসে পণ্য যোগ করুন",
        "cart_empty" to "কার্ট খালি। পণ্য নির্বাচন করুন।",
        "qty" to "পরিমাণ",
        "discount" to "ছাড়",
        "line_total" to "মোট",
        "subtotal" to "সাবটোটাল",
        "total_discount" to "মোট ছাড়",
        "grand_total" to "সর্বমোট",
        "complete_sale" to "বিক্রি সম্পন্ন করুন",
        "sale_completed" to "বিক্রি সফলভাবে সম্পন্ন হয়েছে!",
        "invoice" to "ইনভয়েস",
        "sale_date" to "তারিখ",
        "sales_history" to "সাম্প্রতিক বিক্রি",
        // Rules & Checklist
        "checklist_title" to "মাল্টি-ডিভাইস যাচাই চেকলিস্ট",
        "step_phone_a" to "১. ফোন এ-তে মালিক হিসেবে সাইন ইন",
        "step_phone_b" to "২. ফোন বি-তে একই একাউন্টে সাইন ইন",
        "step_add_sync" to "৩. এ-তে পণ্য যোগ করে বি-তে সরাসরি দেখা",
        "step_persistence" to "৪. লগ আউট ও আবার লগ ইন করে তথ্য অক্ষুণ্ণ রাখা",
        "step_offline" to "৫. অফলাইন ক্যাশ ও ডিভাইস রিস্টার্ট পরীক্ষা",
        "console_steps_title" to "ফায়ারবেস কনসোল সেটআপ নির্দেশিকা",
        "rule_desc" to "সিকিউরিটি রুলস ও সক্রিয়করণ"
    )
}
