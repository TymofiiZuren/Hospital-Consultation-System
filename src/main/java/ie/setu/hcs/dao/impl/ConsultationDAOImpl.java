// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.ConsultationDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing ConsultationDAOImpl with implementation of ConsultationDAO
public class ConsultationDAOImpl implements ConsultationDAO {
    // CREATE
    @Override
    public void save(Consultation consultation) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO consultation (appointment_id, diagnosis, notes, created_at)
                VALUES (?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, consultation.getAppointmentId());
            pstmt.setString(2, consultation.getDiagnosis());
            pstmt.setString(3, consultation.getNotes());
            pstmt.setTimestamp(4, Timestamp.valueOf(consultation.getCreatedAt()));

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the consultationId taken from executed query
                consultation.setConsultationId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public Consultation findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM consultation WHERE consultation_id = ?";

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
                // display the consultation
                return mapRowToConsultation(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM consultation";

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
    public void update(Consultation consultation) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE consultation SET appointment_id = ?,
                                        diagnosis = ?,
                                        notes = ?,
                                        created_at = ?
                              WHERE consultation_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, consultation.getAppointmentId());
            pstmt.setString(2, consultation.getDiagnosis());
            pstmt.setString(3, consultation.getNotes());
            pstmt.setTimestamp(4, Timestamp.valueOf(consultation.getCreatedAt()));
            pstmt.setInt(5, consultation.getConsultationId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM consultation WHERE consultation_id = ?";

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

    // FIND BY APPOINTMENT ID
    @Override
    public Consultation findByAppointmentId(Integer appointmentId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM consultation WHERE appointment_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, appointmentId);

            // creating result set from the query
            ResultSet rs = pstmt.executeQuery();

            // validate the result set
            if (rs.next()) {
                // display the consultation
                return mapRowToConsultation(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // FIND BY PATIENT ID
    @Override
    public DefaultTableModel findByPatientId(Integer patientId) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                SELECT c.* FROM consultation c
                JOIN appointments a ON c.appointment_id = a.appointment_id
                WHERE a.patient_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, patientId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // mapping the consultation to the result set and display the output
    private Consultation mapRowToConsultation(ResultSet rs)
            throws SQLException {

        // creating a new consultation object
        Consultation consultation = new Consultation();

        // mapping consultation id to an object
        consultation.setConsultationId(
                rs.getInt("consultation_id"));

        // mapping appointment id to an object
        consultation.setAppointmentId(
                rs.getInt("appointment_id"));

        // mapping diagnosis to an object
        consultation.setDiagnosis(
                rs.getString("diagnosis"));

        // mapping notes to an object
        consultation.setNotes(
                rs.getString("notes"));

        // taking timestamp from the result set
        Timestamp timestamp =
                rs.getTimestamp("created_at");

        // validating the result set
        if (timestamp != null) {
            // mapping the timestamp to an object
            consultation.setCreatedAt(
                    timestamp.toLocalDateTime());
        }

        // returning the consultation information
        return consultation;
    }
}
