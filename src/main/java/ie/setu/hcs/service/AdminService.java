package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.AdministratorDAOImpl;
import ie.setu.hcs.dao.impl.DepartmentDAOImpl;
import ie.setu.hcs.dao.impl.DoctorDAOImpl;
import ie.setu.hcs.dao.impl.LabTechnicianDAOImpl;
import ie.setu.hcs.dao.impl.PatientDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.AdministratorDAO;
import ie.setu.hcs.dao.interfaces.LabTechnicianDAO;
import ie.setu.hcs.dao.interfaces.PatientDAO;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Administrator;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.model.LabTechnician;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.InputValidationUtil;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Map;

public class AdminService {
    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final PatientDAO patientDAO = new PatientDAOImpl();
    private final DoctorDAOImpl doctorDAO = new DoctorDAOImpl();
    private final LabTechnicianDAO technicianDAO = new LabTechnicianDAOImpl();
    private final AdministratorDAO administratorDAO = new AdministratorDAOImpl();
    private final DepartmentDAOImpl departmentDAO = new DepartmentDAOImpl();

    public DefaultTableModel getAccounts() throws Exception {
        return accountDAO.findAll();
    }

    public DefaultTableModel getAccountsForManagement() throws Exception {
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
                       is_active
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
        doctorDAO.ensureEmployeeNumColumn();
        String sql = """
                SELECT d.doctor_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS doctor,
                       a.email,
                       COALESCE(d.employee_num, '') AS employee_num,
                       d.specialization,
                       d.license_number,
                       d.years_of_experience,
                       d.consultation_fee,
                       COALESCE(dep.name, '') AS department,
                       a.is_active
                FROM doctors d
                JOIN accounts a ON d.account_id = a.account_id
                LEFT JOIN departments dep ON d.dep_id = dep.dep_id
                ORDER BY a.last_name, a.first_name
                """;
        return displayTable(sql);
    }

    public DefaultTableModel getTechnicians() throws Exception {
        return technicianDAO.findAll();
    }

    public DefaultTableModel getTechniciansForManagement() throws Exception {
        String sql = """
                SELECT lt.technician_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS technician,
                       a.email,
                       lt.employee_num,
                       lt.qualification,
                       lt.lab_name,
                       lt.shift,
                       a.is_active
                FROM lab_technicians lt
                JOIN accounts a ON lt.account_id = a.account_id
                ORDER BY a.last_name, a.first_name
                """;
        return displayTable(sql);
    }

    public DefaultTableModel getAdministrators() throws Exception {
        return administratorDAO.findAll();
    }

