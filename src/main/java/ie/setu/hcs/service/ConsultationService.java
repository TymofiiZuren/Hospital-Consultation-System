package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.AppointmentDAOImpl;
import ie.setu.hcs.dao.impl.ConsultationDAOImpl;
import ie.setu.hcs.dao.impl.InvoiceDAOImpl;
import ie.setu.hcs.dao.impl.LabResultDAOImpl;
import ie.setu.hcs.dao.impl.MedicalRecordDAOImpl;
import ie.setu.hcs.dao.interfaces.AppointmentDAO;
import ie.setu.hcs.dao.interfaces.ConsultationDAO;
import ie.setu.hcs.dao.interfaces.InvoiceDAO;
import ie.setu.hcs.dao.interfaces.LabResultDAO;
import ie.setu.hcs.dao.interfaces.MedicalRecordDAO;
import ie.setu.hcs.exception.AuthorizationException;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Appointment;
import ie.setu.hcs.model.Consultation;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.model.Invoice;
import ie.setu.hcs.model.MedicalRecord;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConsultationService {
    private static final DateTimeFormatter FOLLOW_UP_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final ConsultationDAO consultationDAO = new ConsultationDAOImpl();
    private final AppointmentDAO appointmentDAO = new AppointmentDAOImpl();
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAOImpl();
    private final InvoiceDAO invoiceDAO = new InvoiceDAOImpl();
    private final LabResultDAO labResultDAO = new LabResultDAOImpl();
    private final AppointmentService appointmentService = new AppointmentService();
    private final NotificationService notificationService = new NotificationService();

    public DefaultTableModel getConsultationsForPatient(Account account) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        String sql = """
                SELECT c.consultation_id,
                       c.appointment_id,
                       %s AS appointment,
                       c.diagnosis,
                       c.notes,
                       c.created_at
                FROM consultation c
                JOIN appointments a ON c.appointment_id = a.appointment_id
                WHERE a.patient_id = ?
                ORDER BY c.created_at DESC
                """.formatted(appointmentDisplaySql());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patient.getPatientId());
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    public DefaultTableModel getConsultationsForDoctor(Account account) throws Exception {
        Doctor doctor = appointmentService.requireDoctor(account);
        String sql = """
                SELECT c.consultation_id,
                       c.appointment_id,
                       %s AS appointment,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       c.diagnosis,
                       c.notes,
                       c.created_at
                FROM consultation c
                JOIN appointments a ON c.appointment_id = a.appointment_id
                JOIN patients p ON a.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                WHERE a.doctor_id = ?
                ORDER BY c.created_at DESC
                """.formatted(appointmentDisplaySql());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctor.getDoctorId());
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    public DefaultTableModel getAllConsultations() throws Exception {
        String sql = """
                SELECT c.consultation_id,
                       c.appointment_id,
                       %s AS appointment,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       CONCAT('Dr. ', da.first_name, ' ', da.last_name) AS doctor,
                       c.diagnosis,
                       c.notes,
                       c.created_at
                FROM consultation c
                JOIN appointments a ON c.appointment_id = a.appointment_id
                JOIN patients p ON a.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                JOIN doctors d ON a.doctor_id = d.doctor_id
                JOIN accounts da ON d.account_id = da.account_id
                ORDER BY c.created_at DESC
                """.formatted(appointmentDisplaySql());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return TableModelUtil.buildTableModel(rs);
        }
    }

    public List<AppointmentOption> getAppointmentOptionsForDoctor(Account account) throws Exception {
        Doctor doctor = appointmentService.requireDoctor(account);
        String sql = """
                SELECT ap.appointment_id,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       ap.appointment_datetime,
                       ap.status
                FROM appointments ap
                JOIN patients p ON ap.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                WHERE ap.doctor_id = ?
                ORDER BY ap.appointment_datetime DESC
                """;
        return appointmentOptionsFromTable(loadAppointmentOptions(sql, doctor.getDoctorId()));
    }

    public List<AppointmentOption> getAllAppointmentOptions() throws Exception {
        String sql = """
                SELECT ap.appointment_id,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       CONCAT('Dr. ', da.first_name, ' ', da.last_name) AS doctor,
                       ap.appointment_datetime,
                       ap.status
                FROM appointments ap
                JOIN patients p ON ap.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                JOIN doctors d ON ap.doctor_id = d.doctor_id
                JOIN accounts da ON d.account_id = da.account_id
                ORDER BY ap.appointment_datetime DESC
                """;
        return appointmentOptionsFromTable(loadAppointmentOptions(sql, null));
    }

    public Integer saveConsultation(Integer appointmentId, String diagnosis, String notes,
                                    String prescription, Float invoiceAmount) throws Exception {
        if (appointmentId == null) {
            throw new ValidationException("Appointment is required.");
        }
        if (diagnosis == null || diagnosis.isBlank()) {
            throw new ValidationException("Diagnosis is required.");
        }

        Appointment appointment = appointmentDAO.findById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("Appointment was not found.");
        }

        Consultation existing = consultationDAO.findByAppointmentId(appointmentId);
        Integer consultationId;

        if (existing == null) {
            Consultation consultation = new Consultation(
                    appointmentId,
                    diagnosis.trim(),
                    notes == null ? "" : notes.trim(),
                    LocalDateTime.now()
            );
            consultationDAO.save(consultation);
            consultationId = consultation.getConsultationId();
        } else {
            existing.setDiagnosis(diagnosis.trim());
            existing.setNotes(notes == null ? "" : notes.trim());
            existing.setCreatedAt(LocalDateTime.now());
            consultationDAO.update(existing);
            consultationId = existing.getConsultationId();
        }

        upsertMedicalRecord(appointment.getPatientId(), consultationId, prescription);
        upsertInvoice(appointment.getPatientId(), consultationId, invoiceAmount);

        appointmentDAO.updateStatus(appointmentId, "Completed");
        return consultationId;
    }

    public String getPrescriptionForConsultation(Integer consultationId) throws Exception {
        DefaultTableModel records = medicalRecordDAO.findByConsultationId(consultationId);
        if (records.getRowCount() == 0) {
            return "";
        }
        Object value = value(records, 0, "prescription");
        return value == null ? "" : value.toString();
    }

    public Float getInvoiceAmountForConsultation(Integer consultationId) throws Exception {
        DefaultTableModel invoices = invoiceDAO.findByConsultationId(consultationId);
        if (invoices.getRowCount() == 0) {
            return null;
        }
        Object value = value(invoices, 0, "amount");
        return value == null ? null : Float.parseFloat(value.toString());
    }

    public void deleteConsultation(Integer consultationId) throws Exception {
        if (consultationId == null) {
            throw new ValidationException("Please select a consultation first.");
        }

        deleteLinkedRows(labResultDAO.findByConsultationId(consultationId), "lab_result_id", labResultDAO::delete);
        deleteLinkedRows(invoiceDAO.findByConsultationId(consultationId), "invoice_id", invoiceDAO::delete);
        deleteLinkedRows(medicalRecordDAO.findByConsultationId(consultationId), "record_id", medicalRecordDAO::delete);
        consultationDAO.delete(consultationId);
    }

    public Integer scheduleFollowUp(Account doctorAccount, Integer sourceAppointmentId, LocalDate date,
                                    LocalTime time, String reason) throws Exception {
        if (sourceAppointmentId == null) {
            throw new ValidationException("Please choose a consultation or appointment first.");
        }
        if (date == null) {
            throw new ValidationException("Follow-up date is required.");
        }
        if (time == null) {
            throw new ValidationException("Follow-up time is required.");
        }

        Doctor doctor = appointmentService.requireDoctor(doctorAccount);
        Appointment sourceAppointment = appointmentDAO.findById(sourceAppointmentId);
        if (sourceAppointment == null) {
            throw new ResourceNotFoundException("Source appointment was not found.");
        }
        if (!doctor.getDoctorId().equals(sourceAppointment.getDoctorId())) {
            throw new AuthorizationException("You can only schedule follow-up appointments for your own patients.");
        }

        String medicalNeed = reason == null || reason.isBlank()
                ? defaultFollowUpReason(sourceAppointment)
                : reason.trim();

        LocalDateTime followUpDateTime = LocalDateTime.of(date, time);
        if (followUpDateTime.isBefore(LocalDateTime.now())) {
            throw new ConflictException("Follow-up appointments must be scheduled in the future.");
        }

        boolean slotAvailable = false;
        for (AppointmentService.TimeSlotOption slot : appointmentService.getAvailableSlotsForDoctor(doctor.getDoctorId(), date, null)) {
            if (time.equals(slot.time())) {
                slotAvailable = true;
                break;
            }
        }
        if (!slotAvailable) {
            throw new ConflictException("The selected follow-up slot is no longer available.");
        }

        Integer followUpAppointmentId = appointmentService.createAppointment(
                sourceAppointment.getPatientId(),
                doctor.getDoctorId(),
                followUpDateTime,
                "Accepted",
                medicalNeed,
                ""
        );

        String doctorName = "Dr. " + doctorAccount.getFirstName() + " " + doctorAccount.getLastName();
        notificationService.notifyPatient(
                sourceAppointment.getPatientId(),
                "Follow-up scheduled",
                "A follow-up appointment with " + doctorName + " has been scheduled for "
                        + followUpDateTime.format(FOLLOW_UP_FORMAT) + ". Reason: " + medicalNeed + "."
        );

        return followUpAppointmentId;
    }

    private List<AppointmentOption> appointmentOptionsFromTable(DefaultTableModel appointments) {
        List<AppointmentOption> options = new ArrayList<>();

        for (int row = 0; row < appointments.getRowCount(); row++) {
            Integer appointmentId = intValue(appointments, row, "appointment_id");
            String patient = stringValue(appointments, row, "patient");
            String doctor = stringValue(appointments, row, "doctor");
            Object date = value(appointments, row, "appointment_datetime");
            Object status = value(appointments, row, "status");
            if ((patient == null || patient.isBlank())) {
                Integer patientId = intValue(appointments, row, "patient_id");
                patient = patientId == null ? "" : "Patient " + patientId;
            }

            StringBuilder label = new StringBuilder("#").append(appointmentId);
            if (patient != null && !patient.isBlank()) {
                label.append(" | ").append(patient);
            }
            if (doctor != null && !doctor.isBlank()) {
                label.append(" | ").append(doctor);
            }
            if (date != null) {
                label.append(" | ").append(date);
            }
            if (status != null) {
                label.append(" | ").append(status);
            }

            options.add(new AppointmentOption(
                    appointmentId,
                    label.toString()
            ));
        }

        return options;
    }

    private String defaultFollowUpReason(Appointment sourceAppointment) {
        String originalNeed = sourceAppointment.getMedicalNeed();
        if (originalNeed == null || originalNeed.isBlank()) {
            return "Follow-up consultation";
        }
        return "Follow-up for " + originalNeed.trim();
    }

    private String appointmentDisplaySql() {
        return """
                CONCAT(
                    '#', a.appointment_id,
                    ' | ', a.appointment_datetime,
                    ' | ', a.status,
                    CASE
                        WHEN a.consultation_room IS NULL OR TRIM(a.consultation_room) = '' THEN ''
                        ELSE CONCAT(' | Room ', a.consultation_room)
                    END
                )
                """;
    }

    private void upsertMedicalRecord(Integer patientId, Integer consultationId, String prescription) throws Exception {
        DefaultTableModel records = medicalRecordDAO.findByConsultationId(consultationId);
        String normalizedPrescription = prescription == null ? "" : prescription.trim();

        if (records.getRowCount() == 0) {
            if (normalizedPrescription.isBlank()) {
                return;
            }

            MedicalRecord record = new MedicalRecord(
                    patientId,
                    consultationId,
                    normalizedPrescription,
                    LocalDateTime.now()
            );
            medicalRecordDAO.save(record);
            return;
        }

        Integer recordId = intValue(records, 0, "record_id");
        MedicalRecord record = medicalRecordDAO.findById(recordId);
        if (record != null) {
            record.setPrescription(normalizedPrescription);
            record.setCreatedAt(LocalDateTime.now());
            medicalRecordDAO.update(record);
        }
    }

    private void upsertInvoice(Integer patientId, Integer consultationId, Float invoiceAmount) throws Exception {
        DefaultTableModel invoices = invoiceDAO.findByConsultationId(consultationId);

        if (invoices.getRowCount() == 0) {
            if (invoiceAmount == null || invoiceAmount <= 0) {
                return;
            }

            Invoice invoice = new Invoice(
                    patientId,
                    consultationId,
                    invoiceAmount,
                    "UNPAID",
                    LocalDateTime.now(),
                    null
            );
            invoiceDAO.save(invoice);
            return;
        }

        if (invoiceAmount == null || invoiceAmount <= 0) {
            return;
        }

        Integer invoiceId = intValue(invoices, 0, "invoice_id");
        Invoice invoice = invoiceDAO.findById(invoiceId);
        if (invoice != null) {
            invoice.setAmount(invoiceAmount);
            invoiceDAO.update(invoice);
        }
    }

    private Integer intValue(DefaultTableModel model, int row, String columnName) {
        Object value = value(model, row, columnName);
        return value == null ? null : Integer.parseInt(value.toString());
    }

    private String stringValue(DefaultTableModel model, int row, String columnName) {
        Object value = value(model, row, columnName);
        return value == null ? "" : value.toString();
    }

    private void deleteLinkedRows(DefaultTableModel model, String columnName, IdDeleteAction action) throws Exception {
        for (int row = 0; row < model.getRowCount(); row++) {
            Integer id = intValue(model, row, columnName);
            if (id != null) {
                action.delete(id);
            }
        }
    }

    private Object value(DefaultTableModel model, int row, String columnName) {
        int column = model.findColumn(columnName);
        return column < 0 ? null : model.getValueAt(row, column);
    }

    private DefaultTableModel loadAppointmentOptions(String sql, Integer doctorId) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (doctorId != null) {
                ps.setInt(1, doctorId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    public record AppointmentOption(Integer appointmentId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    @FunctionalInterface
    private interface IdDeleteAction {
        void delete(Integer id) throws Exception;
    }
}
