-- Database schema for Digital Wallet System

-- Users table
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Wallets table
CREATE TABLE IF NOT EXISTS wallets (
                                       id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT NOT NULL REFERENCES users(id),
    wallet_number VARCHAR(50) UNIQUE NOT NULL,
    balance DECIMAL(19, 4) DEFAULT 0.0000,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    wallet_type VARCHAR(20) DEFAULT 'STANDARD',
    daily_limit DECIMAL(19, 4) DEFAULT 50000.0000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT positive_balance CHECK (balance >= 0)
    );

-- Transfers table
CREATE TABLE IF NOT EXISTS transfers (
                                         id BIGSERIAL PRIMARY KEY,
                                         transfer_reference VARCHAR(100) UNIQUE NOT NULL,
    source_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    destination_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    transfer_type VARCHAR(20) DEFAULT 'P2P',
    description VARCHAR(500),
    initiated_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT positive_amount CHECK (amount > 0),
    CONSTRAINT different_wallets CHECK (source_wallet_id != destination_wallet_id)
    );

-- Ledger entries table (double-entry bookkeeping)
CREATE TABLE IF NOT EXISTS ledger_entries (
                                              id BIGSERIAL PRIMARY KEY,
                                              transfer_id BIGINT NOT NULL REFERENCES transfers(id),
    wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    entry_type VARCHAR(20) NOT NULL, -- DEBIT or CREDIT
    amount DECIMAL(19, 4) NOT NULL,
    balance_before DECIMAL(19, 4) NOT NULL,
    balance_after DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id BIGSERIAL PRIMARY KEY,
                                          entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by BIGINT REFERENCES users(id),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Savings goals table
CREATE TABLE IF NOT EXISTS savings_goals (
                                             id BIGSERIAL PRIMARY KEY,
                                             user_id BIGINT NOT NULL REFERENCES users(id),
    savings_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    goal_name VARCHAR(200) NOT NULL,
    description TEXT,
    target_amount DECIMAL(19, 4) NOT NULL,
    current_amount DECIMAL(19, 4) DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL,
    target_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    progress_percentage DECIMAL(5, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    achieved_at TIMESTAMP,
    CONSTRAINT positive_target_amount CHECK (target_amount > 0),
    CONSTRAINT positive_current_amount CHECK (current_amount >= 0)
    );

-- Recurring transfers table
CREATE TABLE IF NOT EXISTS recurring_transfers (
                                                   id BIGSERIAL PRIMARY KEY,
                                                   user_id BIGINT NOT NULL REFERENCES users(id),
    source_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    destination_wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    savings_goal_id BIGINT REFERENCES savings_goals(id),
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    day_of_week INTEGER,
    day_of_month INTEGER,
    execution_time TIME DEFAULT '09:00:00',
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE NOT NULL,
    last_executed_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    execution_count INTEGER DEFAULT 0,
    max_executions INTEGER,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT positive_recurring_amount CHECK (amount > 0),
    CONSTRAINT different_recurring_wallets CHECK (source_wallet_id != destination_wallet_id),
    CONSTRAINT valid_day_of_week CHECK (day_of_week IS NULL OR (day_of_week >= 1 AND day_of_week <= 7)),
    CONSTRAINT valid_day_of_month CHECK (day_of_month IS NULL OR (day_of_month >= 1 AND day_of_month <= 28))
    );

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallets_status ON wallets(status);
CREATE INDEX IF NOT EXISTS idx_transfers_source_wallet ON transfers(source_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transfers_destination_wallet ON transfers(destination_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transfers_status ON transfers(status);
CREATE INDEX IF NOT EXISTS idx_transfers_created_at ON transfers(created_at);
CREATE INDEX IF NOT EXISTS idx_ledger_transfer_id ON ledger_entries(transfer_id);
CREATE INDEX IF NOT EXISTS idx_ledger_wallet_id ON ledger_entries(wallet_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at);

-- Savings goals indexes
CREATE INDEX IF NOT EXISTS idx_savings_goals_user_id ON savings_goals(user_id);
CREATE INDEX IF NOT EXISTS idx_savings_goals_wallet_id ON savings_goals(savings_wallet_id);
CREATE INDEX IF NOT EXISTS idx_savings_goals_status ON savings_goals(status);
CREATE INDEX IF NOT EXISTS idx_savings_goals_target_date ON savings_goals(target_date);

-- Recurring transfers indexes
CREATE INDEX IF NOT EXISTS idx_recurring_transfers_user_id ON recurring_transfers(user_id);
CREATE INDEX IF NOT EXISTS idx_recurring_transfers_source_wallet ON recurring_transfers(source_wallet_id);
CREATE INDEX IF NOT EXISTS idx_recurring_transfers_destination_wallet ON recurring_transfers(destination_wallet_id);
CREATE INDEX IF NOT EXISTS idx_recurring_transfers_goal_id ON recurring_transfers(savings_goal_id);
CREATE INDEX IF NOT EXISTS idx_recurring_transfers_status ON recurring_transfers(status);
CREATE INDEX IF NOT EXISTS idx_recurring_transfers_next_execution ON recurring_transfers(next_execution_date);


-- Sample data
-- INSERT INTO users (username, password, email, first_name, last_name)
-- VALUES
--     ('john.doe', '$2a$10$62tI7UIYo7EYtPI8cUQ0u.0w7j8pY1jHNlmat4G.OAJYi/UPsf8r.', 'john.doe@example.com', 'John', 'Doe'),
--     ('jane.smith', '$2a$10$62tI7UIYo7EYtPI8cUQ0u.0w7j8pY1jHNlmat4G.OAJYi/UPsf8r.', 'jane.smith@example.com', 'Jane', 'Smith')
--     ON CONFLICT DO NOTHING;
--
-- INSERT INTO wallets (user_id, wallet_number, balance, currency)
-- VALUES
--     (1, 'WLT-1001', 10000.0000, 'USD'),
--     (2, 'WLT-1002', 5000.0000, 'USD')
--     ON CONFLICT DO NOTHING;