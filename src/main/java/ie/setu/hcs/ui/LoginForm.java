package ie.setu.hcs.ui;

import ie.setu.hcs.controller.LoginController;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.RoundedButton;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class LoginForm extends JFrame {
    private final JTextField txtEmail = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();

    private final JButton btnLogin = new RoundedButton("Continue to workspace", 22);
    private final JButton btnBack = new RoundedButton("Back", 22);

    private final LoginController controller;
    private final JFrame navigationOwner;
    private final Runnable backAction;
    private final Consumer<JFrame> dashboardAction;

    public LoginForm() {
        this(null, null, null);
    }

    public LoginForm(JFrame navigationOwner, Runnable backAction) {
        this(navigationOwner, backAction, null);
    }

    public LoginForm(JFrame navigationOwner, Runnable backAction, Consumer<JFrame> dashboardAction) {
        this.navigationOwner = navigationOwner == null ? this : navigationOwner;
        this.backAction = backAction == null
                ? () -> AppNavigator.replace(this, new MainForm())
                : backAction;
        this.dashboardAction = dashboardAction;
        this.controller = new LoginController(this);
        initUI();
    }

    public void initUI() {
        setTitle("Hospital Consultation System - Log in");
        setSize(860, 680);
        setMinimumSize(new Dimension(700, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        JPanel root = UIHelper.appBackground(new BorderLayout(0, 20));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(UIHelper.pageHeader("System login", "Secure sign-in for patient, doctor, admin, and laboratory workspaces."), BorderLayout.NORTH);
        root.add(createContentPanel(), BorderLayout.CENTER);
        root.add(createButtonPanel(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JComponent createContentPanel() {
        JPanel shell = UIHelper.pageBody(new GridLayout(1, 2, 18, 18));

        shell.add(UIHelper.spotlightCard(
                "Secure Access",
                "One sign-in entry point for every hospital role",
                "Use your registered email and password to continue into the care workspace that matches your account. The refreshed layout keeps the login journey calmer and easier to scan.",
                HCS_Colors.PRIMARY_TEAL_SOFT
        ));

        JPanel formCard = UIHelper.roundedPanel(new BorderLayout(0, 18), HCS_Colors.SURFACE);
        formCard.setBorder(new EmptyBorder(26, 26, 26, 26));

        JPanel heading = new JPanel(new BorderLayout(26, 0));
        heading.setOpaque(false);

        JPanel headingText = new JPanel();
        headingText.setOpaque(false);
        headingText.setLayout(new BoxLayout(headingText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome back");
        title.setFont(UIHelper.font(Font.BOLD, 24));
        title.setForeground(HCS_Colors.TEXT_DARK);

        JLabel subtitle = new JLabel("<html><div style='width:300px'>Enter your credentials to open your hospital consultation workspace.</div></html>");
        subtitle.setFont(UIHelper.font(Font.PLAIN, 13));
        subtitle.setForeground(HCS_Colors.TEXT_MUTED);

        headingText.add(title);
        headingText.add(Box.createVerticalStrut(8));
        headingText.add(subtitle);

        JPanel headingIcon = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        headingIcon.setOpaque(false);
        headingIcon.add(UIHelper.roundedLogoLabel("/images/hospital_logo.png", 136));

        heading.add(headingText, BorderLayout.CENTER);
        heading.add(headingIcon, BorderLayout.EAST);

        JPanel form = UIHelper.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 16, 14, 16);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UIHelper.styleField(txtEmail);
        UIHelper.stylePassword(txtPassword);

        addRow(form, gbc, 0, "Email", txtEmail);
        addRow(form, gbc, 1, "Password", txtPassword);

        JPanel reassurance = new JPanel();
        reassurance.setOpaque(false);
        reassurance.setLayout(new BoxLayout(reassurance, BoxLayout.Y_AXIS));
        JLabel helper = new JLabel("<html><div style='width:320px'>If you do not have an account yet, return to the landing screen and choose the correct registration path for your role.</div></html>");
        helper.setFont(UIHelper.font(Font.PLAIN, 12));
        helper.setForeground(HCS_Colors.TEXT_SOFT);
        reassurance.add(helper);

        formCard.add(heading, BorderLayout.NORTH);
        formCard.add(form, BorderLayout.CENTER);
        formCard.add(reassurance, BorderLayout.SOUTH);
        shell.add(formCard);
        return shell;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(UIHelper.label(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = UIHelper.actionBar();
        ((FlowLayout) buttonPanel.getLayout()).setAlignment(FlowLayout.CENTER);

        setupButton(btnLogin, HCS_Colors.BUTTON_GREEN, null);
        setupButton(btnBack, HCS_Colors.SURFACE, "ghost");

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnBack);

        btnLogin.addActionListener(new LoginHandler());
        btnBack.addActionListener(e -> backAction.run());
        return buttonPanel;
    }

    private void setupButton(JButton button, Color background, String style) {
        button.setPreferredSize(new Dimension(190, 44));
        button.setBackground(background);
        button.setFont(UIHelper.font(Font.BOLD, 13));
        if (style != null) {
            button.putClientProperty("buttonStyle", style);
            button.setForeground(HCS_Colors.TEXT_DARK);
        } else {
            button.setForeground(Color.WHITE);
        }
    }

    public String getEmail() {
        return txtEmail.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    public void navigateToDashboard(JFrame dashboard) {
        if (dashboardAction != null) {
            dashboardAction.accept(dashboard);
            return;
        }
        AppNavigator.replace(navigationOwner, dashboard);
    }

    private class LoginHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                controller.handleLogin();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }
}