    public DefaultTableModel getAdministratorsForManagement() throws Exception {
        String sql = """
                SELECT ad.admin_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS administrator,
                       a.email,
                       ad.job_title,
                       ad.employee_num,
                       COALESCE(dep.name, '') AS department,
                       a.is_active
                FROM administrators ad
                JOIN accounts a ON ad.account_id = a.account_id
                LEFT JOIN departments dep ON ad.dep_id = dep.dep_id
                ORDER BY a.last_name, a.first_name
                """;
        return displayTable(sql);
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

    public void updateAccount(Integer accountId, String email, String firstName, String lastName,
                              String ppsn, String phone, String gender, Boolean isActive) throws Exception {
        Account existing = requireAccount(accountId);
        String normalizedEmail = InputValidationUtil.requireEmail(email);
        String normalizedFirstName = InputValidationUtil.requireNonBlank(firstName, "First name");
        String normalizedLastName = InputValidationUtil.requireNonBlank(lastName, "Last name");

        existing.setEmail(normalizedEmail);
        existing.setFirstName(normalizedFirstName);
        existing.setLastName(normalizedLastName);
        existing.setPpsn(blankToNull(InputValidationUtil.optionalTrim(ppsn)));
        existing.setPhone(blankToNull(InputValidationUtil.optionalTrim(phone)));
        existing.setGender(blankToNull(InputValidationUtil.optionalTrim(gender)));
        existing.setActive(Boolean.TRUE.equals(isActive));
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
        existing.setDateOfBirth(InputValidationUtil.requireDate(dateOfBirth, "Date of birth"));
        existing.setAddress(InputValidationUtil.requireNonBlank(address, "Address"));
        existing.setEircode(InputValidationUtil.requireNonBlank(eircode, "Eircode"));
        existing.setBloodType(InputValidationUtil.requireNonBlank(bloodType, "Blood type"));
        existing.setMedicalRecordNum(InputValidationUtil.requireNonBlank(medicalRecordNumber, "Medical record number"));
        patientDAO.update(existing);
    }

    public Doctor findDoctorById(Integer doctorId) throws Exception {
        if (doctorId == null) {
            return null;
        }
        return doctorDAO.findById(doctorId);
    }

    public void updateDoctor(Integer doctorId, String employeeNum, String specialization, String licenseNumber,
                             Integer yearsOfExperience, Integer consultationFee, String departmentName) throws Exception {
        Doctor existing = requireDoctor(doctorId);
        existing.setEmployeeNum(InputValidationUtil.requireNonBlank(employeeNum, "Employee number"));
        existing.setSpecialization(InputValidationUtil.requireNonBlank(specialization, "Specialization"));
        existing.setLicenseNum(InputValidationUtil.requireNonBlank(licenseNumber, "License number"));
        existing.setYearsOfExperience(InputValidationUtil.requirePositiveInteger(yearsOfExperience, "Years of experience"));
        existing.setConsultationFee(InputValidationUtil.requireNonNegativeInteger(consultationFee, "Consultation fee"));
        Integer depId = departmentDAO.findByName(InputValidationUtil.requireNonBlank(departmentName, "Department"));
        existing.setDepId(InputValidationUtil.requirePositiveInteger(depId, "Department"));
        doctorDAO.update(existing);
    }

    public Administrator findAdministratorById(Integer adminId) throws Exception {
        if (adminId == null) {
            return null;
        }
        return administratorDAO.findById(adminId);
    }

    public void updateAdministrator(Integer adminId, String jobTitle, String employeeNum, String departmentName) throws Exception {
        Administrator existing = requireAdministrator(adminId);
        existing.setJobTitle(InputValidationUtil.requireNonBlank(jobTitle, "Job title"));
        existing.setEmployeeNum(InputValidationUtil.requireNonBlank(employeeNum, "Employee number"));
        Integer depId = departmentDAO.findByName(InputValidationUtil.requireNonBlank(departmentName, "Department"));
        existing.setDepId(InputValidationUtil.requirePositiveInteger(depId, "Department"));
        administratorDAO.update(existing);
    }

    public LabTechnician findTechnicianById(Integer technicianId) throws Exception {
        if (technicianId == null) {
            return null;
        }
        return technicianDAO.findById(technicianId);
    }

    public void updateTechnician(Integer technicianId, String employeeNum, String qualification, String labName, String shift) throws Exception {
        LabTechnician existing = requireTechnician(technicianId);
        existing.setEmployeeNum(InputValidationUtil.requireNonBlank(employeeNum, "Employee number"));
        existing.setQualification(InputValidationUtil.requireNonBlank(qualification, "Qualification"));
        existing.setLabName(InputValidationUtil.requireNonBlank(labName, "Lab name"));
        existing.setShift(InputValidationUtil.requireNonBlank(shift, "Shift"));
        technicianDAO.update(existing);
    }

    public Map<Integer, String> getDepartments() throws Exception {
        return departmentDAO.findAllDepartments();
    }

    public String getDepartmentName(Integer depId) throws Exception {
        return departmentDAO.findNameById(depId);
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

    private Administrator requireAdministrator(Integer adminId) throws Exception {
        if (adminId == null) {
            throw new ValidationException("Please select an administrator first.");
        }
        Administrator administrator = administratorDAO.findById(adminId);
        if (administrator == null) {
            throw new ResourceNotFoundException("Administrator was not found.");
        }
        return administrator;
    }

    private LabTechnician requireTechnician(Integer technicianId) throws Exception {
        if (technicianId == null) {
            throw new ValidationException("Please select a lab technician first.");
        }
        LabTechnician technician = technicianDAO.findById(technicianId);
        if (technician == null) {
            throw new ResourceNotFoundException("Lab technician was not found.");
        }
        return technician;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
