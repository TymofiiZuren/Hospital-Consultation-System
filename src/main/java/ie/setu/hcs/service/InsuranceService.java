package ie.setu.hcs.service;

import ie.setu.hcs.config.DatabaseConfig;
import ie.setu.hcs.dao.impl.InsuranceDAOImpl;
import ie.setu.hcs.dao.interfaces.InsuranceDAO;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.ResourceNotFoundException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Insurance;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.TableModelUtil;

import javax.swing.table.DefaultTableModel;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class InsuranceService {
    public static final String STATUS_PENDING_VERIFICATION = "Pending Verification";
    public static final String STATUS_VERIFIED = "Verified";
    public static final String STATUS_FAILED = "Failed";
    private static final Pattern POLICY_PATTERN = Pattern.compile("^[A-Za-z]{2,5}-[A-Za-z0-9]{4,16}$");

    private final InsuranceDAO insuranceDAO = new InsuranceDAOImpl();
    private final AppointmentService appointmentService = new AppointmentService();

    public DefaultTableModel getInsuranceForPatient(Account account) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        String sql = """
                SELECT i.insurance_id,
                       i.provider_name,
                       i.policy_number,
                       i.status,
                       i.expiration_date
                FROM insurance i
                WHERE i.patient_id = ?
                ORDER BY i.expiration_date DESC
                """;
        return displayInsurance(sql, patient.getPatientId());
    }

    public DefaultTableModel getAllInsurance() throws Exception {
        String sql = """
                SELECT i.insurance_id,
                       CONCAT(a.first_name, ' ', a.last_name) AS patient,
                       i.provider_name,
                       i.policy_number,
                       i.status,
                       i.expiration_date
                FROM insurance i
                JOIN patients p ON i.patient_id = p.patient_id
                JOIN accounts a ON p.account_id = a.account_id
                ORDER BY i.expiration_date DESC
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return TableModelUtil.buildTableModel(rs);
        }
    }

    public Integer addInsurance(Account account, String provider, String policy, String status,
                                LocalDate expirationDate) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        return addInsurance(patient.getPatientId(), provider, policy, status, expirationDate, null);
    }

    public Integer addInsurance(Integer patientId, String provider, String policy, String status,
                                LocalDate expirationDate) throws Exception {
        return addInsurance(patientId, provider, policy, status, expirationDate, null);
    }

    public Integer addInsurance(Integer patientId, String provider, String policy, String status,
                                LocalDate expirationDate, String cardDocumentPath) throws Exception {
        validate(patientId, provider, policy, status, expirationDate);
        Insurance insurance = new Insurance(
                patientId,
                provider.trim(),
                policy.trim(),
                status,
                expirationDate,
                normalizeDocumentPath(cardDocumentPath)
        );
        insuranceDAO.save(insurance);
        return insurance.getInsuranceId();
    }

    public Integer submitForVerification(Account account, String provider, String policy,
                                         LocalDate expirationDate, String cardDocumentPath) throws Exception {
        Patient patient = appointmentService.requirePatient(account);
        validateVerificationSubmission(patient.getPatientId(), provider, policy, expirationDate, cardDocumentPath);
        return addInsurance(
                patient.getPatientId(),
                provider,
                policy,
                STATUS_PENDING_VERIFICATION,
                expirationDate,
                cardDocumentPath
        );
    }

    public void updateInsurance(Integer insuranceId, Integer patientId, String provider, String policy,
                                String status, LocalDate expirationDate) throws Exception {
        updateInsurance(null, insuranceId, patientId, provider, policy, status, expirationDate, null);
    }

    public void updateInsurance(Integer insuranceId, Integer patientId, String provider, String policy,
                                String status, LocalDate expirationDate, String cardDocumentPath) throws Exception {
        updateInsurance(null, insuranceId, patientId, provider, policy, status, expirationDate, cardDocumentPath);
    }

    public void updateInsurance(Account actor, Integer insuranceId, Integer patientId, String provider, String policy,
                                String status, LocalDate expirationDate, String cardDocumentPath) throws Exception {
        Insurance existing = requireInsurance(insuranceId);
        if (!canOverridePendingRestriction(actor)
                && !STATUS_PENDING_VERIFICATION.equalsIgnoreCase(existing.getStatus())) {
            throw new ConflictException("Only pending insurance requests can be updated.");
        }
        validate(patientId, provider, policy, status, expirationDate);
        String normalizedDocumentPath = normalizeDocumentPath(cardDocumentPath);
        if ((normalizedDocumentPath == null || normalizedDocumentPath.isBlank())) {
            normalizedDocumentPath = existing.getCardDocumentPath();
        }
        Insurance insurance = new Insurance(
                insuranceId,
                patientId,
                provider.trim(),
                policy.trim(),
                status,
                expirationDate,
                normalizedDocumentPath
        );
        insuranceDAO.update(insurance);
    }

    public void updateStatus(Integer insuranceId, String status) throws Exception {
        requireInsurance(insuranceId);
        if (status == null || status.isBlank()) {
            throw new ValidationException("Status is required.");
        }
        insuranceDAO.updateStatus(insuranceId, status);
    }

    public void deleteInsurance(Integer insuranceId) throws Exception {
        requireInsurance(insuranceId);
        insuranceDAO.delete(insuranceId);
    }

    public Insurance findInsuranceById(Integer insuranceId) throws Exception {
        if (insuranceId == null) {
            return null;
        }
        return insuranceDAO.findById(insuranceId);
    }

    public Path downloadCardDocument(Integer insuranceId, Path targetPath) throws Exception {
        Insurance insurance = findInsuranceById(insuranceId);
        if (insurance == null) {
            throw new ValidationException("Please select an insurance record first.");
        }
        return downloadCardDocument(insurance.getCardDocumentPath(), targetPath);
    }

    public Path downloadCardDocument(String sourceLocation, Path targetPath) throws Exception {
        String normalizedSource = normalizeDocumentPath(sourceLocation);
        if (normalizedSource == null) {
            throw new ValidationException("No insurance card path is available.");
        }
        if (targetPath == null) {
            throw new ValidationException("Please choose where to save the insurance card.");
        }

        Path absoluteTarget = targetPath.toAbsolutePath();
        Path parent = absoluteTarget.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        if (isRemoteSource(normalizedSource) || normalizedSource.startsWith("file:")) {
            try (InputStream input = URI.create(normalizedSource).toURL().openStream()) {
                Files.copy(input, absoluteTarget, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            Path sourcePath = Path.of(normalizedSource);
            if (!Files.exists(sourcePath)) {
                throw new ResourceNotFoundException("The insurance card file could not be found at: " + normalizedSource);
            }
            Files.copy(sourcePath, absoluteTarget, StandardCopyOption.REPLACE_EXISTING);
        }

        return absoluteTarget;
    }

    public String suggestedCardFileName(Integer insuranceId) throws Exception {
        Insurance insurance = findInsuranceById(insuranceId);
        if (insurance == null) {
            return "insurance-card";
        }
        return suggestedCardFileName(insurance.getCardDocumentPath(), insuranceId);
    }

    public String suggestedCardFileName(String sourceLocation, Integer insuranceId) {
        String normalizedSource = normalizeDocumentPath(sourceLocation);
        if (normalizedSource != null) {
            try {
                if (isRemoteSource(normalizedSource) || normalizedSource.startsWith("file:")) {
                    String path = URI.create(normalizedSource).getPath();
                    if (path != null && !path.isBlank()) {
                        String name = Path.of(path).getFileName().toString();
                        if (!name.isBlank()) {
                            return name;
                        }
                    }
                } else {
                    String name = Path.of(normalizedSource).getFileName().toString();
                    if (!name.isBlank()) {
                        return name;
                    }
                }
            } catch (Exception ignored) {
                // Fall back to a generic name if the path cannot be parsed.
            }
        }
        return insuranceId == null ? "insurance-card" : "insurance-card-" + insuranceId;
    }

    public boolean canManageRegardlessOfStatus(Account actor) {
        return canOverridePendingRestriction(actor);
    }

    private void validate(Integer patientId, String provider, String policy, String status,
                          LocalDate expirationDate) throws Exception {
        if (patientId == null) {
            throw new ValidationException("Patient is required.");
        }
        if (provider == null || provider.isBlank()) {
            throw new ValidationException("Provider name is required.");
        }
        if (policy == null || policy.isBlank()) {
            throw new ValidationException("Policy number is required.");
        }
        if (!POLICY_PATTERN.matcher(policy.trim()).matches()) {
            throw new ValidationException("Policy number must look like HS-98341276.");
        }
        if (status == null || status.isBlank()) {
            throw new ValidationException("Status is required.");
        }
        if (expirationDate == null) {
            throw new ValidationException("Expiration date is required.");
        }
    }

    private void validateVerificationSubmission(Integer patientId, String provider, String policy,
                                                LocalDate expirationDate, String cardDocumentPath) throws Exception {
        validate(patientId, provider, policy, STATUS_PENDING_VERIFICATION, expirationDate);
        if (cardDocumentPath == null || cardDocumentPath.isBlank()) {
            throw new ValidationException("Please attach an insurance card before submitting for verification.");
        }
    }

    private String normalizeDocumentPath(String cardDocumentPath) {
        if (cardDocumentPath == null) {
            return null;
        }
        String normalized = cardDocumentPath.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isRemoteSource(String sourceLocation) {
        String normalized = sourceLocation.toLowerCase();
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }

    private boolean canOverridePendingRestriction(Account actor) {
        return actor != null
                && (Boolean.TRUE.equals(actor.isAdmin()) || Integer.valueOf(4).equals(actor.getRoleId()));
    }

    private Insurance requireInsurance(Integer insuranceId) throws Exception {
        if (insuranceId == null) {
            throw new ValidationException("Please select an insurance record first.");
        }
        Insurance insurance = insuranceDAO.findById(insuranceId);
        if (insurance == null) {
            throw new ResourceNotFoundException("Insurance record was not found.");
        }
        return insurance;
    }

    private DefaultTableModel displayInsurance(String sql, Integer patientId) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                return TableModelUtil.buildTableModel(rs);
            }
        }
    }
}
