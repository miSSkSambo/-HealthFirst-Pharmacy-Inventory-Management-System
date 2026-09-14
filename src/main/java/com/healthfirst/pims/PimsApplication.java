package com.healthfirst.pims;

import com.healthfirst.pims.ui.LoginFrame;

import javax.swing.*;

/** Application entry point. Swing work is started on the Event Dispatch Thread. */
public final class PimsApplication {
    private PimsApplication() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new LoginFrame().setVisible(true);
        });
    }
}
