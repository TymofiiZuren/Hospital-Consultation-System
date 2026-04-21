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
import ie.setu.hcs.util.TransactionRunner;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
                  AND COALESCE(c.delete_flag, FALSE) = FALSE
                  AND COALESCE(a.delete_flag, FALSE) = FALSE
                ORDER BY c.created_at DESC
                """.formatted(appointmentDisplaySql());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareConsultationQuery(conn, sql)) {
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
                  AND COALESCE(c.delete_flag, FALSE) = FALSE
                  AND COALESCE(a.delete_flag, FALSE) = FALSE
                ORDER BY c.created_at DESC
                """.formatted(appointmentDisplaySql());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareConsultationQuery(conn, sql)) {
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
                WHERE COALESCE(c.delete_flag, FALSE) = FALSE
                  AND COALESCE(a.delete_flag, FALSE) = FALSE
                ORDER BY c.created_at DESC
                """.formatted(appointmentDisplaySql());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareConsultationQuery(conn, sql);
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
                  AND COALESCE(ap.delete_flag, FALSE) = FALSE
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
                WHERE COALESCE(ap.delete_flag, FALSE) = FALSE
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

        String normalizedDiagnosis = diagnosis.trim();
        String normalizedNotes = notes == null ? "" : notes.trim();
        String normalizedPrescription = prescription == null ? "" : prescription.trim();
        LocalDateTime now = LocalDateTime.now();

        return TransactionRunner.inTransaction(conn -> {
            ensureSoftDeleteColumns(conn);
            Consultation existing = findConsultationByAppointmentId(conn, appointmentId);
            Integer consultationId;

            if (existing == null) {
                consultationId = insertConsultation(conn, appointmentId, normalizedDiagnosis, normalizedNotes, now);
            } else {
                updateConsultation(conn, existing.getConsultationId(), appointmentId, normalizedDiagnosis, normalizedNotes, now);
                consultationId = existing.getConsultationId();
            }

            upsertMedicalRecord(conn, appointment.getPatientId(), consultationId, normalizedPrescription, now);
            upsertInvoice(conn, appointment.getPatientId(), consultationId, invoiceAmount, now);
            updateAppointmentStatus(conn, appointmentId, "Completed");
            return consultationId;
        });
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

        Consultation consultation = consultationDAO.findById(consultationId);
        if (consultation == null) {
            throw new ResourceNotFoundException("Consultation was not found.");
        }

        TransactionRunner.inTransaction(conn -> {
            ensureSoftDeleteColumns(conn);
            deleteByConsultationId(conn, "lab_results", "consultation_id", consultationId);
            deleteByConsultationId(conn, "invoices", "consultation_id", consultationId);
            deleteByConsultationId(conn, "medical_records", "consultation_id", consultationId);
            deleteConsultation(conn, consultationId);
            return null;
        });
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

    private void upsertMedicalRecord(Connection conn, Integer patientId, Integer consultationId,
                                     String normalizedPrescription, LocalDateTime now) throws Exception {
        MedicalRecord existing = findMedicalRecordByConsultationId(conn, consultationId);

        if (existing == null) {
            if (normalizedPrescription.isBlank()) {
                return;
            }
            insertMedicalRecord(conn, patientId, consultationId, normalizedPrescription, now);
            return;
        }

        updateMedicalRecord(conn, existing.getRecordId(), patientId, consultationId, normalizedPrescription, now);
    }

    private void upsertInvoice(Connection conn, Integer patientId, Integer consultationId,
                               Float invoiceAmount, LocalDateTime now) throws Exception {
        Invoice existing = findInvoiceByConsultationId(conn, consultationId);

        if (existing == null) {
            if (invoiceAmount == null || invoiceAmount <= 0) {
                return;
            }
            insertInvoice(conn, patientId, consultationId, invoiceAmount, "UNPAID", now, null);
            return;
        }

        if (invoiceAmount == null || invoiceAmount <= 0) {
            return;
        }

        updateInvoice(conn, existing.getInvoiceId(), patientId, consultationId, invoiceAmount,
                existing.getInvoiceStatus(), existing.getIssuedAt(), existing.getPaidAt());
    }

    private Integer intValue(DefaultTableModel model, int row, String columnName) {
        return TableModelUtil.intValue(model, row, columnName);
    }

    private String stringValue(DefaultTableModel model, int row, String columnName) {
        return TableModelUtil.stringValue(model, row, columnName);
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
        return TableModelUtil.value(model, row, columnName);
    }

    private DefaultTableModel loadAppointmentOptions(String sql, Integer doctorId) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareConsultationQuery(conn, sql)) {
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

    private Consultation findConsultationByAppointmentId(Connection conn, Integer appointmentId) throws SQLException {
        String sql = """
                SELECT * FROM consultation
                WHERE appointment_id = ?
                  AND COALESCE(delete_flag, FALSE) = FALSE
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapConsultation(rs);
                }
                return null;
            }
        }
    }

    private Integer insertConsultation(Connection conn, Integer appointmentId, String diagnosis,
                                       String notes, LocalDateTime createdAt) throws SQLException {
        String sql = """
                INSERT INTO consultation (appointment_id, diagnosis, notes, created_at)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appointmentId);
            ps.setString(2, diagnosis);
            ps.setString(3, notes);
            ps.setTimestamp(4, Timestamp.valueOf(createdAt));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Consultation insert did not return an ID.");
    }

    private void updateConsultation(Connection conn, Integer consultationId, Integer appointmentId,
                                    String diagnosis, String notes, LocalDateTime createdAt) throws SQLException {
        String sql = """
                UPDATE consultation
                SET appointment_id = ?, diagnosis = ?, notes = ?, created_at = ?
                WHERE consultation_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.setString(2, diagnosis);
            ps.setString(3, notes);
            ps.setTimestamp(4, Timestamp.valueOf(createdAt));
            ps.setInt(5, consultationId);
            ps.executeUpdate();
        }
    }

    private void deleteConsultation(Connection conn, Integer consultationId) throws SQLException {
        String sql = """
                UPDATE consultation
                SET delete_flag = TRUE
                WHERE consultation_id = ?
                  AND COALESCE(delete_flag, FALSE) = FALSE
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, consultationId);
            ps.executeUpdate();
        }
    }

    private MedicalRecord findMedicalRecordByConsultationId(Connection conn, Integer consultationId) throws SQLException {
        String sql = """
                SELECT * FROM medical_records
                WHERE consultation_id = ?
                  AND COALESCE(delete_flag, FALSE) = FALSE
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, consultationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMedicalRecord(rs);
                }
                return null;
            }
        }
    }

    private void insertMedicalRecord(Connection conn, Integer patientId, Integer consultationId,
                                     String prescription, LocalDateTime createdAt) throws SQLException {
        String sql = """
                INSERT INTO medical_records (patient_id, consultation_id, prescription, created_at)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, consultationId);
            ps.setString(3, prescription);
            ps.setTimestamp(4, Timestamp.valueOf(createdAt));
            ps.executeUpdate();
        }
    }

    private void updateMedicalRecord(Connection conn, Integer recordId, Integer patientId,
                                     Integer consultationId, String prescription, LocalDateTime createdAt) throws SQLException {
        String sql = """
                UPDATE medical_records
                SET patient_id = ?, consultation_id = ?, prescription = ?, created_at = ?
                WHERE record_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, consultationId);
            ps.setString(3, prescription);
            ps.setTimestamp(4, Timestamp.valueOf(createdAt));
            ps.setInt(5, recordId);
            ps.executeUpdate();
        }
    }

    private Invoice findInvoiceByConsultationId(Connection conn, Integer consultationId) throws SQLException {
        String sql = """
                SELECT * FROM invoices
                WHERE consultation_id = ?
                  AND COALESCE(delete_flag, FALSE) = FALSE
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, consultationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapInvoice(rs);
                }
                return null;
            }
        }
    }

    private void insertInvoice(Connection conn, Integer patientId, Integer consultationId, Float amount,
                               String status, LocalDateTime issuedAt, LocalDateTime paidAt) throws SQLException {
        String sql = """
                INSERT INTO invoices (patient_id, consultation_id, amount, invoice_status, issued_at, paid_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, consultationId);
            ps.setFloat(3, amount);
            ps.setString(4, status);
            ps.setTimestamp(5, Timestamp.valueOf(issuedAt));
            ps.setTimestamp(6, paidAt == null ? null : Timestamp.valueOf(paidAt));
            ps.executeUpdate();
        }
    }

    private void updateInvoice(Connection conn, Integer invoiceId, Integer patientId, Integer consultationId,
                               Float amount, String status, LocalDateTime issuedAt, LocalDateTime paidAt) throws SQLException {
        String sql = """
                UPDATE invoices
                SET patient_id = ?, consultation_id = ?, amount = ?, invoice_status = ?, issued_at = ?, paid_at = ?
                WHERE invoice_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, consultationId);
            ps.setFloat(3, amount);
            ps.setString(4, status);
            ps.setTimestamp(5, Timestamp.valueOf(issuedAt));
            ps.setTimestamp(6, paidAt == null ? null : Timestamp.valueOf(paidAt));
            ps.setInt(7, invoiceId);
            ps.executeUpdate();
        }
    }

    private void updateAppointmentStatus(Connection conn, Integer appointmentId, String status) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        }
    }

    private void deleteByConsultationId(Connection conn, String tableName, String columnName, Integer consultationId) throws SQLException {
        String sql = "UPDATE " + tableName + " SET delete_flag = TRUE WHERE " + columnName
                + " = ? AND COALESCE(delete_flag, FALSE) = FALSE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, consultationId);
            ps.executeUpdate();
        }
    }

    private PreparedStatement prepareConsultationQuery(Connection conn, String sql) throws SQLException {
        ensureSoftDeleteColumns(conn);
        return conn.prepareStatement(sql);
    }

    private void ensureSoftDeleteColumns(Connection conn) throws SQLException {
        ensureColumn(conn, "appointments", "delete_flag",
                "ALTER TABLE appointments ADD COLUMN delete_flag BOOLEAN NOT NULL DEFAULT FALSE");
        ensureColumn(conn, "consultation", "delete_flag",
                "ALTER TABLE consultation ADD COLUMN delete_flag BOOLEAN NOT NULL DEFAULT FALSE");
        ensureColumn(conn, "medical_records", "delete_flag",
                "ALTER TABLE medical_records ADD COLUMN delete_flag BOOLEAN NOT NULL DEFAULT FALSE");
        ensureColumn(conn, "invoices", "delete_flag",
                "ALTER TABLE invoices ADD COLUMN delete_flag BOOLEAN NOT NULL DEFAULT FALSE");
        ensureColumn(conn, "lab_results", "delete_flag",
                "ALTER TABLE lab_results ADD COLUMN delete_flag BOOLEAN NOT NULL DEFAULT FALSE");
    }

    private void ensureColumn(Connection conn, String tableName, String columnName, String alterSql) throws SQLException {
        if (hasColumn(conn, tableName, columnName)) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(alterSql);
        } catch (SQLException ex) {
            if (!hasColumn(conn, tableName, columnName)) {
                throw ex;
            }
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet columns = conn.getMetaData().getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return columns.next();
        }
    }

    private Consultation mapConsultation(ResultSet rs) throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setConsultationId(rs.getInt("consultation_id"));
        consultation.setAppointmentId(rs.getInt("appointment_id"));
        consultation.setDiagnosis(rs.getString("diagnosis"));
        consultation.setNotes(rs.getString("notes"));
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            consultation.setCreatedAt(timestamp.toLocalDateTime());
        }
        return consultation;
    }

    private MedicalRecord mapMedicalRecord(ResultSet rs) throws SQLException {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setRecordId(rs.getInt("record_id"));
        medicalRecord.setPatientId(rs.getInt("patient_id"));
        medicalRecord.setConsultationId(rs.getInt("consultation_id"));
        medicalRecord.setPrescription(rs.getString("prescription"));
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            medicalRecord.setCreatedAt(timestamp.toLocalDateTime());
        }
        return medicalRecord;
    }

    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getInt("invoice_id"));
        invoice.setPatientId(rs.getInt("patient_id"));
        invoice.setConsultationId(rs.getInt("consultation_id"));
        invoice.setAmount(rs.getFloat("amount"));
        invoice.setInvoiceStatus(rs.getString("invoice_status"));
        Timestamp issuedAt = rs.getTimestamp("issued_at");
        if (issuedAt != null) {
            invoice.setIssuedAt(issuedAt.toLocalDateTime());
        }
        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) {
            invoice.setPaidAt(paidAt.toLocalDateTime());
        }
        return invoice;
    }
}
