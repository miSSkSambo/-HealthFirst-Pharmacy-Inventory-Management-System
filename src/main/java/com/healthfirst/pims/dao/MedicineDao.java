package com.healthfirst.pims.dao;

import com.healthfirst.pims.config.Database;
import com.healthfirst.pims.model.Medicine;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Encapsulates inventory CRUD, search, availability checks, and dashboard totals. */
public final class MedicineDao {
    private static final String BASE_SELECT = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, m.quantity_in_stock, m.reorder_level, m.expiry_date, m.supplier_id, s.name AS supplier_name FROM medicines m JOIN suppliers s ON s.supplier_id=m.supplier_id ";

    public List<Medicine> findAll(String search) throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String filter = search == null ? "" : search.trim();
        String sql = BASE_SELECT + (filter.isBlank() ? "ORDER BY m.name" : "WHERE m.name LIKE ? OR m.company LIKE ? OR m.medicine_type LIKE ? ORDER BY m.name");
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (!filter.isBlank()) for (int i = 1; i <= 3; i++) ps.setString(i, "%" + filter + "%");
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) medicines.add(map(rs)); }
        }
        return medicines;
    }

    public Optional<Medicine> findById(int id) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(BASE_SELECT + "WHERE m.medicine_id=?")) {
            ps.setInt(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        }
    }

    public void create(String name, String company, String type, BigDecimal price, int quantity, int reorderLevel, LocalDate expiry, int supplierId) throws SQLException {
        save("INSERT INTO medicines(name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 0, name, company, type, price, quantity, reorderLevel, expiry, supplierId);
    }

    public void update(int id, String name, String company, String type, BigDecimal price, int quantity, int reorderLevel, LocalDate expiry, int supplierId) throws SQLException {
        save("UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?", id, name, company, type, price, quantity, reorderLevel, expiry, supplierId);
    }

    private void save(String sql, int id, String name, String company, String type, BigDecimal price, int quantity, int reorderLevel, LocalDate expiry, int supplierId) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim()); ps.setString(2, company.trim()); ps.setString(3, type.trim()); ps.setBigDecimal(4, price);
            ps.setInt(5, quantity); ps.setInt(6, reorderLevel); ps.setDate(7, Date.valueOf(expiry)); ps.setInt(8, supplierId);
            if (id > 0) ps.setInt(9, id);
            if (ps.executeUpdate() != 1) throw new SQLException("Medicine record was not found.");
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM medicines WHERE medicine_id=?")) {
            ps.setInt(1, id); if (ps.executeUpdate() != 1) throw new SQLException("Medicine record was not found.");
        }
    }

    public int countAll() throws SQLException { return singleInt("SELECT COUNT(*) FROM medicines"); }
    public int countLowStock() throws SQLException { return singleInt("SELECT COUNT(*) FROM medicines WHERE quantity_in_stock <= reorder_level"); }
    public int countExpiring(int days) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM medicines WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)")) {
            ps.setInt(1, days); try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }
    private int singleInt(String sql) throws SQLException {
        try (Connection con = Database.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { rs.next(); return rs.getInt(1); }
    }

    private Medicine map(ResultSet rs) throws SQLException {
        Date date = rs.getDate("expiry_date");
        return new Medicine(rs.getInt("medicine_id"), rs.getString("name"), rs.getString("company"), rs.getString("medicine_type"), rs.getBigDecimal("price"), rs.getInt("quantity_in_stock"), rs.getInt("reorder_level"), date == null ? null : date.toLocalDate(), rs.getInt("supplier_id"), rs.getString("supplier_name"));
    }
}
