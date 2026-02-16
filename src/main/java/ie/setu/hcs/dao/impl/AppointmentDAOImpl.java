// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.AppointmentDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Implementing AppointmentDAOImpl with implementation of AppointmentDAO
public class AppointmentDAOImpl implements AppointmentDAO {
    // CREATE
    @Override
    public void save(Appointment appointment) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO appointments (patient_id, doctor_id, appointment_datetime, status) VALUES (?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, Timestamp.valueOf(appointment.getDate()));
            pstmt.setString(4, appointment.getStatus());

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the appointmentId taken from executed query
                appointment.setAppointmentId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public Appointment findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";

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
                // display the appointment
                return mapRowToAppointment(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM appointments";
        // creating model to store in swing
        DefaultTableModel model = new DefaultTableModel();

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

    // FIND UPCOMING BY PATIENT ID
    @Override
    public DefaultTableModel findUpByPatientId(Integer patientId)
            throws SQLException {

        // creating sql variable with sql statement
        String sql = """
        SELECT appointment_id,
               doctor_id,
               appointment_datetime,
               status
        FROM appointments
        WHERE patient_id = ?
          AND appointment_datetime >= NOW()
          AND status IN ('Pending', 'Accepted')
        ORDER BY appointment_datetime ASC
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

    // UPDATE
    @Override
    public void update(Appointment appointment) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE appointments SET patient_id = ?,
                                        doctor_id = ?,
                                        appointment_datetime = ?,
                                        status = ?
                                  WHERE appointment_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, Timestamp.valueOf(appointment.getDate()));
            pstmt.setString(4, appointment.getStatus());
            pstmt.setInt(5, appointment.getAppointmentId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";

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
                "SELECT * FROM appointments WHERE patient_id = ?";

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

    // FIND BY DOCTOR ID
    @Override
    public DefaultTableModel findByDoctorId(Integer doctorId)
            throws SQLException {

        // creating sql variable with sql statement
        String sql =
                "SELECT * FROM appointments WHERE doctor_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, doctorId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // FIND BY STATUS
    @Override
    public DefaultTableModel findByStatus(String status)
            throws SQLException {

        // creating sql variable with sql statement
        String sql =
                "SELECT * FROM appointments WHERE status = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setString(1, status);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // UPDATE STATUS
    @Override
    public void updateStatus(Integer appointmentId,
                             String status)
            throws SQLException {

        // creating sql variable with sql statement
        String sql =
                "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setString(1, status);
            ps.setInt(2, appointmentId);

            // execute the query
            ps.executeUpdate();
        }
    }


    // mapping the appointment to the result set and display the output
    private Appointment mapRowToAppointment(ResultSet rs)
            throws SQLException {

        // creating a new appointment object
        Appointment appointment = new Appointment();

        // mapping appointment id to an object
        appointment.setAppointmentId(
                rs.getInt("appointment_id"));

        // mapping patient id to an object
        appointment.setPatientId(
                rs.getInt("patient_id"));

        // mapping doctor id to an object
        appointment.setDoctorId(
                rs.getInt("doctor_id"));

        // taking timestamp from the result set
        Timestamp timestamp =
                rs.getTimestamp("appointment_datetime");

        // validating the result set
        if (timestamp != null) {
            // mapping the timestamp to an object
            appointment.setDate(
                    timestamp.toLocalDateTime());
        }

        // mapping status to an object
        appointment.setStatus(
                rs.getString("status"));

        // returning the appointment information
        return appointment;
    }
}
