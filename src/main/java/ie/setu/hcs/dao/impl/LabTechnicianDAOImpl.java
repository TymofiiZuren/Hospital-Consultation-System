// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.LabTechnicianDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing LabTechnicianDAOImpl with implementation of LabTechnicianDAO
public class LabTechnicianDAOImpl implements LabTechnicianDAO {
    // CREATE
    @Override
    public void save(LabTechnician labTechnician) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO lab_technicians (account_id, qualification, employee_num, lab_name, shift)
                VALUES (?, ?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, labTechnician.getAccountId());
            pstmt.setString(2, labTechnician.getQualification());
            pstmt.setString(3, labTechnician.getEmployeeNum());
            pstmt.setString(4, labTechnician.getLabName());
            pstmt.setString(5, labTechnician.getShift());

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the technicianId taken from executed query
                labTechnician.setTechnicianId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public LabTechnician findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM lab_technicians WHERE technician_id = ?";

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
                // display the lab technician
                return mapRowToLabTechnician(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM lab_technicians";

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
    public void update(LabTechnician labTechnician) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE lab_technicians SET account_id = ?,
                                           qualification = ?,
                                           employee_num = ?,
                                           lab_name = ?,
                                           shift = ?
                              WHERE technician_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, labTechnician.getAccountId());
            pstmt.setString(2, labTechnician.getQualification());
            pstmt.setString(3, labTechnician.getEmployeeNum());
            pstmt.setString(4, labTechnician.getLabName());
            pstmt.setString(5, labTechnician.getShift());
            pstmt.setInt(6, labTechnician.getTechnicianId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM lab_technicians WHERE technician_id = ?";

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

    // FIND BY ACCOUNT ID
    @Override
    public LabTechnician findByAccountId(Integer accountId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM lab_technicians WHERE account_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, accountId);

            // creating result set from the query
            ResultSet rs = pstmt.executeQuery();

            // validate the result set
            if (rs.next()) {
                // display the lab technician
                return mapRowToLabTechnician(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // FIND BY LAB NAME
    @Override
    public DefaultTableModel findByLabName(String labName) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM lab_technicians WHERE lab_name = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setString(1, labName);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // mapping the lab technician to the result set and display the output
    private LabTechnician mapRowToLabTechnician(ResultSet rs)
            throws SQLException {

        // creating a new lab technician object
        LabTechnician labTechnician = new LabTechnician();

        // mapping technician id to an object
        labTechnician.setTechnicianId(
                rs.getInt("technician_id"));

        // mapping account id to an object
        labTechnician.setAccountId(
                rs.getInt("account_id"));

        // mapping qualification to an object
        labTechnician.setQualification(
                rs.getString("qualification"));

        // mapping employee number to an object
        labTechnician.setEmployeeNum(
                rs.getString("employee_num"));

        // mapping lab name to an object
        labTechnician.setLabName(
                rs.getString("lab_name"));

        // mapping shift to an object
        labTechnician.setShift(
                rs.getString("shift"));

        // returning the lab technician information
        return labTechnician;
    }
}
