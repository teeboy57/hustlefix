/**
 * HustleFix Payfast Integration for Firebase Cloud Functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const crypto = require("crypto");
const nodemailer = require("nodemailer");
const templates = require("./emailTemplates");

admin.initializeApp();
const db = admin.database();

const MERCHANT_ID = "10053500";
const MERCHANT_KEY = "s7dtvpr5uallq";
const PASSPHRASE = "Treasure071152";

// SMTP Transporter Setup
const mailTransport = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: functions.config().email?.user || "",
    pass: functions.config().email?.pass || "",
  },
});

/**
 * Global Email Helper
 */
async function sendEmail(to, type, data, lang = "en") {
  if (!to) return;

  const template = templates[lang]?.[type] || templates["en"]?.[type];
  if (!template) return;

  let subject = template.subject;
  let body = template.body;

  // Dynamic replacements
  for (const key in data) {
    const regex = new RegExp(`{${key}}`, "g");
    subject = subject.replace(regex, data[key]);
    body = body.replace(regex, data[key]);
  }

  const mailOptions = {
    from: `"HustleFix" <noreply@hustlefix.com>`,
    to: to,
    subject: subject,
    text: body,
  };

  try {
    await mailTransport.sendMail(mailOptions);

    // Log to Activity Log
    await db.ref("activity_log").push({
      action: "EMAIL_SENT",
      userId: data.uid || "system",
      userName: data.name || "User",
      details: `Sent ${type} email to ${to}`,
      timestamp: Date.now(),
    });

    console.log(`Email sent: ${type} to ${to}`);
  } catch (error) {
    console.error("Email Error:", error);
    // Log Failure
    await db.ref("activity_log").push({
      action: "EMAIL_FAILED",
      details: `Failed to send ${type} email to ${to}. Error: ${error.message}`,
      timestamp: Date.now(),
    });
  }
}

/**
 * 0. Auth Triggers
 */
exports.onUserCreated = functions.auth.user().onCreate(async (user) => {
  const {email, displayName, uid} = user;
  await sendEmail(email, "registration", {name: displayName || "Hustler", uid: uid});
});

exports.onUserDeleted = functions.auth.user().onDelete(async (user) => {
  await sendEmail(user.email, "deletion", {name: user.displayName || "User", uid: user.uid});
});

/**
 * 1. createCheckout
 * HTTPS function called by the Android app to get a signed checkout URL.
 */
exports.createCheckout = functions.https.onRequest(async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).send("Method Not Allowed");
  }

  const {amount, item_name, m_payment_id, name_first, name_last, email_address} = req.body;

  const data = {
    merchant_id: MERCHANT_ID,
    merchant_key: MERCHANT_KEY,
    return_url: "hustlefix://payment-success",
    cancel_url: "hustlefix://payment-cancel",
    notify_url: "https://us-central1-hustlefix-9c7f6.cloudfunctions.net/payfastITN",
    name_first: name_first || "",
    name_last: name_last || "",
    email_address: email_address || "",
    m_payment_id: m_payment_id || "",
    amount: parseFloat(amount || 0).toFixed(2),
    item_name: item_name || "HustleFix Service",
  };

  // Generate Signature
  data.signature = generateSignature(data, PASSPHRASE);

  // Payfast Sandbox URL (Use 'www.payfast.co.za' for production)
  const baseUrl = "https://sandbox.payfast.co.za/eng/process";
  const params = new URLSearchParams();
  for (const key in data) {
    if (data[key] !== "" && data[key] !== null) {
      params.append(key, data[key]);
    }
  }
  const checkoutUrl = `${baseUrl}?${params.toString()}`;

  res.json({
    success: true,
    checkoutUrl: checkoutUrl,
  });
});

/**
 * 2. payfastITN
 * Webhook called by Payfast to notify us of payment status.
 */
