package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.AdministratorDAOImpl;
import ie.setu.hcs.dao.impl.DoctorDAOImpl;
import ie.setu.hcs.dao.impl.LabTechnicianDAOImpl;
import ie.setu.hcs.dao.impl.PatientDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.AdministratorDAO;
import ie.setu.hcs.dao.interfaces.DoctorDAO;
import ie.setu.hcs.dao.interfaces.LabTechnicianDAO;
import ie.setu.hcs.dao.interfaces.PatientDAO;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class AdminService {
    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final DoctorDAO doctorDAO = new DoctorDAOImpl();
    private final LabTechnicianDAO technicianDAO = new LabTechnicianDAOImpl();
    private final AdministratorDAO administratorDAO = new AdministratorDAOImpl();

    public DefaultTableModel getAccounts() throws Exception {
        return accountDAO.findAll();
    }

    public DefaultTableModel getAccountsForManagement() throws Exception {
        ensureAdminColumn();
        String sql = """
                SELECT account_id,
                       first_name,
                       last_name,
                       email,
                       CASE role_id
                           WHEN 1 THEN 'Patient'
                           WHEN 2 THEN 'Doctor'
                           WHEN 3 THEN 'Lab Technician'
                           WHEN 4 THEN 'Admin'
                           ELSE CONCAT('Role ', role_id)
                       END AS role,
                       ppsn,
                       phone,
                       gender,
                       is_active,
                       is_admin
                FROM accounts
                ORDER BY last_name, first_name
                """;
        return displayTable(sql);
    }

    public DefaultTableModel getPatients() throws Exception {
        return patientDAO.findAll();
    }

    public DefaultTableModel getPatientsForManagement() throws Exception {
        String sql = """
                SELECT p.patient_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS patient,
                       a.email,
                       p.date_of_birth,
                       p.address,
                       p.eircode,
                       p.blood_type,
                       p.medical_record_number
                FROM patients p
                JOIN accounts a ON p.account_id = a.account_id
                ORDER BY a.last_name, a.first_name
                """;
        return displayTable(sql);
    }

    public DefaultTableModel getDoctors() throws Exception {
        return doctorDAO.findAll();
    }

    public DefaultTableModel getDoctorsForManagement() throws Exception {
        String sql = """
                SELECT d.doctor_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS doctor,
                       a.email,
                       d.specialization,
                       d.license_number,
                       d.years_of_experience,
                       d.consultation_fee,
                       d.dep_id,
                       a.is_active
                FROM doctors d
                JOIN accounts a ON d.account_id = a.account_id
                ORDER BY a.last_name, a.first_name
                """;
        return displayTable(sql);
    }

    public DefaultTableModel getTechnicians() throws Exception {
        return technicianDAO.findAll();
    }

    public DefaultTableModel getAdministrators() throws Exception {
        return administratorDAO.findAll();
    }

    public void deactivateAccount(Integer accountId) throws Exception {
        if (accountId == null) {
            throw new ValidationException("Please select an account first.");
        }
        accountDAO.deactivate(accountId);
    }

    public Account findAccountById(Integer accountId) throws Exception {
        if (accountId == null) {
            return null;
        }
        return accountDAO.findById(accountId);
    }

    public void updateAccount(Integer accountId, String email, Integer roleId, String firstName, String lastName,
                              String ppsn, String phone, String gender, Boolean isActive, Boolean isAdmin) throws Exception {
        Account existing = requireAccount(accountId);
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email is required.");
        }
        if (roleId == null) {
            throw new ValidationException("Role is required.");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new ValidationException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new ValidationException("Last name is required.");
        }

        existing.setEmail(email.trim());
        existing.setRoleId(roleId);
        existing.setFirstName(firstName.trim());
        existing.setLastName(lastName.trim());
        existing.setPpsn(blankToNull(ppsn));
        existing.setPhone(blankToNull(phone));
        existing.setGender(blankToNull(gender));
        existing.setActive(Boolean.TRUE.equals(isActive));
        existing.setAdmin(Boolean.TRUE.equals(isAdmin));
        accountDAO.update(existing);
    }

    public Patient findPatientById(Integer patientId) throws Exception {
        if (patientId == null) {
            return null;
        }
        return patientDAO.findById(patientId);
    }

    public void updatePatient(Integer patientId, LocalDate dateOfBirth, String address, String eircode,
                              String bloodType, String medicalRecordNumber) throws Exception {
        Patient existing = requirePatient(patientId);
        if (dateOfBirth == null) {
            throw new ValidationException("Date of birth is required.");
        }
        if (address == null || address.isBlank()) {
            throw new ValidationException("Address is required.");
        }
        if (eircode == null || eircode.isBlank()) {
            throw new ValidationException("Eircode is required.");
        }
        if (bloodType == null || bloodType.isBlank()) {
            throw new ValidationException("Blood type is required.");
        }
        if (medicalRecordNumber == null || medicalRecordNumber.isBlank()) {
            throw new ValidationException("Medical record number is required.");
        }

        existing.setDateOfBirth(dateOfBirth);
        existing.setAddress(address.trim());
        existing.setEircode(eircode.trim());
        existing.setBloodType(bloodType.trim());
        existing.setMedicalRecordNum(medicalRecordNumber.trim());
        patientDAO.update(existing);
    }

    public Doctor findDoctorById(Integer doctorId) throws Exception {
        if (doctorId == null) {
            return null;
        }
        return doctorDAO.findById(doctorId);
    }

    public void updateDoctor(Integer doctorId, String specialization, String licenseNumber,
                             Integer yearsOfExperience, Integer consultationFee, Integer depId) throws Exception {
        Doctor existing = requireDoctor(doctorId);
        if (specialization == null || specialization.isBlank()) {
            throw new ValidationException("Specialization is required.");
        }
        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new ValidationException("License number is required.");
        }
        if (yearsOfExperience == null) {
            throw new ValidationException("Years of experience is required.");
        }
        if (consultationFee == null) {
            throw new ValidationException("Consultation fee is required.");
        }
        if (depId == null) {
            throw new ValidationException("Department ID is required.");
        }

        existing.setSpecialization(specialization.trim());
        existing.setLicenseNum(licenseNumber.trim());
        existing.setYearsOfExperience(yearsOfExperience);
        existing.setConsultationFee(consultationFee);
        existing.setDepId(depId);
        doctorDAO.update(existing);
    }

    public void deletePatient(Integer patientId) throws Exception {
        if (patientId == null) {
            throw new ValidationException("Please select a patient first.");
        }
        patientDAO.delete(patientId);
    }

    public void deleteDoctor(Integer doctorId) throws Exception {
        if (doctorId == null) {
            throw new ValidationException("Please select a doctor first.");
        }
        doctorDAO.delete(doctorId);
    }

    public void deleteTechnician(Integer technicianId) throws Exception {
        if (technicianId == null) {
            throw new ValidationException("Please select a technician first.");
        }
        technicianDAO.delete(technicianId);
    }

    public void deleteAdministrator(Integer adminId) throws Exception {
        if (adminId == null) {
            throw new ValidationException("Please select an administrator first.");
        }
        administratorDAO.delete(adminId);
    }

    private DefaultTableModel displayTable(String sql) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return TableModelUtil.buildTableModel(rs);
        }
    }

    private void ensureAdminColumn() throws Exception {
        try (Connection conn = DatabaseConfig.getConnection();
             ResultSet columns = conn.getMetaData().getColumns(conn.getCatalog(), null, "accounts", "is_admin")) {
            if (columns.next()) {
                return;
            }
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("ALTER TABLE accounts ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT FALSE")) {
            ps.executeUpdate();
        } catch (Exception ex) {
            try (Connection conn = DatabaseConfig.getConnection();
                 ResultSet columns = conn.getMetaData().getColumns(conn.getCatalog(), null, "accounts", "is_admin")) {
                if (columns.next()) {
                    return;
                }
            }
            throw ex;
        }
    }

    private Account requireAccount(Integer accountId) throws Exception {
        if (accountId == null) {
            throw new ValidationException("Please select an account first.");
        }
        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new ResourceNotFoundException("Account was not found.");
        }
        return account;
    }

    private Patient requirePatient(Integer patientId) throws Exception {
        if (patientId == null) {
            throw new ValidationException("Please select a patient first.");
        }
        Patient patient = patientDAO.findById(patientId);
        if (patient == null) {
            throw new ResourceNotFoundException("Patient was not found.");
        }
        return patient;
    }

    private Doctor requireDoctor(Integer doctorId) throws Exception {
        if (doctorId == null) {
            throw new ValidationException("Please select a doctor first.");
        }
        Doctor doctor = doctorDAO.findById(doctorId);
        if (doctor == null) {
            throw new ResourceNotFoundException("Doctor was not found.");
        }
        return doctor;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
