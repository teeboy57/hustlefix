/**
 * HustleFix Payfast Integration for Firebase Cloud Functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const crypto = require("crypto");

admin.initializeApp();
const db = admin.database();

const MERCHANT_ID = "17144161";
const MERCHANT_KEY = "cxfxu4iwaewmg";
const PASSPHRASE = ""; // Set this in Payfast Dashboard -> Settings -> Integration

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

