package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.service.DoctorManagementService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class AdminDoctorsFrame extends JFrame {
    private final Account adminAccount;
    private final DoctorManagementService service = new DoctorManagementService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JTextField txtAccount = new JTextField();
    private final JTextField txtEmployeeNum = new JTextField();
    private final JTextField txtSpecialization = new JTextField();
    private final JTextField txtLicense = new JTextField();
    private final JTextField txtYears = new JTextField();
    private final JTextField txtFee = new JTextField();
    private final JComboBox<String> cmbDepartment = new JComboBox<>();

    public AdminDoctorsFrame(Account adminAccount) {
        this.adminAccount = adminAccount;
        initUI();
        loadDepartments();
        loadTable();
    }

    private void initUI() {
        setTitle("Doctors");
        setSize(980, 760);
        setMinimumSize(new Dimension(900, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader("Doctors", "Review doctor profiles and update the selected doctor record"), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(createForm());
        page.add(Box.createVerticalStrut(18));
        page.add(UIHelper.tableAlignedSection(UIHelper.tableSearchBar(table, "Search Doctors")));
        page.add(Box.createVerticalStrut(12));
        page.add(UIHelper.tableScrollPane(table, 380));
        content.add(UIHelper.scrollablePage(page), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFromSelection();
            }
        });

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createForm() {
        JPanel shell = new JPanel(new BorderLayout(0, 12));
        shell.setOpaque(false);

        JPanel form = UIHelper.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int row = 0;

        UIHelper.styleField(txtAccount);
        UIHelper.styleField(txtEmployeeNum);
        UIHelper.styleField(txtSpecialization);
        UIHelper.styleField(txtLicense);
        UIHelper.styleField(txtYears);
        UIHelper.styleField(txtFee);
        UIHelper.styleCombo(cmbDepartment);
        txtAccount.setEditable(false);

        UIHelper.addFormRow(form, gbc, row++, "Account", txtAccount);
        UIHelper.addFormRow(form, gbc, row++, "Employee Number", txtEmployeeNum);
        UIHelper.addFormRow(form, gbc, row++, "Specialization", txtSpecialization);
        UIHelper.addFormRow(form, gbc, row++, "License No.", txtLicense);
        UIHelper.addFormRow(form, gbc, row++, "Years of Experience", txtYears);
        UIHelper.addFormRow(form, gbc, row++, "Consultation Fee", txtFee);
        UIHelper.addFormRow(form, gbc, row, "Department", cmbDepartment);

        JPanel buttons = UIHelper.actionBar();
        JButton update = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
        update.addActionListener(e -> updateDoctor());
        JButton delete = UIHelper.actionButton("Delete", HCS_Colors.ACCENT_RED);
        delete.addActionListener(e -> deleteDoctor());
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> loadTable());
        JButton view = UIHelper.detailsButton(this, table, "Doctor Details");
        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, new AdminDashboard(adminAccount)));

        buttons.add(update);
        buttons.add(delete);
        buttons.add(refresh);
        buttons.add(view);
        buttons.add(close);

        shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        shell.add(UIHelper.tableAlignedSection(buttons), BorderLayout.SOUTH);
        return shell;
    }

    private void loadTable() {
        try {
            table.setModel(service.getDoctorsForManagement());
            UIHelper.hideColumns(table, "doctor_id");
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void populateFromSelection() {
        try {
            Doctor doctor = service.findDoctorById(UIHelper.selectedId(table, "doctor_id"));
            if (doctor == null) {
                return;
            }

            Account account = service.findAccountById(doctor.getAccountId());
            txtAccount.setText(account == null
                    ? "Account #" + doctor.getAccountId()
                    : account.getFirstName() + " " + account.getLastName() + " (" + account.getEmail() + ")");
            txtEmployeeNum.setText(doctor.getEmployeeNum() == null ? "" : doctor.getEmployeeNum());
            txtSpecialization.setText(doctor.getSpecialization() == null ? "" : doctor.getSpecialization());
            txtLicense.setText(doctor.getLicenseNum() == null ? "" : doctor.getLicenseNum());
            txtYears.setText(doctor.getYearsOfExperience() == null ? "" : String.valueOf(doctor.getYearsOfExperience()));
            txtFee.setText(doctor.getConsultationFee() == null ? "" : String.valueOf(doctor.getConsultationFee()));
            String departmentName = service.getDepartmentName(doctor.getDepId());
            if (departmentName != null) {
                cmbDepartment.setSelectedItem(departmentName);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateDoctor() {
        try {
            service.updateDoctor(
                    UIHelper.selectedId(table, "doctor_id"),
                    txtSpecialization.getText(),
                    txtLicense.getText(),
                    txtEmployeeNum.getText(),
                    Integer.parseInt(txtYears.getText().trim()),
                    Integer.parseInt(txtFee.getText().trim()),
                    String.valueOf(cmbDepartment.getSelectedItem())
            );
            loadTable();
            JOptionPane.showMessageDialog(this, "Doctor updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deleteDoctor() {
        try {
            service.deleteDoctor(UIHelper.selectedId(table, "doctor_id"));
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void loadDepartments() {
        try {
            cmbDepartment.removeAllItems();
            for (Map.Entry<Integer, String> entry : service.getDepartments().entrySet()) {
                cmbDepartment.addItem(entry.getValue());
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }
}
