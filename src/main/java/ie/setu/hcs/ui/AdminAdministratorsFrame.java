package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Administrator;
import ie.setu.hcs.service.AdminService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class AdminAdministratorsFrame extends JFrame {
    private final Account adminAccount;
    private final AdminService service = new AdminService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JTextField txtAccount = new JTextField();
    private final JTextField txtJobTitle = new JTextField();
    private final JTextField txtEmployeeNum = new JTextField();
    private final JComboBox<String> cmbDepartment = new JComboBox<>();

    public AdminAdministratorsFrame(Account adminAccount) {
        this.adminAccount = adminAccount;
        initUI();
        loadDepartments();
        loadTable();
    }

    private void initUI() {
        setTitle("Administrators");
        setSize(980, 760);
        setMinimumSize(new Dimension(900, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader("Administrators", "Review administrator profiles and update staff details"), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(createForm());
        page.add(Box.createVerticalStrut(18));
        page.add(UIHelper.tableAlignedSection(UIHelper.tableSearchBar(table, "Search Administrators")));
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
        UIHelper.styleField(txtJobTitle);
        UIHelper.styleField(txtEmployeeNum);
        UIHelper.styleCombo(cmbDepartment);
        txtAccount.setEditable(false);

        UIHelper.addFormRow(form, gbc, row++, "Account", txtAccount);
        UIHelper.addFormRow(form, gbc, row++, "Job Title", txtJobTitle);
        UIHelper.addFormRow(form, gbc, row++, "Employee Number", txtEmployeeNum);
        UIHelper.addFormRow(form, gbc, row, "Department", cmbDepartment);

        JPanel buttons = UIHelper.actionBar();
        JButton update = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
        update.addActionListener(e -> updateAdministrator());
        JButton delete = UIHelper.actionButton("Delete", HCS_Colors.ACCENT_RED);
        delete.addActionListener(e -> deleteAdministrator());
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> loadTable());
        JButton view = UIHelper.detailsButton(this, table, "Administrator Details");
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

    private void loadTable() {
        try {
            table.setModel(service.getAdministratorsForManagement());
            UIHelper.hideColumns(table, "admin_id");
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void populateFromSelection() {
        try {
            Administrator administrator = service.findAdministratorById(UIHelper.selectedId(table, "admin_id"));
            if (administrator == null) {
                return;
            }

            Account account = service.findAccountById(administrator.getAccountId());
            txtAccount.setText(account == null
                    ? "Account #" + administrator.getAccountId()
                    : account.getFirstName() + " " + account.getLastName() + " (" + account.getEmail() + ")");
            txtJobTitle.setText(administrator.getJobTitle() == null ? "" : administrator.getJobTitle());
            txtEmployeeNum.setText(administrator.getEmployeeNum() == null ? "" : administrator.getEmployeeNum());
            String departmentName = service.getDepartmentName(administrator.getDepId());
            if (departmentName != null) {
                cmbDepartment.setSelectedItem(departmentName);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateAdministrator() {
        try {
            service.updateAdministrator(
                    UIHelper.selectedId(table, "admin_id"),
                    txtJobTitle.getText(),
                    txtEmployeeNum.getText(),
                    String.valueOf(cmbDepartment.getSelectedItem())
            );
            loadTable();
            JOptionPane.showMessageDialog(this, "Administrator updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deleteAdministrator() {
        try {
            service.deleteAdministrator(UIHelper.selectedId(table, "admin_id"));
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }
}