exports.payfastITN = functions.https.onRequest(async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).send("Method Not Allowed");
  }

  const pfData = req.body;

  // 1. Verify Signature
  if (!verifySignature(pfData, PASSPHRASE)) {
    console.error("Invalid ITN signature");
    return res.status(400).send("Invalid signature");
  }

  // 2. Process the payment if COMPLETE
  if (pfData.payment_status === "COMPLETE") {
    const bookingId = pfData.m_payment_id;
    const amountPaid = parseFloat(pfData.amount_gross);

    try {
      const bookingRef = db.ref(`bookings/${bookingId}`);
      const snapshot = await bookingRef.get();
      const booking = snapshot.val();

      if (!booking) {
        console.error("Booking not found:", bookingId);
        return res.status(404).send("Booking not found");
      }

      // Update Booking
      await bookingRef.update({
        status: "paid",
        paymentStatus: "PAID",
        payfastTid: pfData.pf_payment_id,
        paidAt: Date.now(),
      });

      // Credit the Worker
      if (booking.workerId) {
        const workerId = booking.workerId;

        await db.ref(`users/${workerId}/walletBalance`).transaction((currentBalance) => {
          return (currentBalance || 0) + amountPaid;
        });

        // Log Transaction
        const transRef = db.ref(`transactions/${workerId}`).push();
        await transRef.set({
          id: transRef.key,
          type: "Job Payout",
          amount: amountPaid,
          timestamp: Date.now(),
          serviceTitle: booking.serviceTitle || "HustleFix Job",
          bookingId: bookingId,
        });
      }

      console.log(`Payment confirmed for booking ${bookingId}`);
    } catch (error) {
      console.error("Error processing database updates:", error);
      return res.status(500).send("Internal Server Error");
    }
  }

  res.status(200).send("OK");
});

/**
 * 3. onMessageSent
 * Triggered when a new message is written to a chat.
 */
exports.onMessageSent = functions.database.ref("/messages/{chatId}/{messageId}")
    .onCreate(async (snapshot, context) => {
      const message = snapshot.val();
      const receiverId = message.receiverId;

      // Get receiver's FCM token
      const userSnapshot = await db.ref(`users/${receiverId}`).get();
      const userData = userSnapshot.val();
      const fcmToken = userData?.fcmToken;

      if (!fcmToken) return null;

      const payload = {
        notification: {
          title: `New message from ${message.senderName}`,
          body: message.messageText,
        },
        data: {
          screen: "chat",
          senderId: message.senderId,
          senderName: message.senderName,
        },
      };

      return admin.messaging().sendToDevice(fcmToken, payload);
    });

/**
 * 4. onBookingStatusChanged
 * Notifies the relevant party (client or worker) when a booking status changes.
 */
exports.onBookingStatusChanged = functions.database.ref("/bookings/{bookingId}")
    .onUpdate(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();

      if (before.status === after.status) return null;

      let targetUserId = null;
      let title = "Booking Update";
      let body = `Your booking for ${after.serviceTitle || "a service"} is now ${after.status}.`;

      if (after.status === "confirmed") {
        // Notify Client that Worker accepted
        targetUserId = after.clientId;
        title = "Booking Confirmed! ✅";
        body = `${after.workerName || "The provider"} has accepted your booking for ${after.serviceTitle}.`;
      } else if (after.status === "paid") {
        // Notify Worker that Client paid
        targetUserId = after.workerId;
        title = "Payment Received! 💰";
        body = `${after.clientName || "The client"} has paid for ${after.serviceTitle}. You can now start the work.`;
      } else if (after.status === "completed") {
        // Notify Client that Worker finished
        targetUserId = after.clientId;
        title = "Job Completed! ✨";
        body = `${after.workerName || "The provider"} has marked your booking as completed. Please rate the service!`;
      }

      if (!targetUserId) return null;

      const userSnapshot = await db.ref(`users/${targetUserId}`).get();
      const fcmToken = userSnapshot.val()?.fcmToken;

      if (!fcmToken) return null;

      const payload = {
        notification: {
          title: title,
          body: body,
        },
        data: {
          screen: "bookings",
          bookingId: context.params.bookingId,
        },
      };

      // Also trigger Email if confirmed
      if (after.status === "confirmed") {
        const userSnapshot = await db.ref(`users/${after.clientId}`).get();
        const user = userSnapshot.val();
        if (user && user.email) {
          await sendEmail(user.email, "booking_confirmed", {
            name: user.name || "Client",
            serviceTitle: after.serviceTitle,
            partnerName: after.workerName,
            date: after.preferredDate,
            uid: after.clientId,
          });
        }
      }

      return admin.messaging().sendToDevice(fcmToken, payload);
    });

