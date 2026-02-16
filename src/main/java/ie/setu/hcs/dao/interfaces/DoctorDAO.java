// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

// defining DoctorDAO interface extending generic Dao
public interface DoctorDAO extends Dao<Doctor>{
    // creating findByAccountId method
    Doctor findByAccountId(Integer accountId) throws SQLException;
    // creating findByDepId method
    DefaultTableModel findByDepId(Integer depId) throws SQLException;
    // creating findBySpec method
    DefaultTableModel findBySpec(String spec) throws SQLException;
}
