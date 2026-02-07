# 🚀 NovaPay Quick Reference Card

## What Is NovaPay?
A fintech platform for digital wallets, savings goals, and automated transfers using reactive architecture.

---

## 🎯 The 3 Key Things

### **1. Wallets** 💰
Users have digital wallets with balance, limits, and status.
- Check balance: `GET /api/wallets/{number}/balance`
- Sample balance: John has $10,000

### **2. Transfers** 💸
Send money between wallets instantly.
- Send money: `POST /api/transfers/initiate`
- Check status: `GET /api/transfers/{id}/status`
- Ledger: Creates DEBIT + CREDIT entries

### **3. Automation** 🤖
Save automatically with goals and recurring transfers.
- Create goal: `POST /api/savings-goals` ($12,000 target)
- Recurring: `POST /api/recurring-transfers` ($500/month)
- Progress: Auto-calculated percentage

---

## ⚡ 5-Minute Journey

```
1. Login (30s)          → POST /api/auth/login
2. Check Balance (10s)  → GET /api/wallets/WAL-001/balance  
3. Create Goal (20s)    → POST /api/savings-goals (Emergency Fund)
4. Make Transfer (20s)  → POST /api/transfers/initiate ($1,000)
5. Recurring (20s)      → POST /api/recurring-transfers ($500/month)
6. Reconcile (10s)      → POST /api/reconciliation/run
```

**Total: 5 minutes ✅**

---

## 📊 Sample Users

```
john_doe / password123        → WAL-001 ($10,000)
jane_smith / password456      → WAL-002 ($5,000)
bob_johnson / password789     → WAL-003 ($8,000)
```

---

## 🧪 Using Postman

1. **Import** `postman_collection.json`
2. **Run** requests 1-13 in order
3. **Variables auto-populate**: {{jwt_token}}, {{goalId}}, etc.
4. **Response** shows results

Example request: `POST /api/transfers/initiate`
```json
{
  "sourceWalletId": 1,
  "destinationWalletId": 2,
  "amount": 1000.00,
  "currency": "USD",
  "description": "Transfer to Jane"
}
```

Example response:
```json
{
  "transferId": "TXN-20240115-001",
  "status": "SUCCESS",
  "amount": 1000.00
}
```

---

## 📡 Event Monitoring

**Kafdrop**: http://localhost:9000/

Three topics to watch:
- `transfer-events` → See transfers
- `savings-goal-events` → See goal updates  
- `recurring-transfer-events` → See scheduled transfers

---

## 💾 Database Verification

```sql
-- Check transfers
SELECT * FROM transfers ORDER BY created_at DESC;

-- Check goals
SELECT * FROM savings_goals WHERE user_id = 1;

-- Verify ledger (debit = credit)
SELECT * FROM ledger_entries LIMIT 10;

-- Check wallets
SELECT * FROM wallets;
```

---

## 🔑 API Endpoints

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Login | POST | /api/auth/login |
| Balance | GET | /api/wallets/{number}/balance |
| Transfer | POST | /api/transfers/initiate |
| Status | GET | /api/transfers/{id}/status |
| Goal | POST | /api/savings-goals |
| Goals | GET | /api/savings-goals/user |
| Contribute | POST | /api/savings-goals/{id}/contribute |
| Recurring | POST | /api/recurring-transfers |
| Pause | PUT | /api/recurring-transfers/{id}/pause |
| Resume | PUT | /api/recurring-transfers/{id}/resume |
| Reconcile | POST | /api/reconciliation/run |

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| "Invalid JWT" | Copy full token from login response |
| "Wallet not found" | Use WAL-001, not wallet ID |
| "Insufficient balance" | Check balance before transfer |
| "No Kafka events" | Verify Docker running: `docker-compose ps` |
| "Transfer failed" | Check currencies match (USD=USD) |

---

## 📚 Which Doc to Read?

| Time | Document |
|------|----------|
| 5 min | **QUICK_START.md** |
| 10 min | **PROJECT_SUMMARY.md** |
| 15 min | **CUSTOMER_JOURNEY_GUIDE.md** |
| 20 min | **ARCHITECTURE.md** |

---

## ✅ Verification Checklist

After testing:

- [ ] JWT token saved automatically
- [ ] Balance shows correctly
- [ ] Transfer created with ID
- [ ] Goal shows progress percentage
- [ ] Recurring transfer scheduled
- [ ] Kafdrop shows events
- [ ] Database has ledger entries
- [ ] Reconciliation reports 0 discrepancies

---

## 🎯 3 Complete Journeys

### **Journey 1: Emergency Fund (2 min)**
Login → Check Balance → Create Goal → Set Recurring → Contribute → Check Progress

### **Journey 2: Multiple Transfers (3 min)**
Create 2 Goals → Create 2 Recurring → Transfer to Jane → Transfer to Bob → Verify All → Reconcile

### **Journey 3: Reconciliation (1 min)**
Run Multiple Transfers → Run Reconciliation → Check Discrepancies

---

## 🚀 Getting Started

1. **Read**: QUICK_START.md (5 min)
2. **Run**: 5-step journey in Postman (5 min)
3. **Watch**: Kafdrop for events (2 min)
4. **Query**: Database (2 min)
5. **Celebrate**: You understand fintech! 🎉

---

## 💡 Key Concepts

**JWT Token**: Security credential, valid 24 hours
**Wallet**: Digital container for money
**Transfer**: Send money between wallets (DEBIT + CREDIT)
**Goal**: Target amount to save (tracks progress %)
**Recurring**: Automated transfer (DAILY/WEEKLY/MONTHLY/etc.)
**Ledger**: Financial record (double-entry: every transfer = 2 entries)
**Kafka**: Event streaming (real-time notifications)
**Kafdrop**: Kafka UI (see events visually)

---

## 📞 Quick Links

- **Kafdrop**: http://localhost:9000/
- **API**: http://localhost:8080/
- **Database**: localhost:5432/wallet_db
- **Collections**: See postman_collection.json

---

## 🎓 Tech Stack

- **Framework**: Spring Boot WebFlux (reactive)
- **Database**: PostgreSQL + R2DBC
- **Events**: Kafka 2.8+
- **Security**: JWT tokens
- **Language**: Java 17

---

**Everything is ready. Start with QUICK_START.md! 🚀**


