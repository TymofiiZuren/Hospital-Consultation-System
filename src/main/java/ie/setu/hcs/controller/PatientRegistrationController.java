// defining package ie.setu.hcs.controller
package ie.setu.hcs.controller;

// importing stuff
import ie.setu.hcs.service.PatientRegistrationService;
import ie.setu.hcs.ui.PatientRegistrationForm;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// implementing PatientRegistrationController class
public class PatientRegistrationController {
    // defining attributes of the class
    private final PatientRegistrationService service;

    private final PatientRegistrationForm view;

    // creating PatientRegistrationController constructor for the class
    public PatientRegistrationController(PatientRegistrationForm view) {
        // implementing attributes of the class
        this.view = view;
        this.service = new PatientRegistrationService();
    }

    // creating handleRegistration method for registering a patient
    public void handleRegistration() throws Exception {

        // validate email format
        if (!view.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            view.showWarning("Invalid email format.");
            return;
        }

        // validate Irish Eircode format
        if (!view.getEircode().matches("^[A-Za-z0-9]{3}\\s?[A-Za-z0-9]{4}$")) {
            view.showWarning("Invalid Irish Eircode format (e.g. A65 F4E2).");
            return;
        }

        // validate PPSN format
        if (!view.getPpsn().matches("^\\d{7}[A-Za-z]{1,2}$")) {
            view.showWarning("Invalid PPSN format (e.g. 1234567A).");
            return;
        }

        // validate Irish mobile number
        if (!view.getPhone().matches("^(\\+353|0)8[3-9]\\d{7}$")) {
            view.showWarning("Invalid Irish mobile number.");
            return;
        }

        LocalDate dob;
        try {
            dob = view.getDob();
        } catch (DateTimeParseException ex) {
            view.showWarning("Date of birth must use YYYY-MM-DD format.");
            return;
        }

        // calling registerPatient method from service to register the patient
        service.registerPatient(view.getFirstName(), view.getLastName(), view.getEmail(),
                view.getPassword(), view.getPpsn(), view.getPhone(), view.getGender(),
                dob, view.getAddress(), view.getEircode(), view.getBloodType());

        // show success message
        view.showSuccess("Registration successful!");
        view.clearForm();
    }
}
