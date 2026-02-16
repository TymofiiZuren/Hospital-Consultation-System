// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

// defining ConsultationDAO interface with generic Dao
public interface ConsultationDAO extends Dao<Consultation>{
    // creating findByAppointmentId method
    Consultation findByAppointmentId(Integer appointmentId) throws SQLException;
    // creating findByPatientId
    DefaultTableModel findByPatientId(Integer patientId) throws SQLException;
}
