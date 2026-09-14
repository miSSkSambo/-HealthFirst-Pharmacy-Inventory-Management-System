package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.ReportDao;
import com.healthfirst.pims.model.ReportRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Administrative reporting interface for sales, item performance,
 * low stock, and expiry alerts.
 */
public final class ReportsPanel extends JPanel {

    private final ReportDao dao = new ReportDao();

    private final JTextField from =
            new JTextField(LocalDate.now().minusDays(30).toString(), 10);

    private final JTextField to =
            new JTextField(LocalDate.now().toString(), 10);

    private final JTextField expiryEnd =
            new JTextField(LocalDate.now().plusMonths(1).toString(), 10);

    private final DefaultTableModel model =
            new DefaultTableModel();

    private final JTable table =
            new JTable(model);

    public ReportsPanel() {
        super(new BorderLayout(10, 10));

        setBackground(UiStyle.BG);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        build();
        showSales();
    }

    private void build() {

        JPanel toolbar =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));

        toolbar.setBackground(UiStyle.BG);

        // -------------------------------------------------
        // Sales date fields
        // -------------------------------------------------

        toolbar.add(
                new JLabel("Sales dates (yyyy-mm-dd):")
        );

        toolbar.add(from);

        toolbar.add(
                new JLabel("to")
        );

        toolbar.add(to);

        // -------------------------------------------------
        // Sales Report button
        // -------------------------------------------------

        JButton sales =
                UiStyle.button("Sales report", UiStyle.NAVY);

        UiStyle.configureButton(sales);

        sales.addActionListener(e -> showSales());

        toolbar.add(sales);

        // -------------------------------------------------
        // Item-wise Report button
        // -------------------------------------------------

        JButton items =
                UiStyle.button("Item-wise report", UiStyle.BLUE);

        UiStyle.configureButton(items);

        items.addActionListener(e -> showItems());

        toolbar.add(items);

        // -------------------------------------------------
        // Low-stock Report button
        // -------------------------------------------------

        JButton low =
                UiStyle.button("Low-stock report", UiStyle.RED);

        UiStyle.configureButton(low);

        low.addActionListener(e -> showLow());

        toolbar.add(low);

        // -------------------------------------------------
        // Separator
        // -------------------------------------------------

        toolbar.add(
                new JSeparator(SwingConstants.VERTICAL)
        );

        // -------------------------------------------------
        // Expiry Report
        // -------------------------------------------------

        toolbar.add(
                new JLabel("Expiry to:")
        );

        toolbar.add(expiryEnd);

        JButton expiry =
                UiStyle.button("Expiry report", UiStyle.GREEN);

        UiStyle.configureButton(expiry);

        expiry.addActionListener(e -> showExpiry());

        toolbar.add(expiry);

        // Add toolbar to screen
        add(toolbar, BorderLayout.NORTH);

        // -------------------------------------------------
        // Reports table
        // -------------------------------------------------

        UiStyle.configureTable(table);

        add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );
    }

    /**
     * Reads and validates the sales date range.
     */
    private LocalDate[] range() {

        try {

            LocalDate a =
                    LocalDate.parse(from.getText().trim());

            LocalDate b =
                    LocalDate.parse(to.getText().trim());

            if (a.isAfter(b)) {
                throw new IllegalArgumentException(
                        "The start date cannot be after the end date."
                );
            }

            return new LocalDate[]{a, b};

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Report dates must use yyyy-mm-dd."
            );
        }
    }

    /**
     * Loads report data into the table.
     */
    private void set(
            String[] headers,
            List<ReportRow> rows
    ) {

        model.setDataVector(
                new Object[0][0],
                headers
        );

        for (ReportRow row : rows) {
            model.addRow(row.values());
        }
    }

    /**
     * Displays the sales report.
     */
    private void showSales() {

        try {

            LocalDate[] d = range();

            set(
                    new String[]{
                            "Sale date",
                            "Transactions",
                            "Revenue"
                    },
                    dao.salesSummary(d[0], d[1])
            );

        } catch (Exception e) {

            Ui.error(this, e);
        }
    }

    /**
     * Displays the item-wise sales report.
     */
    private void showItems() {

        try {

            LocalDate[] d = range();

            set(
                    new String[]{
                            "Medicine",
                            "Type",
                            "Units sold",
                            "Revenue"
                    },
                    dao.itemWiseSales(d[0], d[1])
            );

        } catch (Exception e) {

            Ui.error(this, e);
        }
    }

    /**
     * Displays the low-stock report.
     */
    private void showLow() {

        try {

            set(
                    new String[]{
                            "Medicine",
                            "In stock",
                            "Reorder level",
                            "Shortage"
                    },
                    dao.lowStock()
            );

        } catch (Exception e) {

            Ui.error(this, e);
        }
    }

    /**
     * Displays the expiry report.
     */
    private void showExpiry() {

        try {

            LocalDate expiryDate =
                    LocalDate.parse(
                            expiryEnd.getText().trim()
                    );

            set(
                    new String[]{
                            "Medicine",
                            "Type",
                            "Expiry date",
                            "In stock",
                            "Supplier"
                    },
                    dao.expiry(expiryDate)
            );

        } catch (Exception e) {

            Ui.error(this, e);
        }
    }
}