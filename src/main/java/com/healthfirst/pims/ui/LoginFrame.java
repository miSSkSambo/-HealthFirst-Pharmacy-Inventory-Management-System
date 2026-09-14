package com.healthfirst.pims.ui;

import com.healthfirst.pims.config.Database;
import com.healthfirst.pims.dao.UserDao;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.Optional;

/** Secure role-aware sign-in screen. Successful authentication opens the appropriate dashboard. */
public final class LoginFrame extends JFrame {
    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);
    private final JButton signIn = UiStyle.button("Sign in", UiStyle.NAVY);
    private final JLabel status = new JLabel(" ");
    private final UserDao userDao = new UserDao();

    public LoginFrame() {
        super("HealthFirst PIMS — Secure Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(510, 370); setResizable(false); setLocationRelativeTo(null);
        buildUi();
        getRootPane().setDefaultButton(signIn);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(UiStyle.BG);
        JPanel banner = new JPanel(new BorderLayout()); banner.setBackground(UiStyle.NAVY); banner.setBorder(new EmptyBorder(25,30,25,30));
        JLabel title = new JLabel("HealthFirst Pharmacy"); title.setForeground(Color.WHITE); title.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel subtitle = new JLabel("Inventory Management System"); subtitle.setForeground(new Color(211,229,249)); subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JPanel heading = new JPanel(); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS)); heading.setOpaque(false); heading.add(title); heading.add(Box.createVerticalStrut(5)); heading.add(subtitle); banner.add(heading, BorderLayout.CENTER);
        root.add(banner, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(UiStyle.BG); form.setBorder(new EmptyBorder(25,65,12,65));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7,7,7,7); g.anchor = GridBagConstraints.WEST;
        addRow(form, g, 0, "Username", username); addRow(form, g, 1, "Password", password);
        g.gridx=1; g.gridy=2; g.fill=GridBagConstraints.HORIZONTAL; signIn.addActionListener(this::login); form.add(signIn,g);
        g.gridx=0;g.gridy=3;g.gridwidth=2; status.setForeground(UiStyle.RED); form.add(status,g);
        JLabel hint = new JLabel("Demo accounts: admin / admin123     cashier / cash123"); hint.setForeground(Color.DARK_GRAY); hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g.gridy=4; form.add(hint,g); root.add(form, BorderLayout.CENTER); setContentPane(root);
    }

    private void addRow(JPanel panel, GridBagConstraints g, int y, String label, JComponent field) {
        g.gridx=0;g.gridy=y;g.gridwidth=1;g.fill=GridBagConstraints.NONE; panel.add(new JLabel(label + ":"),g);
        field.setBorder(UiStyle.fieldBorder()); g.gridx=1;g.fill=GridBagConstraints.HORIZONTAL; panel.add(field,g);
    }

    private void login(ActionEvent ignored) {
        String user = username.getText().trim(); String pass = new String(password.getPassword());
        if (user.isBlank() || pass.isBlank()) { status.setText("Enter both username and password."); return; }
        signIn.setEnabled(false); status.setText("Checking credentials...");
        try {
            Database.verifyConnection();
            Optional<User> authenticated = userDao.authenticate(user, pass);
            if (authenticated.isPresent()) { dispose(); new DashboardFrame(authenticated.get()).setVisible(true); }
            else { password.setText(""); status.setText("Invalid username or password. Please try again."); }
        } catch (SQLException e) { status.setText("Cannot connect to MySQL. Run database/pims_database.sql first."); Ui.error(this, e); }
        finally { signIn.setEnabled(true); }
    }
}
