package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.service.AdminService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class AdminPatientsFrame extends JFrame {
    private final Account adminAccount;
    private final AdminService service = new AdminService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JTextField txtAccount = new JTextField();
    private final JTextField txtDateOfBirth = new JTextField();
    private final JTextField txtAddress = new JTextField();
    private final JTextField txtEircode = new JTextField();
    private final JTextField txtBloodType = new JTextField();
    private final JTextField txtMedicalRecordNumber = new JTextField();

    public AdminPatientsFrame(Account adminAccount) {
        this.adminAccount = adminAccount;
        initUI();
        loadTable();
    }

    private void initUI() {
        setTitle("Patients");
        setSize(980, 760);
        setMinimumSize(new Dimension(900, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader("Patients", "Review patient profiles and update the selected patient record"), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(createForm());
        page.add(Box.createVerticalStrut(18));
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
        UIHelper.styleField(txtDateOfBirth);
        UIHelper.styleField(txtAddress);
        UIHelper.styleField(txtEircode);
        UIHelper.styleField(txtBloodType);
        UIHelper.styleField(txtMedicalRecordNumber);
        txtAccount.setEditable(false);

        UIHelper.addFormRow(form, gbc, row++, "Account", txtAccount);
        UIHelper.addFormRow(form, gbc, row++, "Date of Birth (YYYY-MM-DD)", txtDateOfBirth);
        UIHelper.addFormRow(form, gbc, row++, "Address", txtAddress);
        UIHelper.addFormRow(form, gbc, row++, "Eircode", txtEircode);
        UIHelper.addFormRow(form, gbc, row++, "Blood Type", txtBloodType);
        UIHelper.addFormRow(form, gbc, row, "Medical Record No.", txtMedicalRecordNumber);

        JPanel buttons = UIHelper.actionBar();
        JButton update = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
        update.addActionListener(e -> updatePatient());
        JButton delete = UIHelper.actionButton("Delete", HCS_Colors.ACCENT_RED);
        delete.addActionListener(e -> deletePatient());
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> loadTable());
        JButton view = UIHelper.detailsButton(this, table, "Patient Details");
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
            table.setModel(service.getPatientsForManagement());
            UIHelper.hideColumns(table, "patient_id");
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void populateFromSelection() {
        try {
            Patient patient = service.findPatientById(UIHelper.selectedId(table, "patient_id"));
            if (patient == null) {
                return;
            }

            Account account = service.findAccountById(patient.getAccountId());
            txtAccount.setText(account == null
                    ? "Account #" + patient.getAccountId()
                    : account.getFirstName() + " " + account.getLastName() + " (" + account.getEmail() + ")");
            txtDateOfBirth.setText(patient.getDateOfBirth() == null ? "" : patient.getDateOfBirth().toString());
            txtAddress.setText(patient.getAddress() == null ? "" : patient.getAddress());
            txtEircode.setText(patient.getEircode() == null ? "" : patient.getEircode());
            txtBloodType.setText(patient.getBloodType() == null ? "" : patient.getBloodType());
            txtMedicalRecordNumber.setText(patient.getMedicalRecordNum() == null ? "" : patient.getMedicalRecordNum());
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updatePatient() {
        try {
            service.updatePatient(
                    UIHelper.selectedId(table, "patient_id"),
                    LocalDate.parse(txtDateOfBirth.getText().trim()),
                    txtAddress.getText(),
                    txtEircode.getText(),
                    txtBloodType.getText(),
                    txtMedicalRecordNumber.getText()
            );
            loadTable();
            JOptionPane.showMessageDialog(this, "Patient updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deletePatient() {
        try {
            service.deletePatient(UIHelper.selectedId(table, "patient_id"));
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }
}
