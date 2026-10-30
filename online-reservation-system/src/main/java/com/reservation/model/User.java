package com.reservation.model;

/**
 * An authenticated application user. The password hash is deliberately not
 * kept in the model once authentication has succeeded.
 */
public class User {
    private final int id;
    private final String username;

    /**
     * Creates a user.
     *
     * @param id       database id
     * @param username login name
     */
    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    /** @return the database id */
    public int getId() {
        return id;
    }

    /** @return the login name */
    public String getUsername() {
        return username;
    }
}
