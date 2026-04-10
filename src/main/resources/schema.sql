-- Drop existing objects to ensure a fresh start
DROP TRIGGER IF EXISTS log_price_change;;
DROP PROCEDURE IF EXISTS ProcessBulkDiscount;;
DROP PROCEDURE IF EXISTS CalculateAdminStats;;
DROP TABLE IF EXISTS audit_logs;;
DROP TABLE IF EXISTS order_items;;
DROP TABLE IF EXISTS orders;;
DROP TABLE IF EXISTS cart_items;;
DROP TABLE IF EXISTS carts;;
DROP TABLE IF EXISTS users;;
DROP TABLE IF EXISTS products;;
DROP TABLE IF EXISTS categories;;

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    category_id BIGINT,
    image_url VARCHAR(255),
    rating DECIMAL(3,2),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    role VARCHAR(50) DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);;

CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    session_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);;

CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT,
    product_id BIGINT,
    quantity INT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);;

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT,
    session_id VARCHAR(255),
    customer_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    address_line VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_order_id VARCHAR(255),
    payment_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);;

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    image_url VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);;

CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    old_price DECIMAL(10,2),
    new_price DECIMAL(10,2),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);;

CREATE TRIGGER log_price_change
AFTER UPDATE ON products
FOR EACH ROW
BEGIN
    IF OLD.price <> NEW.price THEN
        INSERT INTO audit_logs (product_id, old_price, new_price)
        VALUES (OLD.id, OLD.price, NEW.price);
    END IF;
END;;

CREATE PROCEDURE ProcessBulkDiscount(IN category_name VARCHAR(255), IN discount_percent DECIMAL(5,2))
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE p_id BIGINT;
    
    -- CURSOR definition
    DECLARE product_cursor CURSOR FOR 
        SELECT p.id
        FROM products p
        JOIN categories c ON p.category_id = c.id
        WHERE c.name = category_name;
    
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

CREATE PROCEDURE CalculateAdminStats()
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM products) AS total_products,
        (SELECT COUNT(*) FROM users WHERE role = 'ROLE_USER') AS total_users,
        (SELECT COUNT(*) FROM categories) AS active_categories;
END;;
