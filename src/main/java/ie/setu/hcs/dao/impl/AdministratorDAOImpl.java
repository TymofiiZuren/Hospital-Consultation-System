// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.AdministratorDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing AdministratorDAOImpl with implementation of AdministratorDAO
public class AdministratorDAOImpl implements AdministratorDAO {
    // CREATE
    @Override
    public void save(Administrator administrator) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO administrators (account_id, job_title, employee_num, dep_id)
                VALUES (?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, administrator.getAccountId());
            pstmt.setString(2, administrator.getJobTitle());
            pstmt.setString(3, administrator.getEmployeeNum());
            pstmt.setInt(4, administrator.getDepId());

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the adminId taken from executed query
                administrator.setAdminId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public Administrator findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM administrators WHERE admin_id = ?";

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
                // display the administrator
                return mapRowToAdministrator(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM administrators";

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
    public void update(Administrator administrator) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE administrators SET account_id = ?,
                                         job_title = ?,
                                         employee_num = ?,
                                         dep_id = ?
                              WHERE admin_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, administrator.getAccountId());
            pstmt.setString(2, administrator.getJobTitle());
            pstmt.setString(3, administrator.getEmployeeNum());
            pstmt.setInt(4, administrator.getDepId());
            pstmt.setInt(5, administrator.getAdminId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM administrators WHERE admin_id = ?";

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
    public Administrator findByAccountId(Integer accountId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM administrators WHERE account_id = ?";

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
                // display the administrator
                return mapRowToAdministrator(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // FIND BY DEPARTMENT ID
    @Override
    public DefaultTableModel findByDepId(Integer depId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM administrators WHERE dep_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, depId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // mapping the administrator to the result set and display the output
    private Administrator mapRowToAdministrator(ResultSet rs)
            throws SQLException {

        // creating a new administrator object
        Administrator administrator = new Administrator();

        // mapping admin id to an object
        administrator.setAdminId(
                rs.getInt("admin_id"));

        // mapping account id to an object
        administrator.setAccountId(
                rs.getInt("account_id"));

        // mapping job title to an object
        administrator.setJobTitle(
                rs.getString("job_title"));

        // mapping employee number to an object
        administrator.setEmployeeNum(
                rs.getString("employee_num"));

        // mapping department id to an object
        administrator.setDepId(
                rs.getInt("dep_id"));

        // returning the administrator information
        return administrator;
    }
}
