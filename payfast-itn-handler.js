/**
 * HustleFix Payfast ITN Handler (Node.js/Express)
 * Verified with Merchant ID: 17144161
 */

const express = require('express');
const crypto = require('crypto');
const admin = require('firebase-admin');

// Initialize Firebase Admin
// Note: Ensure your service-account-key.json is present OR set GOOGLE_APPLICATION_CREDENTIALS
try {
  const serviceAccount = require('./service-account-key.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://hustlefix-9c7f6-default-rtdb.firebaseio.com"
  });
} catch (e) {
  console.log("Using default credentials (running on server)");
  admin.initializeApp({
    databaseURL: "https://hustlefix-9c7f6-default-rtdb.firebaseio.com"
  });
}

const db = admin.database();
const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const MERCHANT_ID = '17144161';
const MERCHANT_KEY = 'cxfxu4iwaewmg';
const PASSPHRASE = ''; // Set this in Payfast Dashboard -> Settings -> Integration

/**
 * 1. Create Checkout Request
 * Called by the Android app to get a signed checkout URL.
 */
app.post('/api/payments/create-checkout', async (req, res) => {
  const { amount, item_name, m_payment_id, name_first, name_last, email_address } = req.body;

  const data = {
    merchant_id: MERCHANT_ID,
    merchant_key: MERCHANT_KEY,
    return_url: 'hustlefix://payment-success',
    cancel_url: 'hustlefix://payment-cancel',
    notify_url: 'https://hustlefix.onrender.com/api/payments/payfast-itn',
    name_first: name_first || "",
    name_last: name_last || "",
    email_address: email_address || "",
    m_payment_id: m_payment_id || "",
    amount: parseFloat(amount || 0).toFixed(2),
    item_name: item_name || "HustleFix Service"
  };

  // Generate Signature
  data.signature = generateSignature(data, PASSPHRASE);

  // Payfast Sandbox URL (Use 'www.payfast.co.za' for production)
  const baseUrl = 'https://sandbox.payfast.co.za/eng/process';

  // Only include non-empty values in the URL
  const params = new URLSearchParams();
  for (const key in data) {
    if (data[key] !== "" && data[key] !== null) {
      params.append(key, data[key]);
    }
  }

  const checkoutUrl = `${baseUrl}?${params.toString()}`;

  res.json({
    success: true,
    checkoutUrl: checkoutUrl
  });
});

/**
 * 2. ITN Handler
 * Webhook called by Payfast to notify us of payment status.
 */
app.post('/api/payments/payfast-itn', async (req, res) => {
  const pfData = req.body;

  // Verify Signature
  if (!verifySignature(pfData, PASSPHRASE)) {
    console.error('Invalid ITN signature');
    return res.sendStatus(400);
  }

  if (pfData.payment_status === 'COMPLETE') {
    const bookingId = pfData.m_payment_id;
    const amountPaid = parseFloat(pfData.amount_gross);

    try {
      // 3. Update Booking Status
      const bookingRef = db.ref(`bookings/${bookingId}`);
      const bookingSnapshot = await bookingRef.get();
      const booking = bookingSnapshot.val();

      if (!booking) {
        console.error('Booking not found:', bookingId);
        return res.sendStatus(404);
      }

      await bookingRef.update({
        status: 'paid',
        paymentStatus: 'PAID',
        payfastTid: pfData.pf_payment_id,
        paidAt: Date.now()
      });

      // 4. Payout Release Workflow: Credit the Worker
      if (booking.workerId) {
        const workerId = booking.workerId;

        // Atomic update of worker balance
        await db.ref(`users/${workerId}/walletBalance`).transaction((currentBalance) => {
          return (currentBalance || 0) + amountPaid;
        });

        // Record Transaction for the Worker
        const transRef = db.ref(`transactions/${workerId}`).push();
        await transRef.set({
          id: transRef.key,
          type: 'Job Payout',
          amount: amountPaid,
          timestamp: Date.now(),
          serviceTitle: booking.serviceTitle || 'HustleFix Job',
          bookingId: bookingId
        });
      }

      console.log(`Payment confirmed and payout released for booking ${bookingId}`);
    } catch (error) {
      console.error('Error processing ITN:', error);
    }
  }

  res.sendStatus(200);
});

/**
 * Helper: Generate Payfast Signature
 */
function generateSignature(data, passphrase) {
  let str = '';
  const keys = Object.keys(data).sort();
  keys.forEach(key => {
    const value = data[key];
    if (value !== '' && value !== undefined && value !== null && key !== 'signature') {
      str += `${key}=${encodeURIComponent(String(value).trim()).replace(/%20/g, '+')}&`;
    }
  });
  str = str.substring(0, str.length - 1);
  if (passphrase) {
    str += `&passphrase=${encodeURIComponent(passphrase.trim()).replace(/%20/g, '+')}`;
  }
  return crypto.createHash('md5').update(str).digest('hex');
}

function verifySignature(data, passphrase) {
  const receivedSignature = data.signature;
  const computedSignature = generateSignature(data, passphrase);
  return receivedSignature === computedSignature;
}

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`HustleFix Payfast Backend listening on port ${PORT}`));
