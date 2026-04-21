package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.LabTechnician;
import ie.setu.hcs.service.AdminService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminLabTechniciansFrame extends JFrame {
    private static final String[] LAB_OPTIONS = {
            "Clinical Chemistry Lab",
            "Hematology Lab",
            "Microbiology Lab",
            "Immunology Lab",
            "Biochemistry Lab",
            "Pathology Lab"
    };
    private static final String[] SHIFT_OPTIONS = {
            "Morning (06:00-14:00)",
            "Afternoon (14:00-22:00)",
            "Night (22:00-06:00)"
    };

    private final Account adminAccount;
    private final AdminService service = new AdminService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JTextField txtAccount = new JTextField();
    private final JTextField txtEmployeeNum = new JTextField();
    private final JTextField txtQualification = new JTextField();
    private final JComboBox<String> cmbLabName = new JComboBox<>(LAB_OPTIONS);
    private final JComboBox<String> cmbShift = new JComboBox<>(SHIFT_OPTIONS);

    public AdminLabTechniciansFrame(Account adminAccount) {
        this.adminAccount = adminAccount;
        initUI();
        loadTable();
    }

    private void initUI() {
        setTitle("Lab Technicians");
        setSize(980, 760);
        setMinimumSize(new Dimension(900, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader("Lab Technicians", "Review lab staff profiles and update selected technician details"), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(createForm());
        page.add(Box.createVerticalStrut(18));
        page.add(UIHelper.tableAlignedSection(UIHelper.tableSearchBar(table, "Search Lab Technicians")));
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
        UIHelper.styleField(txtQualification);
        UIHelper.styleCombo(cmbLabName);
        UIHelper.styleCombo(cmbShift);
        txtAccount.setEditable(false);

        UIHelper.addFormRow(form, gbc, row++, "Account", txtAccount);
        UIHelper.addFormRow(form, gbc, row++, "Employee Number", txtEmployeeNum);
        UIHelper.addFormRow(form, gbc, row++, "Qualification", txtQualification);
        UIHelper.addFormRow(form, gbc, row++, "Lab Name", cmbLabName);
        UIHelper.addFormRow(form, gbc, row, "Shift", cmbShift);

        JPanel buttons = UIHelper.actionBar();
        JButton update = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
        update.addActionListener(e -> updateTechnician());
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> loadTable());
        JButton view = UIHelper.detailsButton(this, table, "Lab Technician Details");
        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, new AdminDashboard(adminAccount)));

        buttons.add(update);
        buttons.add(refresh);
        buttons.add(view);
        buttons.add(close);

        shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        shell.add(UIHelper.tableAlignedSection(buttons), BorderLayout.SOUTH);
        return shell;
    }

    private void loadTable() {
        try {
            table.setModel(service.getTechniciansForManagement());
            UIHelper.hideColumns(table, "technician_id");
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void populateFromSelection() {
        try {
            LabTechnician technician = service.findTechnicianById(UIHelper.selectedId(table, "technician_id"));
            if (technician == null) {
                return;
            }

            Account account = service.findAccountById(technician.getAccountId());
            txtAccount.setText(account == null
                    ? "Account #" + technician.getAccountId()
                    : account.getFirstName() + " " + account.getLastName() + " (" + account.getEmail() + ")");
            txtEmployeeNum.setText(technician.getEmployeeNum() == null ? "" : technician.getEmployeeNum());
            txtQualification.setText(technician.getQualification() == null ? "" : technician.getQualification());
            cmbLabName.setSelectedItem(technician.getLabName());
            cmbShift.setSelectedItem(technician.getShift());
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateTechnician() {
        try {
            service.updateTechnician(
                    UIHelper.selectedId(table, "technician_id"),
                    txtEmployeeNum.getText(),
                    txtQualification.getText(),
                    String.valueOf(cmbLabName.getSelectedItem()),
                    String.valueOf(cmbShift.getSelectedItem())
            );
            loadTable();
            JOptionPane.showMessageDialog(this, "Lab technician updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

}
