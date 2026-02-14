# Digital Wallet Transfer System

A reactive digital wallet transfer system built with Spring Boot WebFlux, PostgreSQL (R2DBC), and Kafka for event-driven architecture.

## Features

- **Reactive Architecture**: Built with Spring WebFlux and Project Reactor for non-blocking, asynchronous processing
- **JWT Authentication**: Secure token-based authentication
- **Wallet Management**: Create and manage digital wallets with balance tracking
- **Transfer Processing**: Initiate and process money transfers with validation
- **Savings Automation**: Set savings goals and create recurring transfers
   - Goal tracking with progress percentage
   - Recurring transfers (daily, weekly, monthly, etc.)
   - Automatic goal contribution tracking
   - Suggested monthly contribution calculation
- **Event-Driven Design**: Kafka integration for publishing transfer events
- **Double-Entry Ledger**: Maintain accurate financial records with ledger entries
- **Daily Reconciliation**: Automated reconciliation to detect discrepancies
- **Audit Logging**: Comprehensive audit trail for compliance
- **Validation**: Balance checks, currency matching, wallet status verification
- **Daily Limits**: Configurable daily transfer limits per wallet
- **Scheduled Jobs**: Automatic execution of recurring transfers

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring WebFlux** (Reactive Web)
- **Spring Data R2DBC** (Reactive Database Access)
- **PostgreSQL** (Database)
- **Kafka** (Event Streaming)
- **JWT** (Authentication)
- **Lombok** (Code Generation)

## Project Structure

```
digital-wallet/
├── src/main/java/com/digitalwallet/
│   ├── controller/         # REST API Controllers
│   ├── service/            # Business Logic Services
│   ├── repository/         # R2DBC Repositories
│   ├── model/              # Domain Entities
│   ├── dto/                # Data Transfer Objects
│   ├── config/             # Configuration Classes
│   ├── security/           # Security Components
│   ├── event/              # Event Models
│   └── exception/          # Exception Handlers
└── src/main/resources/
    ├── application.yml     # Application Configuration
    └── schema.sql          # Database Schema
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+

## Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE wallet_db;
```

2. Run the schema.sql script to create tables and sample data

## Configuration

Update `application.yml` with your database and Kafka settings:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/wallet_db
    username: postgres
    password: postgres
  
  kafka:
    bootstrap-servers: localhost:9092

jwt:
  secret: yourSecretKeyForJWTTokenGenerationMustBeLongEnough
  expiration: 86400000
```

## Running the Application

### Using Maven

```bash
mvn clean install
mvn spring-boot:run
```

### Using Docker Compose

```bash
docker-compose up -d
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "password"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "userId": 1,
  "expiresIn": 86400000
}
```

### Wallet Operations

**Get Wallet Balance**
```http
GET /api/wallets/{walletNumber}/balance
Authorization: Bearer {token}
```

**Get User Wallets**
```http
GET /api/wallets/user/{userId}
Authorization: Bearer {token}
```

### Transfer Operations

**Initiate Transfer**
```http
POST /api/transfers/initiate
Authorization: Bearer {token}
Content-Type: application/json

{
  "sourceWalletNumber": "WLT-1001",
  "destinationWalletNumber": "WLT-1002",
  "amount": 100.00,
  "currency": "USD",
  "description": "Payment for services"
}
```

Response:
```json
{
   "transferId": 1,
   "transferReference": "TXN-A1B2C3D4",
   "amount": 100.00,
   "currency": "USD",
   "status": "COMPLETED",
   "initiatedAt": "2024-01-15T10:30:00",
   "completedAt": "2024-01-15T10:30:05",
   "message": "Transfer completed"
}
```

**Check Transfer Status**
```http
GET /api/transfers/{transferReference}/status
Authorization: Bearer {token}
```

### Reconciliation

**Run Daily Reconciliation**
```http
POST /api/reconciliation/run
Authorization: Bearer {token}
```

**Reconcile Single Wallet**
```http
GET /api/reconciliation/wallet/{walletNumber}
Authorization: Bearer {token}
```

### Savings Goals

**Create Savings Goal**
```http
POST /api/savings-goals
Authorization: Bearer {token}
Content-Type: application/json

