// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing AccountDAOImpl with implementation of AccountDAO
public class AccountDAOImpl implements AccountDAO {
    // CREATE
    @Override
    public void save(Account account) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO accounts (email, password_hash, role_id, last_name, first_name, ppsn, phone, gender, is_active, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setString(1, account.getEmail());
            pstmt.setString(2, account.getPasswordHash());
            pstmt.setInt(3, account.getRoleId());
            pstmt.setString(4, account.getLastName());
            pstmt.setString(5, account.getFirstName());
            pstmt.setString(6, account.getPpsn());
            pstmt.setString(7, account.getPhone());
            pstmt.setString(8, account.getGender());
            pstmt.setBoolean(9, account.isActive());
            pstmt.setTimestamp(10, Timestamp.valueOf(account.getCreatedAt()));

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the accountId taken from executed query
                account.setAccountId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public Account findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM accounts WHERE account_id = ?";

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
                // display the account
                return mapRowToAccount(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM accounts";

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
    public void update(Account account) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE accounts SET email = ?,
                                    password_hash = ?,
                                    role_id = ?,
                                    last_name = ?,
                                    first_name = ?,
                                    ppsn = ?,
                                    phone = ?,
                                    gender = ?,
                                    is_active = ?,
                                    created_at = ?
                              WHERE account_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setString(1, account.getEmail());
            pstmt.setString(2, account.getPasswordHash());
            pstmt.setInt(3, account.getRoleId());
            pstmt.setString(4, account.getLastName());
            pstmt.setString(5, account.getFirstName());
            pstmt.setString(6, account.getPpsn());
            pstmt.setString(7, account.getPhone());
            pstmt.setString(8, account.getGender());
            pstmt.setBoolean(9, account.isActive());
            pstmt.setTimestamp(10, Timestamp.valueOf(account.getCreatedAt()));
            pstmt.setInt(11, account.getAccountId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM accounts WHERE account_id = ?";

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

    // FIND BY EMAIL
    @Override
    public Account findByEmail(String email) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM accounts WHERE email = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setString(1, email);

            // creating result set from the query
            ResultSet rs = pstmt.executeQuery();

            // validate the result set
            if (rs.next()) {
                // display the account
                return mapRowToAccount(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // EXISTS BY EMAIL
    @Override
    public Boolean existsByEmail(String email) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT COUNT(*) FROM accounts WHERE email = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setString(1, email);

            // creating result set from the query
            ResultSet rs = pstmt.executeQuery();

            // validate the result set
            if (rs.next()) {
                // return true if count is greater than 0
                return rs.getInt(1) > 0;
            }
        }

        // return false if nothing
        return false;
    }

    // FIND BY ROLE ID
    @Override
    public DefaultTableModel findByRoleId(Integer roleId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM accounts WHERE role_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, roleId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // DEACTIVATE
    @Override
    public void deactivate(Integer accountId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "UPDATE accounts SET is_active = FALSE WHERE account_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, accountId);

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // mapping the account to the result set and display the output
    private Account mapRowToAccount(ResultSet rs)
            throws SQLException {

        // creating a new account object
        Account account = new Account();

        // mapping account id to an object
        account.setAccountId(
                rs.getInt("account_id"));

        // mapping email to an object
        account.setEmail(
                rs.getString("email"));

        // mapping password hash to an object
        account.setPasswordHash(
                rs.getString("password_hash"));

        // mapping role id to an object
        account.setRoleId(
                rs.getInt("role_id"));

        // mapping last name to an object
        account.setLastName(
                rs.getString("last_name"));

        // mapping first name to an object
        account.setFirstName(
                rs.getString("first_name"));

        // mapping ppsn to an object
        account.setPpsn(
                rs.getString("ppsn"));

        // mapping phone to an object
        account.setPhone(
                rs.getString("phone"));

        // mapping gender to an object
        account.setGender(
                rs.getString("gender"));

        // mapping is_active to an object
        account.setActive(
                rs.getBoolean("is_active"));

        // taking timestamp from the result set
        Timestamp timestamp =
                rs.getTimestamp("created_at");

        // validating the result set
        if (timestamp != null) {
            // mapping the timestamp to an object
            account.setCreatedAt(
                    timestamp.toLocalDateTime());
        }

        // returning the account information
        return account;
    }
}
