package com.healthfirst.pims.model;

import java.math.BigDecimal;

/** Mutable shopping-cart line used only while a cashier is assembling a sale. */
public final class CartItem {
    private final Medicine medicine;
    private int quantity;

    public CartItem(Medicine medicine, int quantity) {
        this.medicine = medicine;
        this.quantity = quantity;
    }
    public Medicine medicine() { return medicine; }
    public int quantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal lineTotal() { return medicine.price().multiply(BigDecimal.valueOf(quantity)); }
}
