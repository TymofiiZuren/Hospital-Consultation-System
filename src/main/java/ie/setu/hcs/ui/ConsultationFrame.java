package ie.setu.hcs.ui;

import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.AppointmentService;
import ie.setu.hcs.service.ConsultationService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ConsultationFrame extends JFrame {
    public enum Mode {
        DOCTOR, ADMIN
    }

    private final Account account;
    private final Mode mode;
    private final ConsultationService service = new ConsultationService();
    private final AppointmentService appointmentService = new AppointmentService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JComboBox<ConsultationService.AppointmentOption> cmbAppointment = new JComboBox<>();
    private final JTextField txtDiagnosis = new JTextField();
    private final JTextField txtInvoiceAmount = new JTextField();
    private final JTextArea txtNotes = new JTextArea(4, 24);
    private final JTextArea txtPrescription = new JTextArea(3, 24);

    public ConsultationFrame(Account account, Mode mode) {
        this.account = account;
        this.mode = mode;
        initUI();
        loadAppointments();
        loadTable();
    }

    private void initUI() {
        setTitle(title());
        setSize(980, 720);
        setMinimumSize(new Dimension(880, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = UIHelper.appBackground(new BorderLayout());
        root.add(UIHelper.pageHeader(title(), subtitle()), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.add(createForm());
        page.add(Box.createVerticalStrut(18));
        page.add(UIHelper.tableScrollPane(table, 420));
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

        UIHelper.styleCombo(cmbAppointment);
        UIHelper.styleField(txtDiagnosis);
        UIHelper.styleField(txtInvoiceAmount);
        styleTextArea(txtNotes);
        styleTextArea(txtPrescription);

        int row = 0;
        UIHelper.addFormRow(form, gbc, row++, "Appointment", cmbAppointment);
        UIHelper.addFormRow(form, gbc, row++, "Diagnosis", txtDiagnosis);
        UIHelper.addFormRow(form, gbc, row++, "Notes", UIHelper.textAreaScrollPane(txtNotes));
        UIHelper.addFormRow(form, gbc, row++, "Prescription", UIHelper.textAreaScrollPane(txtPrescription));
        UIHelper.addFormRow(form, gbc, row, "Invoice Amount", txtInvoiceAmount);

        JPanel buttons = UIHelper.actionBar();

        JButton save = UIHelper.actionButton("Save", HCS_Colors.ACCENT_GREEN);
        save.addActionListener(e -> saveConsultation());
        JButton clear = UIHelper.secondaryButton("Clear");
        clear.addActionListener(e -> clearForm());
        JButton followUp = UIHelper.actionButton("Follow-Up", HCS_Colors.PRIMARY_TEAL);
        followUp.addActionListener(e -> showFollowUpDialog());
        JButton delete = UIHelper.actionButton("Delete", HCS_Colors.ACCENT_RED);
        delete.addActionListener(e -> deleteConsultation());
        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_BLUE);
        refresh.addActionListener(e -> {
            loadAppointments();
            loadTable();
        });
        JButton view = UIHelper.detailsButton(this, table, title() + " Details");
        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, backFrame()));

        buttons.add(save);
        buttons.add(clear);
        if (mode == Mode.DOCTOR) {
            buttons.add(followUp);
        }
        buttons.add(delete);
        buttons.add(refresh);
        buttons.add(view);
        buttons.add(close);

        shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        shell.add(UIHelper.tableAlignedSection(buttons), BorderLayout.SOUTH);
        return shell;
    }

    private void styleTextArea(JTextArea area) {
        UIHelper.styleTextArea(area);
    }

    private void loadAppointments() {
        try {
            cmbAppointment.removeAllItems();
            for (ConsultationService.AppointmentOption option : mode == Mode.ADMIN
                    ? service.getAllAppointmentOptions()
                    : service.getAppointmentOptionsForDoctor(account)) {
                cmbAppointment.addItem(option);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void loadTable() {
        try {
            DefaultTableModel model = mode == Mode.ADMIN
                    ? service.getAllConsultations()
                    : service.getConsultationsForDoctor(account);
            table.setModel(model);
            UIHelper.hideColumns(table, "consultation_id", "appointment_id");
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void saveConsultation() {
        try {
            ConsultationService.AppointmentOption option =
                    (ConsultationService.AppointmentOption) cmbAppointment.getSelectedItem();
            if (option == null) {
                throw new ValidationException("Please choose an appointment.");
            }

            service.saveConsultation(
                    option.appointmentId(),
                    txtDiagnosis.getText(),
                    txtNotes.getText(),
                    txtPrescription.getText(),
                    parseAmount()
            );

            clearForm();
            loadTable();
            JOptionPane.showMessageDialog(this, "Consultation saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deleteConsultation() {
        try {
            service.deleteConsultation(UIHelper.selectedId(table, "consultation_id"));
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private Float parseAmount() throws Exception {
        String amount = txtInvoiceAmount.getText().trim();
        if (amount.isEmpty()) {
            return null;
        }

        try {
            return Float.parseFloat(amount);
        } catch (NumberFormatException ex) {
            throw new ValidationException("Invoice amount must be numeric.");
        }
    }

    private void clearForm() {
        table.clearSelection();
        txtDiagnosis.setText("");
        txtNotes.setText("");
        txtPrescription.setText("");
        txtInvoiceAmount.setText("");
        txtDiagnosis.requestFocus();
    }

    private void showFollowUpDialog() {
        try {
            Integer sourceAppointmentId = selectedAppointmentIdForFollowUp();
            if (sourceAppointmentId == null) {
                throw new ValidationException("Please choose an appointment or consultation first.");
            }

            Integer doctorId = appointmentService.requireDoctor(account).getDoctorId();
            JTextField txtFollowUpDate = new JTextField(LocalDate.now().plusWeeks(1).toString());
            JComboBox<AppointmentService.TimeSlotOption> cmbFollowUpSlot = new JComboBox<>();
            JTextField txtFollowUpReason = new JTextField(suggestedFollowUpReason());

            UIHelper.styleField(txtFollowUpDate);
            UIHelper.styleCombo(cmbFollowUpSlot);
            UIHelper.styleField(txtFollowUpReason);

            Runnable refreshSlots = () -> refreshFollowUpSlots(doctorId, txtFollowUpDate, cmbFollowUpSlot);
            txtFollowUpDate.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    refreshSlots.run();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    refreshSlots.run();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    refreshSlots.run();
                }
            });
            refreshSlots.run();

            JPanel form = UIHelper.formPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            int row = 0;
            UIHelper.addFormRow(form, gbc, row++, "Date (YYYY-MM-DD)", txtFollowUpDate);
            UIHelper.addFormRow(form, gbc, row++, "Available Slot", cmbFollowUpSlot);
            UIHelper.addFormRow(form, gbc, row, "Reason", txtFollowUpReason);

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    UIHelper.compactSection(form),
                    "Schedule Follow-Up",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            LocalDate date = LocalDate.parse(txtFollowUpDate.getText().trim());
            AppointmentService.TimeSlotOption slot = (AppointmentService.TimeSlotOption) cmbFollowUpSlot.getSelectedItem();
            if (slot == null || slot.time() == null) {
                throw new ValidationException("Please choose an available follow-up slot.");
            }

            service.scheduleFollowUp(account, sourceAppointmentId, date, slot.time(), txtFollowUpReason.getText());
            loadAppointments();
            JOptionPane.showMessageDialog(this, "Follow-up appointment scheduled.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void refreshFollowUpSlots(Integer doctorId, JTextField txtFollowUpDate,
                                      JComboBox<AppointmentService.TimeSlotOption> cmbFollowUpSlot) {
        cmbFollowUpSlot.removeAllItems();
        LocalDate date;
        try {
            date = LocalDate.parse(txtFollowUpDate.getText().trim());
        } catch (Exception ex) {
            cmbFollowUpSlot.addItem(new AppointmentService.TimeSlotOption(null, "Enter a valid date"));
            return;
        }

        try {
            List<AppointmentService.TimeSlotOption> slots =
                    appointmentService.getAvailableSlotsForDoctor(doctorId, date, null);
            if (slots.isEmpty()) {
                cmbFollowUpSlot.addItem(new AppointmentService.TimeSlotOption(null, "No available slots"));
                return;
            }
            for (AppointmentService.TimeSlotOption slot : slots) {
                cmbFollowUpSlot.addItem(slot);
            }
            cmbFollowUpSlot.setSelectedIndex(0);
        } catch (Exception ex) {
            cmbFollowUpSlot.addItem(new AppointmentService.TimeSlotOption(null, "Unable to load slots"));
        }
    }

    private Integer selectedAppointmentIdForFollowUp() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int row = table.convertRowIndexToModel(selectedRow);
            return intValue(row, "appointment_id");
        }

        ConsultationService.AppointmentOption option =
                (ConsultationService.AppointmentOption) cmbAppointment.getSelectedItem();
        return option == null ? null : option.appointmentId();
    }

    private String suggestedFollowUpReason() {
        String diagnosis = txtDiagnosis.getText().trim();
        if (!diagnosis.isBlank()) {
            return "Follow-up for " + diagnosis;
        }
        return "Follow-up consultation";
    }

    private String title() {
        return mode == Mode.ADMIN ? "Manage Consultations" : "Consultations";
    }

    private String subtitle() {
        return mode == Mode.ADMIN
                ? "Review and maintain consultation notes"
                : "Record diagnosis, notes, prescriptions, and billing";
    }

    private JFrame backFrame() {
        return mode == Mode.ADMIN ? new AdminDashboard(account) : new DoctorDashboard(account);
    }

    private void populateFromSelection() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        Integer appointmentId = intValue(row, "appointment_id");
        txtDiagnosis.setText(value(row, "diagnosis"));
        txtNotes.setText(value(row, "notes"));

        selectAppointment(appointmentId);

        try {
            Integer consultationId = UIHelper.selectedId(table, "consultation_id");
            txtPrescription.setText(service.getPrescriptionForConsultation(consultationId));
            Float invoiceAmount = service.getInvoiceAmountForConsultation(consultationId);
            txtInvoiceAmount.setText(invoiceAmount == null ? "" : stripTrailingZeros(invoiceAmount));
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void selectAppointment(Integer appointmentId) {
        if (appointmentId == null) {
            return;
        }

        for (int i = 0; i < cmbAppointment.getItemCount(); i++) {
            ConsultationService.AppointmentOption option = cmbAppointment.getItemAt(i);
            if (option.appointmentId().equals(appointmentId)) {
                cmbAppointment.setSelectedIndex(i);
                return;
            }
        }
    }

    private Integer intValue(int row, String columnName) {
        int column = ((DefaultTableModel) table.getModel()).findColumn(columnName);
        if (column < 0) {
            return null;
        }
        Object value = table.getModel().getValueAt(row, column);
        return value == null ? null : Integer.parseInt(value.toString());
    }

    private String value(int row, String columnName) {
        int column = ((DefaultTableModel) table.getModel()).findColumn(columnName);
        if (column < 0) {
            return "";
        }
        Object value = table.getModel().getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private String stripTrailingZeros(Float value) {
        if (value == null) {
            return "";
        }

        if (value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }

        return value.toString();
    }
}
