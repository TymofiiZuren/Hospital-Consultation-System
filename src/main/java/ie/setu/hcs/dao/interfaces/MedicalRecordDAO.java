// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;
import java.sql.SQLException;
import java.util.ArrayList;

// defining MedicalRecordDAO interface with generic Dao
public interface MedicalRecordDAO extends Dao<MedicalRecord> {
    // creating findByPatientId method
    ArrayList<MedicalRecord> findByPatientId(Integer patientId) throws SQLException;
    // creating findByConsultationId method
    ArrayList<MedicalRecord> findByConsultationId(Integer consultationId) throws SQLException;
}
