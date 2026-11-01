
package com.bookstore.controller;

import com.bookstore.model.BookOrder;
import com.bookstore.repository.BookOrderRepository;
import com.bookstore.repository.CustomerAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final BookOrderRepository orderRepository;
    private final CustomerAccountRepository accountRepository;

    public OrderController(
            BookOrderRepository orderRepository,
            CustomerAccountRepository accountRepository) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public ResponseEntity<List<BookOrder>> getMyOrders(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName().trim();

        return accountRepository.findByEmailIgnoreCase(email)
                .map(account -> {
                    if (account.getCustomer() == null) {
                        return ResponseEntity.ok(List.<BookOrder>of());
                    }

                    List<BookOrder> orders =
                            orderRepository
                                    .findByCustomer_EmailOrderByOrderDateDesc(
                                            account.getCustomer().getEmail());

                    return ResponseEntity.ok(orders);
                })
                .orElseGet(() -> ResponseEntity.ok(List.of()));
    }
}

