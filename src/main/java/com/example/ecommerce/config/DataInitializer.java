package com.example.ecommerce.config;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(ProductRepository productRepository, 
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed Admin if not exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(new User(null, "admin", passwordEncoder.encode("admin123"), "admin@nebula.com", "ROLE_ADMIN"));
                System.out.println("Default admin created: admin/admin123");
            }

            if (productRepository.count() == 0) {
                productRepository.saveAll(Arrays.asList(
                    new Product(null, "Nebula Pro Smartphone", 74699.00, "Electronics", "/images/phone.png", 4.8, 
                        "Experience the next generation of mobile technology with the Nebula Pro. Features include a stunning Super Retina Display, advanced triple-camera system, and the lightning-fast A15 Bionic chip. Designed for those who demand perfection."),
                    new Product(null, "Quantum Laptop 15\"", 107817.00, "Electronics", "/images/laptop.png", 4.9, 
                        "The Quantum Laptop 15 redefined portability and power. With its sleek aerospace-grade aluminum chassis, 120Hz Liquid Motion display, and up to 18 hours of battery life, it's the ultimate tool for creators and professionals."),
                    new Product(null, "Sonic Wireless Headphones", 12409.00, "Electronics", "/images/headphones.png", 4.7, 
                        "Immerse yourself in pure sound with Sonic Wireless. Featuring industry-leading active noise cancellation, studio-quality audio, and plush memory foam cushions for all-day comfort. Your music has never sounded this good."),
                    new Product(null, "Elevate Series Watch", 24817.00, "Wearables", "/images/watch.png", 4.5, 
                        "Stay connected and track your health like never before with the Elevate Series Watch. Monitors heart rate, blood oxygen, and sleep patterns. With its timeless design and variety of straps, it fits any occasion."),
                    new Product(null, "Aura Smart Lamp", 6639.00, "Home Decor", "/images/lamp.png", 4.6, 
                        "Transform your space with the Aura Smart Lamp. Offering over 16 million colors and adjustable brightness, it creates the perfect ambiance for any mood. Control it effortlessly with your voice or smartphone."),
                    new Product(null, "Titan Gaming Mouse", 4979.00, "Accessories", "/images/mouse.png", 4.4, 
                        "Dominate the competition with the Titan Gaming Mouse. Boasting a 25K DPI optical sensor, customizable RGB lighting, and 11 programmable buttons. Engineered for speed, accuracy, and endurance.")
                ));
                System.out.println("Database seeded with initial products.");
            }
        };
    }
}
