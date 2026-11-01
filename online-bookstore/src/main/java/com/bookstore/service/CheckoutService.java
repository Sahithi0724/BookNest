package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.model.BookOrder;
import com.bookstore.model.Customer;
import com.bookstore.model.OrderItem;
import com.bookstore.repository.BookOrderRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CustomerRepository;
import com.bookstore.repository.OrderItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CheckoutService {

    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;
    private final BookOrderRepository orderRepository;
    private final OrderItemRepository itemRepository;

    public CheckoutService(
            BookRepository bookRepository,
            CustomerRepository customerRepository,
            BookOrderRepository orderRepository,
            OrderItemRepository itemRepository) {

        this.bookRepository = bookRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public BookOrder placeOrder(
            String fullName,
            String email,
            String phone,
            String address,
            Map<Long, Integer> requestedItems) {

        // Validate customer details
        if (fullName == null || fullName.isBlank()
                || email == null || !email.contains("@")
                || phone == null || phone.isBlank()
                || address == null || address.isBlank()) {

            throw new IllegalArgumentException(
                    "Please provide valid customer details.");
        }

        // Validate cart
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }

        // Find an existing customer by email.
        // Create a customer only if the email does not exist.
        String normalizedEmail = email.trim();

        Customer customer = customerRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setFullName(fullName.trim());
                    newCustomer.setEmail(normalizedEmail);
                    newCustomer.setPhone(phone.trim());
                    newCustomer.setAddress(address.trim());

                    return customerRepository.save(newCustomer);
                });

        // Update the existing customer's details
        customer.setFullName(fullName.trim());
        customer.setPhone(phone.trim());
        customer.setAddress(address.trim());
        customer = customerRepository.save(customer);

        // Create the order
        BookOrder order = new BookOrder();
        order.setCustomer(customer);
        order.setStatus("PLACED");

        BigDecimal total = BigDecimal.ZERO;

        Map<Book, Integer> validatedItems = new LinkedHashMap<>();

        // Validate books, quantities, and stock
        for (Map.Entry<Long, Integer> entry : requestedItems.entrySet()) {

            Long bookId = entry.getKey();
            Integer quantity = entry.getValue();

            if (bookId == null || quantity == null || quantity < 1) {
                throw new IllegalArgumentException(
                        "Invalid book or quantity.");
            }

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Book not found: " + bookId));

            if (book.getStock() == null || book.getStock() < quantity) {
                throw new IllegalArgumentException(
                        "Insufficient stock for: " + book.getTitle());
            }

            validatedItems.put(book, quantity);

            total = total.add(
                    book.getPrice().multiply(
                            BigDecimal.valueOf(quantity)));
        }

        // Save the order total
        order.setTotalAmount(total);
        order = orderRepository.save(order);

        // Save order items and update stock
        for (Map.Entry<Book, Integer> entry : validatedItems.entrySet()) {

            Book book = entry.getKey();
            int quantity = entry.getValue();

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setBook(book);
            item.setQuantity(quantity);
            item.setPrice(book.getPrice());
            item.setUnitPrice(book.getPrice());

            itemRepository.save(item);

            book.setStock(book.getStock() - quantity);
            bookRepository.save(book);
        }

        return order;
    }
}
