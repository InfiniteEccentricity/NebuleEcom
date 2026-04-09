-- Drop existing objects to ensure a fresh start
DROP TRIGGER IF EXISTS log_price_change;;
DROP PROCEDURE IF EXISTS ProcessBulkDiscount;;
DROP PROCEDURE IF EXISTS CalculateAdminStats;;
DROP TABLE IF EXISTS audit_logs;;
DROP TABLE IF EXISTS cart_items;;
DROP TABLE IF EXISTS users;;
DROP TABLE IF EXISTS products;;

-- 1. Products Table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(255),
    image_url VARCHAR(255),
    rating DECIMAL(3, 2),
    description TEXT
);;

-- 2. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(50) DEFAULT 'ROLE_USER'
);;

-- 3. Cart Items Table
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    product_name VARCHAR(255),
    price DECIMAL(10, 2),
    quantity INT,
    image_url VARCHAR(255),
    username VARCHAR(255),
    session_id VARCHAR(255)
);;

-- 4. Audit Logs Table (Showcase Feature)
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    old_price DECIMAL(10, 2),
    new_price DECIMAL(10, 2),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);;

-- 5. TRIGGER: Log price changes automatically (Showcase Feature)
CREATE TRIGGER log_price_change
AFTER UPDATE ON products
FOR EACH ROW
BEGIN
    IF OLD.price <> NEW.price THEN
        INSERT INTO audit_logs (product_id, old_price, new_price)
        VALUES (OLD.id, OLD.price, NEW.price);
    END IF;
END;;

-- 6. PROCEDURE: Advanced cursor-based bulk discount (Showcase Feature)
CREATE PROCEDURE ProcessBulkDiscount(IN category_name VARCHAR(255), IN discount_percent DECIMAL(5,2))
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE p_id BIGINT;
    
    -- CURSOR definition
    DECLARE product_cursor CURSOR FOR 
        SELECT id FROM products WHERE category = category_name;
    
    -- HANDLER for cursor end
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN product_cursor;

    read_loop: LOOP
        FETCH product_cursor INTO p_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Business logic inside cursor
        UPDATE products 
        SET price = price * (1 - discount_percent/100) 
        WHERE id = p_id;
    END LOOP;

    CLOSE product_cursor;
END;;

-- 7. PROCEDURE: Stats Aggregation (Showcase Feature)
CREATE PROCEDURE CalculateAdminStats()
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM products) AS total_products,
        (SELECT COUNT(*) FROM users WHERE role = 'ROLE_USER') AS total_users,
        (SELECT COUNT(DISTINCT category) FROM products) AS active_categories;
END;;
