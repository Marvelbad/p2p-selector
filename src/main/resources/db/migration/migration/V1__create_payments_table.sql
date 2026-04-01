CREATE TABLE payments
(
    id           BIGSERIAL PRIMARY KEY,
    invoice_id   VARCHAR(255) NOT NULL UNIQUE,
    merchant_id  VARCHAR(255) NOT NULL,
    endpoint_id  VARCHAR(255) NOT NULL,
    order_number VARCHAR(255) NOT NULL,
    customer     VARCHAR(255) NOT NULL,
    payment_type VARCHAR(50)  NOT NULL,
    amount       NUMERIC(18, 4) NOT NULL,
    currency     VARCHAR(10)  NOT NULL,
    status       VARCHAR(50)  NOT NULL,
    error_code          VARCHAR(255),
    error_message       TEXT,
    sale_response_json  TEXT,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP
);