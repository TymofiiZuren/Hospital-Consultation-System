// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.DoctorDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing DoctorDAOImpl with implementation of DoctorDAO
public class DoctorDAOImpl implements DoctorDAO {
    // CREATE
    @Override
    public void save(Doctor doctor) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO doctors (account_id, specialization, license_number, years_of_experience, consultation_fee, dep_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, doctor.getAccountId());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setInt(3, doctor.getLicenseNum());
            pstmt.setInt(4, doctor.getYearsOfExperience());
            pstmt.setInt(5, doctor.getConsultationFee());
            pstmt.setInt(6, doctor.getDepId());

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the doctorId taken from executed query
                doctor.setDoctorId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public Doctor findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";

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
                // display the doctor
                return mapRowToDoctor(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM doctors";

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
    public void update(Doctor doctor) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE doctors SET account_id = ?,
                                   specialization = ?,
                                   license_number = ?,
                                   years_of_experience = ?,
                                   consultation_fee = ?,
                                   dep_id = ?
                              WHERE doctor_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, doctor.getAccountId());
            pstmt.setString(2, doctor.getSpecialization());
            pstmt.setInt(3, doctor.getLicenseNum());
            pstmt.setInt(4, doctor.getYearsOfExperience());
            pstmt.setInt(5, doctor.getConsultationFee());
            pstmt.setInt(6, doctor.getDepId());
            pstmt.setInt(7, doctor.getDoctorId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";

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
    public Doctor findByAccountId(Integer accountId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM doctors WHERE account_id = ?";

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
                // display the doctor
                return mapRowToDoctor(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // FIND BY DEPARTMENT ID
    @Override
    public DefaultTableModel findByDepId(Integer depId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM doctors WHERE dep_id = ?";

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

    // FIND BY SPECIALIZATION
    @Override
    public DefaultTableModel findBySpec(String spec) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM doctors WHERE specialization = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setString(1, spec);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // mapping the doctor to the result set and display the output
    private Doctor mapRowToDoctor(ResultSet rs)
            throws SQLException {

        // creating a new doctor object
        Doctor doctor = new Doctor();

        // mapping doctor id to an object
        doctor.setDoctorId(
                rs.getInt("doctor_id"));

        // mapping account id to an object
        doctor.setAccountId(
                rs.getInt("account_id"));

        // mapping specialization to an object
        doctor.setSpecialization(
                rs.getString("specialization"));

        // mapping license number to an object
        doctor.setLicenseNum(
                rs.getInt("license_num"));

        // mapping years of experience to an object
        doctor.setYearsOfExperience(
                rs.getInt("years_of_experience"));

        // mapping consultation fee to an object
        doctor.setConsultationFee(
                rs.getInt("consultation_fee"));

        // mapping department id to an object
        doctor.setDepId(
                rs.getInt("dep_id"));

        // returning the doctor information
        return doctor;
    }
}
