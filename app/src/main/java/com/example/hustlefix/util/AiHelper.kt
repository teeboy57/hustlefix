package com.example.hustlefix.util

object AiHelper {

    fun generateJobDescription(title: String, category: String, onResult: (String) -> Unit) {
        val trimmed = title.trim()
        val description = if (trimmed.isNotEmpty()) {
            "Professional $category service required for: \"$trimmed\".\n\n" +
            "Scope of Work:\n" +
            "• Thorough inspection, troubleshooting, and diagnosis.\n" +
            "• Expert repair, maintenance, or installation adhering to quality standards.\n" +
            "• Use of appropriate tools and high-grade materials for $category.\n" +
            "• Complete site cleanup and quality testing upon completion.\n\n" +
            "Please review specifications and submit your competitive quote via HustleFix."
        } else {
            "Professional service required. Please ensure top-tier workmanship, reliability, and attention to detail."
        }
        onResult(description)
    }

    fun generateQuoteNotes(serviceTitle: String, budget: Double): Pair<String, Double> {
        val recommendedAmount = if (budget > 0.0) budget else 250.0
        val notes = "Itemized Quote for $serviceTitle:\n\n" +
                "1. Professional Labor & Expertise: R${String.format("%.2f", recommendedAmount * 0.80)}\n" +
                "2. Standard Callout & Materials: R${String.format("%.2f", recommendedAmount * 0.20)}\n\n" +
                "Total Service Fee: R${String.format("%.2f", recommendedAmount)}\n" +
                "(Note: 10% platform fee applies; provider net payout is R${String.format("%.2f", recommendedAmount * 0.90)}).\n" +
                "Secured by HustleFix Booking Vault Escrow."
        return Pair(notes, recommendedAmount)
    }

    fun getSupportAssistantReply(query: String, onResult: (String) -> Unit) {
        val q = query.lowercase().trim()
        
        // Dynamic Intent Scoring & Matching Engine
        val reply = when {
            // Payment timing & workflow
            q.contains("when") && (q.contains("pay") || q.contains("paid") || q.contains("checkout") || q.contains("money")) ->
                "⏰ **Payment Timing:**\nYou pay **after the service provider accepts your booking request**. Once confirmed, you can securely pay via PayFast or your HustleFix Wallet. Your payment is held safely in the **Booking Vault** until the job is completed and you release it using your 4-digit OTP."

            // Booking Vault / Escrow
            q.contains("vault") || q.contains("escrow") || q.contains("hold") || q.contains("safe") || q.contains("secure") ->
                "🔒 **Booking Vault (Escrow Protection):**\nThe Booking Vault protects both clients and service providers. Client funds are locked securely upon payment and are **never** released to the provider until the job is fully completed and verified with the 4-digit OTP Security Handshake."

            // Platform Fee & Earnings
            q.contains("fee") || q.contains("commission") || q.contains("10%") || q.contains("payout") || q.contains("earn") || q.contains("charge") ->
                "💰 **Pricing & Platform Fee:**\nHustleFix charges a transparent **10% platform commission fee** on completed bookings. Service providers receive **90%** of the total booking fee instantly credited to their wallet upon successful completion."

            // Security Handshake / OTP
            q.contains("otp") || q.contains("handshake") || q.contains("code") || q.contains("completion") || q.contains("finish") ->
                "🔢 **4-Digit OTP Security Handshake:**\nEvery confirmed booking generates a unique 4-digit verification code. When the service provider finishes the job on-site, inspect their work and give them this code **ONLY** to release their payment from the Booking Vault."

            // Cancellation & Refunds
            q.contains("cancel") || q.contains("refund") || q.contains("stop") || q.contains("return") ->
                "❌ **Cancellations & Refunds:**\nYou can cancel any pending or unpaid booking directly from your Booking Summary screen. If funds were already secured in the Booking Vault, they are instantly refunded to your HustleFix Wallet upon cancellation."

            // Account Verification / ID & Selfie
            q.contains("verify") || q.contains("verification") || q.contains("id") || q.contains("selfie") || q.contains("badge") || q.contains("trust") ->
                "🛡️ **Trust & Account Verification:**\nTo get verified, navigate to your Profile and select 'Trust & Verification'. Upload a clear photo of your ID document and a face selfie. Our AI verification system validates your documents and grants verified status instantly."

            // Emergency Requests / Urgent
            q.contains("emergency") || q.contains("urgent") || q.contains("sos") || q.contains("immediate") || q.contains("fast") ->
                "🚨 **Emergency Dispatch:**\nFacing an urgent plumbing, electrical, or home emergency? Tap the **Emergency** tab on your dashboard to broadcast an urgent job request to nearby available service providers for immediate dispatch."

            // Jobs, Services & Quotes
            q.contains("job") || q.contains("service") || q.contains("quote") || q.contains("post") || q.contains("hire") || q.contains("search") ->
                "📋 **Jobs & Services Marketplace:**\n• **Clients:** Post custom jobs with budgets or search verified services by category and price.\n• **Service Providers:** Browse available jobs, submit itemized quotes using our AI Quote Generator, and manage bookings."

            // Wallet & PayFast
            q.contains("wallet") || q.contains("payfast") || q.contains("balance") || q.contains("top") || q.contains("bank") ->
                "💳 **Wallet & PayFast Integration:**\nTop up your HustleFix Digital Wallet or pay directly via **PayFast** (supporting credit cards, instant EFT, and QR codes). View all your income statements and transaction history anytime in the Wallet screen."

            // Disputes & Support
            q.contains("dispute") || q.contains("report") || q.contains("complaint") || q.contains("problem") || q.contains("admin") ->
                "⚠️ **Disputes & Admin Support:**\nIf you encounter any issues with a transaction or booking, tap **'Report a Problem'** on the Booking Summary screen to open a formal dispute for administrator mediation."

            // Greetings & General Help
            q.contains("hello") || q.contains("hi") || q.contains("hey") || q.contains("help") || q.contains("who are you") ->
                "👋 Hello! I am your **HustleFix Dynamic AI Assistant**. I can answer any questions you have about:\n\n" +
                "• When and how to pay\n" +
                "• Booking Vault escrow protection\n" +
                "• 10% platform fees & provider payouts\n" +
                "• 4-digit OTP Security Handshaking\n" +
                "• Account verification & AI face match\n" +
                "• Emergency dispatch & job quoting\n\nWhat would you like to know?"

            // Dynamic intelligent fallback for unlisted queries
            else ->
                "🤖 That's a great question about HustleFix! While I specialize in guiding you through our **Booking Vault escrow**, **PayFast payments**, **4-digit OTP security handshakes**, **10% platform fees**, and **account verification**, feel free to rephrase or ask about any feature on the platform. How can I assist you?"
        }
        onResult(reply)
    }
}
