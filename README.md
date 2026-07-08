# ATM Transaction Engine with Robust Exception Handling

A production-grade full-stack ATM application built with Java Spring Boot and React, featuring strong transactional consistency, custom hardware simulation, and secure authentication.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (React + Vite)                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │  Login   │ │Dashboard │ │Withdraw  │ │Deposit   │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ Transfer │ │Statement │ │Change PIN│ │  Error   │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP REST API (Axios)
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  Backend (Spring Boot 3)                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │Controller│ │  Service │ │Repository│ │  Entity  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ Security │ │Hardware  │ │Exception │ │   DTO   │       │
│  │   JWT    │ │Simulation│ │ Handling │ │         │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└──────────────────────┬──────────────────────────────────────┘
                       │ JPA / Hibernate
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                     MySQL Database                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ Accounts │ │Transact. │ │  Users   │ │Audit Logs│       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────┘
```

## Tech Stack

### Backend
- **Java 21** - Latest LTS with pattern matching, records, sealed classes
- **Spring Boot 3** - Auto-configuration, embedded server
- **Spring Web** - RESTful APIs
- **Spring Data JPA** - Database access
- **Spring Security** - Authentication & authorization
- **Hibernate** - ORM with JPA
- **MySQL** - Relational database
- **Maven** - Build & dependency management
- **Lombok** - Boilerplate code reduction
- **JUnit 5 + Mockito** - Unit & integration testing
- **Swagger OpenAPI** - API documentation
- **MapStruct** - Object mapping
- **SLF4J + Logback** - Logging

### Frontend
- **React 18** - UI library
- **Vite** - Build tool
- **Material UI 6** - Component library
- **Axios** - HTTP client
- **React Router 6** - Routing
- **React Hook Form** - Form management
- **Recharts** - Charts & graphs
- **React Toastify** - Notifications

## Folder Structure

```
atm-transaction-engine/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/atmengine/
│       │   │   ├── config/          # Security, OpenAPI, DataInitializer
│       │   │   ├── constants/       # App constants, enums
│       │   │   ├── controller/      # REST controllers
│       │   │   ├── dto/             # Data transfer objects
│       │   │   ├── entity/          # JPA entities
│       │   │   ├── exception/       # Custom exceptions + global handler
│       │   │   ├── hardware/        # ATM hardware simulation
│       │   │   ├── mapper/          # MapStruct mappers
│       │   │   ├── repository/      # Spring Data repositories
│       │   │   ├── response/        # API response wrapper
│       │   │   ├── security/        # JWT + security components
│       │   │   ├── service/         # Business logic
│       │   │   ├── util/            # Utilities
│       │   │   ├── validation/      # Custom validators
│       │   │   └── AtmEngineApplication.java
│       │   └── resources/
│       │       ├── application.yml  # Config
│       │       └── schema.sql       # Database schema
│       └── test/java/com/atmengine/
│           ├── controller/          # Controller tests
│           ├── service/             # Service tests
│           └── repository/          # Repository tests
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── components/             # Reusable components
│       │   └── Layout.jsx          # App layout with drawer
│       ├── context/                # React contexts
│       │   └── AuthContext.jsx     # Authentication state
│       ├── pages/                  # Page components
│       │   ├── LoginPage.jsx
│       │   ├── DashboardPage.jsx
│       │   ├── WithdrawPage.jsx
│       │   ├── DepositPage.jsx
│       │   ├── TransferPage.jsx
│       │   ├── StatementPage.jsx
│       │   ├── ChangePinPage.jsx
│       │   └── ErrorPage.jsx
│       ├── services/               # API services
│       │   └── api.js
│       ├── utils/                  # Utilities
│       ├── App.jsx                 # Main app with routing
│       └── main.jsx                # Entry point
└── README.md
```

## Database Schema

```sql
-- 6 tables: roles, users, user_roles, accounts, cards, transactions, audit_logs, atm_logs

-- accounts: Core account data with PIN, balance, lock status
-- transactions: Full audit trail of all transactions
-- users: Application users with roles
-- roles: Role-based access control
-- cards: Card information linked to accounts
-- audit_logs: Security audit trail
```

## API Documentation

### REST Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/atm/login` | User login with account + PIN | No |
| POST | `/api/v1/atm/withdraw` | Cash withdrawal | Yes |
| POST | `/api/v1/atm/deposit` | Cash deposit | Yes |
| POST | `/api/v1/atm/transfer` | Fund transfer | Yes |
| GET | `/api/v1/atm/balance` | Check balance | Yes |
| GET | `/api/v1/atm/statement` | Mini statement (last 20) | Yes |
| PUT | `/api/v1/atm/change-pin` | Change PIN | Yes |
| POST | `/api/v1/atm/logout` | Logout + eject card | Yes |
| GET | `/api/v1/atm/status` | ATM hardware status | No |

