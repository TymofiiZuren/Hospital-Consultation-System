// defining package ie.setu.hcs.dao.impl
package ie.setu.hcs.dao.impl;

// importing stuff
import ie.setu.hcs.dao.interfaces.InvoiceDAO;
import ie.setu.hcs.model.*;
import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.*;

// Implementing InvoiceDAOImpl with implementation of InvoiceDAO
public class InvoiceDAOImpl implements InvoiceDAO {
    // CREATE
    @Override
    public void save(Invoice invoice) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                INSERT INTO invoices (patient_id, consultation_id, amount, invoice_status, issued_at, paid_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        // validating connection
        // setting up connection with the database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, invoice.getPatientId());
            pstmt.setInt(2, invoice.getConsultationId());
            pstmt.setFloat(3, invoice.getAmount());
            pstmt.setString(4, invoice.getInvoiceStatus());
            pstmt.setTimestamp(5, Timestamp.valueOf(invoice.getIssuedAt()));
            pstmt.setTimestamp(6, invoice.getPaidAt() != null ? Timestamp.valueOf(invoice.getPaidAt()) : null);

            // executing the query in the database
            pstmt.executeUpdate();

            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            // validating if there is a result
            if (rs.next()) {
                // inserting the invoiceId taken from executed query
                invoice.setInvoiceId(rs.getInt(1));
            }
        }
    }

    // READ BY ID
    @Override
    public Invoice findById(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, id);

            // creating result set from the query
            ResultSet rs = pstmt.executeQuery();

            // validate the result set
            if (rs.next()) {
                // display the invoice
                return mapRowToInvoice(rs);
            }
        }

        // return null if nothing
        return null;
    }

    // READ ALL
    @Override
    public DefaultTableModel findAll() throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM invoices";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        // executing the query
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // returning the model from query
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // UPDATE
    @Override
    public void update(Invoice invoice) throws SQLException {
        // creating sql variable with sql statement
        String sql = """
                UPDATE invoices SET patient_id = ?,
                                    consultation_id = ?,
                                    amount = ?,
                                    invoice_status = ?,
                                    issued_at = ?,
                                    paid_at = ?
                              WHERE invoice_id = ?
                """;

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, invoice.getPatientId());
            pstmt.setInt(2, invoice.getConsultationId());
            pstmt.setFloat(3, invoice.getAmount());
            pstmt.setString(4, invoice.getInvoiceStatus());
            pstmt.setTimestamp(5, Timestamp.valueOf(invoice.getIssuedAt()));
            pstmt.setTimestamp(6, invoice.getPaidAt() != null ? Timestamp.valueOf(invoice.getPaidAt()) : null);
            pstmt.setInt(7, invoice.getInvoiceId());

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // DELETE
    @Override
    public void delete(Integer id) throws SQLException {
        // creating sql variable with sql statement
        String sql = "DELETE FROM invoices WHERE invoice_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setInt(1, id);

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // FIND BY PATIENT ID
    @Override
    public DefaultTableModel findByPatientId(Integer patientId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM invoices WHERE patient_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, patientId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // FIND BY CONSULTATION ID
    @Override
    public DefaultTableModel findByConsultationId(Integer consultationId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM invoices WHERE consultation_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, consultationId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // MARK AS PAID
    @Override
    public void markAsPaid(Integer invoiceId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "UPDATE invoices SET invoice_status = ?, paid_at = ? WHERE invoice_id = ?";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // inserting arguments into the query statement
            pstmt.setString(1, "PAID");
            pstmt.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));
            pstmt.setInt(3, invoiceId);

            // execute the query
            pstmt.executeUpdate();
        }
    }

    // FIND UNPAID BY PATIENT ID
    @Override
    public DefaultTableModel findUnpaidByPatientId(Integer patientId) throws SQLException {
        // creating sql variable with sql statement
        String sql = "SELECT * FROM invoices WHERE patient_id = ? AND invoice_status != 'PAID'";

        // validate connection
        // setting up connection with database
        // creating PreparedStatement
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // inserting arguments into the query statement
            ps.setInt(1, patientId);

            // execute the query
            ResultSet rs = ps.executeQuery();

            // building and returning DefaultTableModel from ResultSet
            return TableModelUtil.buildTableModel(rs);
        }
    }

    // mapping the invoice to the result set and display the output
    private Invoice mapRowToInvoice(ResultSet rs)
            throws SQLException {

        // creating a new invoice object
        Invoice invoice = new Invoice();

        // mapping invoice id to an object
        invoice.setInvoiceId(
                rs.getInt("invoice_id"));

        // mapping patient id to an object
        invoice.setPatientId(
                rs.getInt("patient_id"));

        // mapping consultation id to an object
        invoice.setConsultationId(
                rs.getInt("consultation_id"));

        // mapping amount to an object
        invoice.setAmount(
                rs.getFloat("amount"));

        // mapping invoice status to an object
        invoice.setInvoiceStatus(
                rs.getString("invoice_status"));

        // taking timestamp from the result set
        Timestamp issuedAtTimestamp =
                rs.getTimestamp("issued_at");

        // validating the result set
        if (issuedAtTimestamp != null) {
            // mapping the timestamp to an object
            invoice.setIssuedAt(
                    issuedAtTimestamp.toLocalDateTime());
        }

        // taking timestamp from the result set
        Timestamp paidAtTimestamp =
                rs.getTimestamp("paid_at");

        // validating the result set
        if (paidAtTimestamp != null) {
            // mapping the timestamp to an object
            invoice.setPaidAt(
                    paidAtTimestamp.toLocalDateTime());
        }

        // returning the invoice information
        return invoice;
    }
}
