// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

// defining AccountDAO interface extending generic Dao
public interface AccountDAO extends Dao<Account> {
    // creating findByEmail method
    Account findByEmail(String email) throws SQLException;
    // creating existsByEmail method
    Boolean existsByEmail(String email) throws SQLException;
    // creating findByRoleId method
    DefaultTableModel findByRoleId(Integer roleId) throws SQLException;
    // creating deactivate method
    void deactivate(Integer accountId) throws SQLException;
}
