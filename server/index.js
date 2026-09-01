/**
 * HustleFix Standalone Backend (Render Deployment)
 */
require('dotenv').config();
const express = require('express');
const admin = require('firebase-admin');
const crypto = require('crypto');
const nodemailer = require('nodemailer');
const cors = require('cors');
const bodyParser = require('body-parser');
const templates = require('./emailTemplates');

const app = express();
const port = process.env.PORT || 3000;

// Initialize Firebase Admin
// You need to set GOOGLE_APPLICATION_CREDENTIALS path or provide the JSON object
if (process.env.FIREBASE_SERVICE_ACCOUNT) {
  const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://hustlefix-9c7f6-default-rtdb.firebaseio.com"
  });
} else {
  console.error("Missing FIREBASE_SERVICE_ACCOUNT environment variable");
}

const db = admin.database();

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

const MERCHANT_ID = process.env.PAYFAST_MERCHANT_ID || "10053500";
const MERCHANT_KEY = process.env.PAYFAST_MERCHANT_KEY || "s7dtvpr5uallq";
const PASSPHRASE = process.env.PAYFAST_PASSPHRASE || "Treasure071152";

// SMTP Transporter
const mailTransport = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS,
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

  for (const key in data) {
    const regex = new RegExp(`{${key}}`, "g");
    subject = subject.replace(regex, data[key]);
    body = body.replace(regex, data[key]);
  }

  const mailOptions = {
    from: `"HustleFix" <noreply@hustlefix.com>`,
    to: to,
    subject: subject,
    text: body + "\n\n---\nPlease do not reply to this email, as this mailbox is not monitored.",
  };

  try {
    await mailTransport.sendMail(mailOptions);
    await db.ref("activity_log").push({
      action: "EMAIL_SENT",
      userId: data.uid || "system",
      userName: data.name || "User",
      details: `Sent ${type} email to ${to}`,
      timestamp: Date.now(),
    });
  } catch (error) {
    console.error("Email Error:", error);
  }
}

/**
 * ROUTES
 */

// 1. Payfast Checkout
app.post('/api/payments/create-checkout', async (req, res) => {
  const {amount, item_name, m_payment_id, name_first, name_last, email_address} = req.body;
  const data = {
    merchant_id: MERCHANT_ID,
    merchant_key: MERCHANT_KEY,
    return_url: "hustlefix://payment-success",
    cancel_url: "hustlefix://payment-cancel",
    notify_url: "https://hustlefix.onrender.com/api/payments/payfast-itn",
    name_first: name_first || "",
    name_last: name_last || "",
    email_address: email_address || "",
    m_payment_id: m_payment_id || "",
    amount: parseFloat(amount || 0).toFixed(2),
    item_name: item_name || "HustleFix Service",
  };
  data.signature = generateSignature(data, PASSPHRASE);
  const baseUrl = "https://sandbox.payfast.co.za/eng/process";
  const params = new URLSearchParams();
  for (const key in data) {
    if (data[key] !== "" && data[key] !== null) params.append(key, data[key]);
  }
  res.json({ success: true, checkoutUrl: `${baseUrl}?${params.toString()}` });
});

// 2. Payfast ITN Webhook
app.post('/api/payments/payfast-itn', async (req, res) => {
  const pfData = req.body;
  if (!verifySignature(pfData, PASSPHRASE)) return res.status(400).send("Invalid signature");

  if (pfData.payment_status === "COMPLETE") {
    const bookingId = pfData.m_payment_id;
    const amountPaid = parseFloat(pfData.amount_gross);
    try {
      const bookingRef = db.ref(`bookings/${bookingId}`);
      const snapshot = await bookingRef.get();
      const booking = snapshot.val();
      if (booking) {
        await bookingRef.update({
          status: "paid",
          paymentStatus: "PAID",
          paidAt: Date.now(),
        });
        // Logging for Admin portal
        await db.ref("activity_log").push({
            action: "PAYMENT_COMPLETE",
            details: `Received R${amountPaid} for booking ${bookingId}`,
            timestamp: Date.now()
        });
      }
    } catch (e) { console.error(e); }
  }
  res.status(200).send("OK");
});

// 3. Test Email Trigger
app.get('/api/test/email', async (req, res) => {
    const {email, type, name} = req.query;
    await sendEmail(email, type, {name: name || "Test", amount: "100", month: "August"});
    res.send("Email Triggered");
});

/**
 * REAL-TIME LISTENERS (Replacing Cloud Triggers)
 */

// Listen for new users (for welcome email)
db.ref("users").on("child_added", async (snapshot) => {
  const user = snapshot.val();
  // Only send if recent (e.g., within last 2 minutes) to avoid spamming old users on server restart
  if (user.createdAt > Date.now() - 120000) {
    await sendEmail(user.email, "registration", {name: user.name, uid: snapshot.key});
  }
});

// Listen for withdrawal requests
db.ref("withdrawal_requests").on("child_added", async (snapshot) => {
    const request = snapshot.val();
    if (request.timestamp > Date.now() - 60000) {
        const userSnap = await db.ref(`users/${request.userId}`).get();
        const user = userSnap.val();
        await sendEmail(user.email, "withdrawal", {
            name: user.name,
            amount: request.amount,
            bankName: request.bankName,
            uid: request.userId
        });
    }
});

/**
 * HELPERS
 */
function generateSignature(data, passphrase) {
  let str = "";
  const keys = Object.keys(data).sort();
  keys.forEach((key) => {
    if (data[key] !== "" && data[key] !== undefined && data[key] !== null && key !== "signature") {
      str += `${key}=${encodeURIComponent(String(data[key]).trim()).replace(/%20/g, "+")}&`;
    }
  });
  str = str.substring(0, str.length - 1);
  if (passphrase) str += `&passphrase=${encodeURIComponent(passphrase.trim()).replace(/%20/g, "+")}`;
  return crypto.createHash("md5").update(str).digest("hex");
}

function verifySignature(data, passphrase) {
  const receivedSignature = data.signature;
  const computedSignature = generateSignature(data, passphrase);
  return receivedSignature === computedSignature;
}

app.listen(port, () => {
  console.log(`HustleFix Backend running on port ${port}`);
});
