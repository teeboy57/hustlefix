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

const MERCHANT_ID = '10053500';
const MERCHANT_KEY = 's7dtvpr5uallq';
const PASSPHRASE = 'Treasure071152'; // Set this in Payfast Dashboard -> Settings -> Integration

/**
 * 1. Create Checkout Request
 * Called by the Android app to get a signed checkout URL.
 */
/**
 * Helper: Payfast-compliant URL Encoding
 * Payfast requires spaces as + and specific character encoding.
 */
const pfEncode = (str) => {
  return encodeURIComponent(String(str).trim())
    .replace(/%20/g, '+')
    .replace(/!/g, '%21')
    .replace(/'/g, '%27')
    .replace(/\(/g, '%28')
    .replace(/\)/g, '%29')
    .replace(/\*/g, '%2A')
    .replace(/~/g, '%7E');
};

/**
 * 1. Create Checkout Request
 */
app.post('/api/payments/create-checkout', async (req, res) => {
  const { amount, item_name, m_payment_id, name_first, name_last, email_address } = req.body;

  const data = {
    merchant_id: MERCHANT_ID,
    merchant_key: MERCHANT_KEY,
    return_url: 'https://hustlefix.onrender.com/api/payments/success',
    cancel_url: 'https://hustlefix.onrender.com/api/payments/cancel',
    notify_url: 'https://hustlefix.onrender.com/api/payments/payfast-itn',
    name_first: name_first || "Customer",
    name_last: name_last || "User",
    email_address: (email_address || "customer@example.com").trim(),
    m_payment_id: m_payment_id || String(Date.now()),
    amount: parseFloat(amount || 0).toFixed(2),
    item_name: item_name || "HustleFix Service"
  };

  // 1. Sort keys alphabetically and build the signature string
  let signatureString = '';
  Object.keys(data).sort().forEach(key => {
    if (data[key] !== "" && data[key] !== null) {
      signatureString += `${key}=${pfEncode(data[key])}&`;
    }
  });

  // 2. Remove trailing & and append raw passphrase
  signatureString = signatureString.substring(0, signatureString.length - 1);
  if (PASSPHRASE) {
    signatureString += `&passphrase=${pfEncode(PASSPHRASE)}`;
  }

  // 3. Generate MD5 hash
  const signature = crypto.createHash('md5').update(signatureString).digest('hex');

  // 4. Build final URL (Signature must be at the end)
  const isSandbox = MERCHANT_ID.startsWith('10');
  const baseUrl = isSandbox
    ? 'https://sandbox.payfast.co.za/eng/process'
    : 'https://www.payfast.co.za/eng/process';

  // Important: Use the EXACT same string as the signature (minus the passphrase part)
  const finalParams = signatureString.split('&passphrase=')[0];
  const checkoutUrl = `${baseUrl}?${finalParams}&signature=${signature}`;

  console.log(`Generated signature for ${data.item_name}: ${signature}`);

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
      // Check if it's a Wallet Top Up
      if (bookingId.startsWith('TOPUP_')) {
        const parts = bookingId.split('_');
        const userId = parts[1]; // Extract userId from TOPUP_userId_timestamp

        if (userId) {
          const userRef = db.ref(`users/${userId}`);
          const userSnapshot = await userRef.get();
          const user = userSnapshot.val();

          if (user) {
            const currentBalance = user.walletBalance || 0;
            const newBalance = currentBalance + amountPaid;

            await userRef.update({ walletBalance: newBalance });

            // Log Transaction
            const transRef = db.ref(`transactions/${userId}`).push();
            await transRef.set({
              id: transRef.key,
              type: 'Top Up',
              amount: amountPaid,
              timestamp: Date.now()
            });

            console.log(`WALLET TOPUP SUCCESS: ${amountPaid} added to user ${userId}`);
          }
        }
        return res.sendStatus(200);
      }

      // 3. Update Booking Status to PAID (Funds held by platform)
      const bookingRef = db.ref(`bookings/${bookingId}`);
      const bookingSnapshot = await bookingRef.get();
      const booking = bookingSnapshot.val();

      if (!booking) {
        console.error('Booking not found:', bookingId);
        return res.sendStatus(404);
      }

      await bookingRef.update({
        paymentStatus: 'PAID',
        payfastTid: pfData.pf_payment_id,
        paidAt: Date.now(),
        // Note: We do NOT update booking status to 'completed' here.
        // We also do NOT credit the worker wallet yet.
      });

      console.log(`Payment SECURED for booking ${bookingId}. Awaiting job completion.`);
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

/**
 * 3. Simple Success/Cancel Redirects
 * These ensure Payfast validation passes and give the app a target to intercept.
 */
app.get('/api/payments/success', (req, res) => {
  res.send('<html><body><h1>Payment Successful!</h1><p>Returning to app...</p></body></html>');
});

app.get('/api/payments/cancel', (req, res) => {
  res.send('<html><body><h1>Payment Cancelled</h1><p>Returning to app...</p></body></html>');
});
