# HustleFix Virtual Payment & Escrow System

This document explains the architecture and logic of the payment system implemented in the HustleFix marketplace application.

## 1. Core Architecture: The Virtual Wallet
Since HustleFix is currently a school project, it uses a **Virtual Wallet** system instead of processing real credit cards. Every user (Client and Hustler) has a `walletBalance` field in their Firebase profile.

- **Initial Balance:** New users are granted a starting balance (e.g., R5,000) to allow for immediate testing.
- **Top-Ups:** Users can "Top Up" their wallet in the app to add more virtual funds.
- **Persistence:** All balances are stored securely in the Firebase Realtime Database.

## 2. The Escrow Mechanism
To build trust between strangers, we use an **Escrow** model. This ensures that the Hustler knows the money is available before they start working, and the Client knows the Hustler won't get paid until the work is done.

### The Payment Lifecycle:
1.  **Booking (UNPAID):** A client books a service. No money moves yet.
2.  **Securing Funds (ESCROW):**
    *   The Client taps "Secure Funds" on the booking detail page.
    *   The app verifies the Client has enough money in their wallet.
    *   **The Transaction:** The price is subtracted from the Client's wallet and the booking status changes to `ESCROW`.
    *   *Note:* The money is now "held" by the platform's logic.
3.  **Completion (RELEASED):**
    *   The Hustler finishes the job and taps "Mark as Completed."
    *   **The Payout:** The system automatically "releases" the held funds. The service price is added to the Hustler's wallet balance.
    *   The status changes to `RELEASED`.

## 3. Refunds & Cancellations
If a job is cancelled **after** the money is in Escrow:
- The system automatically triggers a **Refund**.
- The held amount is added back to the Client's `walletBalance`.
- The status is updated to `REFUNDED`.

## 4. Withdrawals
Hustlers (and Clients) can "Withdraw" their earnings to a simulated bank account:
- **Bank Selection:** Users select from major South African banks (Capitec, FNB, etc.).
- **Processing:** The app validates the withdrawal amount against the current balance.
- **Logging:** Every withdrawal is recorded as a negative entry in the transaction history.

## 5. Transaction History (Transparency)
Every financial event is recorded in a `transactions` node in the database:
- **Top-Up:** (Positive / Green)
- **Service Payment:** (Negative / Red)
- **Service Payout:** (Positive / Green)
- **Withdrawal:** (Negative / Red)

This provides users with a full audit trail of their virtual spending and earnings, visible on the **My Wallet** page.
