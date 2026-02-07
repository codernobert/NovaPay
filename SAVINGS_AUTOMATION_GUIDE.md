# Savings Automation Feature Guide

## Overview

The Savings Automation feature enables users to:
- **Set Savings Goals** with target amounts and dates
- **Create Recurring Transfers** to automatically move money to savings
- **Track Progress** with real-time percentage and achievement tracking
- **Get Suggestions** for monthly contributions based on goals

## Features

### 1. Savings Goals

Create and manage savings goals with automatic progress tracking.

**Key Capabilities:**
- Set target amount and date
- Real-time progress percentage calculation
- Automatic achievement detection when target is reached
- Suggested monthly contribution calculation
- Link recurring transfers to goals

### 2. Recurring Transfers

Automate regular transfers to savings wallets.

**Supported Frequencies:**
- **DAILY** - Every day
- **WEEKLY** - Every week on specific day
- **BIWEEKLY** - Every two weeks
- **MONTHLY** - Specific day of month
- **QUARTERLY** - Every 3 months
- **YEARLY** - Once per year

**Features:**
- Automatic execution via scheduled job (runs hourly)
- Optional end date or max execution count
- Link to savings goal for automatic contribution tracking
- Pause/Resume/Cancel capabilities

## API Endpoints

### Savings Goals

#### Create Savings Goal
```http
POST /api/savings-goals
Authorization: Bearer {token}
Content-Type: application/json

{
  "goalName": "Emergency Fund",
  "description": "Build 6 months emergency fund",
  "savingsWalletNumber": "WLT-1002",
  "targetAmount": 10000.00,
  "currency": "USD",
  "targetDate": "2025-12-31"
}
```

**Response:**
```json
{
  "goalId": 1,
  "goalName": "Emergency Fund",
  "description": "Build 6 months emergency fund",
  "savingsWalletNumber": "****",
  "targetAmount": 10000.00,
  "currentAmount": 0.00,
  "currency": "USD",
  "progressPercentage": 0.00,
  "targetDate": "2025-12-31",
  "status": "ACTIVE",
  "daysRemaining": 328,
  "amountNeeded": 10000.00,
  "suggestedMonthlyContribution": 909.09,
  "createdAt": "2025-02-06T10:30:00"
}
```

#### Get User's Goals
```http
GET /api/savings-goals/user
Authorization: Bearer {token}
```

#### Get Active Goals
```http
GET /api/savings-goals/user/active
Authorization: Bearer {token}
```

#### Contribute to Goal
```http
POST /api/savings-goals/{goalId}/contribute?amount=500.00
Authorization: Bearer {token}
```

This manually adds an amount to the goal (useful for one-time contributions).

#### Pause/Resume/Cancel Goal
```http
PUT /api/savings-goals/{goalId}/pause
PUT /api/savings-goals/{goalId}/resume
DELETE /api/savings-goals/{goalId}
Authorization: Bearer {token}
```

### Recurring Transfers

#### Create Recurring Transfer
```http
POST /api/recurring-transfers
Authorization: Bearer {token}
Content-Type: application/json

{
  "sourceWalletNumber": "WLT-1001",
  "destinationWalletNumber": "WLT-1002",
  "savingsGoalId": 1,
  "amount": 500.00,
  "currency": "USD",
  "frequency": "MONTHLY",
  "dayOfMonth": 1,
  "executionTime": "09:00:00",
  "startDate": "2025-03-01",
  "endDate": "2025-12-31",
  "description": "Monthly savings contribution"
}
```

**Response:**
```json
{
  "recurringTransferId": 1,
  "sourceWalletNumber": "WLT-1001",
  "destinationWalletNumber": "WLT-1002",
  "savingsGoalId": 1,
  "savingsGoalName": "Emergency Fund",
  "amount": 500.00,
  "currency": "USD",
  "frequency": "MONTHLY",
  "dayOfMonth": 1,
  "executionTime": "09:00:00",
  "startDate": "2025-03-01",
  "endDate": "2025-12-31",
  "nextExecutionDate": "2025-03-01",
  "status": "ACTIVE",
  "executionCount": 0,
  "description": "Monthly savings contribution",
  "createdAt": "2025-02-06T10:30:00"
}
```

