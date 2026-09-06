/**
 * Localized Email Templates for HustleFix
 */

const templates = {
  en: {
    registration: {
      subject: "Welcome to HustleFix, {name}!",
      body: "Hi {name},\n\nWelcome to HustleFix! Your professional marketplace for local services is ready.\n\nStart your journey today!",
    },
    deletion: {
      subject: "Account Deleted",
      body: "Hi {name},\n\nYour account has been successfully deleted. We're sorry to see you go!",
    },
    withdrawal: {
      subject: "Withdrawal Request Received",
      body: "Hi {name},\n\nWe received your request for R{amount} to your {bankName} account. It will be processed within 2-3 business days.",
    },
    withdrawal_success: {
      subject: "Funds Sent! 💸",
      body: "Hi {name},\n\nGreat news! Your withdrawal request of R{amount} has been processed and sent to your bank account. It should reflect in your balance shortly.",
    },
    booking_confirmed: {
      subject: "Booking Confirmed: {serviceTitle}",
      body: "Hi {name},\n\nYour booking for {serviceTitle} with {partnerName} has been confirmed for {date}.",
    },
    emergency: {
      subject: "🚨 URGENT: Emergency Help Requested",
      body: "Alert!\n\nAn emergency request has been posted at {location}.\n\nDetails: {description}",
    },
    profile_update: {
      subject: "Security Alert: Profile Updated",
      body: "Hi {name},\n\nYour profile information was recently updated. If this wasn't you, please contact support immediately.",
    },
    verification_approved: {
      subject: "Congratulations! You're Verified ✅",
      body: "Hi {name},\n\nGreat news! Your identity verification has been approved. A 'Verified' badge has been added to your profile, making it easier for clients to trust and book you.\n\nHappy hustling!",
    },
    verification_rejected: {
      subject: "Verification Update",
      body: "Hi {name},\n\nWe couldn't approve your verification request at this time.\n\nReason: {reason}\n\nPlease update your documents and try again.",
    },
    monthly_statement: {
      subject: "Your HustleFix Statement for {month}",
      body: "Hi {name},\n\nHere is your performance summary for {month}:\n\n- Total Jobs: {jobCount}\n- Gross Earnings: R{totalEarnings}\n- Platform Fees: R{platformFees}\n- Net Payout: R{netPay}\n\nThank you for your hard work! Keep hustling.\n\nBest regards,\nThe HustleFix Team",
    },
  },
  zu: {
    registration: {
      subject: "Siyakwamukela ku-HustleFix, {name}!",
      body: "Sawubona {name},\n\nSiyakwamukela ku-HustleFix! Indawo yakho yokuhweba yamasevisi asendaweni isilungile.\n\nQala uhambo lwakho namuhla!",
    },
    monthly_statement: {
      subject: "Isitatimende sakho se-HustleFix se-{month}",
      body: "Sawubona {name},\n\nMana kafushane kafushane {month}:\n\n- Imisebenzi isiyonke: {jobCount}\n- Imali Engenile: R{totalEarnings}\n- Izimali Zenkundla: R{platformFees}\n- Imali Oyitholile: R{netPay}\n\nSiyabonga ngokusebenza kanzima!",
    },
  },
  af: {
    registration: {
      subject: "Welkom by HustleFix, {name}!",
      body: "Haai {name},\n\nWelkom by HustleFix! Jou professionele markplek vir plaaslike dienste is gereed.\n\nBegin vandag jou reis!",
    },
    monthly_statement: {
      subject: "Jou HustleFix-staat vir {month}",
      body: "Haai {name},\n\nHier is jou prestasie-opsomming vir {month}:\n\n- Totale werk: {jobCount}\n- Bruto verdienste: R{totalEarnings}\n- Platformfooie: R{platformFees}\n- Netto uitbetaling: R{netPay}\n\nDankie vir jou harde werk!",
    },
  },
};

module.exports = templates;
