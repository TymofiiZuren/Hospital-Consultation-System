// package ie.setu.hcs.ui
package ie.setu.hcs.ui;

// import stuff
import ie.setu.hcs.controller.PatientRegistrationController;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.RoundedButton;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.JButton;

// PatientRegistrationForm - Professional Patient Registration UI
// Provides a comprehensive form for new patients to register to the hospital system.
public class PatientRegistrationForm extends JFrame {

    // FORM FIELDS

    // personal Information Fields
    private final JTextField txtFirstName = new JTextField();
    private final JTextField txtLastName = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JComboBox<String> cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});

    // contact Information Fields
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtPpsn = new JTextField();
    private final JTextField txtDob = new JTextField("YYYY-MM-DD");
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtEircode = new JTextField();

    // medical Information Fields
    private final JTextField txtBloodType = new JTextField();

    // action Buttons
    private final JButton btnRegister = new RoundedButton("Register", 8);
    private final JButton btnClear = new RoundedButton("Clear", 8);
    private final JButton btnBack = new RoundedButton("Back", 8);

    // controller reference
    private final PatientRegistrationController controller;

    // creating PatientRegistrationForm constructor for the class
    public PatientRegistrationForm() {
        // initializing controller
        this.controller = new PatientRegistrationController(this);

        // initializing UI
        initUI();
    }

    // implement initUI
    public void initUI() {
        // setup frame properties
        setTitle("Hospital Consultation System - Patient Registration");
        setSize(700, 760);
        setMinimumSize(new Dimension(620, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(HCS_Colors.LIGHT_BG);

        // header Panel
        JPanel headerPanel = createHeaderPanel();

        // main Content Panel with scroll
        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(HCS_Colors.LIGHT_BG);
        scrollPane.getViewport().setBackground(HCS_Colors.LIGHT_BG);

        // button Panel
        JPanel buttonPanel = createButtonPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the header panel with title and subtitle
     * Matches the professional header style from MainForm
     */
    private JPanel createHeaderPanel() {
        return UIHelper.pageHeader("Patient Registration", "Create a patient account");
    }

    // create the main content panel with form sections
    private JPanel createContentPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(HCS_Colors.LIGHT_BG);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // personal Information Section
        mainPanel.add(createSectionPanel("Personal Information",
                createPersonalInfoFields()));

        // contact Information Section
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createSectionPanel("Contact Information",
                createContactInfoFields()));

        // medical Information Section
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createSectionPanel("Medical Information",
                createMedicalInfoFields()));

        mainPanel.add(Box.createVerticalGlue());
        return mainPanel;
    }

    // create personal information form fields
    private JPanel createPersonalInfoFields() {
        JPanel panel = UIHelper.roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // setup field styles
        setupTextField(txtFirstName);
        setupTextField(txtLastName);
        setupPasswordField(txtPassword);
        setupComboBox(cmbGender);

        // add fields to panel
        int row = 0;
        addRow(panel, gbc, row++, "First Name:", txtFirstName);
        addRow(panel, gbc, row++, "Last Name:", txtLastName);
        addRow(panel, gbc, row++, "Email:", txtEmail);
        addRow(panel, gbc, row++, "Password:", txtPassword);
        addRow(panel, gbc, row++, "Gender:", cmbGender);

        return panel;
    }

    // create contact information form fields
    private JPanel createContactInfoFields() {
        JPanel panel = UIHelper.roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Setup field styles
        setupTextField(txtEmail);
        setupTextField(txtPhone);
        setupTextField(txtPpsn);
        setupTextField(txtDob);
        setupTextField(txtAddress);
        setupTextField(txtEircode);

        // Add fields to panel
        int row = 0;
        addRow(panel, gbc, row++, "Phone Number:", txtPhone);
        addRow(panel, gbc, row++, "PPSN:", txtPpsn);
        addRow(panel, gbc, row++, "Date of Birth:", txtDob);
        addRow(panel, gbc, row++, "Address:", txtAddress);
        addRow(panel, gbc, row++, "Eircode:", txtEircode);

        return panel;
    }

    // create medical information form fields
    private JPanel createMedicalInfoFields() {
        // create a panel with GridBagLayout for form fields
        JPanel panel = UIHelper.roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // setup field styles
        setupTextField(txtBloodType);

        // add fields to panel
        int row = 0;
        addRow(panel, gbc, row, "Blood Type:", txtBloodType);

        return panel;
    }

    // create a section panel with title and content
    private JPanel createSectionPanel(String title, JPanel content) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBackground(HCS_Colors.LIGHT_BG);
        sectionPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // section title with professional styling
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIHelper.font(Font.BOLD, 15));
        titleLabel.setForeground(HCS_Colors.PRIMARY_TEAL);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 5, 12, 5));

        // content with subtle border
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        sectionPanel.add(titleLabel, BorderLayout.NORTH);
        sectionPanel.add(content, BorderLayout.CENTER);
        sectionPanel.setMaximumSize(new Dimension(580, sectionPanel.getPreferredSize().height));
        sectionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        return sectionPanel;
    }

    // create the button panel with Register, Clear, and Back buttons
    private JPanel createButtonPanel() {
        JPanel buttonPanel = UIHelper.actionBar();
        ((FlowLayout) buttonPanel.getLayout()).setAlignment(FlowLayout.CENTER);
        buttonPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // setup button styles
        setupButton(btnRegister, HCS_Colors.ACCENT_GREEN);
        setupButton(btnClear, HCS_Colors.BUTTON_GRAY);
        setupButton(btnBack, new Color(150, 150, 150));

        // add buttons to panel
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        // setup button actions
        btnRegister.addActionListener(new RegisterHandler());
        btnClear.addActionListener(e -> clearForm());
        btnBack.addActionListener(e -> handleBack());

        return buttonPanel;
    }

    // setup styling for a text field
    private void setupTextField(JTextField field) {
        UIHelper.styleField(field);
        field.setPreferredSize(new Dimension(250, 38));
    }

    // setup styling for a password field
    private void setupPasswordField(JPasswordField field) {
        UIHelper.stylePassword(field);
        field.setPreferredSize(new Dimension(250, 38));
    }

    // setup styling for a combo box
    private void setupComboBox(JComboBox<String> comboBox) {
        UIHelper.styleCombo(comboBox);
        comboBox.setPreferredSize(new Dimension(250, 38));
        comboBox.setMaximumSize(new Dimension(250, 38));
    }

    // setup styling for a button
    private void setupButton(JButton button, Color bgColor) {
        button.setPreferredSize(new Dimension(130, 45));
        button.setFont(UIHelper.font(Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
    }

    // create a label with consistent styling
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIHelper.font(Font.PLAIN, 12));
        label.setForeground(HCS_Colors.LABEL_COLOR);
        return label;
    }

    // add a label and field to the panel in a specific row
    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String labelText, JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(createLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    // clear all forms
    public void clearForm() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtPpsn.setText("");
        txtPhone.setText("");
        cmbGender.setSelectedIndex(0);
        txtDob.setText("YYYY-MM-DD");
        txtAddress.setText("");
        txtEircode.setText("");
        txtBloodType.setText("");
        txtFirstName.requestFocus();
    }

    // handle back button action
    private void handleBack() {
        AppNavigator.replace(this, new MainForm());
    }

    // show warning message dialog
    public void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // show success message dialog
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

//    get first name from the account
    public String getFirstName() {
        return txtFirstName.getText().trim();
    }

//    get last name from the account
    public String getLastName() {
        return txtLastName.getText().trim();
    }

//    get email from the account
    public String getEmail() {
        return txtEmail.getText().trim();
    }

//    get password from the account
    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

//    get ppsn from the account
    public String getPpsn() {
        return txtPpsn.getText().trim();
    }

//    get phone number from the account
    public String getPhone() {
        return txtPhone.getText().trim();
    }

//    get gender from the account
    public String getGender() {
        return cmbGender.getSelectedItem().toString();
    }

//    get date of birth from the account
    public LocalDate getDob() {
        return LocalDate.parse(txtDob.getText().trim());
    }

//    get address from the account
    public String getAddress() {
        return txtAddress.getText().trim();
    }

//    get eircode from the account
    public String getEircode() {
        return txtEircode.getText().trim();
    }

//    get blood type from the account
    public String getBloodType() {
        return txtBloodType.getText().trim();
    }

    // action handler for the Register button
    private class RegisterHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // call controller to handle registration
                controller.handleRegistration();
            } catch (Exception ex) {
                showWarning(ex.getMessage());
            }
        }
    }
}
