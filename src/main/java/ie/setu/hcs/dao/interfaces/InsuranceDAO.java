// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

// defining DoctorDAO interface extending generic Dao
public interface InsuranceDAO extends Dao<Insurance>{
    // creating findByPatientId method
    DefaultTableModel findByPatientId(Integer patientId) throws SQLException;
    // creating updateStatus method
    void updateStatus(Integer insuranceId, String status) throws SQLException;
}
