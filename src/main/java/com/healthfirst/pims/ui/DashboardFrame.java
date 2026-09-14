package com.healthfirst.pims.ui;

import com.healthfirst.pims.dao.MedicineDao;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Main application window. Its contents are restricted by the authenticated user's role. */
public final class DashboardFrame extends JFrame {
    private final User currentUser;

    public DashboardFrame(User currentUser) {
        super("HealthFirst PIMS — " + currentUser.role() + " workspace");
        this.currentUser = currentUser;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700)); setSize(1220, 760); setLocationRelativeTo(null);
        buildUi();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(UiStyle.NAVY); header.setBorder(new EmptyBorder(12,18,12,18));
        JLabel title = new JLabel("HealthFirst PIMS"); title.setForeground(Color.WHITE); title.setFont(UiStyle.TITLE);
        JLabel user = new JLabel(currentUser.fullName() + "  |  " + currentUser.role() + "  "); user.setForeground(Color.WHITE);
        JButton logout = new JButton("Log out"); logout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5,0)); right.setOpaque(false); right.add(user); right.add(logout);
        header.add(title, BorderLayout.WEST); header.add(right, BorderLayout.EAST); root.add(header, BorderLayout.NORTH);

        if (currentUser.isAdmin()) root.add(adminWorkspace(), BorderLayout.CENTER);
        else root.add(new CashierPanel(currentUser), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JComponent adminWorkspace() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiStyle.SUBTITLE);
        tabs.addTab("Dashboard", createOverview());
        tabs.addTab("Manage Medicines", new MedicinePanel());
        tabs.addTab("Manage Suppliers", new SupplierPanel());
        tabs.addTab("Manage Users", new UserPanel(currentUser));
        tabs.addTab("Reports", new ReportsPanel());
        return tabs;
    }

    private JComponent createOverview() {
        JPanel body = UiStyle.paddedPanel(new BorderLayout(12,12));
        JLabel welcome = new JLabel("Administrator dashboard"); welcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        JPanel cards = new JPanel(new GridLayout(1,3,14,14)); cards.setBackground(UiStyle.BG);
        MedicineDao dao = new MedicineDao();
        try {
            cards.add(card("Medicine catalogue", String.valueOf(dao.countAll()), "Stocked products"));
            cards.add(card("Low-stock alerts", String.valueOf(dao.countLowStock()), "At or below reorder level"));
            cards.add(card("Expiring in 30 days", String.valueOf(dao.countExpiring(30)), "Requires prompt attention"));
        } catch (SQLException e) { cards.add(card("Database status", "Unavailable", "Run the SQL set-up script")); }
        JPanel headerContent = new JPanel(new BorderLayout(12, 12));
        headerContent.setBackground(UiStyle.BG);
        headerContent.add(welcome, BorderLayout.NORTH);
        headerContent.add(cards, BorderLayout.CENTER);
        body.add(headerContent, BorderLayout.NORTH);
        JTextArea instructions = new JTextArea("Use the tabs above to maintain product, supplier and staff records. The Reports tab provides daily sales, item-wise sales, low-stock and expiry reports. Cashiers receive a separate Point of Sale workspace and cannot access these administrative functions.");
        instructions.setEditable(false); instructions.setLineWrap(true); instructions.setWrapStyleWord(true); instructions.setFont(new Font("SansSerif", Font.PLAIN, 15)); instructions.setBackground(UiStyle.BG); instructions.setBorder(new EmptyBorder(25, 4, 4, 4)); body.add(instructions, BorderLayout.CENTER);
        return body;
    }

    private JPanel card(String heading, String value, String caption) {
        JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBackground(Color.WHITE); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(215,225,235)), new EmptyBorder(18,18,18,18)));
        JLabel h = new JLabel(heading); h.setFont(UiStyle.SUBTITLE); JLabel v = new JLabel(value); v.setFont(new Font("SansSerif", Font.BOLD, 30)); v.setForeground(UiStyle.BLUE); JLabel c = new JLabel(caption); c.setForeground(Color.DARK_GRAY);
        card.add(h); card.add(Box.createVerticalStrut(12)); card.add(v); card.add(Box.createVerticalStrut(7)); card.add(c); return card;
    }
}
