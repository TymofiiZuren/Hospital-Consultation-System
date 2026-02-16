// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

// defining AdministratorDAO interface with generic Dao
public interface AdministratorDAO extends Dao<Administrator> {
    // creating findByAccountId method
    Administrator findByAccountId(Integer accountId) throws SQLException;
    // creating findByDepId method
    DefaultTableModel findByDepId(Integer depId) throws SQLException;
}
