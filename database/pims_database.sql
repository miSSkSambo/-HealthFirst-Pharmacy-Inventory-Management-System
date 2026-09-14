-- ============================================================================
-- HealthFirst Pharmacy Inventory Management System (PIMS)
-- Programming 732 assignment database script
-- Run as MySQL root/administrator: mysql -u root -p < pims_database.sql
-- ============================================================================

DROP DATABASE IF EXISTS healthfirst_pims;
CREATE DATABASE healthfirst_pims
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE healthfirst_pims;

-- A restricted account used by the Java desktop application.
DROP USER IF EXISTS 'pims_user'@'localhost';
CREATE USER 'pims_user'@'localhost' IDENTIFIED BY 'pims_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON healthfirst_pims.* TO 'pims_user'@'localhost';
FLUSH PRIVILEGES;

-- Table 1: Login credentials and roles. Passwords are SHA-256 hashes.
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL,
    full_name VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- Table 2: Medicine supplier information.
CREATE TABLE suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address TEXT NOT NULL
) ENGINE=InnoDB;

-- Table 3: Core medicine inventory.
CREATE TABLE medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(100) NOT NULL,
    medicine_type VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    quantity_in_stock INT NOT NULL CHECK (quantity_in_stock >= 0),
    reorder_level INT NOT NULL CHECK (reorder_level >= 0),
    expiry_date DATE NOT NULL,
    supplier_id INT NOT NULL,
    CONSTRAINT fk_medicines_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_medicines_name (name),
    INDEX idx_medicines_expiry (expiry_date)
) ENGINE=InnoDB;

-- Table 4: Sale header / transaction details.
CREATE TABLE sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    user_id INT NOT NULL,
    CONSTRAINT fk_sales_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_sales_date (sale_date)
) ENGINE=InnoDB;

-- Table 5: Normalised sale line items.
CREATE TABLE sale_items (
    sale_item_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_sold INT NOT NULL CHECK (quantity_sold > 0),
    price_at_sale DECIMAL(10,2) NOT NULL CHECK (price_at_sale >= 0),
    CONSTRAINT fk_sale_items_sale
        FOREIGN KEY (sale_id) REFERENCES sales(sale_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE KEY uq_sale_medicine (sale_id, medicine_id)
) ENGINE=InnoDB;

-- Seed users. Default password details are documented in README.md.
INSERT INTO users (username, password, role, full_name) VALUES
('admin',   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin',   'Amina Dlamini'),
('cashier', 'c246650737293ddc18fc357393db78d1ecc9d1fd1af95469115e4a29f983359a', 'Cashier', 'Thabo Nkosi'),
('manager', '866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5', 'Admin',   'Lerato Molefe');

-- Seed suppliers.
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('MediSupply Distributors', 'Nandi Mokoena', '011 555 0101', 'orders@medisupply.co.za', '18 Market Street, Johannesburg, 2001'),
('Wellness Pharma SA', 'Peter Jacobs', '021 555 0182', 'sales@wellnesspharma.co.za', '45 Harbour Road, Cape Town, 8001'),
('CarePlus Medical', 'Sibongile Khumalo', '031 555 0277', 'support@careplus.co.za', '7 Kingsway Avenue, Durban, 4001');

-- Seed medicine catalogue. Two products deliberately trigger low-stock/expiry reports.
INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Panado 500mg', 'Adcock Ingram', 'Tablet',    39.99, 48, 20, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), 1),
('Disprin', 'Aspirin SA', 'Tablet',            29.50, 32, 15, DATE_ADD(CURDATE(), INTERVAL 10 MONTH), 2),
('Benylin Cough Syrup', 'Johnson & Johnson', 'Syrup', 74.99, 7, 10, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1),
('Allergex 4mg', 'Pharmaplan', 'Tablet',      58.75, 25, 12, DATE_ADD(CURDATE(), INTERVAL 14 MONTH), 2),
('Betadine Antiseptic', 'Mundipharma', 'Cream', 68.90, 8, 8, DATE_ADD(CURDATE(), INTERVAL 27 DAY), 3),
('Vitamin C 1000mg', 'Biogen', 'Capsule',    119.99, 60, 25, DATE_ADD(CURDATE(), INTERVAL 22 MONTH), 3),
('Brufen 400mg', 'Abbott', 'Tablet',          64.50, 19, 15, DATE_ADD(CURDATE(), INTERVAL 16 MONTH), 1),
('Efferflu C', 'Cipla', 'Tablet',             89.95, 5, 12, DATE_ADD(CURDATE(), INTERVAL 9 MONTH), 2);

-- Sample sales make the sales and item-wise report meaningful immediately after installation.
INSERT INTO sales (sale_date, total_amount, user_id) VALUES
(DATE_SUB(NOW(), INTERVAL 5 DAY), 119.97, 2),
(DATE_SUB(NOW(), INTERVAL 2 DAY), 163.24, 2),
(DATE_SUB(NOW(), INTERVAL 1 DAY), 129.00, 2);

INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES
(1, 1, 3, 39.99),
(2, 2, 1, 29.50),
(2, 3, 1, 74.99),
(2, 4, 1, 58.75),
(3, 7, 2, 64.50);

-- Quick verification statements (optional after import):
-- SELECT * FROM users;
-- SELECT m.name, m.quantity_in_stock, s.name AS supplier FROM medicines m JOIN suppliers s ON s.supplier_id=m.supplier_id;
-- SELECT * FROM sales;
