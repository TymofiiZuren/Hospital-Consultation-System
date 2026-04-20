package ie.setu.hcs.ui;

import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Appointment;
import ie.setu.hcs.service.AppointmentService;
import ie.setu.hcs.service.NotificationService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AppointmentFrame extends JFrame {
    public enum Mode {
        PATIENT, DOCTOR, ADMIN
    }

    private final Account account;
    private final Mode mode;
    private final AppointmentService service = new AppointmentService();
    private final NotificationService notificationService = new NotificationService();

    private final JTable table = UIHelper.table(new DefaultTableModel());
    private final JTable pendingTable = UIHelper.table(new DefaultTableModel());
    private final JTable acceptedTable = UIHelper.table(new DefaultTableModel());
    private final JTable cancelledTable = UIHelper.table(new DefaultTableModel());
    private final JTable pastTable = UIHelper.table(new DefaultTableModel());
    private final JComboBox<AppointmentService.DoctorOption> cmbDoctor = new JComboBox<>();
    private final JComboBox<AppointmentService.PatientOption> cmbPatient = new JComboBox<>();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
            "Pending", "Accepted", "Rejected", "Completed", "Cancelled"
    });
    private final JComboBox<AppointmentService.TimeSlotOption> cmbAvailableSlot = new JComboBox<>();
    private final JTextField txtDate = new JTextField(LocalDate.now().toString());
    private final JTextField txtTime = new JTextField("09:00");
    private final JTextField txtConsultationRoom = new JTextField();
    private final JTextArea txtMedicalNeed = new JTextArea();
    private JButton btnUpdate;
    private boolean syncingPatientSelection;

    public AppointmentFrame(Account account, Mode mode) {
        this.account = account;
        this.mode = mode;
        initUI();
        loadOptions();
        loadTable();
    }

    private void initUI() {
        setTitle(title());
        setSize(960, 650);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(HCS_Colors.LIGHT_BG);
        root.add(UIHelper.pageHeader(title(), subtitle()), BorderLayout.NORTH);

        JPanel content = UIHelper.pageBody(new BorderLayout(0, 16));
        content.setBackground(HCS_Colors.LIGHT_BG);

        if (mode == Mode.PATIENT) {
            JPanel patientPage = new JPanel();
            patientPage.setOpaque(false);
            patientPage.setLayout(new BoxLayout(patientPage, BoxLayout.Y_AXIS));
            patientPage.add(createForm());
            patientPage.add(Box.createVerticalStrut(18));
            patientPage.add(createPatientTables());
            content.add(UIHelper.scrollablePage(patientPage), BorderLayout.CENTER);

            registerPatientSelection(pendingTable);
            registerPatientSelection(acceptedTable);
            registerPatientSelection(cancelledTable);
            registerPatientSelection(pastTable);
            registerPatientSlotRefreshers();
        } else if (mode == Mode.ADMIN) {
            JPanel adminPage = new JPanel();
            adminPage.setOpaque(false);
            adminPage.setLayout(new BoxLayout(adminPage, BoxLayout.Y_AXIS));
            adminPage.add(createForm());
            adminPage.add(Box.createVerticalStrut(18));
            adminPage.add(UIHelper.tableScrollPane(table, 360));
            content.add(UIHelper.scrollablePage(adminPage), BorderLayout.CENTER);

            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    populateFromSelection(table);
                    refreshAdminUpdateState();
                }
            });
        } else {
            content.add(createForm(), BorderLayout.NORTH);
            content.add(UIHelper.tableScrollPane(table), BorderLayout.CENTER);
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    populateFromSelection(table);
                    refreshAdminUpdateState();
                }
            });
        }

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createForm() {
        JPanel shell = new JPanel(new BorderLayout(0, 10));
        shell.setOpaque(false);

        JPanel form = null;
        if (mode != Mode.DOCTOR) {
            form = UIHelper.formPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            int row = 0;

            if (mode == Mode.ADMIN) {
                UIHelper.styleCombo(cmbPatient);
                UIHelper.addFormRow(form, gbc, row++, "Patient", cmbPatient);
            }

            UIHelper.styleCombo(cmbDoctor);
            UIHelper.styleTextArea(txtMedicalNeed);
            UIHelper.addFormRow(form, gbc, row++, "Doctor", cmbDoctor);
            UIHelper.addFormRow(form, gbc, row++, "Medical Need", UIHelper.textAreaScrollPane(txtMedicalNeed));

            UIHelper.styleField(txtDate);
            UIHelper.styleField(txtConsultationRoom);
            UIHelper.styleCombo(cmbStatus);

            UIHelper.addFormRow(form, gbc, row++, "Date (YYYY-MM-DD)", txtDate);
            if (mode == Mode.PATIENT) {
                UIHelper.styleCombo(cmbAvailableSlot);
                UIHelper.addFormRow(form, gbc, row++, "Available Slot", cmbAvailableSlot);
            } else {
                UIHelper.styleField(txtTime);
                UIHelper.addFormRow(form, gbc, row++, "Time (HH:MM)", txtTime);
            }

            if (mode == Mode.ADMIN) {
                UIHelper.addFormRow(form, gbc, row++, "Consultation Room", txtConsultationRoom);
                UIHelper.addFormRow(form, gbc, row, "Status", cmbStatus);
            }
        }

        JPanel buttons = UIHelper.actionBar();

        if (mode != Mode.DOCTOR) {
            JButton save = UIHelper.actionButton(mode == Mode.PATIENT ? "Book" : "Add", HCS_Colors.ACCENT_GREEN);
            save.addActionListener(e -> saveAppointment());
            buttons.add(save);
            if (mode == Mode.ADMIN) {
                btnUpdate = UIHelper.actionButton("Update", HCS_Colors.BUTTON_BLUE);
                btnUpdate.addActionListener(e -> updateAppointment());
                btnUpdate.setEnabled(false);
                btnUpdate.setToolTipText("Only pending appointments can be updated.");
                buttons.add(btnUpdate);
            }
        }

        if (mode == Mode.DOCTOR) {
            cmbStatus.setModel(new DefaultComboBoxModel<>(new String[]{"Pending", "Accepted", "Rejected", "Completed"}));
            UIHelper.styleCombo(cmbStatus);
            cmbStatus.setSelectedItem("Accepted");
            buttons.add(cmbStatus);

            JButton changeStatus = UIHelper.actionButton("Change Status", HCS_Colors.ACCENT_GREEN);
            changeStatus.addActionListener(e -> updateStatus(selectedStatus()));
            buttons.add(changeStatus);
        } else {
            JButton cancel = UIHelper.actionButton(mode == Mode.PATIENT ? "Cancel" : "Delete", HCS_Colors.ACCENT_RED);
            cancel.addActionListener(e -> deleteOrCancel());
            buttons.add(cancel);
        }

        JButton refresh = UIHelper.actionButton("Refresh", HCS_Colors.BUTTON_GRAY);
        refresh.addActionListener(e -> loadTable());
        JButton view = UIHelper.actionButton("View Details", HCS_Colors.ACCENT_SKY);
        view.addActionListener(e -> showSelectedDetails());
        JButton close = UIHelper.secondaryButton("Back");
        close.addActionListener(e -> AppNavigator.replace(this, backFrame()));
        buttons.add(refresh);
        buttons.add(view);
        buttons.add(close);

        if (form != null) {
            shell.add(UIHelper.compactSection(form), BorderLayout.CENTER);
        }
        shell.add(UIHelper.tableAlignedSection(buttons), BorderLayout.SOUTH);
        return shell;
    }

    private JPanel createPatientTables() {
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(patientTableSection(
                "Pending Appointments",
                "Upcoming visits waiting for the doctor to confirm.",
                pendingTable
        ));
        stack.add(Box.createVerticalStrut(20));
        stack.add(patientTableSection(
                "Accepted Appointments",
                "Doctor-approved appointments stay here until they are completed or cancelled.",
                acceptedTable
        ));
        stack.add(Box.createVerticalStrut(20));
        stack.add(patientTableSection(
                "Cancelled / Rejected Appointments",
                "Cancelled and rejected visits kept separate from both active and past history.",
                cancelledTable
        ));
        stack.add(Box.createVerticalStrut(20));
        stack.add(patientTableSection(
                "Past Appointments",
                "Earlier visits and completed consultations kept in one history section.",
                pastTable
        ));
        return stack;
    }

    private JPanel patientTableSection(String heading, String description, JTable sourceTable) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(heading);
        titleLabel.setFont(UIHelper.font(Font.BOLD, 18));
        titleLabel.setForeground(HCS_Colors.TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(UIHelper.font(Font.PLAIN, 12));
        descLabel.setForeground(HCS_Colors.TEXT_MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(descLabel);

        section.add(UIHelper.tableAlignedSection(header));
        section.add(Box.createVerticalStrut(10));
        section.add(UIHelper.tableScrollPane(sourceTable));
        return section;
    }

    private void loadOptions() {
        try {
            if (mode != Mode.DOCTOR) {
                cmbDoctor.removeAllItems();
                for (AppointmentService.DoctorOption option : service.getDoctorOptions()) {
                    cmbDoctor.addItem(option);
                }
            }

            if (mode == Mode.ADMIN) {
                cmbPatient.removeAllItems();
                for (AppointmentService.PatientOption option : service.getPatientOptions()) {
                    cmbPatient.addItem(option);
                }
            }
            if (mode == Mode.PATIENT) {
                refreshAvailableSlots(null);
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void loadTable() {
        try {
            if (mode == Mode.PATIENT) {
                pendingTable.setModel(service.getPendingAppointmentsForPatient(account));
                acceptedTable.setModel(service.getAcceptedAppointmentsForPatient(account));
                cancelledTable.setModel(service.getCancelledAppointmentsForPatient(account));
                pastTable.setModel(service.getPastAppointmentsForPatient(account));
                UIHelper.hideColumns(pendingTable, "appointment_id");
                UIHelper.hideColumns(acceptedTable, "appointment_id");
                UIHelper.hideColumns(cancelledTable, "appointment_id");
                UIHelper.hideColumns(pastTable, "appointment_id");
                refreshAvailableSlots(null);
            } else {
                DefaultTableModel model = switch (mode) {
                    case PATIENT -> new DefaultTableModel();
                    case DOCTOR -> service.getAppointmentsForDoctor(account);
                    case ADMIN -> service.getAllAppointments();
                };
                table.setModel(model);
                UIHelper.hideColumns(table, "appointment_id");
                refreshAdminUpdateState();
            }
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void saveAppointment() {
        try {
            if (mode == Mode.PATIENT) {
                service.bookAppointment(account, selectedDoctorId(), parseDateTime(), selectedMedicalNeed());
            } else {
                service.createAppointment(
                        selectedPatientId(),
                        selectedDoctorId(),
                        parseDateTime(),
                        selectedStatus(),
                        selectedMedicalNeed(),
                        selectedConsultationRoom()
                );
            }
            loadTable();
            JOptionPane.showMessageDialog(this, "Appointment saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateAppointment() {
        try {
            Integer appointmentId = selectedAppointmentId();
            if (appointmentId == null) {
                throw new ValidationException("Please select an appointment first.");
            }

            Integer patientId = mode == Mode.PATIENT
                    ? service.requirePatient(account).getPatientId()
                    : selectedPatientId();
            String status = mode == Mode.PATIENT ? "Pending" : selectedStatus();

            service.updateAppointment(
                    appointmentId,
                    patientId,
                    selectedDoctorId(),
                    parseDateTime(),
                    status,
                    selectedMedicalNeed(),
                    selectedConsultationRoom()
            );
            loadTable();
            JOptionPane.showMessageDialog(this, "Appointment updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void updateStatus(String status) {
        try {
            Integer appointmentId = selectedAppointmentId();
            if (mode == Mode.DOCTOR) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Change the selected appointment status to " + status + "?",
                        "Confirm Status Change",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (choice != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            service.updateStatus(appointmentId, status);
            if (mode == Mode.DOCTOR) {
                notificationService.notifyPatientForAppointmentStatus(appointmentId, status);
                JOptionPane.showMessageDialog(this, "Appointment status updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void deleteOrCancel() {
        try {
            Integer appointmentId = selectedAppointmentId();
            if (mode == Mode.PATIENT) {
                service.updateStatus(appointmentId, "Cancelled");
            } else {
                service.deleteAppointment(appointmentId);
            }
            loadTable();
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private Integer selectedDoctorId() throws Exception {
        AppointmentService.DoctorOption option = (AppointmentService.DoctorOption) cmbDoctor.getSelectedItem();
        if (option == null) {
            throw new ValidationException("Please choose a doctor.");
        }
        return option.doctorId();
    }

    private Integer selectedPatientId() throws Exception {
        AppointmentService.PatientOption option = (AppointmentService.PatientOption) cmbPatient.getSelectedItem();
        if (option == null) {
            throw new ValidationException("Please choose a patient.");
        }
        return option.patientId();
    }

    private String selectedStatus() {
        Object status = cmbStatus.getSelectedItem();
        return status == null ? "Pending" : status.toString();
    }

    private String selectedMedicalNeed() {
        return txtMedicalNeed.getText().trim();
    }

    private String selectedConsultationRoom() {
        return mode == Mode.ADMIN ? txtConsultationRoom.getText().trim() : "";
    }

    private LocalDateTime parseDateTime() throws Exception {
        return LocalDateTime.of(
                LocalDate.parse(txtDate.getText().trim()),
                mode == Mode.PATIENT ? selectedSlotTime() : LocalTime.parse(txtTime.getText().trim())
        );
    }

    private void populateFromSelection(JTable sourceTable) {
        if (mode == Mode.PATIENT) {
            return;
        }

        try {
            Integer appointmentId = UIHelper.selectedId(sourceTable, "appointment_id");
            Appointment appointment = service.findById(appointmentId);
            if (appointment == null) {
                return;
            }

            selectDoctor(appointment.getDoctorId());
            selectPatient(appointment.getPatientId());

            if (appointment.getDate() != null) {
                txtDate.setText(appointment.getDate().toLocalDate().toString());
                if (mode == Mode.PATIENT) {
                    refreshAvailableSlots(appointment.getDate().toLocalTime().withSecond(0).withNano(0));
                } else {
                    txtTime.setText(appointment.getDate().toLocalTime().withSecond(0).withNano(0).toString());
                }
            }
            cmbStatus.setSelectedItem(appointment.getStatus());
            txtMedicalNeed.setText(appointment.getMedicalNeed() == null ? "" : appointment.getMedicalNeed());
            txtConsultationRoom.setText(appointment.getConsultationRoom() == null ? "" : appointment.getConsultationRoom());
        } catch (Exception ignored) {
            // Selection should never interrupt table browsing.
        }
    }

    private void refreshAdminUpdateState() {
        if (mode != Mode.ADMIN || btnUpdate == null) {
            return;
        }

        boolean canUpdate = hasPendingSelection(table);
        btnUpdate.setEnabled(canUpdate);
        btnUpdate.setToolTipText(canUpdate ? null : "Only pending appointments can be updated.");
    }

    private boolean hasPendingSelection(JTable sourceTable) {
        if (sourceTable.getSelectedRow() < 0) {
            return false;
        }

        DefaultTableModel model = (DefaultTableModel) sourceTable.getModel();
        int statusColumn = model.findColumn("status");
        if (statusColumn < 0) {
            return false;
        }

        int modelRow = sourceTable.convertRowIndexToModel(sourceTable.getSelectedRow());
        Object status = model.getValueAt(modelRow, statusColumn);
        return status != null && AppointmentService.STATUS_PENDING.equalsIgnoreCase(status.toString().trim());
    }

    private void registerPatientSelection(JTable sourceTable) {
        sourceTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || syncingPatientSelection) {
                return;
            }
            if (sourceTable.getSelectedRow() < 0) {
                return;
            }

            syncingPatientSelection = true;
            clearOtherPatientSelections(sourceTable);
            syncingPatientSelection = false;
            populateFromSelection(sourceTable);
        });
    }

    private Integer selectedAppointmentId() {
        JTable selectedTable = selectedTable();
        return selectedTable == null ? null : UIHelper.selectedId(selectedTable, "appointment_id");
    }

    private JTable selectedTable() {
        if (mode == Mode.PATIENT) {
            if (pendingTable.getSelectedRow() >= 0) {
                return pendingTable;
            }
            if (acceptedTable.getSelectedRow() >= 0) {
                return acceptedTable;
            }
            if (cancelledTable.getSelectedRow() >= 0) {
                return cancelledTable;
            }
            if (pastTable.getSelectedRow() >= 0) {
                return pastTable;
            }
            return null;
        }
        return table;
    }

    private void showSelectedDetails() {
        try {
            JTable selectedTable = selectedTable();
            if (selectedTable == null) {
                throw new ValidationException("Please select an appointment first.");
            }
            UIHelper.showSelectedRowDetails(this, selectedTable, title() + " Details");
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private void clearOtherPatientSelections(JTable selectedSource) {
        for (JTable patientTable : patientTables()) {
            if (patientTable != selectedSource) {
                patientTable.clearSelection();
            }
        }
    }

    private JTable[] patientTables() {
        return new JTable[]{pendingTable, acceptedTable, cancelledTable, pastTable};
    }

    private void registerPatientSlotRefreshers() {
        cmbDoctor.addActionListener(e -> refreshAvailableSlots(null));
        txtDate.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshAvailableSlots(null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshAvailableSlots(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshAvailableSlots(null);
            }
        });
    }

    private void refreshAvailableSlots(LocalTime preferredTime) {
        if (mode != Mode.PATIENT) {
            return;
        }

        cmbAvailableSlot.removeAllItems();

        AppointmentService.DoctorOption option = (AppointmentService.DoctorOption) cmbDoctor.getSelectedItem();
        LocalDate date = parseDateSafely();
        if (option == null || date == null) {
            cmbAvailableSlot.addItem(new AppointmentService.TimeSlotOption(null, "Choose doctor and valid date"));
            return;
        }

        try {
            List<AppointmentService.TimeSlotOption> slots = service.getAvailableSlotsForDoctor(
                    option.doctorId(),
                    date,
                    null
            );
            if (slots.isEmpty()) {
                cmbAvailableSlot.addItem(new AppointmentService.TimeSlotOption(null, "No available slots"));
                return;
            }

            AppointmentService.TimeSlotOption toSelect = null;
            for (AppointmentService.TimeSlotOption slot : slots) {
                cmbAvailableSlot.addItem(slot);
                if (preferredTime != null && preferredTime.equals(slot.time())) {
                    toSelect = slot;
                }
            }

            if (toSelect != null) {
                cmbAvailableSlot.setSelectedItem(toSelect);
            } else {
                cmbAvailableSlot.setSelectedIndex(0);
            }
        } catch (Exception ignored) {
            cmbAvailableSlot.addItem(new AppointmentService.TimeSlotOption(null, "Unable to load slots"));
        }
    }

    private LocalDate parseDateSafely() {
        try {
            return LocalDate.parse(txtDate.getText().trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalTime selectedSlotTime() throws Exception {
        AppointmentService.TimeSlotOption option = (AppointmentService.TimeSlotOption) cmbAvailableSlot.getSelectedItem();
        if (option == null || option.time() == null) {
            throw new ValidationException("Please choose an available appointment slot.");
        }
        return option.time();
    }

    private void selectDoctor(Integer doctorId) {
        for (int i = 0; i < cmbDoctor.getItemCount(); i++) {
            AppointmentService.DoctorOption option = cmbDoctor.getItemAt(i);
            if (option.doctorId().equals(doctorId)) {
                cmbDoctor.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectPatient(Integer patientId) {
        for (int i = 0; i < cmbPatient.getItemCount(); i++) {
            AppointmentService.PatientOption option = cmbPatient.getItemAt(i);
            if (option.patientId().equals(patientId)) {
                cmbPatient.setSelectedIndex(i);
                return;
            }
        }
    }

    private String title() {
        return switch (mode) {
            case PATIENT -> "Appointments";
            case DOCTOR -> "My Appointments";
            case ADMIN -> "Manage Appointments";
        };
    }

    private String subtitle() {
        return switch (mode) {
            case PATIENT -> "Book a visit with a medical need and choose from available appointment slots";
            case DOCTOR -> "Review appointment requests and update their status with patient-facing notifications";
            case ADMIN -> "Create visits and adjust pending requests before they move further in the workflow";
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
