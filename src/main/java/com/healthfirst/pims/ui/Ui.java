package com.healthfirst.pims.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Centralises readable success, validation, and database error messages. */
public final class Ui {
    private Ui() { }
    public static void info(Component parent, String message) { JOptionPane.showMessageDialog(parent, message, "HealthFirst PIMS", JOptionPane.INFORMATION_MESSAGE); }
    public static boolean confirm(Component parent, String message) { return JOptionPane.showConfirmDialog(parent, message, "Confirm action", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION; }
    public static void error(Component parent, Exception error) {
        String message = error.getMessage();
        if (error instanceof SQLException sql && sql.getErrorCode() == 1062) message = "A record with that unique value already exists.";
        if (message == null || message.isBlank()) message = "The requested action could not be completed.";
        JOptionPane.showMessageDialog(parent, message, "HealthFirst PIMS", JOptionPane.ERROR_MESSAGE);
    }
}
