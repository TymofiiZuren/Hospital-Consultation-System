// defining package ie.setu.hcs.dao
package ie.setu.hcs.dao;

// importing stuff
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

// defining Dao interface
public interface Dao<T> {
    // creating save method
    void save(T entity) throws SQLException;
    // creating findById method
    T findById(Integer id) throws SQLException;
    // creating findAll method
    DefaultTableModel findAll() throws SQLException;
    // creating update method
    void update(T entity) throws SQLException;
    // creating delete method
    void delete(Integer id) throws SQLException;
}
