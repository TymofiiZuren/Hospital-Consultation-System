// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;
import java.sql.SQLException;
import java.util.ArrayList;

// defining LabTechnicianDAO interface with generic Dao
public interface LabTechnicianDAO extends Dao<LabTechnician>{
    // creating findByAccountId method
    LabTechnician findByAccountId(Integer accountId) throws SQLException;
    // creating findByLabName method
    ArrayList<LabTechnician> findByLabName(String labName) throws SQLException;
}
