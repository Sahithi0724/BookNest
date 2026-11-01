
package com.bookstore.service;

import com.bookstore.model.CustomerAccount;
import com.bookstore.repository.CustomerAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsService implements UserDetailsService {

    private final CustomerAccountRepository accountRepository;

    public CustomerDetailsService(
            CustomerAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        CustomerAccount account = accountRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Customer account not found"));

        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles("CUSTOMER")
                .build();
    }
}
