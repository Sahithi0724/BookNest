package com.bookstore.controller;

import com.bookstore.model.BookOrder;
import com.bookstore.service.CheckoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        try {
            BookOrder order = checkoutService.placeOrder(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getAddress(),
                    request.getItems()
            );

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Order placed successfully!");
            response.put("orderId", order.getOrderId());
            response.put("totalAmount", order.getTotalAmount());
            response.put("status", order.getStatus());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public static class CheckoutRequest {

        private String fullName;
        private String email;
        private String phone;
        private String address;
        private Map<Long, Integer> items;

        public CheckoutRequest() {
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Map<Long, Integer> getItems() {
            return items;
        }

        public void setItems(Map<Long, Integer> items) {
            this.items = items;
        }
    }
}