/**
 * 5. onEmergencyRequest
 * Notifies all admins when a new emergency is posted.
 */
exports.onEmergencyRequest = functions.database.ref("/emergency_requests/{requestId}")
    .onCreate(async (snapshot, context) => {
      const request = snapshot.val();

      // Find all admins
      const usersSnapshot = await db.ref("users").orderByChild("role").equalTo("admin").get();
      const tokens = [];

      usersSnapshot.forEach((child) => {
        const token = child.val().fcmToken;
        if (token) tokens.push(token);
      });

      if (tokens.length === 0) return null;

      const payload = {
        notification: {
          title: "🚨 URGENT EMERGENCY ALERT",
          body: `${request.userName} needs help: ${request.description}`,
        },
        data: {
          screen: "emergency",
          requestId: request.id,
        },
      };

      return admin.messaging().sendToDevice(tokens, payload);
    });

/**
 * 5. onBookingCompleted
 * Securely handles the payout to the worker when a booking is marked completed.
 */
exports.onBookingCompleted = functions.database.ref("/bookings/{bookingId}")
    .onUpdate(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();

      // Only trigger if status changed to 'completed' and was PAID
      if (after.status === "completed" && before.status !== "completed" && after.paymentStatus === "PAID") {
        const workerId = after.workerId;
        const amount = parseFloat(after.amount || 0);

        if (!workerId || amount <= 0 || after.payoutReleased) return null;

        try {
          // 1. Credit worker
          await db.ref(`users/${workerId}/walletBalance`).transaction((current) => (current || 0) + amount);

          // 2. Log Transaction
          const transRef = db.ref(`transactions/${workerId}`).push();
          await transRef.set({
            id: transRef.key,
            type: "Job Payout",
            amount: amount,
            timestamp: Date.now(),
            serviceTitle: after.serviceTitle || "Completed Job",
            bookingId: context.params.bookingId,
          });

          // 3. Flag as released to prevent re-runs
          return change.after.ref.update({
            payoutReleased: true,
            payoutAt: Date.now(),
          });
        } catch (error) {
          console.error("Payout Error:", error);
        }
      }
      return null;
    });

/**
 * 6. onWithdrawalRequest
 */
exports.onWithdrawalRequest = functions.database.ref("/withdrawal_requests/{id}")
    .onCreate(async (snapshot, context) => {
      const request = snapshot.val();
      const userSnap = await db.ref(`users/${request.userId}`).get();
      const user = userSnap.val();

      await sendEmail(user.email, "withdrawal", {
        name: user.name || "Hustler",
        amount: request.amount,
        bankName: request.bankName,
        uid: request.userId,
      });
    });

/**
 * 7. onEmergencyPosted (Email to Admins)
 */
exports.onEmergencyEmail = functions.database.ref("/emergency_requests/{requestId}")
    .onCreate(async (snapshot, context) => {
      const request = snapshot.val();

      // Find all admins to email
      const adminsSnap = await db.ref("users").orderByChild("role").equalTo("admin").get();

      adminsSnap.forEach((child) => {
        const admin = child.val();
        if (admin.email) {
          sendEmail(admin.email, "emergency", {
            location: request.address,
            description: request.description,
          });
        }
      });
    });

/**
 * 8. onProfileUpdated
 */
exports.onProfileUpdatedEmail = functions.database.ref("/users/{uid}")
    .onUpdate(async (change, context) => {
      const before = change.before.val();
      const after = change.after.val();

      // Only notify on sensitive changes (phone, email, bank details)
      if (before.phone !== after.phone || before.email !== after.email || before.accountNumber !== after.accountNumber) {
        await sendEmail(after.email, "profile_update", {
          name: after.name || "User",
          uid: context.params.uid,
        });
      }
    });

