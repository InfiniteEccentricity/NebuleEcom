INSERT INTO categories (name) VALUES
('Electronics'),
('Wearables'),
('Home Decor'),
('Accessories'),
('Fashion');;

INSERT INTO products (name, price, category_id, image_url, rating, description) VALUES
('Nebula Pro Smartphone', 74699.00, 1, '/images/phone.png', 4.8, 'Experience the next generation of mobile technology with the Nebula Pro. Features include a stunning Super Retina Display, advanced triple-camera system, and the lightning-fast A15 Bionic chip. Designed for those who demand perfection.'),
('Quantum Laptop 15"', 107817.00, 1, '/images/laptop.png', 4.9, 'The Quantum Laptop 15 redefined portability and power. With its sleek aerospace-grade aluminum chassis, 120Hz Liquid Motion display, and up to 18 hours of battery life, it''s the ultimate tool for creators and professionals.'),
('Sonic Wireless Headphones', 12409.00, 1, '/images/headphones.png', 4.7, 'Immerse yourself in pure sound with Sonic Wireless. Featuring industry-leading active noise cancellation, studio-quality audio, and plush memory foam cushions for all-day comfort. Your music has never sounded this good.'),
('Elevate Series Watch', 24817.00, 2, '/images/watch.png', 4.5, 'Stay connected and track your health like never before with the Elevate Series Watch. Monitors heart rate, blood oxygen, and sleep patterns. With its timeless design and variety of straps, it fits any occasion.'),
('Aura Smart Lamp', 6639.00, 3, '/images/lamp.png', 4.6, 'Transform your space with the Aura Smart Lamp. Offering over 16 million colors and adjustable brightness, it creates the perfect ambiance for any mood. Control it effortlessly with your voice or smartphone.'),
('Titan Gaming Mouse', 4979.00, 4, '/images/mouse.png', 4.4, 'Dominate the competition with the Titan Gaming Mouse. Boasting a 25K DPI optical sensor, customizable RGB lighting, and 11 programmable buttons. Engineered for speed, accuracy, and endurance.');;

-- Insert default admin account (Password: admin123)
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uqq31m', 'admin@nebula.com', 'ROLE_ADMIN');;
