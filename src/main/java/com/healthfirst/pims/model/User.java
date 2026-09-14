package com.healthfirst.pims.model;

/** Represents an authenticated PIMS user and the permissions implied by the role. */
public record User(int id, String username, String role, String fullName) {
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }
}
