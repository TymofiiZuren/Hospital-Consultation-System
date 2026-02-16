// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.InsuranceDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing InsuranceDAOImpl with implementation of InsuranceDAO
public class InsuranceDAOImpl implements InsuranceDAO {
    // CREATE
    @Override
    public void save(Insurance insurance) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO insurance (patient_id, provider_name, policy_number, status, expiration_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, insurance.getPatientId());
            pstmt.setString(2, insurance.getProviderName());
            pstmt.setString(3, insurance.getPolicyNum());
            pstmt.setString(4, insurance.getStatus());
            pstmt.setDate(5, Date.valueOf(insurance.getExpirationDate()));

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the insuranceId taken from executed query
                insurance.setInsuranceId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public Insurance findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM insurance WHERE insurance_id = ?";

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
                // display the insurance
                return mapRowToInsurance(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM insurance";

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
    public void update(Insurance insurance) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE insurance SET patient_id = ?,
                                      provider_name = ?,
                                      policy_number = ?,
                                      status = ?,
                                      expiration_date = ?
                              WHERE insurance_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, insurance.getPatientId());
            pstmt.setString(2, insurance.getProviderName());
            pstmt.setString(3, insurance.getPolicyNum());
            pstmt.setString(4, insurance.getStatus());
            pstmt.setDate(5, Date.valueOf(insurance.getExpirationDate()));
            pstmt.setInt(6, insurance.getInsuranceId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM insurance WHERE insurance_id = ?";

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
    public DefaultTableModel findByPatientId(Integer patientId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM insurance WHERE patient_id = ?";

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

    // UPDATE STATUS
    @Override
    public void updateStatus(Integer insuranceId, String status) throws SQLException {
        // creating sql variable with sql statement
        String sql = "UPDATE insurance SET status = ? WHERE insurance_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setString(1, status);
            pstmt.setInt(2, insuranceId);

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // mapping the insurance to the result set and display the output
    private Insurance mapRowToInsurance(ResultSet rs)
            throws SQLException {

        // creating a new insurance object
        Insurance insurance = new Insurance();

        // mapping insurance id to an object
        insurance.setInsuranceId(
                rs.getInt("insurance_id"));

        // mapping patient id to an object
        insurance.setPatientId(
                rs.getInt("patient_id"));

        // mapping provider name to an object
        insurance.setProviderName(
                rs.getString("provider_name"));

        // mapping policy number to an object
        insurance.setPolicyNum(
                rs.getString("policy_num"));

        // mapping status to an object
        insurance.setStatus(
                rs.getString("status"));

        // taking date from the result set
        Date date = rs.getDate("expiration_date");

        // validating the result set
        if (date != null) {
            // mapping the date to an object
            insurance.setExpirationDate(
                    date.toLocalDate());
        }

        // returning the insurance information
        return insurance;
    }
}
