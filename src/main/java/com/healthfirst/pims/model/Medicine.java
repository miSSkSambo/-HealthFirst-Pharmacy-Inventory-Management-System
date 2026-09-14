package com.healthfirst.pims.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Core inventory entity. Price is deliberately BigDecimal to avoid floating-point currency errors. */
public record Medicine(int id, String name, String company, String medicineType, BigDecimal price,
                       int quantityInStock, int reorderLevel, LocalDate expiryDate, int supplierId,
                       String supplierName) {
    @Override public String toString() {
        return name + " (" + medicineType + ") — R" + price;
    }
}
