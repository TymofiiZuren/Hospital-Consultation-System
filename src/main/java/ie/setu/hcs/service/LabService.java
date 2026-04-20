package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.AppointmentDAOImpl;
import ie.setu.hcs.dao.impl.ConsultationDAOImpl;
import ie.setu.hcs.dao.impl.LabResultDAOImpl;
import ie.setu.hcs.dao.impl.LabTechnicianDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.AppointmentDAO;
import ie.setu.hcs.dao.interfaces.ConsultationDAO;
import ie.setu.hcs.dao.interfaces.LabResultDAO;
import ie.setu.hcs.dao.interfaces.LabTechnicianDAO;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.exception.AuthorizationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Appointment;
import ie.setu.hcs.model.Consultation;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.model.LabResult;
import ie.setu.hcs.model.LabTechnician;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LabService {
    private final LabResultDAO labResultDAO = new LabResultDAOImpl();
    private final LabTechnicianDAO technicianDAO = new LabTechnicianDAOImpl();
    private final ConsultationDAO consultationDAO = new ConsultationDAOImpl();
    private final AppointmentDAO appointmentDAO = new AppointmentDAOImpl();
    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final AppointmentService appointmentService = new AppointmentService();

    public DefaultTableModel getResultsForPatient(Account account) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        String sql = """
                SELECT lr.lab_result_id,
                       CASE
                           WHEN c.consultation_id IS NOT NULL THEN CONCAT('Consultation | ', COALESCE(c.diagnosis, CONCAT('#', c.consultation_id)))
                           ELSE CONCAT('Appointment | ', a.appointment_datetime)
                       END AS source,
                       CONCAT(ta.first_name, ' ', ta.last_name) AS technician,
                       lr.test_type,
                       lr.result,
                       lr.uploaded_at
                FROM lab_results lr
                LEFT JOIN consultation c ON lr.consultation_id = c.consultation_id
                JOIN appointments a ON COALESCE(lr.appointment_id, c.appointment_id) = a.appointment_id
                LEFT JOIN lab_technicians t ON lr.technician_id = t.technician_id
                LEFT JOIN accounts ta ON t.account_id = ta.account_id
                WHERE a.patient_id = ?
                ORDER BY lr.uploaded_at DESC
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareLabQuery(conn, sql)) {
            ps.setInt(1, patient.getPatientId());
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    public DefaultTableModel getResultsForTechnician(Account account) throws Exception {
        LabTechnician technician = requireTechnician(account);
        String sql = """
                SELECT lr.lab_result_id,
                       CASE
                           WHEN c.consultation_id IS NOT NULL THEN CONCAT('Consultation | ', COALESCE(c.diagnosis, CONCAT('#', c.consultation_id)))
                           ELSE CONCAT('Appointment | ', a.appointment_datetime)
                       END AS source,
                       lr.test_type,
                       lr.result,
                       lr.uploaded_at
                FROM lab_results lr
                LEFT JOIN consultation c ON lr.consultation_id = c.consultation_id
                JOIN appointments a ON COALESCE(lr.appointment_id, c.appointment_id) = a.appointment_id
                WHERE lr.technician_id = ?
                ORDER BY lr.uploaded_at DESC
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareLabQuery(conn, sql)) {
            ps.setInt(1, technician.getTechnicianId());
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    public DefaultTableModel getResultsForDoctor(Account account) throws Exception {
        Doctor doctor = appointmentService.requireDoctor(account);
        String sql = """
                SELECT lr.lab_result_id,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       CASE
                           WHEN c.consultation_id IS NOT NULL THEN CONCAT('Consultation | ', COALESCE(c.diagnosis, CONCAT('#', c.consultation_id)))
                           ELSE CONCAT('Appointment | ', a.appointment_datetime)
                       END AS source,
                       CONCAT(ta.first_name, ' ', ta.last_name) AS technician,
                       lr.test_type,
                       lr.result,
                       lr.uploaded_at
                FROM lab_results lr
                LEFT JOIN consultation c ON lr.consultation_id = c.consultation_id
                JOIN appointments a ON COALESCE(lr.appointment_id, c.appointment_id) = a.appointment_id
                JOIN patients p ON a.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                LEFT JOIN lab_technicians t ON lr.technician_id = t.technician_id
                LEFT JOIN accounts ta ON t.account_id = ta.account_id
                WHERE a.doctor_id = ?
                ORDER BY lr.uploaded_at DESC
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareLabQuery(conn, sql)) {
            ps.setInt(1, doctor.getDoctorId());
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    public DefaultTableModel getAllResults() throws Exception {
        String sql = """
                SELECT lr.lab_result_id,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       CASE
                           WHEN c.consultation_id IS NOT NULL THEN CONCAT('Consultation | ', COALESCE(c.diagnosis, CONCAT('#', c.consultation_id)))
                           ELSE CONCAT('Appointment | ', a.appointment_datetime)
                       END AS source,
                       CONCAT(ta.first_name, ' ', ta.last_name) AS technician,
                       lr.test_type,
                       lr.result,
                       lr.uploaded_at
                FROM lab_results lr
                LEFT JOIN consultation c ON lr.consultation_id = c.consultation_id
                JOIN appointments a ON COALESCE(lr.appointment_id, c.appointment_id) = a.appointment_id
                JOIN patients p ON a.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                LEFT JOIN lab_technicians t ON lr.technician_id = t.technician_id
                LEFT JOIN accounts ta ON t.account_id = ta.account_id
                ORDER BY lr.uploaded_at DESC
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareLabQuery(conn, sql);
             ResultSet rs = ps.executeQuery()) {
            return TableModelUtil.buildTableModel(rs);
        }
    }

    public Integer uploadResult(Account account, Integer consultationId, String testType, String result)
            throws Exception {
        LabTechnician technician = requireTechnician(account);
        return saveResult(technician.getTechnicianId(), consultationId, null, testType, result);
    }

    public Integer saveResult(Integer technicianId, Integer consultationId, String testType, String result)
            throws Exception {
        return saveResult(technicianId, consultationId, null, testType, result);
    }

    public Integer saveResult(Integer technicianId, Integer consultationId, Integer appointmentId,
                              String testType, String result) throws Exception {
        if (technicianId == null) {
            throw new ValidationException("Technician is required.");
        }
        if (testType == null || testType.isBlank()) {
            throw new ValidationException("Test type is required.");
        }
        if (result == null || result.isBlank()) {
            throw new ValidationException("Result is required.");
        }

        ResolvedLabSource source = resolveSource(consultationId, appointmentId);

        LabResult labResult = new LabResult(
                source.consultationId(),
                source.appointmentId(),
                technicianId,
                testType.trim(),
                result.trim(),
                LocalDateTime.now()
        );
        labResultDAO.save(labResult);
        return labResult.getLabResultId();
    }

    public void updateResult(Integer labResultId, Integer technicianId, Integer consultationId, String testType, String result)
            throws Exception {
        updateResult(labResultId, technicianId, consultationId, null, testType, result);
    }

    public void updateResult(Integer labResultId, Integer technicianId, Integer consultationId, Integer appointmentId,
                             String testType, String result)
            throws Exception {
        if (labResultId == null) {
            throw new ValidationException("Please select a lab result first.");
        }
        if (technicianId == null) {
            throw new ValidationException("Technician is required.");
        }
        if (testType == null || testType.isBlank()) {
            throw new ValidationException("Test type is required.");
        }
        if (result == null || result.isBlank()) {
            throw new ValidationException("Result is required.");
        }

        LabResult labResult = labResultDAO.findById(labResultId);
        if (labResult == null) {
            throw new ResourceNotFoundException("Lab result was not found.");
        }

        ResolvedLabSource source = resolveSource(consultationId, appointmentId);
        labResult.setConsultationId(source.consultationId());
        labResult.setAppointmentId(source.appointmentId());
        labResult.setTechnicianId(technicianId);
        labResult.setTestType(testType.trim());
        labResult.setResult(result.trim());
        labResult.setUploadedAt(LocalDateTime.now());
        labResultDAO.update(labResult);
    }

    public void deleteResult(Integer labResultId) throws Exception {
        if (labResultId == null) {
            throw new ValidationException("Please select a lab result first.");
        }
        labResultDAO.delete(labResultId);
    }

    public LabResult findResultById(Integer labResultId) throws Exception {
        if (labResultId == null) {
            return null;
        }
        return labResultDAO.findById(labResultId);
    }

    public List<ResultSourceOption> getResultSourceOptions() throws Exception {
        String sql = """
                SELECT c.consultation_id,
                       a.appointment_id,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       c.diagnosis,
                       a.appointment_datetime
                FROM consultation c
                JOIN appointments a ON c.appointment_id = a.appointment_id
                JOIN patients p ON a.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                ORDER BY c.created_at DESC
                """;
        DefaultTableModel consultations;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareLabQuery(conn, sql);
             ResultSet rs = ps.executeQuery()) {
            consultations = TableModelUtil.buildTableModel(rs);
        }

        List<ResultSourceOption> options = new ArrayList<>();

        for (int row = 0; row < consultations.getRowCount(); row++) {
            Integer consultationId = intValue(consultations, row, "consultation_id");
            Integer appointmentId = intValue(consultations, row, "appointment_id");
            String patient = stringValue(consultations, row, "patient");
            Object diagnosis = value(consultations, row, "diagnosis");
            Object date = value(consultations, row, "appointment_datetime");
            StringBuilder label = new StringBuilder("Consultation #").append(consultationId);
            if (patient != null && !patient.isBlank()) {
                label.append(" | ").append(patient);
            }
            if (diagnosis != null) {
                label.append(" | ").append(diagnosis);
            }
            if (date != null) {
                label.append(" | ").append(date);
            }
            options.add(new ResultSourceOption(
                    consultationId,
                    appointmentId,
                    label.toString()
            ));
        }

        String appointmentSql = """
                SELECT a.appointment_id,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       a.appointment_datetime,
                       a.status,
                       a.medical_need
                FROM appointments a
                JOIN patients p ON a.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                LEFT JOIN consultation c ON c.appointment_id = a.appointment_id
                WHERE c.consultation_id IS NULL
                  AND LOWER(a.status) NOT IN ('cancelled', 'rejected')
                ORDER BY a.appointment_datetime DESC
                """;
        DefaultTableModel appointments;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareLabQuery(conn, appointmentSql);
             ResultSet rs = ps.executeQuery()) {
            appointments = TableModelUtil.buildTableModel(rs);
        }

        for (int row = 0; row < appointments.getRowCount(); row++) {
            Integer appointmentId = intValue(appointments, row, "appointment_id");
            String patient = stringValue(appointments, row, "patient");
            String status = stringValue(appointments, row, "status");
            String medicalNeed = stringValue(appointments, row, "medical_need");
            Object date = value(appointments, row, "appointment_datetime");
            StringBuilder label = new StringBuilder("Appointment #").append(appointmentId);
            if (!patient.isBlank()) {
                label.append(" | ").append(patient);
            }
            if (date != null) {
                label.append(" | ").append(date);
            }
            if (!medicalNeed.isBlank()) {
                label.append(" | ").append(medicalNeed);
            }
            if (!status.isBlank()) {
                label.append(" | ").append(status);
            }
            label.append(" | No consultation yet");

            options.add(new ResultSourceOption(
                    null,
                    appointmentId,
                    label.toString()
            ));
        }

        return options;
    }

    public List<ConsultationOption> getConsultationOptions() throws Exception {
        List<ResultSourceOption> sources = getResultSourceOptions();
        List<ConsultationOption> options = new ArrayList<>();
        for (ResultSourceOption source : sources) {
            if (source.consultationId() != null) {
                options.add(new ConsultationOption(source.consultationId(), source.label()));
            }
        }
        return options;
    }

    public LabTechnician requireTechnician(Account account) throws Exception {
        if (account == null || account.getAccountId() == null) {
            throw new AuthorizationException("No logged-in technician account was found.");
        }

        LabTechnician technician = technicianDAO.findByAccountId(account.getAccountId());
        if (technician == null) {
            throw new ResourceNotFoundException("Lab technician profile was not found for this account.");
        }
        return technician;
    }

    public List<TechnicianOption> getTechnicianOptions() throws Exception {
        DefaultTableModel technicians = technicianDAO.findAll();
        List<TechnicianOption> options = new ArrayList<>();

        for (int row = 0; row < technicians.getRowCount(); row++) {
            Integer technicianId = intValue(technicians, row, "technician_id");
            Integer accountId = intValue(technicians, row, "account_id");
            String labName = stringValue(technicians, row, "lab_name");

            Account account = accountDAO.findById(accountId);
            String label = account == null
                    ? "Technician #" + technicianId
                    : account.getFirstName() + " " + account.getLastName();

            if (!labName.isBlank()) {
                label += " | " + labName;
            }
            label += " (#" + technicianId + ")";

            options.add(new TechnicianOption(technicianId, label));
        }

        return options;
    }

    private Integer intValue(DefaultTableModel model, int row, String columnName) {
        Object value = value(model, row, columnName);
        return value == null ? null : Integer.parseInt(value.toString());
    }

    private String stringValue(DefaultTableModel model, int row, String columnName) {
        Object value = value(model, row, columnName);
        return value == null ? "" : value.toString();
    }

    private Object value(DefaultTableModel model, int row, String columnName) {
        int column = model.findColumn(columnName);
        return column < 0 ? null : model.getValueAt(row, column);
    }

    private PreparedStatement prepareLabQuery(Connection conn, String sql) throws SQLException {
        ensureLabSchema(conn);
        return conn.prepareStatement(sql);
    }

    private void ensureLabSchema(Connection conn) throws SQLException {
        ensureAppointmentIdColumn(conn);
        ensureConsultationIdNullable(conn);
    }

    private void ensureAppointmentIdColumn(Connection conn) throws SQLException {
        if (hasColumn(conn, "appointment_id")) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE lab_results ADD COLUMN appointment_id INT NULL");
        } catch (SQLException ex) {
            if (!hasColumn(conn, "appointment_id")) {
                throw ex;
            }
        }
    }

    private void ensureConsultationIdNullable(Connection conn) throws SQLException {
        try (ResultSet columns = conn.getMetaData().getColumns(conn.getCatalog(), null, "lab_results", "consultation_id")) {
            if (!columns.next()) {
                return;
            }

            if ("YES".equalsIgnoreCase(columns.getString("IS_NULLABLE"))) {
                return;
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE lab_results MODIFY COLUMN consultation_id INT NULL");
        } catch (SQLException ex) {
            try (ResultSet columns = conn.getMetaData().getColumns(conn.getCatalog(), null, "lab_results", "consultation_id")) {
                if (columns.next() && "YES".equalsIgnoreCase(columns.getString("IS_NULLABLE"))) {
                    return;
                }
            }
            throw ex;
        }
    }

    private boolean hasColumn(Connection conn, String columnName) throws SQLException {
        try (ResultSet columns = conn.getMetaData().getColumns(conn.getCatalog(), null, "lab_results", columnName)) {
            return columns.next();
        }
    }

    private ResolvedLabSource resolveSource(Integer consultationId, Integer appointmentId) throws Exception {
        if (consultationId == null && appointmentId == null) {
            throw new ValidationException("Please choose a consultation or appointment.");
        }

        Integer resolvedConsultationId = consultationId;
        Integer resolvedAppointmentId = appointmentId;

        if (consultationId != null) {
            Consultation consultation = consultationDAO.findById(consultationId);
            if (consultation == null) {
                throw new ResourceNotFoundException("Consultation was not found.");
            }
            resolvedAppointmentId = consultation.getAppointmentId();
        } else {
            Appointment appointment = appointmentDAO.findById(appointmentId);
            if (appointment == null) {
                throw new ResourceNotFoundException("Appointment was not found.");
            }
        }

        return new ResolvedLabSource(resolvedConsultationId, resolvedAppointmentId);
    }

    public record ConsultationOption(Integer consultationId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record ResultSourceOption(Integer consultationId, Integer appointmentId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record TechnicianOption(Integer technicianId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record ResolvedLabSource(Integer consultationId, Integer appointmentId) {}
}
