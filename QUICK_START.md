# NovaPay Quick Start Guide - Testing Your First Customer Journey

## 🎯 Quick Summary

**NovaPay** is a fintech platform that lets users:
- 💰 Create digital wallets with secure transfers
- 🎯 Set and track savings goals
- 🔄 Automate recurring transfers for bills and savings
- 📊 Get daily reconciliation reports for compliance

Everything is **event-driven** (Kafka), **reactive** (non-blocking), and **accurate** (double-entry ledger).

---

## ⚡ 5-Minute Quick Start

### 1. **Start Your Application**
```bash
mvn clean install
mvn spring-boot:run
# Or use Docker
docker-compose up -d
```

### 2. **Open Postman**
- Import the updated `postman_collection.json`
- Set `base_url` to `http://localhost:8080`
- Click "Run Collection" or execute requests manually

### 3. **Run the Customer Journey (5 mins)**
Follow these steps in order:

**Step 1: Authentication (30 seconds)**
```
Request: "1. Login as John Doe"
- Gets JWT token
- Auto-saves to {{jwt_token}}
```

**Step 2: Check Balance (10 seconds)**
```
Request: "2. Get Wallet Balance (John's Wallet)"
- Shows: $10,000 initial balance
```

**Step 3: Create Savings Goal (20 seconds)**
```
Request: "5. Create Savings Goal (Emergency Fund)"
- Creates $12,000 goal
- Auto-saves to {{goalId}}
```

**Step 4: Make a Transfer (20 seconds)**
```
Request: "3. Initiate Transfer (John to Jane)"
- Transfers $1,000 from John to Jane
- Auto-saves {{transferId}}
- Watch Kafdrop at http://localhost:9000/
```

**Step 5: Create Recurring Transfer (20 seconds)**
```
Request: "8. Create Recurring Transfer"
- Automates $500/month to savings goal
- Auto-saves {{recurringId}}
```

**Step 6: Contribute to Goal (10 seconds)**
```
Request: "7. Contribute to Goal"
- Adds $500 to emergency fund
- Shows progress: 4.17%
```

**Step 7: Run Reconciliation (10 seconds)**
```
Request: "12. Run Daily Reconciliation"
- Verifies all wallets match ledger
- Reports any discrepancies
```

---

## 🔍 What to Watch For

### In Postman Console
✅ JWT token saved automatically
✅ Goal ID, Recurring ID, Transfer ID saved
✅ Status responses show SUCCESS

### In Kafdrop (http://localhost:9000/)
1. Open in browser
2. Look for these topics:
   - `transfer-events` → See each transfer as it happens
   - `savings-goal-events` → See goal updates
   - `recurring-transfer-events` → See scheduled transfers

### In PostgreSQL Database
```sql
-- Check wallets
SELECT * FROM wallets;

-- Check transfers
SELECT * FROM transfers;

-- Check savings goals
SELECT * FROM savings_goals;

-- Check ledger entries (double-entry proof)
SELECT * FROM ledger_entries;
```

---

## 📋 Sample User Accounts

| Username | Password | Wallet | Balance |
|----------|----------|--------|---------|
| john_doe | password123 | WAL-001 | $10,000 |
| jane_smith | password456 | WAL-002 | $5,000 |
| bob_johnson | password789 | WAL-003 | $8,000 |

---

## 🎬 Two Complete Customer Journeys (Ready to Run)

### **Scenario A: Sarah's Emergency Fund (2 minutes)**
Perfect for understanding savings automation.

```
1. Login as john_doe
2. Check balance → $10,000
3. Create "Emergency Fund" goal ($12,000)
4. Create recurring $500/month transfer
5. Make $1,000 contribution
6. Check goal progress → 16.7%
7. View all goals
8. Run reconciliation
9. Check Kafdrop for events
```

**Expected Results:**
- Balance reduced by $1,000
- Goal shows 16.7% progress
- Recurring transfer scheduled for next month
- All transactions recorded in ledger

---

### **Scenario B: Multiple Transfers & Goals (3 minutes)**
Perfect for testing complex workflows.

```
1. Login as john_doe
2. Create TWO savings goals:
   - Emergency Fund ($12,000)
   - Vacation Fund ($5,000)
3. Create RECURRING monthly transfer to Emergency Fund
4. Make TRANSFER to jane_smith ($1,000)
5. Make TRANSFER to bob_johnson ($500)
6. Get all goals → See progress on both
7. Get all recurring transfers
8. PAUSE the recurring transfer
9. RESUME the recurring transfer
10. Run reconciliation
```

