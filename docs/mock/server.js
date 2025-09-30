const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(bodyParser.json());

// Simple in-memory store for mock
let wallet = { balance: 100000, frozen_amount: 0, currency: 'CNY' };
let transactions = [];

app.get('/v1/wallet', (req, res) => {
  res.json({ code: 200, message: 'OK', data: wallet });
});

app.post('/v1/payments/recharge', (req, res) => {
  const { amount, payment_method, idempotency_key } = req.body;
  const id = '3fa85f64-5717-4562-b3fc-2c963f66afa6';
  // create pending order
  res.status(201).json({ code: 201, message: 'Created', data: { id, status: 'PENDING', payment_payload: {} } });
});

app.post('/v1/payments/recharge/callback', (req, res) => {
  // simulate successful payment
  const { id, amount } = req.body;
  wallet.balance += amount || 10000;
  transactions.push({ id: String(transactions.length+1), tx_type: 'RECHARGE', amount: amount || 10000, balance_after: wallet.balance, created_at: new Date().toISOString() });
  res.json({ code: 200, message: 'OK' });
});

app.post('/v1/orders', (req, res) => {
  const orderId = 'order-' + Date.now();
  res.status(201).json({ code: 201, message: 'Created', data: { id: orderId, status: 'PAID' } });
});

app.get('/v1/wallet/transactions', (req, res) => {
  res.json({ code: 200, message: 'OK', data: transactions });
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`Mock server started on ${port}`));
