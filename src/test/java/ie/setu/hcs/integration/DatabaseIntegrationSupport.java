package ie.setu.hcs.integration;

import ie.setu.hcs.config.DatabaseConfig;
import org.junit.jupiter.api.Assumptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

abstract class DatabaseIntegrationSupport {

    protected void assumeDatabaseAvailable() {
        try (Connection ignored = DatabaseConfig.getConnection()) {
            // Local integration database is available.
        } catch (Exception ex) {
            Assumptions.assumeTrue(false, "Integration database unavailable: " + ex.getMessage());
        }
    }

    protected String uniqueEmail(String prefix) {
        return prefix + "." + uniqueSuffix() + "@itest.local";
    }

    protected String uniquePpsn(String prefix) {
        String raw = (prefix + uniqueSuffix()).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return raw.substring(0, Math.min(raw.length(), 10));
    }

    protected void deleteAccountByEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM accounts WHERE email = ?")) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    protected Integer findAccountIdByEmail(String email) throws SQLException {
        return singleInteger(
                "SELECT account_id FROM accounts WHERE email = ?",
                email
        );
    }

    protected Integer findConsultationIdByAppointmentId(Integer appointmentId) throws SQLException {
        return singleInteger(
                "SELECT consultation_id FROM consultation WHERE appointment_id = ?",
                appointmentId
        );
    }

    protected int countRows(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    protected void deleteAppointmentTree(Integer appointmentId) throws SQLException {
        if (appointmentId == null) {
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            Integer consultationId = null;
            try (PreparedStatement lookup = conn.prepareStatement(
                    "SELECT consultation_id FROM consultation WHERE appointment_id = ?"
            )) {
                lookup.setInt(1, appointmentId);
                try (ResultSet rs = lookup.executeQuery()) {
                    if (rs.next()) {
                        consultationId = rs.getInt(1);
                    }
                }
            }

            if (consultationId != null) {
                deleteById(conn, "DELETE FROM lab_results WHERE consultation_id = ?", consultationId);
                deleteById(conn, "DELETE FROM invoices WHERE consultation_id = ?", consultationId);
                deleteById(conn, "DELETE FROM medical_records WHERE consultation_id = ?", consultationId);
                deleteById(conn, "DELETE FROM consultation WHERE consultation_id = ?", consultationId);
            }

            deleteById(conn, "DELETE FROM appointments WHERE appointment_id = ?", appointmentId);
        }
    }

    private Integer singleInteger(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private void deleteById(Connection conn, String sql, Integer id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            ps.setObject(index + 1, params[index]);
        }
    }

    private String uniqueSuffix() {
        long now = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return now + String.valueOf(random);
    }
}
