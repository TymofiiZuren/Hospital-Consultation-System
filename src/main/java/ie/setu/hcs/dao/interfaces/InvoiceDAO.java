// defining package ie.setu.hcs.dao.interfaces
package ie.setu.hcs.dao.interfaces;
// importing stuff
import ie.setu.hcs.model.*;
import ie.setu.hcs.dao.Dao;
import java.sql.SQLException;
import java.util.ArrayList;

// defining InvoiceDAO interface with generic Dao
public interface InvoiceDAO extends Dao<Invoice> {
    // creating findByPatientId method
    ArrayList<Invoice> findByPatientId(Integer patientId) throws SQLException;
    // creating findByConsultationId method
    ArrayList<Invoice> findByConsultationId(Integer consultationId) throws SQLException;
    // creating markAsPaid method
    void markAsPaid(Integer invoiceId) throws SQLException;
    // creating findUnpaidByPatientId
    ArrayList<Invoice> findUnpaidByPatientId(Integer patientId) throws SQLException;
}
