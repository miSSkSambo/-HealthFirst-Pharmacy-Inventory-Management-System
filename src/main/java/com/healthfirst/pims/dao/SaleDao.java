package com.healthfirst.pims.dao;

import com.healthfirst.pims.config.Database;
import com.healthfirst.pims.model.CartItem;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.SaleResult;
import com.healthfirst.pims.model.User;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Performs checkout atomically: every sale header, line item, and stock deduction succeeds or rolls back together. */
public final class SaleDao {
    public SaleResult checkout(User cashier, List<CartItem> cart) throws SQLException {
        if (cart == null || cart.isEmpty()) throw new IllegalArgumentException("Add at least one medicine before checkout.");
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try {
                BigDecimal total = BigDecimal.ZERO;
                List<CartItem> resolvedItems = new ArrayList<>();
                // A stable lock order reduces the risk of two concurrent cashiers deadlocking.
                List<CartItem> orderedCart = cart.stream().sorted(Comparator.comparingInt(item -> item.medicine().id())).toList();
                for (CartItem item : orderedCart) {
                    try (PreparedStatement lock = con.prepareStatement("SELECT quantity_in_stock, price, name FROM medicines WHERE medicine_id=? FOR UPDATE")) {
                        lock.setInt(1, item.medicine().id());
                        try (ResultSet rs = lock.executeQuery()) {
                            if (!rs.next()) throw new IllegalArgumentException(item.medicine().name() + " is no longer available.");
                            if (rs.getInt("quantity_in_stock") < item.quantity()) throw new IllegalArgumentException("Insufficient stock for " + rs.getString("name") + ". Available: " + rs.getInt("quantity_in_stock"));
                            BigDecimal currentPrice = rs.getBigDecimal("price");
                            Medicine cataloguedMedicine = item.medicine();
                            Medicine pricedMedicine = new Medicine(cataloguedMedicine.id(), rs.getString("name"), cataloguedMedicine.company(),
                                    cataloguedMedicine.medicineType(), currentPrice, cataloguedMedicine.quantityInStock(),
                                    cataloguedMedicine.reorderLevel(), cataloguedMedicine.expiryDate(), cataloguedMedicine.supplierId(), cataloguedMedicine.supplierName());
                            CartItem resolvedItem = new CartItem(pricedMedicine, item.quantity());
                            resolvedItems.add(resolvedItem);
                            total = total.add(resolvedItem.lineTotal());
                        }
                    }
                }
                int saleId;
                try (PreparedStatement sale = con.prepareStatement("INSERT INTO sales(total_amount, user_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    sale.setBigDecimal(1, total); sale.setInt(2, cashier.id()); sale.executeUpdate();
                    try (ResultSet keys = sale.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("No sale identifier was generated."); saleId = keys.getInt(1); }
                }
                for (CartItem item : resolvedItems) {
                    try (PreparedStatement line = con.prepareStatement("INSERT INTO sale_items(sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)")) {
                        line.setInt(1, saleId); line.setInt(2, item.medicine().id()); line.setInt(3, item.quantity()); line.setBigDecimal(4, item.medicine().price()); line.executeUpdate();
                    }
                    try (PreparedStatement stock = con.prepareStatement("UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id=?")) {
                        stock.setInt(1, item.quantity()); stock.setInt(2, item.medicine().id()); stock.executeUpdate();
                    }
                }
                con.commit();
                return new SaleResult(saleId, LocalDateTime.now(), cashier.fullName(), List.copyOf(resolvedItems), total);
            } catch (Exception e) {
                con.rollback();
                if (e instanceof SQLException sql) throw sql;
                if (e instanceof RuntimeException runtime) throw runtime;
                throw new SQLException("Checkout failed.", e);
            } finally { con.setAutoCommit(true); }
        }
    }
}
