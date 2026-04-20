package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.AdminService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.RoundedPanel;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private final Account account;
    private final AdminService adminService = new AdminService();
    private JPanel contentHost;

    private JButton btnHome;
    private JButton btnAccounts;
    private JButton btnPatients;
    private JButton btnDoctors;
    private JButton btnAppointments;
    private JButton btnConsultations;
    private JButton btnLabs;
    private JButton btnInvoices;
    private JButton btnInsurance;

    public AdminDashboard(Account account) {
        this.account = account;
        initUI();
        showHome();
    }

    private void initUI() {
        setTitle("Administrator Dashboard");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = UIHelper.appBackground(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(20, 24, 24, 24));

        contentHost = UIHelper.animatedContentHost();
        root.add(createNavbar(), BorderLayout.NORTH);
        root.add(contentHost, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createNavbar() {
        RoundedPanel shell = new RoundedPanel(new BorderLayout(), HCS_Colors.DARK_SIDEBAR, HCS_Colors.SIDEBAR_BORDER, 34);
        shell.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new BorderLayout(18, 0));
        topRow.setOpaque(false);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brand.setOpaque(false);

        JLabel logoLabel = UIHelper.roundedLogoLabel("/images/hospital_logo.png", 42);

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Administrative Operations");
        title.setFont(UIHelper.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Manage staff, visits, billing, and insurance");
        subtitle.setFont(UIHelper.font(Font.PLAIN, 12));
        subtitle.setForeground(HCS_Colors.TEXT_LIGHT);

        brandText.add(title);
        brandText.add(Box.createVerticalStrut(4));
        brandText.add(subtitle);
        brand.add(logoLabel);
        brand.add(brandText);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JLabel userLabel = new JLabel(account.getFirstName() + " " + account.getLastName());
        userLabel.setFont(UIHelper.font(Font.PLAIN, 13));
        userLabel.setForeground(Color.WHITE);
        actions.add(userLabel);

        JButton logout = UIHelper.secondaryButton("Logout");
        logout.addActionListener(e -> AppNavigator.replace(this, new MainForm()));
        actions.add(logout);

        topRow.add(brand, BorderLayout.WEST);
        topRow.add(actions, BorderLayout.EAST);

        JPanel navGrid = new JPanel(new GridLayout(0, 5, 10, 10));
        navGrid.setOpaque(false);
        navGrid.setBorder(new EmptyBorder(18, 0, 0, 0));

        btnHome = UIHelper.navButton("Overview", true);
        btnAccounts = UIHelper.navButton("Accounts", false);
        btnPatients = UIHelper.navButton("Patients", false);
        btnDoctors = UIHelper.navButton("Doctors", false);
        btnAppointments = UIHelper.navButton("Visits", false);
        btnConsultations = UIHelper.navButton("Consults", false);
        btnLabs = UIHelper.navButton("Lab Results", false);
        btnInvoices = UIHelper.navButton("Billing", false);
        btnInsurance = UIHelper.navButton("Insurance", false);

        btnHome.addActionListener(e -> showHome());
        btnAccounts.addActionListener(e -> showFrame(accountsFrame(), btnAccounts));
        btnPatients.addActionListener(e -> showFrame(patientsFrame(), btnPatients));
        btnDoctors.addActionListener(e -> showFrame(doctorsFrame(), btnDoctors));
        btnAppointments.addActionListener(e -> showFrame(new AppointmentFrame(account, AppointmentFrame.Mode.ADMIN), btnAppointments));
        btnConsultations.addActionListener(e -> showFrame(new ConsultationFrame(account, ConsultationFrame.Mode.ADMIN), btnConsultations));
        btnLabs.addActionListener(e -> showFrame(new LabResultsFrame(account, LabResultsFrame.Mode.ADMIN), btnLabs));
        btnInvoices.addActionListener(e -> showFrame(new InvoiceFrame(account, InvoiceFrame.Mode.ADMIN), btnInvoices));
        btnInsurance.addActionListener(e -> showFrame(new InsuranceFrame(account, InsuranceFrame.Mode.ADMIN), btnInsurance));

        navGrid.add(btnHome);
        navGrid.add(btnAccounts);
        navGrid.add(btnPatients);
        navGrid.add(btnDoctors);
        navGrid.add(btnAppointments);
        navGrid.add(btnConsultations);
        navGrid.add(btnLabs);
        navGrid.add(btnInvoices);
        navGrid.add(btnInsurance);

        stack.add(topRow);
        stack.add(navGrid);
        shell.add(stack, BorderLayout.CENTER);
        return UIHelper.navbarWrapper(shell);
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setOpaque(false);
        content.add(UIHelper.pageHeader(
                "Admin Overview",
                "Manage the main hospital tools from one place."
        ), BorderLayout.NORTH);

        JPanel body = UIHelper.pageBody(new BorderLayout());
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JComponent registrationSection = createRegistrationSection();
        registrationSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(registrationSection);
        stack.add(Box.createVerticalStrut(18));

        JPanel metrics = new JPanel(new GridLayout(1, 3, 18, 18));
        metrics.setOpaque(false);
        metrics.add(UIHelper.metricCard("People", "Accounts", "View and manage system users.", HCS_Colors.PRIMARY_TEAL));
        metrics.add(UIHelper.metricCard("Visits", "Clinical", "Open visits, consults, and lab work.", HCS_Colors.BUTTON_BLUE));
        metrics.add(UIHelper.metricCard("Billing", "Finance", "Check invoices and insurance.", HCS_Colors.ACCENT_AMBER));
        metrics.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(metrics);
        stack.add(Box.createVerticalStrut(18));

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 18));
        lower.setOpaque(false);
        lower.add(UIHelper.spotlightCard(
                "Admin",
                "Everything important is close",
                "Use the top bar for operations and the registration shortcuts below for new accounts.",
                HCS_Colors.PRIMARY_TEAL_SOFT
        ));

        JPanel notes = UIHelper.roundedPanel(new BorderLayout(0, 14), HCS_Colors.SURFACE);
        notes.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel notesTitle = new JLabel("Quick Notes");
        notesTitle.setFont(UIHelper.font(Font.BOLD, 20));
        notesTitle.setForeground(HCS_Colors.TEXT_DARK);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(infoLine("Accounts are easy to open from the home screen."));
        list.add(Box.createVerticalStrut(10));
        list.add(infoLine("Visits, consults, labs, billing, and insurance stay in one workspace."));
        list.add(Box.createVerticalStrut(10));
        list.add(infoLine("Visit management includes doctor and room assignment."));

        notes.add(notesTitle, BorderLayout.NORTH);
        notes.add(list, BorderLayout.CENTER);
        lower.add(notes);
        lower.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(lower);
        stack.add(Box.createVerticalStrut(18));

        JPanel cards = new JPanel(new GridLayout(3, 3, 18, 18));
        cards.setOpaque(false);
        cards.add(card("Accounts", "Update system users.", () -> showFrame(accountsFrame(), btnAccounts)));
        cards.add(card("Patients", "Open patient records.", () -> showFrame(patientsFrame(), btnPatients)));
        cards.add(card("Doctors", "Manage doctor profiles.", () -> showFrame(doctorsFrame(), btnDoctors)));
        cards.add(card("Appointments", "Manage visits and rooms.", () -> showFrame(new AppointmentFrame(account, AppointmentFrame.Mode.ADMIN), btnAppointments)));
        cards.add(card("Consultations", "Review consult notes.", () -> showFrame(new ConsultationFrame(account, ConsultationFrame.Mode.ADMIN), btnConsultations)));
        cards.add(card("Lab Results", "Review lab results.", () -> showFrame(new LabResultsFrame(account, LabResultsFrame.Mode.ADMIN), btnLabs)));
        cards.add(card("Invoices", "Review invoices.", () -> showFrame(new InvoiceFrame(account, InvoiceFrame.Mode.ADMIN), btnInvoices)));
        cards.add(card("Insurance", "Review insurance.", () -> showFrame(new InsuranceFrame(account, InsuranceFrame.Mode.ADMIN), btnInsurance)));
        cards.add(card("Staff", "View lab technicians.", () -> showFrame(staffFrame(), btnHome)));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(cards);

        body.add(stack, BorderLayout.NORTH);
        content.add(UIHelper.scrollablePage(body), BorderLayout.CENTER);

        return content;
    }

    private JPanel infoLine(String text) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        line.setOpaque(false);

        JPanel accent = new JPanel();
        accent.setBackground(HCS_Colors.PRIMARY_TEAL);
        accent.setPreferredSize(new Dimension(10, 10));

        JLabel label = new JLabel(text);
        label.setFont(UIHelper.font(Font.PLAIN, 13));
        label.setForeground(HCS_Colors.TEXT_MUTED);

        line.add(accent);
        line.add(label);
        return line;
    }

    private JComponent createRegistrationSection() {
        JPanel section = UIHelper.roundedPanel(new BorderLayout(0, 14), HCS_Colors.SURFACE);
        section.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel title = new JLabel("Registration Forms");
        title.setFont(UIHelper.font(Font.BOLD, 20));
        title.setForeground(HCS_Colors.TEXT_DARK);

        JLabel subtitle = new JLabel("<html><div style='width:640px'>Open the exact patient, doctor, admin, and lab technician registration forms directly from the admin dashboard.</div></html>");
        subtitle.setFont(UIHelper.font(Font.PLAIN, 13));
        subtitle.setForeground(HCS_Colors.TEXT_MUTED);

        JPanel buttons = new JPanel(new GridLayout(2, 2, 12, 12));
        buttons.setOpaque(false);
        buttons.add(registrationButton("Register Patient", () -> showFrame(new PatientRegistrationForm(), btnHome)));
        buttons.add(registrationButton("Register Doctor", () -> showFrame(new DoctorRegistrationForm(), btnHome)));
        buttons.add(registrationButton("Register Admin", () -> showFrame(new AdminRegistrationForm(), btnHome)));
        buttons.add(registrationButton("Register Lab Technician", () -> showFrame(new TechnicianRegistrationForm(), btnHome)));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(8));
        top.add(subtitle);

        section.add(top, BorderLayout.NORTH);
        section.add(buttons, BorderLayout.CENTER);
        return section;
    }

    private JButton registrationButton(String label, Runnable action) {
        JButton button = UIHelper.actionButton(label, HCS_Colors.BUTTON_BLUE);
        button.setPreferredSize(new Dimension(0, 46));
        button.addActionListener(e -> action.run());
        return button;
    }

    private JFrame accountsFrame() {
        return new AdminAccountsFrame(account);
    }

    private JFrame patientsFrame() {
        return new AdminPatientsFrame(account);
    }

    private JFrame doctorsFrame() {
        return new AdminDoctorsFrame(account);
    }

    private DataTableFrame staffFrame() {
        return new DataTableFrame("Lab Technicians", "Registered laboratory staff", "technician_id", adminService::getTechnicians)
                .addSelectedAction("Delete", HCS_Colors.ACCENT_RED, adminService::deleteTechnician)
                .withBack(() -> new AdminDashboard(account));
    }

    private JButton card(String title, String desc, Runnable action) {
        JButton card = UIHelper.cardButton(title, desc);
        card.addActionListener(e -> action.run());
        return card;
    }

    private void showHome() {
        setActiveButton(btnHome);
        showPanel(createContent());
    }

    private void showFrame(JFrame frame, JButton activeButton) {
        setActiveButton(activeButton);
        showPanel(UIHelper.embeddedView(frame));
    }

    private void showPanel(JComponent component) {
        UIHelper.showContent(contentHost, component);
    }

    private void setActiveButton(JButton activeButton) {
        UIHelper.setNavActive(btnHome, btnHome == activeButton);
        UIHelper.setNavActive(btnAccounts, btnAccounts == activeButton);
        UIHelper.setNavActive(btnPatients, btnPatients == activeButton);
        UIHelper.setNavActive(btnDoctors, btnDoctors == activeButton);
        UIHelper.setNavActive(btnAppointments, btnAppointments == activeButton);
        UIHelper.setNavActive(btnConsultations, btnConsultations == activeButton);
        UIHelper.setNavActive(btnLabs, btnLabs == activeButton);
        UIHelper.setNavActive(btnInvoices, btnInvoices == activeButton);
        UIHelper.setNavActive(btnInsurance, btnInsurance == activeButton);
    }
}
