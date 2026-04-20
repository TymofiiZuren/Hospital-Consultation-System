package ie.setu.hcs.controller;

import ie.setu.hcs.service.AdminRegistrationService;
import ie.setu.hcs.ui.AdminRegistrationForm;

public class AdminRegistrationController {

    private final AdminRegistrationService service;

    private final AdminRegistrationForm view;

    public AdminRegistrationController(AdminRegistrationForm view) {
        this.service = new AdminRegistrationService();

        this.view = view;
    }

    public void handleRegistration() throws Exception {

        // email format validation
        if (!view.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            view.showWarning("Invalid email format.");
            return;
        }

        // irish phone validation
        if (!view.getPhone().matches("^(\\+353|0)8[3-9]\\d{7}$")) {
            view.showWarning("Invalid Irish mobile number.");
            return;
        }

        // validate PPSN format
        if (!view.getPpsn().matches("^\\d{7}[A-Za-z]{1,2}$")) {
            view.showWarning("Invalid PPSN format (e.g. 1234567A).");
            return;
        }

        // employee number validation
        if (view.getEmployeeNum().isEmpty()) {
            view.showWarning("Employee number is required.");
            return;
        }

        service.registerAdmin(view.getFirstName(), view.getLastName(), view.getEmail(),
                view.getPassword(), view.getPpsn(), view.getPhone(), view.getGender(),
                view.getEmployeeNum(), view.getDepartment(), view.getJobTitle());

        // show success message
        view.showSuccess("Administrator registration successful!");
        view.clearForm();
    }
}
