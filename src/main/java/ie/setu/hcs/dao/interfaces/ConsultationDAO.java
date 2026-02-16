// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;
import java.sql.SQLException;
import java.util.ArrayList;

// defining ConsultationDAO interface with generic Dao
public interface ConsultationDAO extends Dao<Consultation>{
    // creating findByAppointmentId method
    Consultation findByAppointmentId(Integer appointmentId) throws SQLException;
    // creating findByPatientId
    ArrayList<Consultation> findByPatientId(Integer patientId) throws SQLException;
}
