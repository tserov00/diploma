ALTER TABLE savings_accounts
    ADD COLUMN IF NOT EXISTS reserved_amount NUMERIC(19, 5) NOT NULL DEFAULT 0;

ALTER TABLE currencies ALTER COLUMN id TYPE BIGINT;
ALTER TABLE transaction_types ALTER COLUMN id TYPE BIGINT;
ALTER TABLE savings_accounts ALTER COLUMN id TYPE BIGINT;
ALTER TABLE savings_accounts ALTER COLUMN customer_account_id TYPE BIGINT;
ALTER TABLE savings_accounts ALTER COLUMN currency_id TYPE BIGINT;
ALTER TABLE balance_history ALTER COLUMN savings_account_id TYPE BIGINT;
ALTER TABLE balance_history ALTER COLUMN transaction_type TYPE BIGINT;

-- Old standing demo orders reserved half of every account. The obsolete reservations must be released.
UPDATE savings_accounts SET reserved_amount = 0 WHERE reserved_amount <> 0;

DROP TABLE IF EXISTS processed_operations CASCADE;
CREATE TABLE processed_operations (
    operation_id UUID PRIMARY KEY
);

INSERT INTO transaction_types (id, transaction_type)
SELECT 5, 'BUY'
WHERE NOT EXISTS (SELECT 1 FROM transaction_types WHERE transaction_type = 'BUY');

INSERT INTO transaction_types (id, transaction_type)
SELECT 6, 'SELL'
WHERE NOT EXISTS (SELECT 1 FROM transaction_types WHERE transaction_type = 'SELL');
