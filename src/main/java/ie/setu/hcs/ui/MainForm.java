package ie.setu.hcs.ui;

import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.RoundedPanel;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainForm extends JFrame {
    private JButton btnHome;
    private JButton btnLogin;
    private JButton btnPatientRegister;
    private JButton btnExit;
    private JPanel contentHost;
    private JPanel navbarHost;
    private JPanel rootHost;

    public MainForm() {
        initUI();
        registerDefaultListeners();
        showHome();
    }

    private void initUI() {
        setTitle("Hospital Consultation System");
        setSize(1240, 780);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        rootHost = UIHelper.appBackground(new BorderLayout(0, 18));
        rootHost.setBorder(new EmptyBorder(20, 24, 24, 24));

        contentHost = UIHelper.animatedContentHost();
        navbarHost = createNavbar();
        rootHost.add(navbarHost, BorderLayout.NORTH);
        rootHost.add(contentHost, BorderLayout.CENTER);
        setContentPane(rootHost);
    }

    private JPanel createNavbar() {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setOpaque(false);

        RoundedPanel shell = new RoundedPanel(new BorderLayout(), HCS_Colors.DARK_SIDEBAR, HCS_Colors.SIDEBAR_BORDER, 34);
        shell.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JPanel chrome = new JPanel(new BorderLayout(18, 0));
        chrome.setOpaque(false);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brand.setOpaque(false);

        JLabel logoLabel = UIHelper.roundedLogoLabel("/images/hospital_logo.png", 48);

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Hospital Consultation System");
        title.setFont(UIHelper.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Unified access for patients, clinicians, administrators, and lab teams");
        subtitle.setFont(UIHelper.font(Font.PLAIN, 12));
        subtitle.setForeground(HCS_Colors.TEXT_LIGHT);

        brandText.add(title);
        brandText.add(Box.createVerticalStrut(4));
        brandText.add(subtitle);

        brand.add(logoLabel);
        brand.add(brandText);

        JPanel navGroup = new JPanel(new GridLayout(1, 3, 10, 10));
        navGroup.setOpaque(false);

        btnHome = UIHelper.navButton("Overview", true);
        btnLogin = UIHelper.navButton("Login", false);
        btnPatientRegister = UIHelper.navButton("Patient", false);

        navGroup.add(btnHome);
        navGroup.add(btnLogin);
        navGroup.add(btnPatientRegister);

        JPanel trailing = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        trailing.setOpaque(false);

        btnExit = UIHelper.secondaryButton("Exit");
        btnExit.addActionListener(e -> System.exit(0));
        trailing.add(btnExit);

        chrome.add(brand, BorderLayout.WEST);
        chrome.add(trailing, BorderLayout.EAST);

        navGroup.setBorder(new EmptyBorder(18, 0, 0, 0));

        stack.add(chrome);
        stack.add(navGroup);

        shell.add(stack, BorderLayout.CENTER);
        navbar.add(shell, BorderLayout.CENTER);
        return UIHelper.navbarWrapper(navbar);
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setOpaque(false);
        content.add(UIHelper.pageHeader(
                "Modern hospital access in one secure workspace",
                "Move between consultation booking, records, billing, and clinical coordination without the older sidebar-heavy layout."
        ), BorderLayout.NORTH);

        JPanel body = UIHelper.pageBody(new BorderLayout(0, 24));

        JPanel metrics = new JPanel(new GridLayout(1, 3, 18, 18));
        metrics.setOpaque(false);
        metrics.add(UIHelper.metricCard("4", "Role-ready portals", "Patient, doctor, administrator, and laboratory workspaces are available from one entry point.", HCS_Colors.PRIMARY_TEAL));
        metrics.add(UIHelper.metricCard("1", "Shared care journey", "Appointments, notes, lab uploads, invoices, and insurance remain connected across the system.", HCS_Colors.BUTTON_BLUE));
        metrics.add(UIHelper.metricCard("Smooth", "Embedded transitions", "Navigation now swaps embedded views with motion so the product feels cohesive rather than page-by-page.", HCS_Colors.ACCENT_AMBER));

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 18));
        lower.setOpaque(false);

        lower.add(UIHelper.spotlightCard(
                "Care Coordination",
                "Built for a hospital front desk feel instead of an admin console feel",
                "The new landing experience uses a branded top navigation bar, cleaner action cards, and calmer healthcare colours so the system feels more like a modern patient services platform.",
                HCS_Colors.PRIMARY_TEAL_SOFT
        ));

        JPanel readiness = UIHelper.roundedPanel(new BorderLayout(0, 14), HCS_Colors.SURFACE);
        readiness.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel readinessTitle = new JLabel("Choose how to continue");
        readinessTitle.setFont(UIHelper.font(Font.BOLD, 20));
        readinessTitle.setForeground(HCS_Colors.TEXT_DARK);

        JLabel readinessText = new JLabel("<html><div style='width:360px'>Sign in to an existing workspace or create a patient account. Doctor, admin, and lab registration now live inside the admin dashboard.</div></html>");
        readinessText.setFont(UIHelper.font(Font.PLAIN, 13));
        readinessText.setForeground(HCS_Colors.TEXT_MUTED);

        JPanel bullets = new JPanel();
        bullets.setOpaque(false);
        bullets.setLayout(new BoxLayout(bullets, BoxLayout.Y_AXIS));
        bullets.add(createBullet("Patients can still register directly from this starter screen."));
        bullets.add(Box.createVerticalStrut(10));
        bullets.add(createBullet("Doctor, admin, and lab technician registration is handled from admin."));
        bullets.add(Box.createVerticalStrut(10));
        bullets.add(createBullet("Login still opens every role workspace from one place."));

        readiness.add(readinessTitle, BorderLayout.NORTH);
        readiness.add(readinessText, BorderLayout.CENTER);
        readiness.add(bullets, BorderLayout.SOUTH);
        lower.add(readiness);

        JPanel cardsPanel = new JPanel(new GridLayout(0, 2, 18, 18));
        cardsPanel.setOpaque(false);
        cardsPanel.add(createCard("Login", "Access your care or staff workspace securely.", () -> showFrame(new LoginForm(this, this::showHome, this::showEmbeddedDashboard), btnLogin)));
        cardsPanel.add(createCard("Patient Registration", "Create a patient account and begin booking visits.", () -> showFrame(new PatientRegistrationForm(), btnPatientRegister)));

        body.add(metrics, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);
        body.add(cardsPanel, BorderLayout.SOUTH);
        content.add(UIHelper.scrollablePage(body), BorderLayout.CENTER);

        return content;
    }

    private JPanel createBullet(String text) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        line.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setOpaque(true);
        dot.setBackground(HCS_Colors.PRIMARY_TEAL);
        dot.setPreferredSize(new Dimension(10, 10));

        JLabel label = new JLabel(text);
        label.setFont(UIHelper.font(Font.PLAIN, 13));
        label.setForeground(HCS_Colors.TEXT_MUTED);

        line.add(dot);
        line.add(label);
        return line;
    }

    private JButton createCard(String title, String description, Runnable action) {
        JButton card = UIHelper.cardButton(title, description);
        card.addActionListener(e -> action.run());
        return card;
    }

    public void setLoginListener(ActionListener listener) {
        btnLogin.addActionListener(listener);
    }

    public void setPatientRegisterListener(ActionListener listener) {
        btnPatientRegister.addActionListener(listener);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainForm frame = new MainForm();
            AppNavigator.show(frame);
        });
    }

    private void registerDefaultListeners() {
        btnHome.addActionListener(e -> showHome());
        setLoginListener(e -> showFrame(new LoginForm(this, this::showHome, this::showEmbeddedDashboard), btnLogin));
        setPatientRegisterListener(e -> showFrame(new PatientRegistrationForm(), btnPatientRegister));
    }

    private void showHome() {
        showStarterNavbar(true);
        setActiveButton(btnHome);
        showPanel(createContent());
    }

    private void showFrame(JFrame frame, JButton activeButton) {
        showStarterNavbar(true);
        setActiveButton(activeButton);
        AppNavigator.registerEmbeddedHost(frame, this);
        showPanel(UIHelper.embeddedView(frame, true));
    }

    private void showEmbeddedDashboard(JFrame frame) {
        setActiveButton(null);
        showStarterNavbar(false);
        AppNavigator.registerEmbeddedHost(frame, this);
        showPanel(UIHelper.embeddedView(frame, true));
    }

    public void returnToLanding() {
        showHome();
        toFront();
    }

    private void showPanel(JComponent component) {
        UIHelper.showContent(contentHost, component);
    }

    private void setActiveButton(JButton activeButton) {
        UIHelper.setNavActive(btnHome, btnHome == activeButton);
        UIHelper.setNavActive(btnLogin, btnLogin == activeButton);
        UIHelper.setNavActive(btnPatientRegister, btnPatientRegister == activeButton);
    }

    private void showStarterNavbar(boolean visible) {
        if (rootHost == null || navbarHost == null) {
            return;
        }

        boolean attached = navbarHost.getParent() == rootHost;
        if (visible && !attached) {
            rootHost.add(navbarHost, BorderLayout.NORTH);
        } else if (!visible && attached) {
            rootHost.remove(navbarHost);
        }

        rootHost.revalidate();
        rootHost.repaint();
    }
}
