package com.libraryhub.web;

import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.libraryhub.model.User;
import com.libraryhub.repository.UserRepository;
import com.libraryhub.service.LibraryUserDetails;
import com.libraryhub.util.SecurityUtil;

/** Adds "currentUser" (fresh from the database) to every page model, or null when signed out. */
@ControllerAdvice
public class GlobalModelAdvice {

    private final UserRepository users;

    public GlobalModelAdvice(UserRepository users) {
        this.users = users;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return SecurityUtil.principal(SecurityContextHolder.getContext().getAuthentication())
                .flatMap(this::lookup)
                .orElse(null);
    }

    private Optional<User> lookup(LibraryUserDetails principal) {
        try {
            return users.findById(principal.getId());
        } catch (DataAccessException ex) {
            return Optional.empty();
        }
    }
}
