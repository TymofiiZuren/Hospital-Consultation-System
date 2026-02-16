// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.LabResultDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing LabResultDAOImpl with implementation of LabResultDAO
public class LabResultDAOImpl implements LabResultDAO {
    // CREATE
    @Override
    public void save(LabResult labResult) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO lab_results (consultation_id, technician_id, test_type, result, uploaded_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, labResult.getConsultationId());
            pstmt.setInt(2, labResult.getTechnicianId());
            pstmt.setString(3, labResult.getTestType());
            pstmt.setString(4, labResult.getResult());
            pstmt.setTimestamp(5, Timestamp.valueOf(labResult.getUploadedAt()));

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the labResultId taken from executed query
                labResult.setLabResultId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public LabResult findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM lab_results WHERE lab_result_id = ?";

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
                // display the lab result
                return mapRowToLabResult(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM lab_results";

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
    public void update(LabResult labResult) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE lab_results SET consultation_id = ?,
                                       technician_id = ?,
                                       test_type = ?,
                                       result = ?,
                                       uploaded_at = ?
                              WHERE lab_result_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, labResult.getConsultationId());
            pstmt.setInt(2, labResult.getTechnicianId());
            pstmt.setString(3, labResult.getTestType());
            pstmt.setString(4, labResult.getResult());
            pstmt.setTimestamp(5, Timestamp.valueOf(labResult.getUploadedAt()));
            pstmt.setInt(6, labResult.getLabResultId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM lab_results WHERE lab_result_id = ?";

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

    // FIND BY CONSULTATION ID
    @Override
    public DefaultTableModel findByConsultationId(Integer consultationId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM lab_results WHERE consultation_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, consultationId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // FIND BY TECHNICIAN ID
    @Override
    public DefaultTableModel findByTechnicianId(Integer technicianId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM lab_results WHERE technician_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, technicianId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // mapping the lab result to the result set and display the output
    private LabResult mapRowToLabResult(ResultSet rs)
            throws SQLException {

        // creating a new lab result object
        LabResult labResult = new LabResult();

        // mapping lab result id to an object
        labResult.setLabResultId(
                rs.getInt("lab_result_id"));

        // mapping consultation id to an object
        labResult.setConsultationId(
                rs.getInt("consultation_id"));

        // mapping technician id to an object
        labResult.setTechnicianId(
                rs.getInt("technician_id"));

        // mapping test type to an object
        labResult.setTestType(
                rs.getString("test_type"));

        // mapping result to an object
        labResult.setResult(
                rs.getString("result"));

        // taking timestamp from the result set
        Timestamp timestamp =
                rs.getTimestamp("uploaded_at");

        // validating the result set
        if (timestamp != null) {
            // mapping the timestamp to an object
            labResult.setUploadedAt(
                    timestamp.toLocalDateTime());
        }

        // returning the lab result information
        return labResult;
    }
}
