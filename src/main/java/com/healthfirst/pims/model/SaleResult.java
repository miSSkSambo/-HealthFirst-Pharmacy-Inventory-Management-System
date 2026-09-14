package com.healthfirst.pims.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Immutable receipt data returned only after a successful transactional checkout. */
public record SaleResult(int saleId, LocalDateTime saleDate, String cashierName,
                         List<CartItem> items, BigDecimal totalAmount) { }
