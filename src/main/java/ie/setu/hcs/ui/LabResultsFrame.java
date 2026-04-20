package ie.setu.hcs.ui;

import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.LabService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LabResultsFrame extends JFrame {
    public enum Mode {
        PATIENT, DOCTOR, TECHNICIAN, ADMIN
    }

    private final Account account;
    private final Mode mode;
    private final LabService service = new LabService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JComboBox<LabService.ResultSourceOption> cmbSource = new JComboBox<>();
    private final JComboBox<LabService.TechnicianOption> cmbTechnician = new JComboBox<>();
    private final JTextField txtTestType = new JTextField();
    private final JTextArea txtResult = new JTextArea(4, 24);

    public LabResultsFrame(Account account, Mode mode) {
        this.account = account;
        this.mode = mode;
        initUI();
        loadConsultations();
        loadTechnicians();
        loadTable();
    }

    private void initUI() {
        setTitle(title());
        setSize(940, 650);
        setMinimumSize(new Dimension(840, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader(title(), subtitle()), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        if (mode == Mode.TECHNICIAN || mode == Mode.ADMIN) {
            content.add(createForm(), BorderLayout.NORTH);
        }

        content.add(UIHelper.tableScrollPane(table), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFromSelection();
            }
        });

        if (mode == Mode.PATIENT || mode == Mode.DOCTOR) {
            JPanel buttons = UIHelper.actionBar();
            JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
            refresh.addActionListener(e -> loadTable());
            JButton view = UIHelper.detailsButton(this, table, title() + " Details");
            JButton close = UIHelper.secondaryButton("Back");
            close.addActionListener(e -> AppNavigator.replace(this, backFrame()));
            buttons.add(refresh);
            buttons.add(view);
            buttons.add(close);
            content.add(UIHelper.tableAlignedSection(buttons), BorderLayout.NORTH);
        }

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createForm() {
        JPanel shell = new JPanel(new BorderLayout(0, 12));
        shell.setOpaque(false);

        JPanel form = UIHelper.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int row = 0;

        UIHelper.styleCombo(cmbSource);
        UIHelper.styleCombo(cmbTechnician);
        UIHelper.styleField(txtTestType);
        UIHelper.styleTextArea(txtResult);

        UIHelper.addFormRow(form, gbc, row++, "Source", cmbSource);
        if (mode == Mode.ADMIN) {
            UIHelper.addFormRow(form, gbc, row++, "Technician", cmbTechnician);
        }
        UIHelper.addFormRow(form, gbc, row++, "Test Type", txtTestType);
        UIHelper.addFormRow(form, gbc, row, "Result", UIHelper.textAreaScrollPane(txtResult));

        JPanel buttons = UIHelper.actionBar();
        JButton save = UIHelper.actionButton("Save Result", HCS_Colors.ACCENT_GREEN);
        save.addActionListener(e -> saveResult());
        JButton update = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
        update.addActionListener(e -> updateResult());
        JButton delete = UIHelper.actionButton("Delete", HCS_Colors.ACCENT_RED);
        delete.addActionListener(e -> deleteResult());
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> {
            loadConsultations();
            loadTechnicians();
            loadTable();
        });
        JButton view = UIHelper.detailsButton(this, table, title() + " Details");
        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, backFrame()));

        buttons.add(save);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(refresh);
        buttons.add(view);
        buttons.add(close);

        shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        shell.add(UIHelper.tableAlignedSection(buttons), BorderLayout.SOUTH);
        return shell;
    }

    private void loadConsultations() {
        if (mode == Mode.PATIENT || mode == Mode.DOCTOR) {
            return;
        }

        try {
            cmbSource.removeAllItems();
            for (LabService.ResultSourceOption option : service.getResultSourceOptions()) {
                cmbSource.addItem(option);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void loadTechnicians() {
        if (mode != Mode.ADMIN) {
            return;
        }

        try {
            cmbTechnician.removeAllItems();
            for (LabService.TechnicianOption option : service.getTechnicianOptions()) {
                cmbTechnician.addItem(option);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void loadTable() {
        try {
            DefaultTableModel model = switch (mode) {
                case PATIENT -> service.getResultsForPatient(account);
                case DOCTOR -> service.getResultsForDoctor(account);
                case TECHNICIAN -> service.getResultsForTechnician(account);
                case ADMIN -> service.getAllResults();
            };
            table.setModel(model);
            UIHelper.hideColumns(table, "lab_result_id");
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void saveResult() {
        try {
            LabService.ResultSourceOption option = (LabService.ResultSourceOption) cmbSource.getSelectedItem();
            if (option == null) {
                throw new ValidationException("Please choose an appointment or consultation.");
            }

            Integer technicianId = mode == Mode.TECHNICIAN
                    ? service.requireTechnician(account).getTechnicianId()
                    : selectedTechnicianId();

            service.saveResult(
                    technicianId,
                    option.consultationId(),
                    option.appointmentId(),
                    txtTestType.getText(),
                    txtResult.getText()
            );

            clearForm();
            loadConsultations();
            txtTestType.setText("");
            txtResult.setText("");
            loadTable();
            JOptionPane.showMessageDialog(this, "Lab result saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private Integer selectedTechnicianId() throws Exception {
        LabService.TechnicianOption option = (LabService.TechnicianOption) cmbTechnician.getSelectedItem();
        if (option == null) {
            throw new ValidationException("Please choose a technician.");
        }
        return option.technicianId();
    }

    private void deleteResult() {
        try {
            service.deleteResult(UIHelper.selectedId(table, "lab_result_id"));
            clearForm();
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateResult() {
        try {
            LabService.ResultSourceOption option = (LabService.ResultSourceOption) cmbSource.getSelectedItem();
            if (option == null) {
                throw new ValidationException("Please choose an appointment or consultation.");
            }

            Integer technicianId = mode == Mode.TECHNICIAN
                    ? service.requireTechnician(account).getTechnicianId()
                    : selectedTechnicianId();
            service.updateResult(
                    UIHelper.selectedId(table, "lab_result_id"),
                    technicianId,
                    option.consultationId(),
                    option.appointmentId(),
                    txtTestType.getText(),
                    txtResult.getText()
            );
            loadConsultations();
            loadTable();
            JOptionPane.showMessageDialog(this, "Lab result updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void populateFromSelection() {
        if (mode == Mode.PATIENT || mode == Mode.DOCTOR) {
            return;
        }

        try {
            Integer labResultId = UIHelper.selectedId(table, "lab_result_id");
            if (labResultId == null) {
                return;
            }

            var result = service.findResultById(labResultId);
            if (result == null) {
                return;
            }

            selectSource(result.getConsultationId(), result.getAppointmentId());
            txtTestType.setText(result.getTestType());
            txtResult.setText(result.getResult());

            if (mode == Mode.ADMIN) {
                selectTechnician(result.getTechnicianId());
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void selectSource(Integer consultationId, Integer appointmentId) {
        if (consultationId == null && appointmentId == null) {
            return;
        }

        for (int i = 0; i < cmbSource.getItemCount(); i++) {
            LabService.ResultSourceOption option = cmbSource.getItemAt(i);
            boolean consultationMatch = consultationId != null
                    && consultationId.equals(option.consultationId());
            boolean appointmentOnlyMatch = consultationId == null
                    && option.consultationId() == null
                    && appointmentId != null
                    && appointmentId.equals(option.appointmentId());

            if (consultationMatch || appointmentOnlyMatch) {
                cmbSource.setSelectedIndex(i);
                return;
            }
        }
    }

    private void clearForm() {
        table.clearSelection();
        if (cmbSource.getItemCount() > 0) {
            cmbSource.setSelectedIndex(0);
        }
        if (cmbTechnician.getItemCount() > 0) {
            cmbTechnician.setSelectedIndex(0);
        }
        txtTestType.setText("");
        txtResult.setText("");
    }

    private void selectTechnician(Integer technicianId) {
        if (technicianId == null) {
            return;
        }

        for (int i = 0; i < cmbTechnician.getItemCount(); i++) {
            LabService.TechnicianOption option = cmbTechnician.getItemAt(i);
            if (technicianId.equals(option.technicianId())) {
                cmbTechnician.setSelectedIndex(i);
                return;
            }
        }
    }

    private String title() {
        return switch (mode) {
            case PATIENT -> "Lab Results";
            case DOCTOR -> "Patient Lab Results";
            case TECHNICIAN -> "Upload Lab Results";
            case ADMIN -> "Manage Lab Results";
        };
    }

    private String subtitle() {
        return switch (mode) {
            case PATIENT -> "Review results linked to your visits and consultations";
            case DOCTOR -> "Review lab results linked to your patients and visits";
            case TECHNICIAN -> "Upload and review lab tests for consultations or direct appointments";
            case ADMIN -> "Review and maintain all lab test results";
        };
    }

    private JFrame backFrame() {
        return switch (mode) {
            case PATIENT -> new PatientDashboard(account);
            case DOCTOR -> new DoctorDashboard(account);
            case TECHNICIAN -> new LabTechnicianDashboard(account);
            case ADMIN -> new AdminDashboard(account);
        };
    }
}
