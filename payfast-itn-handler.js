/**
 * HustleFix Payfast ITN Handler (Node.js/Express)
 * Verified with Merchant ID: 17144161
 */

const express = require('express');
const crypto = require('crypto');
const admin = require('firebase-admin');
const axios = require('axios');

// Initialize Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(require('./service-account-key.json')),
  databaseURL: "https://hustlefix-9c7f6-default-rtdb.firebaseio.com"
});

const db = admin.database();
const app = express();
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
    notify_url: 'https://your-server.com/api/payments/payfast-itn',
    name_first,
    name_last,
    email_address,
    m_payment_id, // This is the HustleFix Booking ID
    amount: parseFloat(amount).toFixed(2),
    item_name
  };

  // Generate Signature
  data.signature = generateSignature(data, PASSPHRASE);

  // Payfast Sandbox URL (Use 'www.payfast.co.za' for production)
  const baseUrl = 'https://sandbox.payfast.co.za/eng/process';
  const checkoutUrl = `${baseUrl}?${new URLSearchParams(data).toString()}`;

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

  // Verify IP/Server with Payfast (Important for security)
  const isValid = await verifyWithPayfast(pfData);
  if (!isValid) {
    console.error('ITN verification failed with Payfast servers');
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
  Object.keys(data).forEach(key => {
    if (data[key] !== '' && key !== 'signature') {
      str += `${key}=${encodeURIComponent(data[key].trim()).replace(/%20/g, '+')}&`;
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

/**
 * Helper: Verify with Payfast Servers
 */
async function verifyWithPayfast(data) {
  // In production, post the data back to Payfast to verify it came from them
  // return axios.post('https://www.payfast.co.za/eng/query/validate', data)...
  return true;
}

app.listen(3000, () => console.log('HustleFix Payfast Backend listening on port 3000'));
