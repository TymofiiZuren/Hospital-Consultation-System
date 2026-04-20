package ie.setu.hcs.ui;

import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.AdminService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminAccountsFrame extends JFrame {
    private final Account adminAccount;
    private final AdminService service = new AdminService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtFirstName = new JTextField();
    private final JTextField txtLastName = new JTextField();
    private final JTextField txtPpsn = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JTextField txtGender = new JTextField();
    private final JComboBox<RoleOption> cmbRole = new JComboBox<>();
    private final JCheckBox chkActive = new JCheckBox("Active");
    private final JCheckBox chkAdmin = new JCheckBox("Admin");

    public AdminAccountsFrame(Account adminAccount) {
        this.adminAccount = adminAccount;
        initUI();
        loadRoles();
        loadTable();
    }

    private void initUI() {
        setTitle("Accounts");
        setSize(980, 760);
        setMinimumSize(new Dimension(900, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader("Accounts", "Review, view, and update system login accounts"), BorderLayout.NORTH);

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

        UIHelper.styleField(txtEmail);
        UIHelper.styleField(txtFirstName);
        UIHelper.styleField(txtLastName);
        UIHelper.styleField(txtPpsn);
        UIHelper.styleField(txtPhone);
        UIHelper.styleField(txtGender);
        UIHelper.styleCombo(cmbRole);

        UIHelper.addFormRow(form, gbc, row++, "Email", txtEmail);
        UIHelper.addFormRow(form, gbc, row++, "First Name", txtFirstName);
        UIHelper.addFormRow(form, gbc, row++, "Last Name", txtLastName);
        UIHelper.addFormRow(form, gbc, row++, "Role", cmbRole);
        UIHelper.addFormRow(form, gbc, row++, "PPSN", txtPpsn);
        UIHelper.addFormRow(form, gbc, row++, "Phone", txtPhone);
        UIHelper.addFormRow(form, gbc, row++, "Gender", txtGender);
        UIHelper.addFormRow(form, gbc, row, "Flags", flagsPanel());

        JPanel buttons = UIHelper.actionBar();
        JButton update = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
        update.addActionListener(e -> updateAccount());
        JButton deactivate = UIHelper.actionButton("Deactivate", HCS_Colors.BUTTON_GRAY);
        deactivate.addActionListener(e -> deactivateAccount());
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> loadTable());
        JButton view = UIHelper.detailsButton(this, table, "Account Details");
        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, new AdminDashboard(adminAccount)));

        buttons.add(update);
        buttons.add(deactivate);
        buttons.add(refresh);
        buttons.add(view);
        buttons.add(close);

        shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        shell.add(UIHelper.tableAlignedSection(buttons), BorderLayout.SOUTH);
        return shell;
    }

    private JPanel flagsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panel.setOpaque(false);
        chkActive.setOpaque(false);
        chkAdmin.setOpaque(false);
        panel.add(chkActive);
        panel.add(chkAdmin);
        return panel;
    }

    private void loadRoles() {
        cmbRole.removeAllItems();
        cmbRole.addItem(new RoleOption(1, "Patient"));
        cmbRole.addItem(new RoleOption(2, "Doctor"));
        cmbRole.addItem(new RoleOption(3, "Lab Technician"));
        cmbRole.addItem(new RoleOption(4, "Admin"));
    }

    private void loadTable() {
        try {
            table.setModel(service.getAccountsForManagement());
            UIHelper.hideColumns(table, "account_id");
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void populateFromSelection() {
        try {
            Account account = service.findAccountById(UIHelper.selectedId(table, "account_id"));
            if (account == null) {
                return;
            }

            txtEmail.setText(account.getEmail());
            txtFirstName.setText(account.getFirstName());
            txtLastName.setText(account.getLastName());
            txtPpsn.setText(account.getPpsn() == null ? "" : account.getPpsn());
            txtPhone.setText(account.getPhone() == null ? "" : account.getPhone());
            txtGender.setText(account.getGender() == null ? "" : account.getGender());
            chkActive.setSelected(Boolean.TRUE.equals(account.isActive()));
            chkAdmin.setSelected(Boolean.TRUE.equals(account.isAdmin()));
            selectRole(account.getRoleId());
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateAccount() {
        try {
            service.updateAccount(
                    UIHelper.selectedId(table, "account_id"),
                    txtEmail.getText(),
                    selectedRoleId(),
                    txtFirstName.getText(),
                    txtLastName.getText(),
                    txtPpsn.getText(),
                    txtPhone.getText(),
                    txtGender.getText(),
                    chkActive.isSelected(),
                    chkAdmin.isSelected()
            );
            loadTable();
            JOptionPane.showMessageDialog(this, "Account updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deactivateAccount() {
        try {
            service.deactivateAccount(UIHelper.selectedId(table, "account_id"));
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private Integer selectedRoleId() throws Exception {
        RoleOption option = (RoleOption) cmbRole.getSelectedItem();
        if (option == null) {
            throw new ValidationException("Please choose a role.");
        }
        return option.roleId();
    }

    private void selectRole(Integer roleId) {
        if (roleId == null) {
            return;
        }
        for (int i = 0; i < cmbRole.getItemCount(); i++) {
            RoleOption option = cmbRole.getItemAt(i);
            if (roleId.equals(option.roleId())) {
                cmbRole.setSelectedIndex(i);
                return;
            }
        }
    }

    private record RoleOption(Integer roleId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
