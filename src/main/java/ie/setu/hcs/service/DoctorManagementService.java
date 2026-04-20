package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.DepartmentDAOImpl;
import ie.setu.hcs.dao.impl.DoctorDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.util.InputValidationUtil;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class DoctorManagementService {
    private final DoctorDAOImpl doctorDAO = new DoctorDAOImpl();
    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final DepartmentDAOImpl departmentDAO = new DepartmentDAOImpl();

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

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return TableModelUtil.buildTableModel(rs);
        }
    }

    public Doctor findDoctorById(Integer doctorId) throws Exception {
        if (doctorId == null) {
            return null;
        }
        return doctorDAO.findById(doctorId);
    }

    public Account findAccountById(Integer accountId) throws Exception {
        if (accountId == null) {
            return null;
        }
        return accountDAO.findById(accountId);
    }

    public void updateDoctor(Integer doctorId, String specialization, String licenseNumber,
                             String employeeNum, Integer yearsOfExperience, Integer consultationFee,
                             String departmentName) throws Exception {
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

    public Map<Integer, String> getDepartments() throws Exception {
        return departmentDAO.findAllDepartments();
    }

    public String getDepartmentName(Integer depId) throws Exception {
        return departmentDAO.findNameById(depId);
    }

    public void deleteDoctor(Integer doctorId) throws Exception {
        requireDoctor(doctorId);
        doctorDAO.delete(doctorId);
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
}
