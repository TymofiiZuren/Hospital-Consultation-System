// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;

// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;

// defining LabResultDAO interface extending generic Dao
public interface LabResultDAO extends Dao<LabResult>{
    // creating findByConsultationId method
    DefaultTableModel findByConsultationId(Integer consultationId) throws SQLException;
    // creating findByTechnicianId method
    DefaultTableModel findByTechnicianId(Integer technicianId) throws SQLException;
}
