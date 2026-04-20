package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.InvoiceDAOImpl;
import ie.setu.hcs.dao.interfaces.InvoiceDAO;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Invoice;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class InvoiceService {
    public static final String STATUS_UNPAID = "UNPAID";
    public static final String STATUS_PAID = "PAID";

    private final InvoiceDAO invoiceDAO = new InvoiceDAOImpl();
    private final AppointmentService appointmentService = new AppointmentService();

    public DefaultTableModel getInvoicesForPatient(Account account) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        String sql = """
                SELECT i.invoice_id,
                       i.consultation_id,
                       %s AS consultation,
                       i.amount,
                       i.invoice_status,
                       i.issued_at,
                       i.paid_at
                FROM invoices i
                LEFT JOIN consultation c ON i.consultation_id = c.consultation_id
                WHERE i.patient_id = ?
                ORDER BY i.issued_at DESC
                """.formatted(consultationDisplaySql());
        return displayInvoices(sql, patient.getPatientId());
    }

    public DefaultTableModel getUnpaidInvoicesForPatient(Account account) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        String sql = """
                SELECT i.invoice_id,
                       i.consultation_id,
                       %s AS consultation,
                       i.amount,
                       i.invoice_status,
                       i.issued_at,
                       i.paid_at
                FROM invoices i
                LEFT JOIN consultation c ON i.consultation_id = c.consultation_id
                WHERE i.patient_id = ?
                  AND i.invoice_status != 'PAID'
                ORDER BY i.issued_at DESC
                """.formatted(consultationDisplaySql());
        return displayInvoices(sql, patient.getPatientId());
    }

    public DefaultTableModel getAllInvoices() throws Exception {
        String sql = """
                SELECT i.invoice_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS patient,
                       i.consultation_id,
                       %s AS consultation,
                       i.amount,
                       i.invoice_status,
                       i.issued_at,
                       i.paid_at
                FROM invoices i
                JOIN patients p ON i.patient_id = p.patient_id
                JOIN accounts a ON p.account_id = a.account_id
                LEFT JOIN consultation c ON i.consultation_id = c.consultation_id
                ORDER BY i.issued_at DESC
                """.formatted(consultationDisplaySql());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return TableModelUtil.buildTableModel(rs);
        }
    }

    public Invoice findInvoiceById(Integer invoiceId) throws Exception {
        if (invoiceId == null) {
            throw new ValidationException("Please select an invoice first.");
        }

        Invoice invoice = invoiceDAO.findById(invoiceId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Invoice not found.");
        }
        return invoice;
    }

    public void updateInvoice(Integer invoiceId, Float amount, String status) throws Exception {
        Invoice invoice = findInvoiceById(invoiceId);
        invoice.setAmount(requireAmount(amount));
        invoice.setInvoiceStatus(normalizeStatus(status));
        invoice.setPaidAt(STATUS_PAID.equals(invoice.getInvoiceStatus())
                ? invoice.getPaidAt() == null ? LocalDateTime.now() : invoice.getPaidAt()
                : null);
        invoiceDAO.update(invoice);
    }

    public void markAsPaid(Integer invoiceId) throws Exception {
        findInvoiceById(invoiceId);
        invoiceDAO.markAsPaid(invoiceId);
    }

    public void deleteInvoice(Integer invoiceId) throws Exception {
        findInvoiceById(invoiceId);
        invoiceDAO.delete(invoiceId);
    }

    private DefaultTableModel displayInvoices(String sql, Integer patientId) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    private float requireAmount(Float amount) throws Exception {
        if (amount == null || amount <= 0) {
            throw new ValidationException("Amount must be greater than zero.");
        }
        return amount;
    }

    private String normalizeStatus(String status) throws Exception {
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Please choose an invoice status.");
        }

        String normalized = status.trim().toUpperCase();
        if (!STATUS_UNPAID.equals(normalized) && !STATUS_PAID.equals(normalized)) {
            throw new ValidationException("Unsupported invoice status.");
        }
        return normalized;
    }

    private String consultationDisplaySql() {
        return """
                CASE
                    WHEN i.consultation_id IS NULL THEN 'No linked consultation'
                    WHEN c.diagnosis IS NULL OR TRIM(c.diagnosis) = '' THEN CONCAT('Consultation #', i.consultation_id)
                    ELSE CONCAT('Consultation #', i.consultation_id, ' | ', c.diagnosis)
                END
                """;
    }
}
