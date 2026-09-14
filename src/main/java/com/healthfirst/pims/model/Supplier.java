package com.healthfirst.pims.model;

/** Supplier contact information linked to stocked medicines. */
public record Supplier(int id, String name, String contactPerson, String phone, String email, String address) {
    @Override public String toString() { return name; }
}
