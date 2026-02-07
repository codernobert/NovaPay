# NovaPay Customer Journey Guide - Complete Testing Workflow

## 📋 Project Overview

**NovaPay** is a reactive fintech platform that demonstrates modern digital wallet capabilities with:
- ✅ Instant fund transfers
- ✅ Savings automation with goal tracking
- ✅ Recurring transfers (payments, savings)
- ✅ Real-time reconciliation
- ✅ Event-driven architecture (Kafka)
- ✅ Double-entry ledger for financial accuracy
- ✅ JWT-based security

## 🏗️ Technology Stack

- **Backend**: Spring Boot 3.2.0 + Spring WebFlux (Reactive)
- **Database**: PostgreSQL (R2DBC)
- **Event Streaming**: Apache Kafka
- **Authentication**: JWT Tokens
- **Real-time UI**: Kafdrop (http://localhost:9000/)

---

## 👥 Sample Customer Journeys

### **Journey 1: Sarah's Emergency Fund (Basic User)**
Sarah (age 28) wants to build a 6-month emergency fund of $12,000. She earns $4,000/month and wants to automate $500/month to savings.

**Goals:**
1. Login to the system
2. Check wallet balance
3. Create a savings goal ($12,000 emergency fund)
4. Set up a recurring monthly transfer of $500
5. Make a one-time transfer to boost savings
6. Track goal progress

---

### **Journey 2: Mike's Business Payments (Freelancer)**
Mike is a freelancer who earns irregular income and needs to automate vendor payments.

**Goals:**
1. Login to the system
2. Check available balance
3. Create a "Vendor Payments" savings goal
4. Set up recurring weekly transfers for fixed expenses
5. Make one-time transfers to clients/vendors
6. Monitor all transactions

---

### **Journey 3: Admin Reconciliation (Compliance)**
Daily reconciliation to ensure financial accuracy and detect discrepancies.

**Goals:**
1. Run daily reconciliation process
2. View reconciliation report
3. Check for wallet discrepancies
4. Verify all transactions are properly recorded

---

## 🧪 Postman Testing Workflow

### **Prerequisites**
- Application running on `http://localhost:8080`
- PostgreSQL running with wallet_db
- Kafka running on localhost:9092
- Kafdrop accessible at http://localhost:9000/

### **Sample Users (Pre-loaded in Database)**
```
User: john_doe
Password: password123
Wallet ID: 1
Balance: $10,000

User: jane_smith
Password: password456
Wallet ID: 2
Balance: $5,000

User: bob_johnson
Password: password789
Wallet ID: 3
Balance: $8,000
```

---

## 🚀 Step-by-Step API Testing Flow

### **Step 1: Authentication**
```
POST /api/auth/login
Body: {
  "username": "john_doe",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful"
}
```

**Action**: Save the token as `{{jwt_token}}` variable in Postman

---

### **Step 2: Check Initial Wallet Balance**
```
GET /api/wallets/WAL-001/balance
Header: Authorization: Bearer {{jwt_token}}

Response:
{
  "walletNumber": "WAL-001",
  "balance": 10000.00,
  "currency": "USD",
  "status": "ACTIVE",
  "dailyLimit": 50000.00,
  "availableBalance": 10000.00,
  "lastUpdated": "2024-01-15T10:30:00Z"
}
```

---

### **Step 3: Create Savings Goal (Emergency Fund)**
```
POST /api/savings-goals
Header: Authorization: Bearer {{jwt_token}}
Header: X-User-Id: 1

Body: {
  "goalName": "Emergency Fund",
  "description": "6 months of living expenses",
  "targetAmount": 12000.00,
  "currency": "USD",
  "destinationWalletId": 1,
  "targetDate": "2026-12-31"
}

Response:
{
  "goalId": 101,
  "goalName": "Emergency Fund",
  "targetAmount": 12000.00,
  "currentAmount": 0.00,
  "progressPercentage": 0.0,
  "currency": "USD",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:35:00Z",
  "targetDate": "2026-12-31"
}
```

**Action**: Save goalId as `{{goalId}}`

---

### **Step 4: Create Recurring Transfer (Auto-Save)**
```
POST /api/recurring-transfers
Header: Authorization: Bearer {{jwt_token}}
Header: X-User-Id: 1

Body: {
  "sourceWalletId": 1,
  "destinationWalletId": 1,
  "amount": 500.00,
  "currency": "USD",
  "frequency": "MONTHLY",
  "description": "Monthly savings contribution",
  "linkedGoalId": 101
}

Response:
{
  "recurringId": 201,
  "sourceWalletId": 1,
  "destinationWalletId": 1,
  "amount": 500.00,
  "frequency": "MONTHLY",
  "status": "ACTIVE",
  "linkedGoalId": 101,
  "createdAt": "2024-01-15T10:40:00Z",
  "nextExecutionDate": "2024-02-15"
}
```

**Action**: Save recurringId as `{{recurringId}}`

---

### **Step 5: Make One-Time Transfer**
```
POST /api/transfers/initiate
Header: Authorization: Bearer {{jwt_token}}
Header: Content-Type: application/json
Header: X-User-Id: 1

Body: {
  "sourceWalletId": 1,
  "destinationWalletId": 2,
  "amount": 1000.00,
  "currency": "USD",
  "description": "Personal loan to Jane"
}

Response:
{
  "transferId": "TXN-20240115-001",
  "sourceWalletId": 1,
  "destinationWalletId": 2,
  "amount": 1000.00,
  "status": "SUCCESS",
  "message": "Transfer completed successfully",
  "createdAt": "2024-01-15T10:45:00Z",
  "processedAt": "2024-01-15T10:45:05Z"
}
```

**Action**: Save transferId as `{{transferId}}`

---

### **Step 6: Check Transfer Status**
```
GET /api/transfers/TXN-20240115-001/status
Header: Authorization: Bearer {{jwt_token}}

Response:
{
  "transferId": "TXN-20240115-001",
  "status": "SUCCESS",
  "sourceWalletId": 1,
  "destinationWalletId": 2,
  "amount": 1000.00,
  "createdAt": "2024-01-15T10:45:00Z",
  "processedAt": "2024-01-15T10:45:05Z"
}
```

---

### **Step 7: Get User Savings Goals**
```
GET /api/savings-goals/user
Header: Authorization: Bearer {{jwt_token}}
Header: X-User-Id: 1

Response:
[
  {
    "goalId": 101,
    "goalName": "Emergency Fund",
    "targetAmount": 12000.00,
    "currentAmount": 500.00,
    "progressPercentage": 4.17,
    "status": "ACTIVE",
    "currency": "USD"
  },
  {
    "goalId": 102,
    "goalName": "Vacation Fund",
    "targetAmount": 5000.00,
    "currentAmount": 0.00,
    "progressPercentage": 0.0,
    "status": "ACTIVE",
    "currency": "USD"
  }
]
```

---

### **Step 8: Contribute to Savings Goal**
```
POST /api/savings-goals/101/contribute
Header: Authorization: Bearer {{jwt_token}}
Header: X-User-Id: 1
Param: amount=500.00

Response:
{
  "goalId": 101,
  "goalName": "Emergency Fund",
  "currentAmount": 1000.00,
  "progressPercentage": 8.33,
  "status": "ACTIVE"
}
```

---

### **Step 9: Get User Recurring Transfers**
```
GET /api/recurring-transfers/user
Header: Authorization: Bearer {{jwt_token}}
Header: X-User-Id: 1

Response:
[
  {
    "recurringId": 201,
    "amount": 500.00,
    "frequency": "MONTHLY",
    "status": "ACTIVE",
    "nextExecutionDate": "2024-02-15",
    "linkedGoalId": 101
  }
]
```

---

### **Step 10: Pause Recurring Transfer**
```
PUT /api/recurring-transfers/201/pause
Header: Authorization: Bearer {{jwt_token}}
Header: X-User-Id: 1

Response:
{
  "recurringId": 201,
  "status": "PAUSED",
  "reason": "User paused"
}
```

---

### **Step 11: Resume Recurring Transfer**
```
PUT /api/recurring-transfers/201/resume
Header: Authorization: Bearer {{jwt_token}}
Header: X-User-Id: 1

Response:
{
  "recurringId": 201,
  "status": "ACTIVE",
  "nextExecutionDate": "2024-02-15"
}
```

---

### **Step 12: Run Daily Reconciliation**
```
POST /api/reconciliation/run
Header: Authorization: Bearer {{jwt_token}}

Response:
{
  "reconciliationId": "RECON-20240115-001",
  "executedAt": "2024-01-15T23:00:00Z",
  "status": "COMPLETED",
  "totalWalletsReconciled": 3,
  "discrepanciesFound": 0,
  "walletDiscrepancies": []
}
```

---

### **Step 13: Reconcile Single Wallet**
```
GET /api/reconciliation/wallet/WAL-001
Header: Authorization: Bearer {{jwt_token}}

Response:
{
  "walletNumber": "WAL-001",
  "walletBalance": 8000.00,
  "ledgerTotal": 8000.00,
  "isBalanced": true,
  "discrepancyAmount": 0.00,
  "lastReconciled": "2024-01-15T23:00:00Z"
}
```

---

## 📊 Monitoring with Kafdrop

### Access Kafka Topics
1. Open http://localhost:9000/ (Kafdrop UI)
2. View topics created by the application:
   - `transfer-events` - All transfer events
   - `savings-goal-events` - Goal updates
   - `recurring-transfer-events` - Scheduled transfers

### Monitor Events in Real-Time
- Click on a topic to see recent messages
- View event payloads (JSON)
- Track event ordering and timestamps

---

## 🎯 Complete Journey Scenarios

### **Scenario 1: Monthly Savings Automation (Duration: 2 minutes)**
1. ✅ Login as john_doe
2. ✅ Check wallet balance ($10,000)
3. ✅ Create "Emergency Fund" goal ($12,000)
4. ✅ Set recurring monthly transfer ($500)
5. ✅ Make immediate contribution ($1,000)
6. ✅ Check goal progress (16.7%)
7. ✅ View Kafka events in Kafdrop
8. ✅ Run reconciliation

### **Scenario 2: Multiple Transfers & Goal Tracking (Duration: 5 minutes)**
1. ✅ Login as john_doe
2. ✅ Create 3 different savings goals
3. ✅ Create recurring transfers for 2 goals
4. ✅ Make one-time transfers to different users
5. ✅ Check balance after transfers
6. ✅ Pause one recurring transfer
7. ✅ Check all goals and their progress
8. ✅ Run full reconciliation

### **Scenario 3: Compliance Testing (Duration: 3 minutes)**
1. ✅ Run multiple transfers
2. ✅ Check wallet discrepancies
3. ✅ Run daily reconciliation
4. ✅ Verify all transactions recorded
5. ✅ Check audit trail in database

---

## 🔍 Key Features to Test

| Feature | Endpoint | Method | Time |
|---------|----------|--------|------|
| Login | `/api/auth/login` | POST | 5s |
| Check Balance | `/api/wallets/{walletNumber}/balance` | GET | 2s |
| Create Goal | `/api/savings-goals` | POST | 3s |
| Create Recurring | `/api/recurring-transfers` | POST | 3s |
| One-time Transfer | `/api/transfers/initiate` | POST | 5s |
| Contribute to Goal | `/api/savings-goals/{id}/contribute` | POST | 3s |
| List Goals | `/api/savings-goals/user` | GET | 2s |
| Pause Transfer | `/api/recurring-transfers/{id}/pause` | PUT | 2s |
| Resume Transfer | `/api/recurring-transfers/{id}/resume` | PUT | 2s |
| Run Reconciliation | `/api/reconciliation/run` | POST | 10s |

---

## 🐛 Troubleshooting

### "Invalid JWT Token"
- Ensure you're copying the full token from the login response
- Check that the token hasn't expired (24 hours default)
- Re-login to get a fresh token

### "Wallet not found"
- Use the correct wallet number (WAL-001, WAL-002, etc.)
- Check available wallets via `/api/wallets/user/{userId}`

### "Insufficient balance"
- Check current balance before making transfers
- Ensure source wallet has enough funds

### "Transfer failed"
- Verify both wallets exist
- Check daily limit hasn't been exceeded
- Ensure currencies match

### "Kafka events not appearing"
- Check Kafka is running: `docker-compose ps`
- Verify Kafdrop is accessible: http://localhost:9000/
- Look for error logs in application

---

## 📝 Next Steps

1. **Import Updated Postman Collection** (`postman_collection.json`)
2. **Set Postman Variables**:
   - `base_url`: http://localhost:8080
   - `jwt_token`: (Will be filled after login)
   - `goalId`: (Will be filled after creating goal)
   - `recurringId`: (Will be filled after creating recurring transfer)

3. **Run Sample Journey**:
   - Execute "1. Login" request
   - Run "2. Check Balance"
   - Continue through the workflow

4. **Monitor in Kafdrop**:
   - Open http://localhost:9000/
   - Watch events flow in real-time

5. **Check Database**:
   - Query `wallets`, `savings_goals`, `transfers` tables
   - Verify ledger entries (double-entry bookkeeping)

---

## 📚 Additional Resources

- **README.md** - Project setup instructions
- **COMPLETE_SUMMARY.md** - Technical architecture
- **SAVINGS_AUTOMATION_GUIDE.md** - Deep dive into savings features
- **KAFKA_SETUP_GUIDE.md** - Kafka configuration details


