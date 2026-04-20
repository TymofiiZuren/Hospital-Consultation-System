package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileFrame extends JFrame {
    private final Account account;

    public ProfileFrame(Account account) {
        this.account = account;
        initUI();
    }

    private void initUI() {
        setTitle("My Profile");
        setSize(720, 520);
        setMinimumSize(new Dimension(620, 440));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader("My Profile", "Your account details"), BorderLayout.NORTH);

        JPanel card = UIHelper.roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 6, 8, 16);

        int row = 0;
        addRow(card, gbc, row++, "Name", account.getFirstName() + " " + account.getLastName());
        addRow(card, gbc, row++, "Email", account.getEmail());
        addRow(card, gbc, row++, "PPSN", account.getPpsn());
        addRow(card, gbc, row++, "Phone", account.getPhone());
        addRow(card, gbc, row++, "Gender", account.getGender());
        addRow(card, gbc, row++, "Role", roleName());
        addRow(card, gbc, row, "Active", Boolean.TRUE.equals(account.isActive()) ? "Yes" : "No");
        Dimension preferred = card.getPreferredSize();
        card.setPreferredSize(new Dimension(Math.max(700, preferred.width), preferred.height));
        card.setMaximumSize(new Dimension(760, Integer.MAX_VALUE));

        JPanel content = UIHelper.pageBody(new BorderLayout());
        content.setBackground(HCS_Colors.LIGHT_BG);
        content.add(UIHelper.compactSection(card), BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);

        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, backFrame()));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 24, 24, 24));
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel labelComponent = UIHelper.label(label + ":");
        labelComponent.setFont(UIHelper.font(Font.BOLD, 12));
        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel valueComponent = new JLabel(value == null ? "" : value);
        valueComponent.setFont(UIHelper.font(Font.PLAIN, 13));
        valueComponent.setForeground(HCS_Colors.TEXT_DARK);
        panel.add(valueComponent, gbc);
    }

    private JFrame backFrame() {
        if (Boolean.TRUE.equals(account.isAdmin()) || Integer.valueOf(4).equals(account.getRoleId())) {
            return new AdminDashboard(account);
        }

        return switch (account.getRoleId()) {
            case 1 -> new PatientDashboard(account);
            case 2 -> new DoctorDashboard(account);
            case 3 -> new LabTechnicianDashboard(account);
            default -> new MainForm();
        };
    }

    private String roleName() {
        if (Boolean.TRUE.equals(account.isAdmin()) || Integer.valueOf(4).equals(account.getRoleId())) {
            return "Administrator";
        }

        return switch (account.getRoleId()) {
            case 1 -> "Patient";
            case 2 -> "Doctor";
            case 3 -> "Lab Technician";
            default -> "Unknown";
        };
    }
}