{
  "goalName": "Emergency Fund",
  "savingsWalletNumber": "WLT-1002",
  "targetAmount": 10000.00,
  "currency": "USD",
  "targetDate": "2025-12-31"
}
```

**Get User Goals**
```http
GET /api/savings-goals/user
GET /api/savings-goals/user/active
Authorization: Bearer {token}
```

**Contribute to Goal**
```http
POST /api/savings-goals/{goalId}/contribute?amount=500.00
Authorization: Bearer {token}
```

### Recurring Transfers

**Create Recurring Transfer**
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
  "startDate": "2025-03-01"
}
```

**Manage Recurring Transfers**
```http
GET /api/recurring-transfers/user/active
PUT /api/recurring-transfers/{recurringId}/pause
PUT /api/recurring-transfers/{recurringId}/resume
DELETE /api/recurring-transfers/{recurringId}
Authorization: Bearer {token}
```

## Sample Test Data

The schema includes sample users and wallets:

**User 1:**
- Username: `john.doe`
- Password: `password` (BCrypt hashed)
- Wallet: `WLT-1001` with balance $10,000

**User 2:**
- Username: `jane.smith`
- Password: `password` (BCrypt hashed)
- Wallet: `WLT-1002` with balance $5,000

## Transfer Workflow

1. **Initiation**: User initiates transfer via API
2. **Validation**: System validates:
   - Sufficient balance
   - Currency match
   - Wallet status (both must be ACTIVE)
   - Transfer limits (min, max, daily)
3. **Processing**:
   - Debit source wallet
   - Credit destination wallet
   - Create ledger entries (double-entry bookkeeping)
4. **Event Publishing**: Publish transfer events to Kafka
5. **Audit Logging**: Log all actions for compliance

## Event Types

The system publishes the following events to Kafka:
- `TRANSFER_INITIATED`
- `TRANSFER_VALIDATED`
- `TRANSFER_PROCESSING`
- `TRANSFER_COMPLETED`
- `TRANSFER_FAILED`
- `WALLET_DEBITED`
- `WALLET_CREDITED`

## Error Handling

The application includes comprehensive error handling:
- `InsufficientBalanceException`: Thrown when wallet has insufficient funds
- `WalletNotFoundException`: Thrown when wallet doesn't exist
- `GlobalExceptionHandler`: Centralized exception handling for REST APIs

## Security

- JWT-based authentication for all protected endpoints
- BCrypt password encoding
- Token validation on each request
- Role-based access control ready

## Monitoring & Audit

- All wallet operations are logged in `audit_logs` table
- Transfer events are published to Kafka for monitoring
- Reconciliation service detects discrepancies between wallet and ledger balances

## Testing

Sample curl commands:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john.doe","password":"password"}'

# Get Balance
curl -X GET http://localhost:8080/api/wallets/WLT-1001/balance \
  -H "Authorization: Bearer {token}"

# Initiate Transfer
curl -X POST http://localhost:8080/api/transfers/initiate \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceWalletNumber":"WLT-1001",
    "destinationWalletNumber":"WLT-1002",
    "amount":50.00,
    "currency":"USD",
    "description":"Test transfer"
  }'
```

## Future Enhancements

- Multi-currency support with exchange rates
- Transaction history with pagination
- Push notifications
- Admin dashboard
- Rate limiting
- Idempotency keys for transfer requests
- Goal templates and categories
- Family/shared savings goals
- Gamification with badges and milestones

## License

MIT License

## Contact

For questions or support, please open an issue in the repository
or reach me at: norbertokoth01@gmail.com