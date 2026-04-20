package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.AppointmentDAOImpl;
import ie.setu.hcs.dao.impl.DoctorDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.AppointmentDAO;
import ie.setu.hcs.dao.interfaces.DoctorDAO;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Appointment;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private static final DateTimeFormatter APPOINTMENT_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final AppointmentDAO appointmentDAO = new AppointmentDAOImpl();
    private final DoctorDAO doctorDAO = new DoctorDAOImpl();
    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final AppointmentService appointmentService = new AppointmentService();

    public void notifyPatient(Integer patientId, String title, String message) throws Exception {
        if (patientId == null) {
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            ensureNotificationsTable(conn);
            String sql = """
                    INSERT INTO notifications (patient_id, title, message, is_read, created_at)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                ps.setString(2, title);
                ps.setString(3, message);
                ps.setBoolean(4, false);
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
        }
    }

    public void notifyPatientForAppointmentStatus(Integer appointmentId, String status) throws Exception {
        Appointment appointment = appointmentDAO.findById(appointmentId);
        if (appointment == null || appointment.getPatientId() == null) {
            return;
        }

        String doctorName = doctorName(appointment.getDoctorId());
        String appointmentDate = appointment.getDate() == null
                ? "your scheduled visit"
                : appointment.getDate().format(APPOINTMENT_FORMAT);
        String readableStatus = status == null || status.isBlank() ? "Updated" : status.trim();
        String roomMessage = appointment.getConsultationRoom() == null || appointment.getConsultationRoom().isBlank()
                ? ""
                : " Assigned room: " + appointment.getConsultationRoom() + ".";

        notifyPatient(
                appointment.getPatientId(),
                "Appointment " + readableStatus,
                "Your appointment with " + doctorName + " on " + appointmentDate + " is now " + readableStatus + "." + roomMessage
        );
    }

    public List<NotificationItem> getRecentNotifications(Account account, int limit) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        return loadNotifications(patient.getPatientId(), limit, false);
    }

    public List<NotificationItem> consumeUnreadNotifications(Account account, int limit) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        List<NotificationItem> unread = loadNotifications(patient.getPatientId(), limit, true);
        if (unread.isEmpty()) {
            return unread;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            ensureNotificationsTable(conn);
            StringBuilder sql = new StringBuilder("UPDATE notifications SET is_read = ? WHERE notification_id IN (");
            for (int i = 0; i < unread.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append("?");
            }
            sql.append(")");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                ps.setBoolean(1, true);
                for (int i = 0; i < unread.size(); i++) {
                    ps.setInt(i + 2, unread.get(i).notificationId());
                }
                ps.executeUpdate();
            }
        }

        return unread;
    }

    private List<NotificationItem> loadNotifications(Integer patientId, int limit, boolean unreadOnly) throws Exception {
        List<NotificationItem> notifications = new ArrayList<>();
        if (patientId == null) {
            return notifications;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            ensureNotificationsTable(conn);

            String sql = unreadOnly
                    ? """
                    SELECT notification_id, title, message, is_read, created_at
                    FROM notifications
                    WHERE patient_id = ? AND is_read = ?
                    ORDER BY created_at DESC
                    LIMIT ?
                    """
                    : """
                    SELECT notification_id, title, message, is_read, created_at
                    FROM notifications
                    WHERE patient_id = ?
                    ORDER BY created_at DESC
                    LIMIT ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                if (unreadOnly) {
                    ps.setBoolean(2, false);
                    ps.setInt(3, limit);
                } else {
                    ps.setInt(2, limit);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp createdAt = rs.getTimestamp("created_at");
                        notifications.add(new NotificationItem(
                                rs.getInt("notification_id"),
                                rs.getString("title"),
                                rs.getString("message"),
                                createdAt == null ? null : createdAt.toLocalDateTime(),
                                rs.getBoolean("is_read")
                        ));
                    }
                }
            }
        }

        return notifications;
    }

    private String doctorName(Integer doctorId) throws Exception {
        if (doctorId == null) {
            return "your doctor";
        }

        Doctor doctor = doctorDAO.findById(doctorId);
        if (doctor == null || doctor.getAccountId() == null) {
            return "your doctor";
        }

        Account account = accountDAO.findById(doctor.getAccountId());
        if (account == null) {
            return "your doctor";
        }
        return "Dr. " + account.getFirstName() + " " + account.getLastName();
    }

    private void ensureNotificationsTable(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        notification_id INT AUTO_INCREMENT PRIMARY KEY,
                        patient_id INT NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        message TEXT NOT NULL,
                        is_read BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at DATETIME NOT NULL
                    )
                    """);
        }
    }

    public record NotificationItem(
            Integer notificationId,
            String title,
            String message,
            LocalDateTime createdAt,
            boolean read
    ) {}
}
