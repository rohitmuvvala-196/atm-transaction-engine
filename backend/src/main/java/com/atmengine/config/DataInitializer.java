package com.atmengine.config;

import com.atmengine.entity.Account;
import com.atmengine.entity.Role;
import com.atmengine.entity.User;
import com.atmengine.repository.AccountRepository;
import com.atmengine.repository.RoleRepository;
import com.atmengine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("Initializing roles...");
            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator role")
                    .build();
            Role customerRole = Role.builder()
                    .name("ROLE_CUSTOMER")
                    .description("Customer role")
                    .build();
            roleRepository.save(adminRole);
            roleRepository.save(customerRole);
            log.info("Roles initialized successfully");
        }

        if (userRepository.count() == 0) {
            log.info("Initializing users...");
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow();
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

            User customer = User.builder()
                    .username("john_doe")
                    .password(passwordEncoder.encode("password123"))
                    .email("john@example.com")
                    .fullName("John Doe")
                    .phoneNumber("+911234567890")
                    .roles(Set.of(customerRole))
                    .build();

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@atmengine.com")
                    .fullName("System Administrator")
                    .phoneNumber("+919876543210")
                    .roles(Set.of(adminRole, customerRole))
                    .build();

            userRepository.save(customer);
            userRepository.save(admin);
            log.info("Users initialized successfully");
        }

        if (accountRepository.count() == 0) {
            log.info("Initializing accounts...");
            User customer = userRepository.findByUsername("john_doe").orElseThrow();

            Account account1 = Account.builder()
                    .accountNumber("ACC1234567890")
                    .pin("1234")
                    .balance(new BigDecimal("50000.00"))
                    .accountHolderName("John Doe")
                    .accountType("SAVINGS")
                    .user(customer)
                    .build();

            Account account2 = Account.builder()
                    .accountNumber("ACC0987654321")
                    .pin("5678")
                    .balance(new BigDecimal("1000000.00"))
                    .accountHolderName("Jane Smith")
                    .accountType("CURRENT")
                    .user(customer)
                    .build();

            Account account3 = Account.builder()
                    .accountNumber("ACC1122334455")
                    .pin("4321")
                    .balance(new BigDecimal("25000.00"))
                    .accountHolderName("Bob Johnson")
                    .accountType("SAVINGS")
                    .user(customer)
                    .build();

            accountRepository.save(account1);
            accountRepository.save(account2);
            accountRepository.save(account3);
            log.info("Accounts initialized successfully");
            log.info("Test Account: ACC1234567890 (PIN: 1234, Balance: 50000.00)");
            log.info("Test Account: ACC0987654321 (PIN: 5678, Balance: 100000.00)");
            log.info("Test Account: ACC1122334455 (PIN: 4321, Balance: 25000.00)");
        }
    }
}