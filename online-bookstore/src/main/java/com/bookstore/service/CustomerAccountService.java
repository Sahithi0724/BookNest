
package com.bookstore.service;

import com.bookstore.model.Customer;
import com.bookstore.model.CustomerAccount;
import com.bookstore.repository.CustomerAccountRepository;
import com.bookstore.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerAccountService {

    private final CustomerAccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerAccountService(
            CustomerAccountRepository accountRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String email, String password) {
        if (email == null || email.isBlank()
                || password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Enter a valid email and a password of at least 8 characters.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        CustomerAccount account = new CustomerAccount();
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(password));

        Customer customer = customerRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElse(null);

        account.setCustomer(customer);
        accountRepository.save(account);
    }
}