### Swagger UI
Access API documentation at: `http://localhost:8080/swagger-ui.html`

## Features

### Core Features
- ✅ Card insertion & PIN authentication
- ✅ Maximum 3 PIN attempts with account locking
- ✅ Cash withdrawal with hardware simulation
- ✅ Cash deposit
- ✅ Fund transfer between accounts
- ✅ Balance inquiry
- ✅ Mini statement (last 20 transactions)
- ✅ Secure PIN change
- ✅ Card ejection on logout

### Transaction Safety
- ✅ Never debit before successful cash dispense
- ✅ Automatic rollback on dispense failure
- ✅ Balance restoration with audit log
- ✅ Spring @Transactional management
- ✅ Pessimistic locking for concurrent access

### Hardware Simulation
- ✅ CardReader - 2% random failure rate
- ✅ CashDispenser - 5% random failure rate
- ✅ ReceiptPrinter - 3% random failure rate
- ✅ ATM connectivity - 1% random failure
- ✅ Configurable failure probabilities

### Custom Exceptions
- ✅ InvalidPinException - Wrong PIN attempts
- ✅ AccountLockedException - Account after 3 failures
- ✅ InsufficientFundsException - Low balance
- ✅ CashDispenseException - Hardware cash issue
- ✅ HardwareFailureException - Component failure
- ✅ TransactionRollbackException - Automatic rollback
- ✅ ATMOfflineException - Connectivity issues
- ✅ InvalidAmountException - Amount validation
- ✅ DailyLimitExceededException - Daily cap exceeded

### Security
- ✅ BCrypt password encryption
- ✅ Session-based authentication
- ✅ Protected routes (frontend)
- ✅ CORS configuration
- ✅ Input validation

### Frontend UI
- ✅ Dark mode with banking colors
- ✅ Responsive design
- ✅ Animated cards
- ✅ Transaction charts (Recharts)
- ✅ Loading indicators
- ✅ Toast notifications
- ✅ Error states
- ✅ Material UI components

## Installation

### Prerequisites
- Java 21+
- Maven 3.8+
- Node.js 18+
- MySQL 8+

### Backend Setup

1. **Create MySQL Database**
```sql
CREATE DATABASE IF NOT EXISTS atm_engine;
```

2. **Configure Database**
Edit `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/atm_engine
    username: root
    password: your_password
```

3. **Build & Run Backend**
```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```
Backend runs at `http://localhost:8080`

### Frontend Setup

1. **Install Dependencies**
```bash
cd frontend
npm install
```

2. **Run Development Server**
```bash
npm run dev
```
Frontend runs at `http://localhost:3000`

### Test Credentials

| Account Number | PIN  | Balance   | Holder      |
|---------------|------|-----------|-------------|
| ACC1234567890 | 1234 | ₹50,000   | John Doe    |
| ACC0987654321 | 5678 | ₹1,00,000 | Jane Smith  |
| ACC1122334455 | 4321 | ₹25,000   | Bob Johnson |

## Testing

```bash
# Run all tests
cd backend
mvn test

# Specific test class
mvn test -Dtest=ATMServiceTest
```

### Test Coverage
- ✅ Service layer unit tests
- ✅ Mockito mocks for dependencies
- ✅ Exception handling tests
- ✅ Business logic verification
- ✅ Account locking behavior
- ✅ Transaction rollback scenarios

## Exception Handling

Global exception handler returns consistent JSON:
```json
{
  "success": false,
  "message": "Insufficient funds in account",
  "status": 400,
  "timestamp": "2024-01-15T10:30:00"
}
```

## Future Enhancements

- [ ] JWT token-based authentication
- [ ] Email/SMS notifications
- [ ] PDF receipt download
- [ ] Excel statement export
- [ ] Admin dashboard with ATM cash monitoring
- [ ] Live transaction history
- [ ] Daily limit configuration
- [ ] Transaction search & filtering
- [ ] Multi-language support
- [ ] Rate limiting
- [ ] API versioning
- [ ] Docker containerization
- [ ] CI/CD pipeline integration
- [ ] Performance monitoring (Micrometer)

## License
MIT License - See LICENSE file for details