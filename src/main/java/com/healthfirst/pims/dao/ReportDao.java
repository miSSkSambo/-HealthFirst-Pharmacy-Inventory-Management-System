package com.healthfirst.pims.dao;

import com.healthfirst.pims.config.Database;
import com.healthfirst.pims.model.ReportRow;
import com.healthfirst.pims.util.FormatUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Provides the four analytical reports required by the assignment specification. */
public final class ReportDao {
    public List<ReportRow> salesSummary(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT DATE(sale_date) AS day, COUNT(*) AS transactions, COALESCE(SUM(total_amount),0) AS revenue FROM sales WHERE DATE(sale_date) BETWEEN ? AND ? GROUP BY DATE(sale_date) ORDER BY day DESC";
        List<ReportRow> rows = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(new ReportRow(new Object[]{FormatUtil.date(rs.getDate("day").toLocalDate()), rs.getInt("transactions"), FormatUtil.money(rs.getBigDecimal("revenue"))})); }
        } return rows;
    }

    public List<ReportRow> itemWiseSales(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT m.name, m.medicine_type, SUM(si.quantity_sold) AS units, SUM(si.quantity_sold*si.price_at_sale) AS revenue FROM sale_items si JOIN sales s ON s.sale_id=si.sale_id JOIN medicines m ON m.medicine_id=si.medicine_id WHERE DATE(s.sale_date) BETWEEN ? AND ? GROUP BY m.medicine_id, m.name, m.medicine_type ORDER BY units DESC, revenue DESC";
        List<ReportRow> rows = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(new ReportRow(new Object[]{rs.getString("name"), rs.getString("medicine_type"), rs.getInt("units"), FormatUtil.money(rs.getBigDecimal("revenue"))})); }
        } return rows;
    }

    public List<ReportRow> lowStock() throws SQLException {
        String sql = "SELECT name, quantity_in_stock, reorder_level, (reorder_level-quantity_in_stock) AS shortage FROM medicines WHERE quantity_in_stock <= reorder_level ORDER BY quantity_in_stock, name";
        List<ReportRow> rows = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(new ReportRow(new Object[]{rs.getString("name"), rs.getInt("quantity_in_stock"), rs.getInt("reorder_level"), rs.getInt("shortage")}));
        } return rows;
    }

    public List<ReportRow> expiry(LocalDate endDate) throws SQLException {
        String sql = "SELECT m.name, m.medicine_type, m.expiry_date, m.quantity_in_stock, s.name AS supplier FROM medicines m JOIN suppliers s ON s.supplier_id=m.supplier_id WHERE m.expiry_date BETWEEN CURDATE() AND ? ORDER BY m.expiry_date, m.name";
        List<ReportRow> rows = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) rows.add(new ReportRow(new Object[]{rs.getString("name"), rs.getString("medicine_type"), FormatUtil.date(rs.getDate("expiry_date").toLocalDate()), rs.getInt("quantity_in_stock"), rs.getString("supplier")})); }
        } return rows;
    }
}
