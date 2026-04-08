INSERT INTO users (email, password_hash, role, status)
VALUES ('admin@example.com', '$2a$10$UZ1y9MfJUrcXfx9O2047oOg7JDEoEGiMP2eMJiSRzOrWOwvLvBJVa', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role = VALUES(role),
    status = VALUES(status);
