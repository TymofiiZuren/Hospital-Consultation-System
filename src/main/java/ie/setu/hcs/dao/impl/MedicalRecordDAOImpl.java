// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.MedicalRecordDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing MedicalRecordDAOImpl with implementation of MedicalRecordDAO
public class MedicalRecordDAOImpl implements MedicalRecordDAO {
    // CREATE
    @Override
    public void save(MedicalRecord medicalRecord) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO medical_records (patient_id, consultation_id, prescription, created_at) VALUES (?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, medicalRecord.getPatientId());
            pstmt.setInt(2, medicalRecord.getConsultationId());
            pstmt.setString(3, medicalRecord.getPrescription());
            pstmt.setTimestamp(4, Timestamp.valueOf(medicalRecord.getCreatedAt()));

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the recordId taken from executed query
                medicalRecord.setRecordId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public MedicalRecord findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM medical_records WHERE record_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, id);

            // creating result set from the query
            ResultSet rs = pstmt.executeQuery();

            // validate the result set
            if (rs.next()) {
                // display the medical record
                return mapRowToMedicalRecord(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM medical_records";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        // executing the query
        try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

            // returning the model from query
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // UPDATE
    @Override
    public void update(MedicalRecord medicalRecord) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE medical_records SET patient_id = ?,
                                        consultation_id = ?,
                                        prescription = ?,
                                        created_at = ?
                                  WHERE record_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, medicalRecord.getPatientId());
            pstmt.setInt(2, medicalRecord.getConsultationId());
            pstmt.setString(3, medicalRecord.getPrescription());
            pstmt.setTimestamp(4, Timestamp.valueOf(medicalRecord.getCreatedAt()));
            pstmt.setInt(5, medicalRecord.getRecordId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM medical_records WHERE record_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, id);

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // FIND BY PATIENT ID
    @Override
    public DefaultTableModel findByPatientId(Integer patientId)
            throws SQLException {

        // creating sql variable with sql statement
        String sql =
                "SELECT * FROM medical_records WHERE patient_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, patientId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // FIND BY CONSULTATION ID
    @Override
    public DefaultTableModel findByConsultationId(Integer consultationId)
            throws SQLException {

        // creating sql variable with sql statement
        String sql =
                "SELECT * FROM medical_records WHERE consultation_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, consultationId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // mapping the medical record to the result set and display the output
    private MedicalRecord mapRowToMedicalRecord(ResultSet rs)
            throws SQLException {

        // creating a new medical record object
        MedicalRecord medicalRecord = new MedicalRecord();

        // mapping record id to an object
        medicalRecord.setRecordId(
                rs.getInt("record_id"));

        // mapping patient id to an object
        medicalRecord.setPatientId(
                rs.getInt("patient_id"));

        // mapping consultation id to an object
        medicalRecord.setConsultationId(
                rs.getInt("consultation_id"));

        // mapping prescription to an object
        medicalRecord.setPrescription(
                rs.getString("prescription"));

        // taking timestamp from the result set
        Timestamp timestamp =
                rs.getTimestamp("created_at");

        // validating the result set
        if (timestamp != null) {
            // mapping the timestamp to an object
            medicalRecord.setCreatedAt(
                    timestamp.toLocalDateTime());
        }

        // returning the medical record information
        return medicalRecord;
    }
}
