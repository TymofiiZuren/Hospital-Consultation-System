// defining package ie.setu.hcs.controller
package ie.setu.hcs.controller;

// importing stuff
import ie.setu.hcs.service.DoctorRegistrationService;
import ie.setu.hcs.ui.DoctorRegistrationForm;

// implementing DoctorRegistrationController class
public class DoctorRegistrationController {

    // defining attributes of the class
    private final DoctorRegistrationService service;

    private final DoctorRegistrationForm view;

    // creating constructor for the class
    public DoctorRegistrationController(DoctorRegistrationForm view) {
        this.service = new DoctorRegistrationService();

        this.view = view;
    }

    // creating handleRegistration method to handle the registration of a doctor
    public void handleRegistration() throws Exception {

        // email format validation
        if (!view.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            view.showWarning("Invalid email format.");
            return;
        }

        // validate PPSN format
        if (!view.getPpsn().matches("^\\d{7}[A-Za-z]{1,2}$")) {
            view.showWarning("Invalid PPSN format (e.g. 1234567A).");
            return;
        }

        // irish phone validation
        if (!view.getPhone().matches("^(\\+353|0)8[3-9]\\d{7}$")) {
            view.showWarning("Invalid Irish mobile number.");
            return;
        }

        // license number validation
        if (view.getMedicalLicense().isEmpty()) {
            view.showWarning("Medical license number is required.");
            return;
        }

        if (view.getEmployeeNum().isEmpty()) {
            view.showWarning("Employee number is required.");
            return;
        }

        // years of experience validation
        int yearsOfExperience;
        try {
            yearsOfExperience = Integer.parseInt(view.getYearsExperience());
        } catch (NumberFormatException ex) {
            view.showWarning("Years of experience must be a valid number.");
            return;
        }

        // calling the registerDoctor method of the service class
        service.registerDoctor(view.getFirstName(), view.getLastName(), view.getEmail(),
                view.getPassword(), view.getPpsn(), view.getPhone(), view.getGender(),
                view.getEmployeeNum(), view.getMedicalLicense(), yearsOfExperience,
                view.getDepartment(), view.getSpecialization());

        // show success message
        view.showSuccess("Doctor registration successful!");
        view.clearForm();
    }
}
