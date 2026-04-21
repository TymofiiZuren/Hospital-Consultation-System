package ie.setu.hcs.ui;

import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.AppointmentService;
import ie.setu.hcs.service.InsuranceService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;

public class InsuranceFrame extends JFrame {
    public enum Mode {
        PATIENT, ADMIN
    }

    private final Account account;
    private final Mode mode;
    private final InsuranceService service = new InsuranceService();
    private final AppointmentService appointmentService = new AppointmentService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JComboBox<AppointmentService.PatientOption> cmbPatient = new JComboBox<>();
    private final JTextField txtProvider = new JTextField();
    private final JTextField txtPolicy = new JTextField();
    private final JTextField txtExpiration = new JTextField(LocalDate.now().plusYears(1).toString());
    private final JTextField txtCardDocument = new JTextField();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
            InsuranceService.STATUS_PENDING_VERIFICATION,
            InsuranceService.STATUS_VERIFIED,
            InsuranceService.STATUS_FAILED
    });
    private JButton btnUpdate;

    public InsuranceFrame(Account account, Mode mode) {
        this.account = account;
        this.mode = mode;
        initUI();
        loadPatients();
        loadTable();
    }

    private void initUI() {
        setTitle(title());
        setSize(mode == Mode.ADMIN ? 980 : 940, mode == Mode.ADMIN ? 760 : 650);
        setMinimumSize(new Dimension(840, mode == Mode.ADMIN ? 680 : 580));
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
            page.add(createForm());
            page.add(Box.createVerticalStrut(18));
            page.add(UIHelper.tableAlignedSection(UIHelper.tableSearchBar(table, "Search Insurance")));
            page.add(Box.createVerticalStrut(12));
            page.add(UIHelper.tableScrollPane(table, 380));
            content.add(UIHelper.scrollablePage(page), BorderLayout.CENTER);
        } else {
            content.add(createForm(), BorderLayout.NORTH);
            content.add(UIHelper.tableScrollPane(table), BorderLayout.CENTER);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFromSelection();
                refreshUpdateState();
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

        UIHelper.styleCombo(cmbPatient);
        UIHelper.styleField(txtProvider);
        UIHelper.styleField(txtPolicy);
        UIHelper.styleField(txtExpiration);
        UIHelper.styleField(txtCardDocument);
        UIHelper.styleCombo(cmbStatus);
        txtCardDocument.setToolTipText("Paste an image URL or browse for a local insurance card file.");

        if (mode == Mode.ADMIN) {
            UIHelper.addFormRow(form, gbc, row++, "Patient", cmbPatient);
        }
        UIHelper.addFormRow(form, gbc, row++, mode == Mode.PATIENT ? "Insurance Provider" : "Provider", txtProvider);
        UIHelper.addFormRow(form, gbc, row++, "Policy Number", txtPolicy);
        UIHelper.addFormRow(form, gbc, row++, mode == Mode.PATIENT ? "Expiration Date (YYYY-MM-DD)" : "Expiration (YYYY-MM-DD)", txtExpiration);
        UIHelper.addFormRow(form, gbc, row++, "Attach Insurance Card", createDocumentInput());
        if (mode == Mode.ADMIN) {
            UIHelper.addFormRow(form, gbc, row, "Status", cmbStatus);
        }

        JPanel buttons = UIHelper.actionBar();

        if (mode == Mode.PATIENT) {
            JButton submit = UIHelper.actionButton("Submit for Verification", HCS_Colors.ACCENT_GREEN);
            submit.addActionListener(e -> submitForVerification());
            buttons.add(submit);
        } else {
            JButton add = UIHelper.actionButton("Add", HCS_Colors.ACCENT_GREEN);
            add.addActionListener(e -> addInsurance());
            buttons.add(add);
        }

        btnUpdate = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
        btnUpdate.addActionListener(e -> updateInsurance());
        btnUpdate.setEnabled(false);
        btnUpdate.setToolTipText(service.canManageRegardlessOfStatus(account)
                ? null
                : "Only pending insurance requests can be updated.");
        JButton delete = UIHelper.actionButton("Delete", HCS_Colors.ACCENT_RED);
        delete.addActionListener(e -> deleteInsurance());
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_GRAY);
        refresh.addActionListener(e -> loadTable());
        JButton view = UIHelper.detailsButton(this, table, title() + " Details");
        JButton download = UIHelper.secondaryButton("Download Card");
        download.addActionListener(e -> downloadCard());
        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, backFrame()));

        buttons.add(btnUpdate);
        buttons.add(delete);
        buttons.add(refresh);
        buttons.add(view);
        buttons.add(download);
        buttons.add(close);

        shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        shell.add(UIHelper.tableAlignedSection(buttons), BorderLayout.SOUTH);
        return shell;
    }

    private JComponent createDocumentInput() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 0));
        wrapper.setOpaque(false);

        JButton upload = UIHelper.secondaryButton("Upload File");
        upload.addActionListener(e -> chooseDocument());

        wrapper.add(txtCardDocument, BorderLayout.CENTER);
        wrapper.add(upload, BorderLayout.EAST);
        return wrapper;
    }

    private void loadPatients() {
        if (mode != Mode.ADMIN) {
            return;
        }

        try {
            cmbPatient.removeAllItems();
            for (AppointmentService.PatientOption option : appointmentService.getPatientOptions()) {
                cmbPatient.addItem(option);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void loadTable() {
        try {
            table.clearSelection();
            table.setModel(mode == Mode.PATIENT
                    ? service.getInsuranceForPatient(account)
                    : service.getAllInsurance());
            UIHelper.hideColumns(table, "insurance_id");
            table.clearSelection();
            refreshUpdateState();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void addInsurance() {
        try {
            if (mode == Mode.PATIENT) {
                submitForVerification();
            } else {
                service.addInsurance(selectedPatientId(), txtProvider.getText(), txtPolicy.getText(),
                        selectedStatus(), parseExpiration(), cardDocumentPath());
                clearForm();
                loadTable();
                JOptionPane.showMessageDialog(this, "Insurance saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateInsurance() {
        try {
            Integer insuranceId = UIHelper.selectedId(table, "insurance_id");
            Integer patientId = mode == Mode.PATIENT ? selectedTablePatientId() : selectedPatientId();
            service.updateInsurance(account, insuranceId, patientId, txtProvider.getText(), txtPolicy.getText(),
                    selectedStatus(), parseExpiration(), cardDocumentPath());
            loadTable();
            JOptionPane.showMessageDialog(this, "Insurance updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deleteInsurance() {
        try {
            service.deleteInsurance(UIHelper.selectedId(table, "insurance_id"));
            clearForm();
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private Integer selectedPatientId() throws Exception {
        AppointmentService.PatientOption option = (AppointmentService.PatientOption) cmbPatient.getSelectedItem();
        if (option == null) {
            throw new ValidationException("Please choose a patient.");
        }
        return option.patientId();
    }

    private Integer selectedTablePatientId() throws Exception {
        var insurance = service.findInsuranceById(UIHelper.selectedId(table, "insurance_id"));
        if (insurance == null) {
            throw new ValidationException("Please select an insurance record first.");
        }
        return insurance.getPatientId();
    }

    private String selectedStatus() {
        Object status = cmbStatus.getSelectedItem();
        return status == null ? InsuranceService.STATUS_PENDING_VERIFICATION : status.toString();
    }

    private LocalDate parseExpiration() {
        return LocalDate.parse(txtExpiration.getText().trim());
    }

    private String cardDocumentPath() {
        String text = txtCardDocument.getText().trim();
        return text.isEmpty() ? null : text;
    }

    private void populateFromSelection() {
        try {
            var insurance = service.findInsuranceById(UIHelper.selectedId(table, "insurance_id"));
            if (insurance == null) {
                return;
            }

            txtProvider.setText(insurance.getProviderName());
            txtPolicy.setText(insurance.getPolicyNum());
            txtExpiration.setText(insurance.getExpirationDate() == null ? "" : insurance.getExpirationDate().toString());
            cmbStatus.setSelectedItem(insurance.getStatus());
            txtCardDocument.setText(insurance.getCardDocumentPath() == null ? "" : insurance.getCardDocumentPath());

            if (mode == Mode.ADMIN) {
                for (int i = 0; i < cmbPatient.getItemCount(); i++) {
                    if (cmbPatient.getItemAt(i).patientId().equals(insurance.getPatientId())) {
                        cmbPatient.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void clearForm() {
        table.clearSelection();
        txtProvider.setText("");
        txtPolicy.setText("");
        txtExpiration.setText(LocalDate.now().plusYears(1).toString());
        cmbStatus.setSelectedIndex(0);
        txtCardDocument.setText("");
        txtProvider.requestFocus();
        refreshUpdateState();
    }

    private void refreshUpdateState() {
        if (btnUpdate == null) {
            return;
        }

        boolean canUpdate = service.canManageRegardlessOfStatus(account) || hasPendingSelection();
        btnUpdate.setEnabled(canUpdate);
        btnUpdate.setToolTipText(canUpdate ? null : "Only pending insurance requests can be updated.");
    }

    private boolean hasPendingSelection() {
        Integer modelRow = UIHelper.selectedModelRow(table);
        if (modelRow == null) {
            return false;
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int statusColumn = model.findColumn("status");
        if (statusColumn < 0) {
            return false;
        }

        Object status = model.getValueAt(modelRow, statusColumn);
        return status != null && InsuranceService.STATUS_PENDING_VERIFICATION.equalsIgnoreCase(status.toString().trim());
    }

    private void chooseDocument() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Insurance Card");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            txtCardDocument.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void downloadCard() {
        try {
            Integer insuranceId = UIHelper.selectedId(table, "insurance_id");
            String sourceLocation = insuranceId == null ? cardDocumentPath() : null;
            if (insuranceId == null && (sourceLocation == null || sourceLocation.isBlank())) {
                throw new ValidationException("Select an insurance record or enter a card path first.");
            }

            JFileChooser chooser = new JFileChooser(defaultDownloadDirectory().toFile());
            chooser.setDialogTitle("Save Insurance Card");
            String suggestedName = insuranceId == null
                    ? service.suggestedCardFileName(sourceLocation, null)
                    : service.suggestedCardFileName(insuranceId);
            chooser.setSelectedFile(new File(defaultDownloadDirectory().toFile(), suggestedName));

            int result = chooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
                return;
            }

            Path savedPath = insuranceId == null
                    ? service.downloadCardDocument(sourceLocation, chooser.getSelectedFile().toPath())
                    : service.downloadCardDocument(insuranceId, chooser.getSelectedFile().toPath());

            JOptionPane.showMessageDialog(
                    this,
                    "Insurance card downloaded to:\n" + savedPath,
                    "Downloaded",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private Path defaultDownloadDirectory() {
        Path downloads = Path.of(System.getProperty("user.home"), "Downloads");
        return downloads.toFile().isDirectory() ? downloads : Path.of(System.getProperty("user.home"));
    }

    private void submitForVerification() {
        try {
            service.submitForVerification(
                    account,
                    txtProvider.getText(),
                    txtPolicy.getText(),
                    parseExpiration(),
                    cardDocumentPath()
            );
            clearForm();
            loadTable();
            JOptionPane.showMessageDialog(
                    this,
                    "Your insurance details have been submitted for verification.",
                    "Submitted",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private String title() {
        return mode == Mode.PATIENT ? "Insurance" : "Manage Insurance";
    }

    private String subtitle() {
        return mode == Mode.PATIENT
                ? "Attach your insurance card and submit cover details for verification"
                : service.canManageRegardlessOfStatus(account)
                ? "Review insurance submissions and update them at any stage as an administrator."
                : "Review insurance submissions and update them while they are still pending";
    }

    private JFrame backFrame() {
        return mode == Mode.PATIENT ? new PatientDashboard(account) : new AdminDashboard(account);
    }
}
