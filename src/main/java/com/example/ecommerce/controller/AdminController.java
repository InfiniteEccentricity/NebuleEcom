package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final com.example.ecommerce.repository.ProductRepository productRepository;

    public AdminController(ProductService productService, CategoryService categoryService,
                           com.example.ecommerce.repository.ProductRepository productRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        // Use SQL Stored Procedure for Stats (Showcase Feature)
        List<Object[]> statsResult = productRepository.getDatabaseStats();
        if (!statsResult.isEmpty()) {
            Object[] stats = statsResult.get(0);
            model.addAttribute("totalProducts", stats[0]);
            model.addAttribute("totalUsers", stats[1]);
            model.addAttribute("totalCategories", stats[2]);
        }
        
        // Product List for summary
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        
        // Fetch Audit Logs from SQL Trigger (Showcase Feature)
        model.addAttribute("auditLogs", productRepository.getAuditLogs());
        
        return "admin/dashboard";
    }

    @PostMapping("/products/bulk-discount")
    public String applyDiscount(@RequestParam String category, @RequestParam Double discount) {
        // Use SQL Stored Procedure with CURSOR (Showcase Feature)
        productRepository.applyBulkDiscount(category, discount);
        return "redirect:/admin";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        productService.getProductById(id).ifPresent(p -> model.addAttribute("product", p));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, @RequestParam Long categoryId) {
        productService.saveProduct(product, categoryId);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }
}
