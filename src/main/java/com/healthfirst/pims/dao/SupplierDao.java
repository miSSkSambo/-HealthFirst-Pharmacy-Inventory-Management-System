package com.healthfirst.pims.dao;

import com.healthfirst.pims.config.Database;
import com.healthfirst.pims.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Encapsulates CRUD operations for medicine suppliers. */
public final class SupplierDao {
    public List<Supplier> findAll() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT supplier_id, name, contact_person, phone, email, address FROM suppliers ORDER BY name"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) suppliers.add(map(rs));
        }
        return suppliers;
    }

    public void create(String name, String contact, String phone, String email, String address) throws SQLException {
        save("INSERT INTO suppliers(name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)", 0, name, contact, phone, email, address);
    }

    public void update(int id, String name, String contact, String phone, String email, String address) throws SQLException {
        save("UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?", id, name, contact, phone, email, address);
    }

    private void save(String sql, int id, String name, String contact, String phone, String email, String address) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim()); ps.setString(2, contact.trim()); ps.setString(3, phone.trim());
            ps.setString(4, email.trim()); ps.setString(5, address.trim());
            if (id > 0) ps.setInt(6, id);
            if (ps.executeUpdate() != 1) throw new SQLException("Supplier record was not found.");
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM suppliers WHERE supplier_id=?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() != 1) throw new SQLException("Supplier record was not found.");
        }
    }

    private Supplier map(ResultSet rs) throws SQLException {
        return new Supplier(rs.getInt("supplier_id"), rs.getString("name"), rs.getString("contact_person"),
                rs.getString("phone"), rs.getString("email"), rs.getString("address"));
    }
}
