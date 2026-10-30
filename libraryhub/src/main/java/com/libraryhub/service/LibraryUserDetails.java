package com.libraryhub.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.libraryhub.model.Role;
import com.libraryhub.model.User;

/** The signed-in principal. Holds only what security needs; pages read fresh data from the database. */
public class LibraryUserDetails implements UserDetails, CredentialsContainer {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String email;
    private final String displayName;
    private final Role role;
    private final boolean active;
    private String passwordHash;

    public LibraryUserDetails(User user) {
        this.id = user.id();
        this.email = user.email();
        this.displayName = user.name();
        this.role = user.role();
        this.active = user.active();
        this.passwordHash = user.passwordHash();
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    /** Deactivated members cannot sign in. */
    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }
}
