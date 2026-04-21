package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Invoice;
import ie.setu.hcs.service.InvoiceService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InvoiceFrame extends JFrame {
    public enum Mode {
        PATIENT, ADMIN
    }

    private final Account account;
    private final Mode mode;
    private final InvoiceService service = new InvoiceService();
    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JTextField txtPatient = new JTextField();
    private final JTextField txtConsultation = new JTextField();
    private final JTextField txtAmount = new JTextField();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[] {
            InvoiceService.STATUS_UNPAID,
            InvoiceService.STATUS_PAID
    });
    private final JTextField txtIssuedAt = new JTextField();
    private final JTextField txtPaidAt = new JTextField();
    private JButton btnUpdate;

    public InvoiceFrame(Account account, Mode mode) {
        this.account = account;
        this.mode = mode;
        initUI();
        loadTable();
    }

    private void initUI() {
        setTitle(title());
        setSize(mode == Mode.ADMIN ? 980 : 900, mode == Mode.ADMIN ? 760 : 600);
        setMinimumSize(new Dimension(800, mode == Mode.ADMIN ? 680 : 540));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader(title(), subtitle()), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        if (mode == Mode.ADMIN) {
            JPanel page = new JPanel();
            page.setOpaque(false);
            page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
            page.add(createAdminForm());
            page.add(Box.createVerticalStrut(18));
            page.add(UIHelper.tableScrollPane(table, 400));
            content.add(UIHelper.scrollablePage(page), BorderLayout.CENTER);

            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    populateFromSelection();
                }
            });
        } else {
            content.add(UIHelper.tableAlignedSection(createActionBar()), BorderLayout.NORTH);
            content.add(UIHelper.tableScrollPane(table), BorderLayout.CENTER);
        }

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createAdminForm() {
        JPanel shell = new JPanel(new BorderLayout(0, 12));
        shell.setOpaque(false);

        JPanel form = UIHelper.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int row = 0;

        UIHelper.styleField(txtPatient);
        UIHelper.styleField(txtConsultation);
        UIHelper.styleField(txtAmount);
        UIHelper.styleCombo(cmbStatus);
        UIHelper.styleField(txtIssuedAt);
        UIHelper.styleField(txtPaidAt);

        txtPatient.setEditable(false);
        txtConsultation.setEditable(false);
        txtIssuedAt.setEditable(false);
        txtPaidAt.setEditable(false);

        UIHelper.addFormRow(form, gbc, row++, "Patient", txtPatient);
        UIHelper.addFormRow(form, gbc, row++, "Consultation Ref", txtConsultation);
        UIHelper.addFormRow(form, gbc, row++, "Amount", txtAmount);
        UIHelper.addFormRow(form, gbc, row++, "Status", cmbStatus);
        UIHelper.addFormRow(form, gbc, row++, "Issued At", txtIssuedAt);
        UIHelper.addFormRow(form, gbc, row, "Paid At", txtPaidAt);

        shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        shell.add(UIHelper.tableAlignedSection(createActionBar()), BorderLayout.SOUTH);
        return shell;
    }

    private JPanel createActionBar() {
        JPanel buttons = UIHelper.actionBar();

        JButton pay = UIHelper.actionButton("Mark Paid", HCS_Colors.ACCENT_GREEN);
        pay.addActionListener(e -> markPaid());
        buttons.add(pay);

        if (mode == Mode.ADMIN) {
            btnUpdate = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
            btnUpdate.addActionListener(e -> updateInvoice());
            btnUpdate.setEnabled(false);
            buttons.add(btnUpdate);
        }

        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> loadTable());
        buttons.add(refresh);
        buttons.add(UIHelper.detailsButton(this, table, title() + " Details"));

        if (mode == Mode.ADMIN) {
            JButton delete = UIHelper.actionButton("Delete", HCS_Colors.ACCENT_RED);
            delete.addActionListener(e -> deleteInvoice());
            buttons.add(delete);
        }

        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, backFrame()));
        buttons.add(close);
        return buttons;
    }

    private void loadTable() {
        try {
            table.setModel(mode == Mode.PATIENT
                    ? service.getInvoicesForPatient(account)
                    : service.getAllInvoices());
            UIHelper.hideColumns(table, "invoice_id");
            if (mode == Mode.ADMIN) {
                clearForm();
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void populateFromSelection() {
        Integer invoiceId = UIHelper.selectedId(table, "invoice_id");
        if (invoiceId == null) {
            clearForm();
            return;
        }

        try {
            Invoice invoice = service.findInvoiceById(invoiceId);
            txtPatient.setText(selectedValue("patient"));
            txtConsultation.setText(selectedConsultationReference());
            txtAmount.setText(invoice.getAmount() == null ? "" : String.valueOf(invoice.getAmount()));
            cmbStatus.setSelectedItem(invoice.getInvoiceStatus() == null
                    ? InvoiceService.STATUS_UNPAID
                    : invoice.getInvoiceStatus().trim().toUpperCase());
            txtIssuedAt.setText(invoice.getIssuedAt() == null ? "" : invoice.getIssuedAt().toString());
            txtPaidAt.setText(invoice.getPaidAt() == null ? "" : invoice.getPaidAt().toString());
            refreshUpdateState();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateInvoice() {
        try {
            service.updateInvoice(
                    UIHelper.selectedId(table, "invoice_id"),
                    Float.parseFloat(txtAmount.getText().trim()),
                    selectedStatus()
            );
            loadTable();
            JOptionPane.showMessageDialog(this, "Invoice updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void markPaid() {
        try {
            service.markAsPaid(UIHelper.selectedId(table, "invoice_id"));
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deleteInvoice() {
        try {
            service.deleteInvoice(UIHelper.selectedId(table, "invoice_id"));
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void clearForm() {
        txtPatient.setText("");
        txtConsultation.setText("");
        txtAmount.setText("");
        cmbStatus.setSelectedItem(InvoiceService.STATUS_UNPAID);
        txtIssuedAt.setText("");
        txtPaidAt.setText("");
        refreshUpdateState();
    }

    private void refreshUpdateState() {
        if (btnUpdate != null) {
            btnUpdate.setEnabled(UIHelper.selectedId(table, "invoice_id") != null);
        }
    }

    private String selectedValue(String columnName) {
        Integer modelRow = UIHelper.selectedModelRow(table);
        if (modelRow == null) {
            return "";
        }

        int column = ((DefaultTableModel) table.getModel()).findColumn(columnName);
        if (column < 0) {
            return "";
        }

        Object value = table.getModel().getValueAt(modelRow, column);
        return value == null ? "" : value.toString();
    }

    private String selectedStatus() {
        Object value = cmbStatus.getSelectedItem();
        return value == null ? InvoiceService.STATUS_UNPAID : value.toString();
    }

    private String selectedConsultationReference() {
        String consultation = selectedValue("consultation");
        String consultationId = selectedValue("consultation_id");
        if (!consultation.isBlank()) {
            return consultation;
        }
        if (!consultationId.isBlank()) {
            return "Consultation #" + consultationId;
        }
        return "No linked consultation";
    }

    private String title() {
        return mode == Mode.PATIENT ? "Invoices" : "Manage Invoices";
    }

    private String subtitle() {
        return mode == Mode.PATIENT
                ? "Review and mark your invoices as paid"
                : "Review billing, update invoice details, and mark invoices as paid";
    }

    private JFrame backFrame() {
        return mode == Mode.PATIENT ? new PatientDashboard(account) : new AdminDashboard(account);
    }
}