/**
 * 9. testEmailTrigger (HTTPS)
 * Call this to simulate an email trigger.
 * Params: ?email=...&type=registration&name=...
 */
exports.testEmailTrigger = functions.https.onRequest(async (req, res) => {
  const {email, type, name, amount, bankName} = req.query;

  if (!email || !type) {
    return res.status(400).send("Missing email or type");
  }

  const data = {
    name: name || "Test User",
    amount: amount || "0.00",
    bankName: bankName || "Test Bank",
    serviceTitle: "Test Service",
    partnerName: "Test Partner",
    date: new Date().toLocaleDateString(),
    location: "Test Location",
    description: "Test Description",
    month: "August 2026",
    jobCount: "12",
    totalEarnings: "1200.00",
    platformFees: "120.00",
    netPay: "1080.00",
  };

  try {
    await sendEmail(email, type, data);
    res.status(200).send(`Test email of type ${type} sent to ${email}`);
  } catch (error) {
    res.status(500).send("Test Failed: " + error.message);
  }
});

/**
 * 9. generateMonthlyStatements (HTTPS / Scheduled)
 */
exports.generateMonthlyStatements = functions.https.onRequest(async (req, res) => {
  const usersSnap = await db.ref("users").orderByChild("role").equalTo("worker").get();
  const date = new Date();
  date.setMonth(date.getMonth() - 1); // Get previous month
  const monthName = date.toLocaleString("en-US", {month: "long", year: "numeric"});

  const startOfMonth = new Date(date.getFullYear(), date.getMonth(), 1).getTime();
  const endOfMonth = new Date(date.getFullYear(), date.getMonth() + 1, 0).getTime();

  let sentCount = 0;

  const userPromises = [];
  usersSnap.forEach((child) => {
    const user = child.val();
    const uid = child.key;

    if (user.email) {
      const p = (async () => {
        const transSnap = await db.ref(`transactions/${uid}`).get();
        let totalEarnings = 0;
        let jobCount = 0;

        transSnap.forEach((tChild) => {
          const t = tChild.val();
          if (t.timestamp >= startOfMonth && t.timestamp <= endOfMonth && t.type === "Job Payout") {
            totalEarnings += parseFloat(t.amount || 0);
            jobCount++;
          }
        });

        if (jobCount > 0) {
          const fees = totalEarnings * 0.10;
          const net = totalEarnings - fees;

          await sendEmail(user.email, "monthly_statement", {
            name: user.name || "Hustler",
            month: monthName,
            jobCount: jobCount.toString(),
            totalEarnings: totalEarnings.toFixed(2),
            platformFees: fees.toFixed(2),
            netPay: net.toFixed(2),
            uid: uid
          });
          sentCount++;
        }
      })();
      userPromises.push(p);
    }
  });

  await Promise.all(userPromises);
  res.status(200).send(`Generated and sent ${sentCount} statements for ${monthName}`);
});

/**
 * Signature Generation Helper
 */
function generateSignature(data, passphrase) {
  let str = "";
  const keys = Object.keys(data).sort();
  keys.forEach((key) => {
    const value = data[key];
    if (value !== "" && value !== undefined && value !== null && key !== "signature") {
      str += `${key}=${encodeURIComponent(String(value).trim()).replace(/%20/g, "+")}&`;
    }
  });
  str = str.substring(0, str.length - 1);
  if (passphrase) {
    str += `&passphrase=${encodeURIComponent(passphrase.trim()).replace(/%20/g, "+")}`;
  }
  return crypto.createHash("md5").update(str).digest("hex");
}

/**
 * Signature Verification Helper
 */
function verifySignature(data, passphrase) {
  const receivedSignature = data.signature;
  const computedSignature = generateSignature(data, passphrase);
  return receivedSignature === computedSignature;
}

