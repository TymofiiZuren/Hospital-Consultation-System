// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

// defining AppointmentDao interface with generic Dao
public interface AppointmentDAO extends Dao<Appointment> {
    // creating findByPatientId method
    DefaultTableModel findByPatientId(Integer patientId) throws SQLException;
    // creating findByDoctorId method
    DefaultTableModel findByDoctorId(Integer doctorId) throws SQLException;
    // creating findByStatus method
    DefaultTableModel findByStatus(String status) throws SQLException;
    // creating updateStatus method
    void updateStatus(Integer appointmentId, String status) throws SQLException;
    // creating findUpByPatientId method
    DefaultTableModel findUpByPatientId(Integer patientId) throws SQLException;
}
