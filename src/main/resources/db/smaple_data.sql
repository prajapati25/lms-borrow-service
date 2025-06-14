-- Insert sample borrows
INSERT INTO borrows (user_id, book_id, borrow_date, due_date, status,created_at,updated_at) VALUES
(1, 1, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '9 days', 'BORROWED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(1, 2, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 'OVERDUE',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(2, 1, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '1 day', 'RETURNED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(2, 3, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '12 days', 'BORROWED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(3, 2, CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP + INTERVAL '7 days', 'BORROWED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- Insert sample returns
INSERT INTO returns (borrow_id, return_date, fine_amount,created_at) VALUES
(3, CURRENT_TIMESTAMP - INTERVAL '1 day', 0.00,CURRENT_TIMESTAMP);

-- Insert sample fines
INSERT INTO fines (borrow_id, amount, status,created_at,updated_at) VALUES
(2, 30.00, 'PENDING',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
(3, 0.00, 'PAID',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- Insert sample extensions
INSERT INTO borrow_extensions (borrow_id, extended_days, new_due_date,created_at) VALUES
(2, 7, CURRENT_TIMESTAMP + INTERVAL '16 days',CURRENT_TIMESTAMP),
(3, 7, CURRENT_TIMESTAMP + INTERVAL '19 days',CURRENT_TIMESTAMP);