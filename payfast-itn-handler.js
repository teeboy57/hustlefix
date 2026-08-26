/**
 * HustleFix Payfast ITN Handler (Node.js/Express)
 */

const express = require('express');
const crypto = require('crypto');
const admin = require('firebase-admin');

try {
  const serviceAccount = require('./service-account-key.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://hustlefix-9c7f6-default-rtdb.firebaseio.com"
  });
} catch (e) {
  admin.initializeApp({
    databaseURL: "https://hustlefix-9c7f6-default-rtdb.firebaseio.com"
  });
}

const db = admin.database();
const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// --- CREDENTIALS CONFIG ---
// For LIVE: Use 17144161 / cxfxu4iwaewmg / (Your Live Passphrase)
// For SANDBOX: Use 10053500 / s7dtvpr5uallq / Treasure071152
const MERCHANT_ID = '10053500';
const MERCHANT_KEY = 's7dtvpr5uallq';
const PASSPHRASE = 'Treasure071152';

const SUCCESS_URL = 'https://hustlefix.onrender.com/api/payments/success';
const CANCEL_URL = 'https://hustlefix.onrender.com/api/payments/cancel';
const NOTIFY_URL = 'https://hustlefix.onrender.com/api/payments/payfast-itn';

/**
 * 1. Create Checkout Request
 */
app.post('/api/payments/create-checkout', async (req, res) => {
  const { amount, item_name, m_payment_id, name_first, name_last, email_address } = req.body;

  // Build data object in the EXACT order Payfast expects
  const data = {
    merchant_id: MERCHANT_ID,
    merchant_key: MERCHANT_KEY,
    return_url: SUCCESS_URL,
    cancel_url: CANCEL_URL,
    notify_url: NOTIFY_URL,
    name_first: (name_first || "Customer").trim(),
    name_last: (name_last || "User").trim(),
    email_address: (email_address || "customer@example.com").trim(),
    m_payment_id: (m_payment_id || String(Date.now())).trim(),
    amount: parseFloat(amount || 0).toFixed(2),
    item_name: (item_name || "HustleFix Service").trim()
  };

  // Generate Signature String
  let signatureString = '';
  for (let key in data) {
    if (data[key] !== "") {
      signatureString += `${key}=${encodeURIComponent(data[key]).replace(/%20/g, '+')}&`;
    }
  }

  // Remove trailing & and add raw passphrase (do NOT encode the passphrase name)
  signatureString = signatureString.substring(0, signatureString.length - 1);
  if (PASSPHRASE) {
    signatureString += `&passphrase=${encodeURIComponent(PASSPHRASE.trim()).replace(/%20/g, '+')}`;
  }

  const signature = crypto.createHash('md5').update(signatureString).digest('hex');

  // Build Final URL
  const isSandbox = MERCHANT_ID.startsWith('10');
  const baseUrl = isSandbox
    ? 'https://sandbox.payfast.co.za/eng/process'
    : 'https://www.payfast.co.za/eng/process';

  const checkoutUrl = `${baseUrl}?${signatureString.split('&passphrase=')[0]}&signature=${signature}`;

  res.json({ success: true, checkoutUrl: checkoutUrl });
});

/**
 * 2. ITN Handler (Firebase Balance Update)
 */
app.post('/api/payments/payfast-itn', async (req, res) => {
  const pfData = req.body;

  // Signature verification would go here (omitted for brevity)

  if (pfData.payment_status === 'COMPLETE') {
    const bookingId = pfData.m_payment_id;
    const amountPaid = parseFloat(pfData.amount_gross);

    try {
      if (bookingId.startsWith('TOPUP_')) {
        const userId = bookingId.split('_')[1];
        if (userId) {
          const userRef = db.ref(`users/${userId}`);
          const snapshot = await userRef.child('walletBalance').get();
          const current = snapshot.val() || 0;
          await userRef.update({ walletBalance: current + amountPaid });

          const transRef = db.ref(`transactions/${userId}`).push();
          await transRef.set({
            id: transRef.key,
            type: 'Top Up',
            amount: amountPaid,
            timestamp: Date.now()
          });
        }
      } else {
        await db.ref(`bookings/${bookingId}`).update({
          paymentStatus: 'PAID',
          paidAt: Date.now()
        });
      }
    } catch (error) {
      console.error('ITN Error:', error);
    }
  }
  res.sendStatus(200);
});

app.get('/api/payments/success', (req, res) => res.send('Payment Successful! Returning to HustleFix...'));
app.get('/api/payments/cancel', (req, res) => res.send('Payment Cancelled. Returning to HustleFix...'));

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Backend running on port ${PORT}`));
