package com.libraryhub.service;

import java.util.Locale;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.libraryhub.model.Role;
import com.libraryhub.model.User;
import com.libraryhub.repository.UserRepository;
import com.libraryhub.util.DateUtil;

/** Sign-in lookup for Spring Security and member registration. */
@Service
public class AuthService implements UserDetailsService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final DateUtil dates;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, DateUtil dates) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.dates = dates;
    }

    @Override
    public LibraryUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return users.findByEmail(normalizeEmail(email))
                .map(LibraryUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("No account for that email"));
    }

    public boolean emailTaken(String email) {
        return users.existsByEmail(normalizeEmail(email));
    }

    /** Creates a member account. Throws EmailAlreadyUsedException when the email is taken. */
    @Transactional
    public User register(String name, String email, String rawPassword) {
        String normalized = normalizeEmail(email);
        if (users.existsByEmail(normalized)) {
            throw new EmailAlreadyUsedException(normalized);
        }
        try {
            long id = users.insert(name.trim(), normalized, passwordEncoder.encode(rawPassword),
                    Role.USER, true, dates.now());
            return users.findById(id).orElseThrow(() -> new IllegalStateException("New account not found"));
        } catch (DataAccessException ex) {
            if (users.existsByEmail(normalized)) {
                throw new EmailAlreadyUsedException(normalized);
            }
            throw ex;
        }
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