#### Get User's Recurring Transfers
```http
GET /api/recurring-transfers/user
GET /api/recurring-transfers/user/active
Authorization: Bearer {token}
```

#### Pause/Resume/Cancel Recurring Transfer
```http
PUT /api/recurring-transfers/{recurringId}/pause
PUT /api/recurring-transfers/{recurringId}/resume
DELETE /api/recurring-transfers/{recurringId}
Authorization: Bearer {token}
```

## Frequency Configuration

### DAILY
```json
{
  "frequency": "DAILY",
  "executionTime": "09:00:00"
}
```
Executes every day at 9 AM.

### WEEKLY
```json
{
  "frequency": "WEEKLY",
  "dayOfWeek": 1,
  "executionTime": "09:00:00"
}
```
- `dayOfWeek`: 1=Monday, 2=Tuesday, ..., 7=Sunday
- Executes every Monday at 9 AM

### MONTHLY
```json
{
  "frequency": "MONTHLY",
  "dayOfMonth": 15,
  "executionTime": "09:00:00"
}
```
- `dayOfMonth`: 1-28 (limited to 28 for consistency across all months)
- Executes on the 15th of each month at 9 AM

### BIWEEKLY
```json
{
  "frequency": "BIWEEKLY",
  "dayOfWeek": 5,
  "executionTime": "09:00:00"
}
```
Executes every other Friday at 9 AM.

## Use Cases

### Example 1: Emergency Fund Goal with Monthly Contributions

**Step 1: Create Savings Goal**
```bash
curl -X POST http://localhost:8080/api/savings-goals \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "goalName": "Emergency Fund",
    "savingsWalletNumber": "WLT-1002",
    "targetAmount": 6000.00,
    "currency": "USD",
    "targetDate": "2025-12-31"
  }'
```

**Step 2: Set Up Recurring Transfer**
```bash
curl -X POST http://localhost:8080/api/recurring-transfers \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceWalletNumber": "WLT-1001",
    "destinationWalletNumber": "WLT-1002",
    "savingsGoalId": 1,
    "amount": 500.00,
    "currency": "USD",
    "frequency": "MONTHLY",
    "dayOfMonth": 1,
    "startDate": "2025-03-01"
  }'
```

### Example 2: Vacation Fund with Weekly Contributions

```bash
# Create goal
curl -X POST http://localhost:8080/api/savings-goals \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "goalName": "Summer Vacation",
    "savingsWalletNumber": "WLT-1002",
    "targetAmount": 3000.00,
    "currency": "USD",
    "targetDate": "2025-07-01"
  }'

# Set up weekly recurring transfer
curl -X POST http://localhost:8080/api/recurring-transfers \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceWalletNumber": "WLT-1001",
    "destinationWalletNumber": "WLT-1002",
    "savingsGoalId": 2,
    "amount": 150.00,
    "currency": "USD",
    "frequency": "WEEKLY",
    "dayOfWeek": 5,
    "startDate": "2025-02-07",
    "maxExecutions": 20
  }'
```

## How Recurring Transfers Work

### Execution Process

1. **Scheduled Job** runs every hour (`@Scheduled(cron = "0 0 * * * *")`)
2. **Finds Due Transfers** where `next_execution_date <= today`
3. **For Each Transfer:**
    - Validates source wallet has sufficient funds
    - Creates a regular transfer
    - Updates savings goal contribution (if linked)
    - Calculates next execution date
    - Updates execution count
    - Checks if should stop (end date or max executions)

### Execution States

- **ACTIVE**: Ready to execute
- **PAUSED**: Temporarily stopped
- **COMPLETED**: Finished (reached end date or max executions)
- **CANCELLED**: Manually cancelled by user
- **FAILED**: Last execution failed

### Automatic Stopping

Recurring transfer stops automatically when:
- `endDate` is reached
- `maxExecutions` is reached
- Source wallet insufficient balance (status → FAILED)

