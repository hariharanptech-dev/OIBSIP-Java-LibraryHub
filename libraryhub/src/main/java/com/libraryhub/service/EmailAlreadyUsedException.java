package com.libraryhub.service;

public class EmailAlreadyUsedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailAlreadyUsedException(String email) {
        super("An account already exists for " + email);
    }
}
