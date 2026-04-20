// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;

import java.sql.SQLException;

// defining DepartmentDAO interface extending generic Dao
public interface DepartmentDAO {
    // creating findByName method
    Integer findByName(String name) throws SQLException;
}