## Database Schema

### savings_goals Table
```sql
id                  BIGSERIAL PRIMARY KEY
user_id             BIGINT (FK to users)
savings_wallet_id   BIGINT (FK to wallets)
goal_name           VARCHAR(200)
description         TEXT
target_amount       DECIMAL(19,4)
current_amount      DECIMAL(19,4)
currency            VARCHAR(3)
target_date         DATE
status              VARCHAR(20)
progress_percentage DECIMAL(5,2)
created_at          TIMESTAMP
updated_at          TIMESTAMP
achieved_at         TIMESTAMP
```

### recurring_transfers Table
```sql
id                    BIGSERIAL PRIMARY KEY
user_id               BIGINT (FK to users)
source_wallet_id      BIGINT (FK to wallets)
destination_wallet_id BIGINT (FK to wallets)
savings_goal_id       BIGINT (FK to savings_goals, nullable)
amount                DECIMAL(19,4)
currency              VARCHAR(3)
frequency             VARCHAR(20)
day_of_week           INTEGER (1-7)
day_of_month          INTEGER (1-28)
execution_time        TIME
start_date            DATE
end_date              DATE (nullable)
next_execution_date   DATE
last_executed_at      TIMESTAMP
status                VARCHAR(20)
execution_count       INTEGER
max_executions        INTEGER (nullable)
description           VARCHAR(500)
created_at            TIMESTAMP
updated_at            TIMESTAMP
```

## Progress Tracking

### Goal Progress Calculation

```java
progressPercentage = (currentAmount / targetAmount) * 100
```

### Suggested Monthly Contribution

```java
monthsRemaining = daysRemaining / 30
amountNeeded = targetAmount - currentAmount
        suggestedMonthly = amountNeeded / monthsRemaining
```

### Achievement Detection

When `currentAmount >= targetAmount`:
- Status → ACHIEVED
- `achieved_at` timestamp set
- Audit log created

## Best Practices

### For Users

1. **Set Realistic Goals**: Ensure target date gives enough time
2. **Start Small**: Begin with achievable amounts
3. **Review Regularly**: Check progress monthly
4. **Adjust as Needed**: Pause/resume based on financial situation
5. **Use Goal Linking**: Connect recurring transfers to goals for automatic tracking

### For Developers

1. **Monitor Scheduled Job**: Ensure recurring transfer job runs successfully
2. **Handle Failures**: Failed transfers set status to FAILED for investigation
3. **Audit Everything**: All goal/transfer actions are logged
4. **Validate Thoroughly**: Check wallet ownership, balances, dates
5. **Consider Timezones**: Execution times are server-local

## Testing

### Test Scenarios

**1. Create and Achieve Goal:**
```bash
# Create goal
# Create recurring transfer
# Wait for executions or manually contribute
# Verify goal marked as ACHIEVED when target reached
```

**2. Pause and Resume:**
```bash
# Create recurring transfer
# Pause it
# Verify no executions occur
# Resume it
# Verify executions continue
```

**3. Insufficient Balance:**
```bash
# Create recurring transfer
# Reduce source wallet balance
# Verify transfer fails
# Check status changed to FAILED
```

## Monitoring & Logs

### What to Monitor

- Recurring transfer execution success rate
- Failed transfers (investigate insufficient funds)
- Goals achievement rate
- Average time to achieve goals

### Log Examples

```
INFO: Executing recurring transfer: 123
INFO: Recurring transfer executed successfully: 123
INFO: Contribution successful. New balance: 5000.00
INFO: Goal achieved: Emergency Fund
ERROR: Failed to execute recurring transfer: 123 - Insufficient balance
```

## Future Enhancements

- Smart goal suggestions based on spending patterns
- Goal templates (Emergency Fund, Vacation, etc.)
- Goal sharing/family goals
- Gamification (badges, milestones)
- Visual progress charts
- Email/SMS notifications on achievement
- Auto-adjust recurring amount based on goal progress
- Goal categories and analytics

---

**Happy Saving! 💰**