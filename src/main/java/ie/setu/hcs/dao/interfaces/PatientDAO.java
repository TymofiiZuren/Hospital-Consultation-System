// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;
import java.sql.SQLException;

// defining PatientDAO interface extending generic Dao
public interface PatientDAO extends Dao<Patient> {
    // creating findByAccountId method
    Patient findByAccountId(Integer accountId) throws SQLException;
    // creating findByMedRecordNum method
    Patient findByMedRecordNum(Integer medRecordNum) throws SQLException;
}