**Expected Results:**
- 2 goals created, both ACTIVE
- 1 recurring transfer created, ACTIVE
- 2 transfers completed, balances updated
- All changes recorded in Kafka topics
- Reconciliation shows 0 discrepancies

---

## 🚀 Running Automated Test Collection

Instead of clicking each request:

1. Open Postman
2. Click the collection name
3. Click the "Run" button (play icon)
4. Select requests to run
5. Watch execution progress
6. View all results

---

## ✅ Verification Checklist

After completing the journey, verify:

- [ ] Login response contains JWT token
- [ ] Wallet balance decreases after transfer
- [ ] Savings goal shows progress percentage
- [ ] Recurring transfer status is ACTIVE
- [ ] Transfer ID appears in status check
- [ ] Kafdrop shows events for:
  - [ ] transfer-events
  - [ ] savings-goal-events
  - [ ] recurring-transfer-events
- [ ] Reconciliation report shows 0 discrepancies
- [ ] Database ledger has double entries for each transfer

---

## 🐛 Troubleshooting

### Issue: "Invalid JWT Token"
**Solution:** 
- Copy the full token from login response
- Paste into Authorization header
- Or let the Post-Test script save it automatically

### Issue: "Wallet not found"
**Solution:**
- Use correct wallet number: WAL-001, WAL-002, etc.
- Not wallet ID, but wallet NUMBER

### Issue: "Transfer failed"
**Solution:**
- Check balance is sufficient
- Both wallets must exist
- Currencies must match (USD)

### Issue: "No Kafka events showing"
**Solution:**
- Verify Kafka is running: `docker-compose ps`
- Check Kafdrop is accessible: http://localhost:9000/
- Refresh Kafdrop page to see latest events

### Issue: "Reconciliation shows discrepancies"
**Solution:**
- This is rare but means a wallet balance doesn't match ledger
- Check database for orphaned transactions
- Review transfer logs

---

## 📚 Next Steps

1. **Test More Scenarios:**
   - Transfer between different users
   - Create multiple goals with different targets
   - Set up daily, weekly, monthly recurring transfers
   - Test pause/resume functionality

2. **Monitor Events:**
   - Open Kafdrop while making transfers
   - Watch events flow in real-time
   - Understand event-driven architecture

3. **Check Database:**
   - Connect to PostgreSQL wallet_db
   - Query transactions, ledger, goals
   - Verify double-entry bookkeeping

4. **Load Testing:**
   - Use Postman Runner to send 100+ transfers
   - Monitor Kafka message throughput
   - Check reconciliation performance

---

## 📞 Key Endpoints

| Feature | Endpoint | Method |
|---------|----------|--------|
| Login | `/api/auth/login` | POST |
| Check Balance | `/api/wallets/{number}/balance` | GET |
| Create Goal | `/api/savings-goals` | POST |
| List Goals | `/api/savings-goals/user` | GET |
| Contribute Goal | `/api/savings-goals/{id}/contribute` | POST |
| Transfer Money | `/api/transfers/initiate` | POST |
| Transfer Status | `/api/transfers/{id}/status` | GET |
| Recurring Transfer | `/api/recurring-transfers` | POST |
| List Recurring | `/api/recurring-transfers/user` | GET |
| Pause Recurring | `/api/recurring-transfers/{id}/pause` | PUT |
| Reconciliation | `/api/reconciliation/run` | POST |

---

## 🎓 Learning Outcomes

After completing these journeys, you'll understand:

✅ **JWT Authentication** - How secure token-based auth works
✅ **Reactive Programming** - Non-blocking, event-driven flows
✅ **Kafka Events** - Real-time event streaming and monitoring
✅ **Savings Automation** - Goal tracking with recurring transfers
✅ **Financial Accuracy** - Double-entry ledger system
✅ **API Design** - RESTful endpoints for financial services

---

## 💡 Pro Tips

1. **Use Postman Variables**: They auto-save IDs between requests
2. **Watch Console Output**: See what's happening in real-time
3. **Check Kafdrop First**: Understand event flow before code
4. **Query Database**: Verify backend state independently
5. **Try Edge Cases**: What happens with $0 balance? Negative amounts?

---

## 📞 Support

For more details:
- See `CUSTOMER_JOURNEY_GUIDE.md` for detailed scenarios
- See `README.md` for setup instructions
- See `KAFKA_SETUP_GUIDE.md` for Kafka configuration
- Check controller code for endpoint details

**Happy Testing! 🚀**


