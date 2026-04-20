package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.AppointmentDAOImpl;
import ie.setu.hcs.dao.impl.DoctorDAOImpl;
import ie.setu.hcs.dao.impl.PatientDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.AppointmentDAO;
import ie.setu.hcs.dao.interfaces.DoctorDAO;
import ie.setu.hcs.dao.interfaces.PatientDAO;
import ie.setu.hcs.exception.AuthorizationException;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Appointment;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppointmentService {
    public static final String STATUS_PENDING = "Pending";
    private final AppointmentDAO appointmentDAO = new AppointmentDAOImpl();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final DoctorDAO doctorDAO = new DoctorDAOImpl();
    private final AccountDAO accountDAO = new AccountDAOImpl();

    public DefaultTableModel getAppointmentsForPatient(Account account) throws Exception {
        Patient patient = requirePatient(account);
        String sql = """
                SELECT ap.appointment_id,
                       CONCAT('Dr. ', da.first_name, ' ', da.last_name) AS doctor,
                       ap.appointment_datetime,
                       ap.consultation_room,
                       ap.status,
                       ap.medical_need
                FROM appointments ap
                JOIN doctors d ON ap.doctor_id = d.doctor_id
                JOIN accounts da ON d.account_id = da.account_id
                WHERE ap.patient_id = ?
                ORDER BY ap.appointment_datetime DESC
                """;
        return displayAppointments(sql, patient.getPatientId());
    }

    public DefaultTableModel getPendingAppointmentsForPatient(Account account) throws Exception {
        return filterPatientAppointments(getAppointmentsForPatient(account), PatientAppointmentGroup.PENDING);
    }

    public DefaultTableModel getAcceptedAppointmentsForPatient(Account account) throws Exception {
        return filterPatientAppointments(getAppointmentsForPatient(account), PatientAppointmentGroup.ACCEPTED);
    }

    public DefaultTableModel getCancelledAppointmentsForPatient(Account account) throws Exception {
        return filterPatientAppointments(getAppointmentsForPatient(account), PatientAppointmentGroup.CANCELLED);
    }

    public DefaultTableModel getPastAppointmentsForPatient(Account account) throws Exception {
        return filterPatientAppointments(getAppointmentsForPatient(account), PatientAppointmentGroup.PAST);
    }

    public DefaultTableModel getAppointmentsForDoctor(Account account) throws Exception {
        Doctor doctor = requireDoctor(account);
        String sql = """
                SELECT ap.appointment_id,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       ap.appointment_datetime,
                       ap.consultation_room,
                       ap.medical_need,
                       ap.status
                FROM appointments ap
                JOIN patients p ON ap.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                WHERE ap.doctor_id = ?
                ORDER BY ap.appointment_datetime DESC
                """;
        return displayAppointments(sql, doctor.getDoctorId());
    }

    public DefaultTableModel getAllAppointments() throws Exception {
        String sql = """
                SELECT ap.appointment_id,
                       CONCAT(pa.first_name, ' ', pa.last_name) AS patient,
                       CONCAT('Dr. ', da.first_name, ' ', da.last_name) AS doctor,
                       ap.appointment_datetime,
                       ap.consultation_room,
                       ap.medical_need,
                       ap.status
                FROM appointments ap
                JOIN patients p ON ap.patient_id = p.patient_id
                JOIN accounts pa ON p.account_id = pa.account_id
                JOIN doctors d ON ap.doctor_id = d.doctor_id
                JOIN accounts da ON d.account_id = da.account_id
                ORDER BY ap.appointment_datetime DESC
                """;

        try (Connection conn = DatabaseConfig.getConnection()) {
            ensureAppointmentColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    public DefaultTableModel getPatientsForDoctor(Account account) throws Exception {
        Doctor doctor = requireDoctor(account);
        String sql = """
                SELECT DISTINCT p.patient_id,
                       a.first_name,
                       a.last_name,
                       a.email,
                       a.phone,
                       p.date_of_birth,
                       p.address,
                       p.eircode,
                       p.blood_type
                FROM patients p
                JOIN accounts a ON p.account_id = a.account_id
                JOIN appointments ap ON p.patient_id = ap.patient_id
                WHERE ap.doctor_id = ?
                ORDER BY a.last_name, a.first_name
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctor.getDoctorId());
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }

    public Integer bookAppointment(Account account, Integer doctorId, LocalDateTime date, String medicalNeed)
            throws Exception {
        Patient patient = requirePatient(account);
        return createAppointment(patient.getPatientId(), doctorId, date, STATUS_PENDING, medicalNeed, "");
    }

    public Integer createAppointment(Integer patientId, Integer doctorId, LocalDateTime date, String status)
            throws Exception {
        return createAppointment(patientId, doctorId, date, status, "");
    }

    public Integer createAppointment(Integer patientId, Integer doctorId, LocalDateTime date, String status,
                                     String medicalNeed) throws Exception {
        return createAppointment(patientId, doctorId, date, status, medicalNeed, "");
    }

    public Integer createAppointment(Integer patientId, Integer doctorId, LocalDateTime date, String status,
                                     String medicalNeed, String consultationRoom) throws Exception {
        validateAppointment(patientId, doctorId, date, status, medicalNeed);
        Appointment appointment = new Appointment(
                patientId,
                doctorId,
                date,
                status,
                medicalNeed == null ? "" : medicalNeed.trim(),
                normalizeRoom(consultationRoom)
        );
        appointmentDAO.save(appointment);
        return appointment.getAppointmentId();
    }

    public void updateAppointment(Integer appointmentId, Integer patientId, Integer doctorId,
                                  LocalDateTime date, String status) throws Exception {
        updateAppointment(appointmentId, patientId, doctorId, date, status, "");
    }

    public void updateAppointment(Integer appointmentId, Integer patientId, Integer doctorId,
                                  LocalDateTime date, String status, String medicalNeed) throws Exception {
        updateAppointment(null, appointmentId, patientId, doctorId, date, status, medicalNeed, "");
    }

    public void updateAppointment(Integer appointmentId, Integer patientId, Integer doctorId,
                                  LocalDateTime date, String status, String medicalNeed, String consultationRoom) throws Exception {
        updateAppointment(null, appointmentId, patientId, doctorId, date, status, medicalNeed, consultationRoom);
    }

    public void updateAppointment(Account actor, Integer appointmentId, Integer patientId, Integer doctorId,
                                  LocalDateTime date, String status, String medicalNeed, String consultationRoom) throws Exception {
        Appointment existing = requireAppointment(appointmentId);
        if (!canOverridePendingRestriction(actor) && !isPendingStatus(existing.getStatus())) {
            throw new ConflictException("Only pending appointments can be updated.");
        }
        validateAppointment(patientId, doctorId, date, status, medicalNeed);
        Appointment appointment = new Appointment(
                appointmentId,
                patientId,
                doctorId,
                date,
                status,
                medicalNeed == null ? "" : medicalNeed.trim(),
                normalizeRoom(consultationRoom)
        );
        appointmentDAO.update(appointment);
    }

    public void updateStatus(Integer appointmentId, String status) throws Exception {
        requireAppointment(appointmentId);
        if (status == null || status.isBlank()) {
            throw new ValidationException("Appointment status is required.");
        }
        appointmentDAO.updateStatus(appointmentId, status);
    }

    public void deleteAppointment(Integer appointmentId) throws Exception {
        requireAppointment(appointmentId);
        appointmentDAO.delete(appointmentId);
    }

    public Appointment findById(Integer appointmentId) throws Exception {
        if (appointmentId == null) {
            return null;
        }
        return appointmentDAO.findById(appointmentId);
    }

    public List<TimeSlotOption> getAvailableSlotsForDoctor(Integer doctorId, LocalDate date,
                                                           Integer excludeAppointmentId) throws Exception {
        List<TimeSlotOption> options = new ArrayList<>();
        if (doctorId == null || date == null) {
            return options;
        }

        DefaultTableModel appointments = appointmentDAO.findByDoctorId(doctorId);
        int dateColumn = appointments.findColumn("appointment_datetime");
        if (dateColumn < 0) {
            return options;
        }

        Set<LocalTime> takenSlots = new HashSet<>();
        for (int row = 0; row < appointments.getRowCount(); row++) {
            Integer appointmentId = intValue(appointments, row, "appointment_id");
            if (excludeAppointmentId != null && excludeAppointmentId.equals(appointmentId)) {
                continue;
            }

            LocalDateTime appointmentDate = toLocalDateTime(appointments.getValueAt(row, dateColumn));
            if (appointmentDate == null || !appointmentDate.toLocalDate().equals(date)) {
                continue;
            }

            String status = stringValue(appointments, row, "status").trim().toLowerCase();
            if ("cancelled".equals(status) || "rejected".equals(status)) {
                continue;
            }
            takenSlots.add(appointmentDate.toLocalTime().withSecond(0).withNano(0));
        }

        for (LocalTime slot = LocalTime.of(9, 0); !slot.isAfter(LocalTime.of(16, 0)); slot = slot.plusHours(1)) {
            if (!takenSlots.contains(slot)) {
                options.add(new TimeSlotOption(slot, slot.toString()));
            }
        }

        return options;
    }

    public List<DoctorOption> getDoctorOptions() throws Exception {
        DefaultTableModel doctors = doctorDAO.findAll();
        List<DoctorOption> options = new ArrayList<>();

        for (int row = 0; row < doctors.getRowCount(); row++) {
            Integer doctorId = intValue(doctors, row, "doctor_id");
            Integer accountId = intValue(doctors, row, "account_id");
            String specialization = stringValue(doctors, row, "specialization");
            Account account = accountDAO.findById(accountId);
            String name = account == null
                    ? "Doctor #" + doctorId
                    : "Dr. " + account.getFirstName() + " " + account.getLastName();
            if (specialization != null && !specialization.isBlank()) {
                name += " - " + specialization;
            }
            options.add(new DoctorOption(doctorId, name));
        }

        return options;
    }

    public List<PatientOption> getPatientOptions() throws Exception {
        DefaultTableModel patients = patientDAO.findAll();
        List<PatientOption> options = new ArrayList<>();

        for (int row = 0; row < patients.getRowCount(); row++) {
            Integer patientId = intValue(patients, row, "patient_id");
            Integer accountId = intValue(patients, row, "account_id");
            Account account = accountDAO.findById(accountId);
            String label = account == null
                    ? "Patient #" + patientId
                    : account.getFirstName() + " " + account.getLastName() + " (" + account.getEmail() + ")";
            options.add(new PatientOption(patientId, label));
        }

        return options;
    }

    public Patient requirePatient(Account account) throws Exception {
        if (account == null || account.getAccountId() == null) {
            throw new AuthorizationException("No logged-in patient account was found.");
        }

        Patient patient = patientDAO.findByAccountId(account.getAccountId());
        if (patient == null) {
            throw new ResourceNotFoundException("Patient profile was not found for this account.");
        }
        return patient;
    }

    public Doctor requireDoctor(Account account) throws Exception {
        if (account == null || account.getAccountId() == null) {
            throw new AuthorizationException("No logged-in doctor account was found.");
        }

        Doctor doctor = doctorDAO.findByAccountId(account.getAccountId());
        if (doctor == null) {
            throw new ResourceNotFoundException("Doctor profile was not found for this account.");
        }
        return doctor;
    }

    private void validateAppointment(Integer patientId, Integer doctorId, LocalDateTime date, String status,
                                     String medicalNeed)
            throws Exception {
        if (patientId == null) {
            throw new ValidationException("Patient is required.");
        }
        if (doctorId == null) {
            throw new ValidationException("Doctor is required.");
        }
        if (date == null) {
            throw new ValidationException("Appointment date and time are required.");
        }
        if (status == null || status.isBlank()) {
            throw new ValidationException("Appointment status is required.");
        }
        if (medicalNeed == null || medicalNeed.isBlank()) {
            throw new ValidationException("Medical need is required.");
        }
    }

    private Integer intValue(DefaultTableModel model, int row, String columnName) {
        Object value = model.getValueAt(row, model.findColumn(columnName));
        return value == null ? null : Integer.parseInt(value.toString());
    }

    private String stringValue(DefaultTableModel model, int row, String columnName) {
        int column = model.findColumn(columnName);
        if (column < 0) {
            return "";
        }
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private DefaultTableModel filterPatientAppointments(
            DefaultTableModel source,
            PatientAppointmentGroup group
    ) {
        DefaultTableModel filtered = emptyCopy(source);
        int dateColumn = source.findColumn("appointment_datetime");
        int statusColumn = source.findColumn("status");
        if (dateColumn < 0) {
            return filtered;
        }

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<Object[]> rows = new ArrayList<>();

        for (int row = 0; row < source.getRowCount(); row++) {
            Object[] values = rowValues(source, row);
            LocalDateTime appointmentDate = toLocalDateTime(values[dateColumn]);
            if (appointmentDate == null) {
                continue;
            }

            String status = statusColumn < 0 || values[statusColumn] == null
                    ? ""
                    : values[statusColumn].toString().trim().toLowerCase();
            boolean futureOrToday = !appointmentDate.isBefore(startOfToday);

            boolean include = switch (group) {
                case PENDING -> futureOrToday && status.equals("pending");
                case ACCEPTED -> status.equals("accepted");
                case CANCELLED -> status.equals("cancelled") || status.equals("rejected");
                case PAST -> status.equals("completed")
                        || (!futureOrToday
                        && !status.equals("cancelled")
                        && !status.equals("accepted")
                        && !status.equals("rejected"));
            };
            if (include) {
                rows.add(values);
            }
        }

        rows.sort((left, right) -> {
            LocalDateTime leftDate = toLocalDateTime(left[dateColumn]);
            LocalDateTime rightDate = toLocalDateTime(right[dateColumn]);

            if (leftDate == null && rightDate == null) {
                return 0;
            }
            if (leftDate == null) {
                return 1;
            }
            if (rightDate == null) {
                return -1;
            }
            return group == PatientAppointmentGroup.PAST
                    ? rightDate.compareTo(leftDate)
                    : leftDate.compareTo(rightDate);
        });

        for (Object[] row : rows) {
            filtered.addRow(row);
        }

        return filtered;
    }

    private DefaultTableModel emptyCopy(DefaultTableModel source) {
        DefaultTableModel copy = new DefaultTableModel();
        for (int column = 0; column < source.getColumnCount(); column++) {
            copy.addColumn(source.getColumnName(column));
        }
        return copy;
    }

    private Object[] rowValues(DefaultTableModel source, int row) {
        Object[] values = new Object[source.getColumnCount()];
        for (int column = 0; column < source.getColumnCount(); column++) {
            values[column] = source.getValueAt(row, column);
        }
        return values;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }

        try {
            return Timestamp.valueOf(text).toLocalDateTime();
        } catch (IllegalArgumentException ignored) {
            try {
                return LocalDateTime.parse(text.replace(' ', 'T'));
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    private enum PatientAppointmentGroup {
        PENDING,
        ACCEPTED,
        CANCELLED,
        PAST
    }

    public record DoctorOption(Integer doctorId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record PatientOption(Integer patientId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record TimeSlotOption(LocalTime time, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private DefaultTableModel displayAppointments(String sql, Integer id) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            ensureAppointmentColumns(conn);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return TableModelUtil.buildTableModel(rs);
                }
            }
        }
    }

    private void ensureAppointmentColumns(Connection conn) throws Exception {
        ensureMedicalNeedColumn(conn);
        ensureConsultationRoomColumn(conn);
    }

    private void ensureMedicalNeedColumn(Connection conn) throws Exception {
        if (hasColumn(conn, "medical_need")) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE appointments ADD COLUMN medical_need TEXT NULL");
        } catch (Exception ex) {
            if (!hasColumn(conn, "medical_need")) {
                throw ex;
            }
        }
    }

    private void ensureConsultationRoomColumn(Connection conn) throws Exception {
        if (hasColumn(conn, "consultation_room")) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE appointments ADD COLUMN consultation_room VARCHAR(120) NULL");
        } catch (Exception ex) {
            if (!hasColumn(conn, "consultation_room")) {
                throw ex;
            }
        }
    }

    private String normalizeRoom(String consultationRoom) {
        return consultationRoom == null ? "" : consultationRoom.trim();
    }

    private Appointment requireAppointment(Integer appointmentId) throws Exception {
        if (appointmentId == null) {
            throw new ValidationException("Please select an appointment first.");
        }
        Appointment appointment = appointmentDAO.findById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException("Appointment was not found.");
        }
        return appointment;
    }

    private boolean isPendingStatus(String status) {
        return status != null && STATUS_PENDING.equalsIgnoreCase(status.trim());
    }

    public boolean canManageRegardlessOfStatus(Account actor) {
        return canOverridePendingRestriction(actor);
    }

    private boolean canOverridePendingRestriction(Account actor) {
        return actor != null
                && (Boolean.TRUE.equals(actor.isAdmin()) || Integer.valueOf(4).equals(actor.getRoleId()));
    }

    private boolean hasColumn(Connection conn, String columnName) throws Exception {
        try (ResultSet columns = conn.getMetaData().getColumns(conn.getCatalog(), null, "appointments", columnName)) {
            return columns.next();
        }
    }
}
