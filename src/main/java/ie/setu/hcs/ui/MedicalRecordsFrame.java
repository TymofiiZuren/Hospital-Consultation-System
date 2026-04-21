package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.MedicalRecordService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MedicalRecordsFrame extends JFrame {
    public enum Mode {
        PATIENT, DOCTOR, ADMIN
    }

    private final Account account;
    private final Mode mode;
    private final MedicalRecordService service = new MedicalRecordService();
    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JTextField txtDiagnosis = new JTextField();
    private final JTextArea txtNotes = new JTextArea(4, 24);
    private final JTextArea txtPrescription = new JTextArea(3, 24);

    public MedicalRecordsFrame(Account account, Mode mode) {
        this.account = account;
        this.mode = mode;
        initUI();
        loadTable();
    }

    private void initUI() {
        setTitle(title());
        setSize(980, 700);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader(title(), subtitle()), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);
        content.add(createForm(), BorderLayout.NORTH);
        content.add(UIHelper.tableScrollPane(table, 320), BorderLayout.CENTER);

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

        UIHelper.styleField(txtDiagnosis);
        UIHelper.styleTextArea(txtNotes);
        UIHelper.styleTextArea(txtPrescription);

        boolean editable = mode == Mode.ADMIN;
        txtDiagnosis.setEditable(editable);
        txtDiagnosis.setFocusable(editable);
        txtNotes.setEditable(editable);
        txtNotes.setFocusable(editable);
        txtPrescription.setEditable(editable);
        txtPrescription.setFocusable(editable);

        UIHelper.addFormRow(form, gbc, row++, "Diagnosis", txtDiagnosis);
        if (mode != Mode.PATIENT) {
            UIHelper.addFormRow(form, gbc, row++, "Notes", UIHelper.textAreaScrollPane(txtNotes));
        }
        UIHelper.addFormRow(form, gbc, row, "Prescription", UIHelper.textAreaScrollPane(txtPrescription));

        JPanel buttons = UIHelper.actionBar();
        JButton view = UIHelper.detailsButton(this, table, title() + " Details", "View Details");
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> loadTable());
        JButton back = UIHelper.secondaryButton("Back");
        back.addActionListener(e -> AppNavigator.replace(this, backFrame()));

        if (mode == Mode.ADMIN) {
            JButton update = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
            update.addActionListener(e -> updateRecord());
            JButton clear = UIHelper.secondaryButton("Clear");
            clear.addActionListener(e -> clearForm());
            buttons.add(update);
            buttons.add(clear);
        }

        buttons.add(refresh);
        buttons.add(view);

        if (mode == Mode.ADMIN) {
            JButton delete = UIHelper.actionButton("Delete", HCS_Colors.ACCENT_RED);
            delete.addActionListener(e -> deleteRecord());
            buttons.add(delete);
        }

        buttons.add(back);

        shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        shell.add(UIHelper.tableAlignedSection(buttons), BorderLayout.SOUTH);
        return shell;
    }

    private void loadTable() {
        try {
            table.setModel(switch (mode) {
                case PATIENT -> service.getRecordsForPatient(account);
                case DOCTOR -> service.getRecordsForDoctor(account);
                case ADMIN -> service.getAllRecords();
            });
            UIHelper.hideColumns(table, "record_id", "consultation_id", "appointment_id");
            if (mode == Mode.PATIENT) {
                UIHelper.hideColumns(table, "notes");
            }
            if (table.getRowCount() > 0) {
                table.setRowSelectionInterval(0, 0);
            } else {
                clearForm();
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void populateFromSelection() {
        Integer row = UIHelper.selectedModelRow(table);
        if (row == null) {
            return;
        }
        txtDiagnosis.setText(value(row, "diagnosis"));
        if (mode != Mode.PATIENT) {
            txtNotes.setText(value(row, "notes"));
        } else {
            txtNotes.setText("");
        }
        txtPrescription.setText(value(row, "prescription"));
    }

    private void updateRecord() {
        try {
            service.updateRecord(
                    UIHelper.selectedId(table, "record_id"),
                    txtDiagnosis.getText(),
                    txtNotes.getText(),
                    txtPrescription.getText()
            );
            loadTable();
            JOptionPane.showMessageDialog(this, "Medical record updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deleteRecord() {
        try {
            service.deleteRecord(UIHelper.selectedId(table, "record_id"));
            clearForm();
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void clearForm() {
        table.clearSelection();
        txtDiagnosis.setText("");
        txtNotes.setText("");
        txtPrescription.setText("");
        txtDiagnosis.requestFocus();
    }

    private String value(int row, String columnName) {
        int column = ((DefaultTableModel) table.getModel()).findColumn(columnName);
        if (column < 0) {
            return "";
        }
        Object value = table.getModel().getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private String title() {
        return switch (mode) {
            case PATIENT -> "Medical Records";
            case DOCTOR -> "Patient Medical Records";
            case ADMIN -> "Manage Medical Records";
        };
    }

    private String subtitle() {
        return switch (mode) {
            case PATIENT -> "Read diagnosis and prescriptions from your visit history";
            case DOCTOR -> "Review diagnosis history, notes, and prescriptions for your patients";
            case ADMIN -> "Review, update, and remove stored medical records";
        };
    }

    private JFrame backFrame() {
        return switch (mode) {
            case PATIENT -> new PatientDashboard(account);
            case DOCTOR -> new DoctorDashboard(account);
            case ADMIN -> new AdminDashboard(account);
        };
    }
}
