// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.PatientDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing PatientDAOImpl with implementation of PatientDAO
public class PatientDAOImpl implements PatientDAO {
    // CREATE
    @Override
    public void save(Patient patient) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO patients (account_id, date_of_birth, address, eircode, blood_type, medical_record_number) VALUES (?, ?, ?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, patient.getAccountId());
            pstmt.setDate(2, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(3, patient.getAddress());
            pstmt.setString(4, patient.getEircode());
            pstmt.setString(5, patient.getBloodType());
            pstmt.setString(6, patient.getMedicalRecordNum());

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the patientId taken from executed query
                patient.setPatientId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public Patient findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

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
                // display the patient
                return mapRowToPatient(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM patients";

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
    public void update(Patient patient) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE patients SET account_id = ?,
                                        date_of_birth = ?,
                                        address = ?,
                                        eircode = ?,
                                        blood_type = ?,
                                        medical_record_num = ?
                                  WHERE patient_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, patient.getAccountId());
            pstmt.setDate(2, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(3, patient.getAddress());
            pstmt.setString(4, patient.getEircode());
            pstmt.setString(5, patient.getBloodType());
            pstmt.setString(6, patient.getMedicalRecordNum());
            pstmt.setInt(7, patient.getPatientId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM patients WHERE patient_id = ?";

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
    public Patient findByAccountId(Integer accountId)
            throws SQLException {

        // creating sql variable with sql statement
        String sql =
                "SELECT * FROM patients WHERE account_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, accountId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // validate the result set
            if (rs.next()) {
                // display the patient
                return mapRowToPatient(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // FIND BY MEDICAL RECORD NUMBER
    @Override
    public Patient findByMedRecordNum(Integer medRecordNum)
            throws SQLException {

        // creating sql variable with sql statement
        String sql =
                "SELECT * FROM patients WHERE medical_record_num = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setString(1, medRecordNum.toString());

            // execute the query
            ResultSet rs = ps.executeQuery();

            // validate the result set
            if (rs.next()) {
                // display the patient
                return mapRowToPatient(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // mapping the patient to the result set and display the output
    private Patient mapRowToPatient(ResultSet rs)
            throws SQLException {

        // creating a new patient object
        Patient patient = new Patient();

        // mapping patient id to an object
        patient.setPatientId(
                rs.getInt("patient_id"));

        // mapping account id to an object
        patient.setAccountId(
                rs.getInt("account_id"));

        // taking date from the result set
        Date date = rs.getDate("date_of_birth");

        // validating the result set
        if (date != null) {
            // mapping the date to an object
            patient.setDateOfBirth(
                    date.toLocalDate());
        }

        // mapping address to an object
        patient.setAddress(
                rs.getString("address"));

        // mapping eircode to an object
        patient.setEircode(
                rs.getString("eircode"));

        // mapping blood type to an object
        patient.setBloodType(
                rs.getString("blood_type"));

        // mapping medical record number to an object
        patient.setMedicalRecordNum(
                rs.getString("medical_record_num"));

        // returning the patient information
        return patient;
    }
}
