package com.healthfirst.pims.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Shared visual language for a clean, readable desktop interface.
 */
public final class UiStyle {

    public static final Color NAVY = new Color(17, 62, 112);
    public static final Color BLUE = new Color(28, 105, 173);
    public static final Color GREEN = new Color(25, 135, 84);
    public static final Color RED = new Color(187, 45, 59);

    public static final Color BG = new Color(245, 248, 252);

    public static final Font TITLE =
            new Font("SansSerif", Font.BOLD, 22);

    public static final Font SUBTITLE =
            new Font("SansSerif", Font.BOLD, 15);

    private UiStyle() {
    }

    /**
     * Creates a padded panel using the application's background colour.
     */
    public static JPanel paddedPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        return p;
    }

    /**
     * Creates a coloured button with clearly visible BLACK text.
     */
    public static JButton button(String text, Color color) {
        JButton b = new JButton(text);

        // Black button text
        b.setForeground(Color.BLACK);

        // Button background colour
        b.setBackground(color);

        // Bold, readable text
        b.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Remove focus outline
        b.setFocusPainted(false);

        // Ensure background colour is displayed
        b.setOpaque(true);
        b.setContentAreaFilled(true);

        // Give the button a clear border
        b.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                color.darker(), 1
                        ),
                        new EmptyBorder(8, 14, 8, 14)
                )
        );

        return b;
    }

    /**
     * Configures standard buttons that are not created
     * using the coloured button() method.
     */
    public static void configureButton(JButton b) {

        // Black button text
        b.setForeground(Color.BLACK);

        // Readable font
        b.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Remove focus outline
        b.setFocusPainted(false);
    }

    /**
     * Creates the standard border used by text fields.
     */
    public static Border fieldBorder() {
        return new CompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(150, 165, 185), 1
                ),
                new EmptyBorder(5, 7, 5, 7)
        );
    }

    /**
     * Configures tables throughout the application.
     */
    public static void configureTable(JTable table) {

        // Increase row height for readability
        table.setRowHeight(32);

        // Readable table font
        table.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        // Only one row can be selected at a time
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        // Allow sorting by clicking column headers
        table.setAutoCreateRowSorter(true);

        // Prevent users from rearranging columns
        table.getTableHeader().setReorderingAllowed(false);

        // Make column headings readable
        table.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        // Increase header height
        table.getTableHeader().setPreferredSize(
                new Dimension(0, 32)
        );
    }
}