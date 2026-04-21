package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.ConsultationDAOImpl;
import ie.setu.hcs.dao.impl.MedicalRecordDAOImpl;
import ie.setu.hcs.dao.interfaces.ConsultationDAO;
import ie.setu.hcs.dao.interfaces.MedicalRecordDAO;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Consultation;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.model.MedicalRecord;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MedicalRecordService {
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAOImpl();
    private final ConsultationDAO consultationDAO = new ConsultationDAOImpl();
    private final AppointmentService appointmentService = new AppointmentService();

    public DefaultTableModel getRecordsForPatient(Account account) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        String sql = """
                SELECT mr.record_id,
                       mr.consultation_id,
                       c.appointment_id,
                       c.diagnosis,
                       c.notes,
                       mr.prescription,
                       mr.created_at
                FROM medical_records mr
                JOIN consultation c ON mr.consultation_id = c.consultation_id
                WHERE mr.patient_id = ?
                  AND COALESCE(mr.delete_flag, FALSE) = FALSE
                  AND COALESCE(c.delete_flag, FALSE) = FALSE
                ORDER BY mr.created_at DESC
                """;
        return loadRecords(sql, patient.getPatientId());
    }

    public DefaultTableModel getAllRecords() throws Exception {
        String sql = """
                SELECT mr.record_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS patient,
                       mr.consultation_id,
                       c.appointment_id,
                       c.diagnosis,
                       c.notes,
                       mr.prescription,
                       mr.created_at
                FROM medical_records mr
                JOIN consultation c ON mr.consultation_id = c.consultation_id
                LEFT JOIN patients p ON mr.patient_id = p.patient_id
                LEFT JOIN accounts a ON p.account_id = a.account_id
                WHERE COALESCE(mr.delete_flag, FALSE) = FALSE
                  AND COALESCE(c.delete_flag, FALSE) = FALSE
                ORDER BY mr.created_at DESC
                """;
        return loadRecords(sql, null);
    }

    public DefaultTableModel getRecordsForDoctor(Account account) throws Exception {
        Doctor doctor = appointmentService.requireDoctor(account);
        String sql = """
                SELECT mr.record_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS patient,
                       mr.consultation_id,
                       c.appointment_id,
                       c.diagnosis,
                       c.notes,
                       mr.prescription,
                       mr.created_at
                FROM medical_records mr
                JOIN consultation c ON mr.consultation_id = c.consultation_id
                JOIN appointments ap ON c.appointment_id = ap.appointment_id
                LEFT JOIN patients p ON mr.patient_id = p.patient_id
                LEFT JOIN accounts a ON p.account_id = a.account_id
                WHERE ap.doctor_id = ?
                  AND COALESCE(mr.delete_flag, FALSE) = FALSE
                  AND COALESCE(c.delete_flag, FALSE) = FALSE
                  AND COALESCE(ap.delete_flag, FALSE) = FALSE
                ORDER BY mr.created_at DESC
                """;

        return loadRecords(sql, doctor.getDoctorId());
    }

    public void updateRecord(Integer recordId, String diagnosis, String notes, String prescription) throws Exception {
        if (recordId == null) {
            throw new ValidationException("Please select a medical record first.");
        }

        MedicalRecord record = medicalRecordDAO.findById(recordId);
        if (record == null) {
            throw new ResourceNotFoundException("Medical record was not found.");
        }

        record.setPrescription(prescription == null ? "" : prescription.trim());
        medicalRecordDAO.update(record);

        if (record.getConsultationId() != null) {
            Consultation consultation = consultationDAO.findById(record.getConsultationId());
            if (consultation != null) {
                if (diagnosis == null || diagnosis.isBlank()) {
                    throw new ValidationException("Diagnosis is required.");
                }
                consultation.setDiagnosis(diagnosis.trim());
                consultation.setNotes(notes == null ? "" : notes.trim());
                consultationDAO.update(consultation);
            }
        }
    }

    public void deleteRecord(Integer recordId) throws Exception {
        if (recordId == null) {
            throw new ValidationException("Please select a medical record first.");
        }
        medicalRecordDAO.delete(recordId);
    }

    private DefaultTableModel loadRecords(String sql, Integer parameter) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = prepareRecordsQuery(conn, sql)) {
            if (parameter != null) {
                ps.setInt(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    private PreparedStatement prepareRecordsQuery(Connection conn, String sql) throws Exception {
        ensureDeleteFlagColumn(conn, "medical_records");
        ensureDeleteFlagColumn(conn, "consultation");
        ensureDeleteFlagColumn(conn, "appointments");
        return conn.prepareStatement(sql);
    }

    private void ensureDeleteFlagColumn(Connection conn, String tableName) throws Exception {
        if (hasColumn(conn, tableName, "delete_flag")) {
            return;
        }

        try (var stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN delete_flag BOOLEAN NOT NULL DEFAULT FALSE");
        } catch (Exception ex) {
            if (!hasColumn(conn, tableName, "delete_flag")) {
                throw ex;
            }
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws Exception {
        try (ResultSet columns = conn.getMetaData().getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return columns.next();
        }
    }
}
