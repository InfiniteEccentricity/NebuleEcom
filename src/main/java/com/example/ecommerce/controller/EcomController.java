package com.example.ecommerce.controller;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class EcomController {

    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CategoryService categoryService;

    public EcomController(CartService cartService, UserService userService, ProductService productService,
                          OrderService orderService, CategoryService categoryService) {
        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.categoryService = categoryService;
    }

    private String getIdentity(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return session.getId();
    }

    private boolean isUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser");
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String query, 
                       @RequestParam(required = false) String sort,
                       Model model, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();
        
        // Merge cart if user just logged in
        if (isUser) {
            cartService.mergeCart(session.getId(), identity);
        }

        List<Product> products;
        if (query != null && !query.isEmpty()) {
            products = productService.searchProducts(query);
            model.addAttribute("searchQuery", query);
        } else if (sort != null && !sort.isEmpty()) {
            products = productService.getProductsSorted(sort);
            model.addAttribute("activeSort", sort);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("cartCount", cartService.getItems(identity, isUser).size());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(User user, org.springframework.ui.Model model) {
        User registered = userService.registerUser(user);
        if (registered == null) {
            model.addAttribute("error", "Username already taken. Please choose another one.");
            return "register";
        }
        return "redirect:/login";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();

        Optional<Product> product = productService.getProductById(productId);
        
        product.ifPresent(p -> cartService.addItem(p, identity, isUser));
        
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();
        model.addAttribute("items", cartService.getItems(identity, isUser));
        model.addAttribute("total", cartService.getTotalAmount(identity, isUser));
        return "cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId, HttpSession session) {
        cartService.removeItem(productId, getIdentity(session), isUsername());
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();
        if (cartService.getItems(identity, isUser).isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("total", cartService.getTotalAmount(identity, isUser));
        if (isUser) {
            userService.findByUsername(identity).ifPresent(user -> model.addAttribute("accountEmail", user.getEmail()));
        }
        return "checkout";
    }

    @PostMapping("/checkout/pay")
    public String processPayment(HttpSession session) {
        cartService.clear(getIdentity(session), isUsername());
        return "redirect:/payment-success";
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam(required = false) String orderNumber, Model model, HttpSession session) {
        String resolvedOrderNumber = orderNumber;
        if (resolvedOrderNumber == null || resolvedOrderNumber.isEmpty()) {
            Object lastOrderNumber = session.getAttribute("lastOrderNumber");
            if (lastOrderNumber != null) {
                resolvedOrderNumber = lastOrderNumber.toString();
            }
        }

        if (resolvedOrderNumber != null && !resolvedOrderNumber.isEmpty()) {
            orderService.findByOrderNumber(resolvedOrderNumber).ifPresent(order -> model.addAttribute("order", order));
        }
        return "payment-success";
    }

    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id, Model model, HttpSession session) {
        String identity = getIdentity(session);
        boolean isUser = isUsername();
        
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            model.addAttribute("cartCount", cartService.getItems(identity, isUser).size());
            return "product";
        }
        return "redirect:/";
    }

    @GetMapping("/account")
    public String account(Model model, HttpSession session) {
        String username = getIdentity(session);
        if (!isUsername()) {
            return "redirect:/login";
        }
        
        Optional<User> user = userService.findByUsername(username);
        user.ifPresent(u -> model.addAttribute("user", u));
        model.addAttribute("cartCount", cartService.getItems(username, true).size());
        model.addAttribute("orders", orderService.getOrdersForUser(username));
        return "account";
    }

    @PostMapping("/account/update")
    public String updateProfile(@RequestParam String email, 
                                @RequestParam(required = false) String newPassword, 
                                HttpSession session) {
        if (!isUsername()) {
            return "redirect:/login";
        }
        
        userService.updateProfile(getIdentity(session), email, newPassword);
        return "redirect:/account?success";
    }
}
