// package ie.setu.hcs.ui
package ie.setu.hcs.ui;

// import stuff
import ie.setu.hcs.controller.DoctorRegistrationController;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.RoundedButton;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// DoctorRegistrationForm - Professional Doctor Registration UI
// Provides a comprehensive form for doctors to register to the hospital system.
public class DoctorRegistrationForm extends JFrame {

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

    // professional Information Fields
    private final JTextField txtEmployeeNum = new JTextField();
    private final JTextField txtMedicalLicense = new JTextField();
    private final JTextField txtYearsExperience = new JTextField();

    // specialization Fields
    private final JComboBox<String> cmbDepartment = new JComboBox<>(new String[]{
            "Cardiology", "Neurology", "Orthopedics", "Pediatrics",
            "Psychiatry", "Surgery", "General Practice", "Emergency Medicine"
    });
    private final JTextField txtSpecialization = new JTextField();

    // action buttons
    private final JButton btnRegister = new RoundedButton("Register", 8);
    private final JButton btnClear = new RoundedButton("Clear", 8);
    private final JButton btnBack = new RoundedButton("Back", 8);

    // controller reference
    private final DoctorRegistrationController controller;

    // creating constructor for the class
    public DoctorRegistrationForm() {
        // initialize controller
        this.controller = new DoctorRegistrationController(this);

        // initialize UI
        initUI();
    }

    public void initUI() {
        // setup frame properties
        setTitle("Hospital Consultation System - Doctor Registration");
        setSize(700, 820);
        setMinimumSize(new Dimension(620, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        setLayout(new BorderLayout());
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

    private JPanel createHeaderPanel() {
        return UIHelper.pageHeader("Doctor Registration", "Create a doctor account");
    }

    private JPanel createContentPanel() {
        // creating main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(HCS_Colors.LIGHT_BG);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // personal Information Section
        mainPanel.add(createSectionPanel("Personal Information",
                createPersonalInfoFields()));

        // contact Information Section
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createSectionPanel("Contact Information",
                createContactInfoFields()));

        // professional Information Section
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createSectionPanel("Professional Information",
                createProfessionalInfoFields()));

        // specialization Section
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createSectionPanel("Specialization",
                createSpecializationFields()));

        // add vertical glue to push content to top
        mainPanel.add(Box.createVerticalGlue());
        return mainPanel;
    }

    // creating method to create personal info fields panel
    private JPanel createPersonalInfoFields() {
        // creating the grid layout panel for personal info fields
        JPanel panel = UIHelper.roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 30);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // setup fields
        setupTextField(txtFirstName);
        setupTextField(txtLastName);
        setupComboBox(cmbGender);
        setupTextField(txtEmail);
        setupPasswordField(txtPassword);

        // adding fields to panel
        int row = 0;
        addRow(panel, gbc, row++, "First Name:", txtFirstName);
        addRow(panel, gbc, row++, "Last Name:", txtLastName);
        addRow(panel, gbc, row++, "Email:", txtEmail);
        addRow(panel, gbc, row++, "Password:", txtPassword);
        addRow(panel, gbc, row++, "Gender:", cmbGender);

