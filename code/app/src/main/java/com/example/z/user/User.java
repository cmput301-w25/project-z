package com.example.z.user;

/**
 * Represents a user in the application with an email and username.
 *
 *  Outstanding issues:
 *      - None
 */
public class User {
    private String email;
    private String username;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public User() {}

    /**
     * Constructs a new User with the specified email and username.
     *
     * @param email    The email address of the user.
     * @param username The username of the user.
     */
    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }

    /**
     * Retrieves the user's email.
     *
     * @return The email address of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     *
     * @param email The new email address of the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retrieves the user's username.
     *
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the user's username.
     *
     * @param username The new username of the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }
}


