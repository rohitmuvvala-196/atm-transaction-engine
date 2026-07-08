-- ATM Transaction Engine Database Schema
-- MySQL Database

CREATE DATABASE IF NOT EXISTS atm_engine;
USE atm_engine;

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    INDEX idx_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User-Role mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    pin VARCHAR(4) NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    account_holder_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) DEFAULT 'SAVINGS',
    is_locked BOOLEAN DEFAULT FALSE,
    lock_time DATETIME,
    failed_attempts INT DEFAULT 0,
    daily_withdrawal_total DECIMAL(15,2) DEFAULT 0.00,
    last_withdrawal_date DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_account_number (account_number),
    INDEX idx_user_id (user_id),
    INDEX idx_is_active (is_active),
    INDEX idx_is_locked (is_locked),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cards table
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_number VARCHAR(20) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    card_type VARCHAR(20) DEFAULT 'DEBIT',
    expiry_date DATETIME NOT NULL,
    cvv VARCHAR(4),
    is_active BOOLEAN DEFAULT TRUE,
    is_inserted BOOLEAN DEFAULT FALSE,
    inserted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_card_number (card_number),
    INDEX idx_account_id (account_id),
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_before DECIMAL(15,2),
    balance_after DECIMAL(15,2),
    target_account VARCHAR(50),
    description VARCHAR(200),
    is_successful BOOLEAN DEFAULT FALSE,
    failure_reason VARCHAR(500),
    is_rolled_back BOOLEAN DEFAULT FALSE,
    rollback_reason VARCHAR(500),
    transaction_date DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_account_id (account_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_is_successful (is_successful),
    INDEX idx_is_rolled_back (is_rolled_back),
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    account_number VARCHAR(20),
    username VARCHAR(100),
    action_detail VARCHAR(500),
    ip_address VARCHAR(500),
    user_agent VARCHAR(500),
    is_successful BOOLEAN DEFAULT FALSE,
    failure_reason VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_type (event_type),
    INDEX idx_account_number (account_number),
    INDEX idx_created_at (created_at),
    INDEX idx_is_successful (is_successful)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ATM logs table
CREATE TABLE IF NOT EXISTS atm_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    component VARCHAR(50),
    message VARCHAR(500),
    is_error BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_type (event_type),
    INDEX idx_component (component),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;