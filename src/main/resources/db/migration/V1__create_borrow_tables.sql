-- Create borrows table
CREATE TABLE borrows (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT borrow_status_check CHECK (status IN ('BORROWED', 'RETURNED', 'OVERDUE'))
);

-- Create returns table
CREATE TABLE returns (
    id BIGSERIAL PRIMARY KEY,
    borrow_id BIGINT NOT NULL,
    return_date TIMESTAMP NOT NULL,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_returns_borrow FOREIGN KEY (borrow_id) REFERENCES borrows(id)
);

-- Create fines table
CREATE TABLE fines (
    id BIGSERIAL PRIMARY KEY,
    borrow_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fines_borrow FOREIGN KEY (borrow_id) REFERENCES borrows(id),
    CONSTRAINT fine_status_check CHECK (status IN ('PENDING', 'PAID', 'WAIVED'))
);

-- Create borrow_extensions table
CREATE TABLE borrow_extensions (
    id BIGSERIAL PRIMARY KEY,
    borrow_id BIGINT NOT NULL,
    extended_days INTEGER NOT NULL,
    new_due_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_extensions_borrow FOREIGN KEY (borrow_id) REFERENCES borrows(id)
);

-- Create indexes
CREATE INDEX idx_borrows_user_id ON borrows(user_id);
CREATE INDEX idx_borrows_book_id ON borrows(book_id);
CREATE INDEX idx_borrows_status ON borrows(status);
CREATE INDEX idx_borrows_due_date ON borrows(due_date);
CREATE INDEX idx_fines_status ON fines(status); 