        return panel;
    }

    private JPanel createContactInfoFields() {
        // creating the grid layout panel for contact info fields
        JPanel panel = UIHelper.roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 30);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // setup fields
        setupTextField(txtPhone);
        setupTextField(txtPpsn);

        // adding fields to panel
        int row = 0;
        addRow(panel, gbc, row++, "Phone Number:", txtPhone);
        addRow(panel, gbc, row++, "PPSN:", txtPpsn);

        return panel;
    }

    private JPanel createProfessionalInfoFields() {
        // creating the grid layout panel for professional info fields
        JPanel panel = UIHelper.roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 30);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // setup fields
        setupTextField(txtEmployeeNum);
        setupTextField(txtMedicalLicense);
        setupTextField(txtYearsExperience);

        // adding fields to panel
        int row = 0;
        addRow(panel, gbc, row++, "Employee Number:", txtEmployeeNum);
        addRow(panel, gbc, row++, "Medical License Number:", txtMedicalLicense);
        addRow(panel, gbc, row++, "Years of Experience:", txtYearsExperience);

        return panel;
    }

    private JPanel createSpecializationFields() {
        // creating the grid layout panel for specialization fields
        JPanel panel = UIHelper.roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 30);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // setup fields
        setupComboBox(cmbDepartment);
        setupTextField(txtSpecialization);

        // department dropdown
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel deptLabel = createLabel("Department:");
        panel.add(deptLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(cmbDepartment, gbc);

        // specialization field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        addRow(panel, gbc, 1, "Specialization:", txtSpecialization);

        return panel;
    }

    private JPanel createSectionPanel(String title, JPanel content) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBackground(HCS_Colors.LIGHT_BG);
        sectionPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIHelper.font(Font.BOLD, 14));
        titleLabel.setForeground(HCS_Colors.PRIMARY_TEAL);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(10, 5, 10, 5));

        // content with border
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        sectionPanel.add(titleLabel, BorderLayout.NORTH);
        sectionPanel.add(content, BorderLayout.CENTER);
        sectionPanel.setMaximumSize(new Dimension(580, sectionPanel.getPreferredSize().height));
        sectionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        return sectionPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = UIHelper.actionBar();
        ((FlowLayout) buttonPanel.getLayout()).setAlignment(FlowLayout.CENTER);
        buttonPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // setup buttons
        setupButton(btnRegister, HCS_Colors.ACCENT_GREEN);
        setupButton(btnClear, HCS_Colors.BUTTON_GRAY);
        setupButton(btnBack, new Color(150, 150, 150));

        // adding buttons to panel
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnBack);

        // add action listeners
        btnRegister.addActionListener(new RegisterHandler());
        btnClear.addActionListener((e) -> clearForm());
        btnBack.addActionListener((e) -> handleBack());

        return buttonPanel;
    }

    private void setupTextField(JTextField field) {
        UIHelper.styleField(field);
        field.setPreferredSize(new Dimension(250, 38));
    }

    private void setupPasswordField(JPasswordField field) {
        UIHelper.stylePassword(field);
        field.setPreferredSize(new Dimension(250, 38));
    }

    private void setupComboBox(JComboBox<String> comboBox) {
        UIHelper.styleCombo(comboBox);
        comboBox.setPreferredSize(new Dimension(250, 38));
        comboBox.setMaximumSize(new Dimension(250, 38));
    }

    // method to setup buttons
    private void setupButton(JButton button, Color bgColor) {
        // setting properties for button
        button.setPreferredSize(new Dimension(120, 40));
        button.setFont(UIHelper.font(Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
    }

    // method to create labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIHelper.font(Font.PLAIN, 12));
        label.setForeground(HCS_Colors.LABEL_COLOR);
        return label;
    }

    // method to add a row
    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String labelText, JComponent field) {

        // adding label and field to the panel with grid bag constraints
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(createLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    public void clearForm() {
        // clearing all form fields to default values
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtPhone.setText("");
        txtEmployeeNum.setText("");
        txtMedicalLicense.setText("");
        txtYearsExperience.setText("");
        txtSpecialization.setText("");
        txtPpsn.setText("");
        cmbDepartment.setSelectedIndex(0);
        cmbGender.setSelectedIndex(0);
        txtFirstName.requestFocus();
    }

    private void handleBack() {
        AppNavigator.replace(this, new MainForm());
    }

    public void showWarning(String message) {
        // warning message dialog
        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public void showSuccess(String message) {
        // success message dialog
        JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // get first name from the account
    public String getFirstName() {
        return txtFirstName.getText().trim();
    }

    // get last name from the account
    public String getLastName() {
        return txtLastName.getText().trim();
    }

    // get email from the account
    public String getEmail() {
        return txtEmail.getText().trim();
    }

    // get password from the account
    public String getPassword() {
        return new String(txtPassword.getPassword()).trim();
    }

    // get phone from the account
    public String getPhone() {
        return txtPhone.getText().trim();
    }

    // get medical license from the account
    public String getMedicalLicense() {
        return txtMedicalLicense.getText().trim();
    }

    public String getEmployeeNum() {
        return txtEmployeeNum.getText().trim();
    }

    // get years experience from the account
    public String getYearsExperience() {
        return txtYearsExperience.getText().trim();
    }

    // get specialization from the account
    public String getSpecialization() {
        return txtSpecialization.getText().trim();
    }

    // get ppsn from the account
    public String getPpsn() {
        return txtPpsn.getText().trim();
    }

    // get department from the account
    public String getDepartment() {
        return cmbDepartment.getSelectedItem().toString();
    }

    // get gender from the account
    public String getGender() {
        return cmbGender.getSelectedItem().toString();
    }

    // nested event handler
    private class RegisterHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // call controller to handle registration logic
                controller.handleRegistration();
            } catch (Exception ex) {
                showWarning(ex.getMessage());
            }
        }
    }
}
