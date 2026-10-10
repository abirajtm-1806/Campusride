package ui;

import exception.InvalidLoginException;
import exception.InvalidRideException;
import exception.RideNotFoundException;
import model.Driver;
import model.Ride;
import model.Student;
import model.Vehicle;
import service.AuthService;
import service.FileService;
import service.RideService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Presentation Layer: Modern Java Swing Graphical User Interface for CampusRide.
 *
 * Architecture:
 * - Swing UI handles presentation, layout, and user interactions.
 * - Does NOT contain business logic directly in event handlers.
 * - Delegates all business logic strictly to AuthService and RideService.
 * - Demonstrates CardLayout navigation between all 8 required application screens.
 *
 * Architecture Flow:
 * Swing UI -> Service Layer (AuthService, RideService) -> Model Layer (Student, Driver, Ride) -> Supabase Data Layer
 */
public class CampusRideUI {

    // Screen Navigation Identifiers
    public static final String SCREEN_LOGIN = "SCREEN_LOGIN";
    public static final String SCREEN_REGISTER = "SCREEN_REGISTER";
    public static final String SCREEN_VERIFY = "SCREEN_VERIFY";
    public static final String SCREEN_DASHBOARD = "SCREEN_DASHBOARD";
    public static final String SCREEN_OFFER_RIDE = "SCREEN_OFFER_RIDE";
    public static final String SCREEN_SEARCH_RIDE = "SCREEN_SEARCH_RIDE";
    public static final String SCREEN_MY_RIDES = "SCREEN_MY_RIDES";
    public static final String SCREEN_PROFILE = "SCREEN_PROFILE";

    // Application Services
    private final AuthService authService;
    private final RideService rideService;
    private final FileService fileService;

    // Core Swing UI Components
    private JFrame mainFrame;
    private JPanel mainContainer;
    private CardLayout cardLayout;

    // Track email currently undergoing verification
    private String pendingVerificationEmail = "";
    private JTextField loginEmailField;

    // Dashboard dynamic labels
    private JLabel dashboardGreetingLabel;
    private DefaultTableModel dashboardRidesModel;

    // Search & My Rides Table Models
    private DefaultTableModel searchResultsModel;
    private DefaultTableModel myRidesModel;

    // Profile Screen dynamic labels
    private JLabel profileNameLabel;
    private JLabel profileIdLabel;
    private JLabel profileEmailLabel;
    private JLabel profileRoleLabel;
    private JLabel profileVerifyStatusLabel;

    // Verification screen dynamic label
    private JLabel verifyTargetEmailLabel;
    private JLabel verifyStatusBadgeLabel;

    public CampusRideUI() {
        this.authService = new AuthService();
        this.rideService = new RideService();
        this.fileService = new FileService();

        // Load academic file handling records into memory cache
        this.authService.setStudents(fileService.loadStudents());
        this.rideService.setRides(fileService.loadRides());
    }

    /**
     * Initializes and displays the Swing GUI on the Event Dispatch Thread.
     */
    public void start() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            initFrame();
            showScreen(SCREEN_LOGIN);
            mainFrame.setVisible(true);
        });
    }

    /**
     * Configures the main application window and CardLayout card deck.
     */
    private void initFrame() {
        mainFrame = new JFrame("CampusRide - NSSCE College Ride Sharing System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(950, 680);
        mainFrame.setMinimumSize(new Dimension(850, 600));
        mainFrame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(new Color(248, 250, 252));

        // Register each required screen panel
        mainContainer.add(createLoginPanel(), SCREEN_LOGIN);
        mainContainer.add(createRegisterPanel(), SCREEN_REGISTER);
        mainContainer.add(createVerifyPanel(), SCREEN_VERIFY);
        mainContainer.add(createDashboardPanel(), SCREEN_DASHBOARD);
        mainContainer.add(createOfferRidePanel(), SCREEN_OFFER_RIDE);
        mainContainer.add(createSearchRidePanel(), SCREEN_SEARCH_RIDE);
        mainContainer.add(createMyRidesPanel(), SCREEN_MY_RIDES);
        mainContainer.add(createProfilePanel(), SCREEN_PROFILE);

        mainFrame.setContentPane(mainContainer);
    }

    public void showScreen(String screenName) {
        if (SCREEN_DASHBOARD.equals(screenName)) {
            refreshDashboard();
        } else if (SCREEN_MY_RIDES.equals(screenName)) {
            refreshMyRides();
        } else if (SCREEN_PROFILE.equals(screenName)) {
            refreshProfile();
        } else if (SCREEN_VERIFY.equals(screenName)) {
            refreshVerifyScreen();
        }
        cardLayout.show(mainContainer, screenName);
    }

    // =========================================================================
    // SCREEN 1: Welcome & Login Screen
    // =========================================================================
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 245, 249));

        JPanel card = createCardPanel(420, 480);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("CampusRide");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(15, 23, 42));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("NSS College of Engineering Ride Sharing");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLabel = new JLabel("College Email (@nssce.ac.in):");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginEmailField = createStyledTextField();
        loginEmailField.setMaximumSize(new Dimension(360, 36));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setMaximumSize(new Dimension(360, 36));

        JButton loginButton = createPrimaryButton("Login to Account");
        loginButton.setMaximumSize(new Dimension(360, 40));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerButton = createSecondaryButton("Create New Account");
        registerButton.setMaximumSize(new Dimension(360, 36));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton verifyStatusButton = createOutlineButton("Check Email Verification Status");
        verifyStatusButton.setMaximumSize(new Dimension(360, 32));
        verifyStatusButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Event: Login Action (Delegates to AuthService)
        loginButton.addActionListener(e -> {
            String email = loginEmailField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter both college email and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!email.toLowerCase().endsWith("@nssce.ac.in")) {
                JOptionPane.showMessageDialog(mainFrame, "Only @nssce.ac.in college email addresses are allowed.", "Login Rejected", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Student loggedIn = authService.login(email, password);
                if (loggedIn != null) {
                    passField.setText("");
                    showScreen(SCREEN_DASHBOARD);
                }
            } catch (InvalidLoginException ex) {
                // Check if the issue was email verification
                if (ex.getMessage().contains("verify your college email")) {
                    pendingVerificationEmail = email;
                    int opt = JOptionPane.showConfirmDialog(mainFrame,
                            ex.getMessage() + "\n\nWould you like to open the Email Verification screen now?",
                            "Email Verification Required", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (opt == JOptionPane.YES_OPTION) {
                        showScreen(SCREEN_VERIFY);
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        registerButton.addActionListener(e -> showScreen(SCREEN_REGISTER));

        verifyStatusButton.addActionListener(e -> {
            String email = loginEmailField.getText().trim();
            if (!email.isEmpty()) {
                pendingVerificationEmail = email;
            }
            showScreen(SCREEN_VERIFY);
        });

        // Assemble Card
        card.add(Box.createVerticalStrut(15));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(loginEmailField);
        card.add(Box.createVerticalStrut(15));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(passField);
        card.add(Box.createVerticalStrut(22));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(10));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(8));
        card.add(verifyStatusButton);
        card.add(Box.createVerticalStrut(15));

        panel.add(card);
        return panel;
    }

    // =========================================================================
    // SCREEN 2: Registration Screen
    // =========================================================================
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 245, 249));

        JPanel card = createCardPanel(460, 620);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Create NSSCE Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(15, 23, 42));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noticeLabel = new JLabel("Official domain: only @nssce.ac.in emails accepted");
        noticeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        noticeLabel.setForeground(new Color(13, 148, 136));
        noticeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameField = createStyledTextField();
        JTextField regNumField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setMaximumSize(new Dimension(400, 36));
        JPasswordField confirmPassField = new JPasswordField();
        confirmPassField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmPassField.setMaximumSize(new Dimension(400, 36));

        JButton registerBtn = createPrimaryButton("Register Account");
        registerBtn.setMaximumSize(new Dimension(400, 40));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backBtn = createOutlineButton("← Back to Login");
        backBtn.setMaximumSize(new Dimension(400, 32));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Registration Event Handler
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String regNum = regNumField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String confirmPassword = new String(confirmPassField.getPassword()).trim();

            // 1. Validate all fields
            if (name.isEmpty() || regNum.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "All fields are required. Please fill in all fields.", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Validate email ends with @nssce.ac.in (case-insensitive)
            if (!email.toLowerCase().endsWith("@nssce.ac.in")) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Only @nssce.ac.in college email addresses are allowed.",
                        "Registration Rejected", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Validate password and confirm password match
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Passwords do not match. Please re-enter your password.",
                        "Password Mismatch", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Password must be at least 6 characters long.",
                        "Weak Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Student newStudent = new Student(name, regNum, email, password);

            try {
                // 4, 5, 6. Call Supabase Auth signup and insert profile into public.students
                boolean success = authService.registerStudent(newStudent);
                if (success) {
                    // Academic file handling demonstration persistence
                    fileService.saveStudent(newStudent);

                    // 7 & 8. Clear success notification
                    pendingVerificationEmail = email;
                    JOptionPane.showMessageDialog(mainFrame,
                            "Registration successful. Please verify your college email before logging in.",
                            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);

                    nameField.setText("");
                    regNumField.setText("");
                    emailField.setText("");
                    passField.setText("");
                    confirmPassField.setText("");

                    // 9. Return user to Login screen with email prefilled
                    if (loginEmailField != null) {
                        loginEmailField.setText(email);
                    }
                    showScreen(SCREEN_LOGIN);
                }
            } catch (InvalidLoginException ex) {
                // Do not display fake registration success if Supabase operation failed
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> showScreen(SCREEN_LOGIN));

        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(noticeLabel);
        card.add(Box.createVerticalStrut(18));

        addFormField(card, "Full Name:", nameField);
        addFormField(card, "Registration Number:", regNumField);
        addFormField(card, "College Email (@nssce.ac.in):", emailField);
        addFormField(card, "Password:", passField);
        addFormField(card, "Confirm Password:", confirmPassField);

        card.add(Box.createVerticalStrut(10));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(backBtn);
        card.add(Box.createVerticalStrut(10));

        panel.add(card);
        return panel;
    }

    // =========================================================================
    // SCREEN 3: Email Verification Status Screen
    // =========================================================================
    private JPanel createVerifyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 245, 249));

        JPanel card = createCardPanel(480, 500);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("College Email Verification");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(15, 23, 42));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        verifyTargetEmailLabel = new JLabel("Email: None selected");
        verifyTargetEmailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        verifyTargetEmailLabel.setForeground(new Color(71, 85, 105));
        verifyTargetEmailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        verifyStatusBadgeLabel = new JLabel("STATUS: PENDING VERIFICATION");
        verifyStatusBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        verifyStatusBadgeLabel.setForeground(new Color(217, 119, 6));
        verifyStatusBadgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea infoText = new JTextArea("A confirmation link was dispatched to your @nssce.ac.in inbox via Supabase Auth.\n\n"
                + "Students must confirm their college email address before accessing the CampusRide dashboard.");
        infoText.setWrapStyleWord(true);
        infoText.setLineWrap(true);
        infoText.setOpaque(false);
        infoText.setEditable(false);
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoText.setForeground(new Color(100, 116, 139));
        infoText.setMaximumSize(new Dimension(420, 70));
        infoText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton checkStatusBtn = createPrimaryButton("Check Supabase Verification Status");
        checkStatusBtn.setMaximumSize(new Dimension(420, 38));
        checkStatusBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton simulateVerifyBtn = createSecondaryButton("Simulate Email Confirmation (Dev/Demo)");
        simulateVerifyBtn.setMaximumSize(new Dimension(420, 36));
        simulateVerifyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton toLoginBtn = createOutlineButton("Go to Login Screen");
        toLoginBtn.setMaximumSize(new Dimension(420, 32));
        toLoginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        checkStatusBtn.addActionListener(e -> {
            if (pendingVerificationEmail.isEmpty()) {
                String input = JOptionPane.showInputDialog(mainFrame, "Enter @nssce.ac.in email to check:", "Check Email", JOptionPane.QUESTION_MESSAGE);
                if (input != null && !input.trim().isEmpty()) {
                    pendingVerificationEmail = input.trim();
                }
            }

            if (!pendingVerificationEmail.isEmpty()) {
                boolean verified = authService.isEmailVerified(pendingVerificationEmail);
                if (verified) {
                    verifyStatusBadgeLabel.setText("STATUS: VERIFIED ✔");
                    verifyStatusBadgeLabel.setForeground(new Color(16, 185, 129));
                    JOptionPane.showMessageDialog(mainFrame, "Your email is verified! You can now log in.", "Email Verified", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    verifyStatusBadgeLabel.setText("STATUS: PENDING VERIFICATION ⏳");
                    verifyStatusBadgeLabel.setForeground(new Color(217, 119, 6));
                    JOptionPane.showMessageDialog(mainFrame, "Verification is still pending. Please click the link sent to your college email.", "Pending", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        simulateVerifyBtn.addActionListener(e -> {
            if (pendingVerificationEmail.isEmpty()) {
                String input = JOptionPane.showInputDialog(mainFrame, "Enter @nssce.ac.in email to verify:", "Verify Email", JOptionPane.QUESTION_MESSAGE);
                if (input != null && !input.trim().isEmpty()) {
                    pendingVerificationEmail = input.trim();
                }
            }

            if (!pendingVerificationEmail.isEmpty()) {
                authService.markEmailAsVerified(pendingVerificationEmail);
                verifyStatusBadgeLabel.setText("STATUS: VERIFIED ✔");
                verifyStatusBadgeLabel.setForeground(new Color(16, 185, 129));
                JOptionPane.showMessageDialog(mainFrame, "Success! " + pendingVerificationEmail + " marked as verified.", "Verification Simulated", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        toLoginBtn.addActionListener(e -> showScreen(SCREEN_LOGIN));

        card.add(Box.createVerticalStrut(15));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(verifyTargetEmailLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(verifyStatusBadgeLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(infoText);
        card.add(Box.createVerticalStrut(25));
        card.add(checkStatusBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(simulateVerifyBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(toLoginBtn);
        card.add(Box.createVerticalStrut(15));

        panel.add(card);
        return panel;
    }

    private void refreshVerifyScreen() {
        if (!pendingVerificationEmail.isEmpty()) {
            verifyTargetEmailLabel.setText("Target: " + pendingVerificationEmail);
            boolean isV = authService.isEmailVerified(pendingVerificationEmail);
            if (isV) {
                verifyStatusBadgeLabel.setText("STATUS: VERIFIED ✔");
                verifyStatusBadgeLabel.setForeground(new Color(16, 185, 129));
            } else {
                verifyStatusBadgeLabel.setText("STATUS: PENDING VERIFICATION ⏳");
                verifyStatusBadgeLabel.setForeground(new Color(217, 119, 6));
            }
        }
    }

    // =========================================================================
    // SCREEN 4: Main Dashboard Screen
    // =========================================================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Top Navigation Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        dashboardGreetingLabel = new JLabel("Welcome back to CampusRide!");
        dashboardGreetingLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        dashboardGreetingLabel.setForeground(new Color(15, 23, 42));

        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navButtons.setOpaque(false);

        JButton offerBtn = createPrimaryButton("+ Offer Ride");
        JButton searchBtn = createSecondaryButton("Search Rides");
        JButton myRidesBtn = createSecondaryButton("My Rides");
        JButton profileBtn = createSecondaryButton("Profile");
        JButton logoutBtn = createDangerButton("Logout");

        offerBtn.addActionListener(e -> showScreen(SCREEN_OFFER_RIDE));
        searchBtn.addActionListener(e -> showScreen(SCREEN_SEARCH_RIDE));
        myRidesBtn.addActionListener(e -> showScreen(SCREEN_MY_RIDES));
        profileBtn.addActionListener(e -> showScreen(SCREEN_PROFILE));
        logoutBtn.addActionListener(e -> handleLogout());

        navButtons.add(offerBtn);
        navButtons.add(searchBtn);
        navButtons.add(myRidesBtn);
        navButtons.add(profileBtn);
        navButtons.add(logoutBtn);

        topBar.add(dashboardGreetingLabel, BorderLayout.WEST);
        topBar.add(navButtons, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Center: Available Rides Table
        JPanel tableContainer = new JPanel(new BorderLayout(5, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel tableTitle = new JLabel("Available Campus Rides");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(new Color(30, 41, 59));

        String[] cols = {"Ride ID", "Driver", "From", "To", "Date", "Time", "Seats Left", "Fare ($)"};
        dashboardRidesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(dashboardRidesModel);
        styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel tableBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tableBottom.setOpaque(false);

        JButton quickBookBtn = createPrimaryButton("Book Selected Ride");
        quickBookBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(mainFrame, "Please select a ride to book from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String rideId = (String) dashboardRidesModel.getValueAt(selectedRow, 0);
            try {
                // Delegate booking to RideService
                boolean success = rideService.bookRide(rideId);
                if (success) {
                    fileService.saveAllRides(rideService.getRides());
                    JOptionPane.showMessageDialog(mainFrame, "Seat successfully booked for Ride " + rideId + "!", "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    refreshDashboard();
                }
            } catch (RideNotFoundException | InvalidRideException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Booking Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        tableBottom.add(quickBookBtn);

        tableContainer.add(tableTitle, BorderLayout.NORTH);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        tableContainer.add(tableBottom, BorderLayout.SOUTH);

        panel.add(tableContainer, BorderLayout.CENTER);
        return panel;
    }

    private void refreshDashboard() {
        Student current = authService.getCurrentLoggedInStudent();
        if (current != null) {
            dashboardGreetingLabel.setText("Welcome back, " + current.getName() + " (" + current.getStudentId() + ")");
        }

        dashboardRidesModel.setRowCount(0);
        ArrayList<Ride> rides = rideService.getRides();
        for (Ride r : rides) {
            String driverName = (r.getDriver() != null) ? r.getDriver().getName() : "N/A";
            dashboardRidesModel.addRow(new Object[]{
                    r.getRideId(),
                    driverName,
                    r.getStartingLocation(),
                    r.getDestination(),
                    r.getDate(),
                    r.getDepartureTime(),
                    r.getAvailableSeats(),
                    String.format("%.2f", r.getCostPerPerson())
            });
        }
    }

    // =========================================================================
    // SCREEN 5: Offer Ride Screen
    // =========================================================================
    private JPanel createOfferRidePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Offer a Campus Ride");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JButton backBtn = createOutlineButton("← Back to Dashboard");
        backBtn.addActionListener(e -> showScreen(SCREEN_DASHBOARD));
        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel card = createCardPanel(600, 480);
        card.setLayout(new GridLayout(9, 2, 10, 8));

        JTextField startLocField = createStyledTextField();
        JTextField destField = createStyledTextField();
        JTextField dateField = createStyledTextField();
        dateField.setText("2026-10-15");
        JTextField timeField = createStyledTextField();
        timeField.setText("08:30 AM");

        JComboBox<String> vehicleTypeCombo = new JComboBox<>(new String[]{"Car", "Bike", "Scooter"});
        vehicleTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField regNumField = createStyledTextField();
        regNumField.setText("KL-09-AB-1234");
        JTextField capacityField = createStyledTextField();
        capacityField.setText("4");
        JTextField seatsField = createStyledTextField();
        seatsField.setText("3");
        JTextField costField = createStyledTextField();
        costField.setText("20.00");

        card.add(new JLabel("Starting Location:"));
        card.add(startLocField);
        card.add(new JLabel("Destination:"));
        card.add(destField);
        card.add(new JLabel("Date (YYYY-MM-DD):"));
        card.add(dateField);
        card.add(new JLabel("Departure Time:"));
        card.add(timeField);
        card.add(new JLabel("Vehicle Type:"));
        card.add(vehicleTypeCombo);
        card.add(new JLabel("Vehicle Reg Number:"));
        card.add(regNumField);
        card.add(new JLabel("Total Vehicle Capacity:"));
        card.add(capacityField);
        card.add(new JLabel("Available Passenger Seats:"));
        card.add(seatsField);
        card.add(new JLabel("Cost Per Person ($):"));
        card.add(costField);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        JButton publishBtn = createPrimaryButton("Publish Ride to CampusRide");
        publishBtn.setPreferredSize(new Dimension(280, 40));

        publishBtn.addActionListener(e -> {
            Student currentStudent = authService.getCurrentLoggedInStudent();
            if (currentStudent == null) {
                JOptionPane.showMessageDialog(mainFrame, "Session expired. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
                showScreen(SCREEN_LOGIN);
                return;
            }

            try {
                String startLoc = startLocField.getText().trim();
                String dest = destField.getText().trim();
                String date = dateField.getText().trim();
                String time = timeField.getText().trim();
                String vType = (String) vehicleTypeCombo.getSelectedItem();
                String vNumber = regNumField.getText().trim();
                int capacity = Integer.parseInt(capacityField.getText().trim());
                int availableSeats = Integer.parseInt(seatsField.getText().trim());
                double cost = Double.parseDouble(costField.getText().trim());

                if (startLoc.isEmpty() || dest.isEmpty() || date.isEmpty() || time.isEmpty() || vNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String rideId = "RIDE-" + (rideService.getRides().size() + 101);

                // OOP Concept: Aggregation & Multilevel Inheritance (Driver extends Student)
                Vehicle vehicle = new Vehicle(vType, vNumber, capacity);
                Driver driver = new Driver(
                        currentStudent.getName(),
                        currentStudent.getStudentId(),
                        currentStudent.getEmail(),
                        currentStudent.getPassword(),
                        vehicle
                );

                Ride ride = new Ride(rideId, driver, startLoc, dest, date, time, availableSeats, cost);

                // Business Service Layer
                rideService.offerRide(ride);

                // Academic file handling persistence
                fileService.saveRide(ride);

                JOptionPane.showMessageDialog(mainFrame, "Ride published successfully! ID: " + rideId, "Success", JOptionPane.INFORMATION_MESSAGE);
                startLocField.setText("");
                destField.setText("");
                showScreen(SCREEN_DASHBOARD);

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter valid numbers for capacity, seats, and cost.", "Numeric Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(publishBtn);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);

        panel.add(centerWrapper, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // =========================================================================
    // SCREEN 6: Search Ride Screen (Demonstrating Method Overloading)
    // =========================================================================
    private JPanel createSearchRidePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Search Available Campus Rides");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JButton backBtn = createOutlineButton("← Back to Dashboard");
        backBtn.addActionListener(e -> showScreen(SCREEN_DASHBOARD));
        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Search Criteria Panel
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JTextField srcField = new JTextField(12);
        JTextField dstField = new JTextField(12);
        JTextField dateField = new JTextField(10);

        JButton searchBtn = createPrimaryButton("Search Route");
        JButton showAllBtn = createSecondaryButton("Show All Rides");

        filterCard.add(new JLabel("From:"));
        filterCard.add(srcField);
        filterCard.add(new JLabel("To:"));
        filterCard.add(dstField);
        filterCard.add(new JLabel("Date (Optional):"));
        filterCard.add(dateField);
        filterCard.add(searchBtn);
        filterCard.add(showAllBtn);

        // Search Results Table
        String[] cols = {"Ride ID", "Driver", "From", "To", "Date", "Time", "Seats Left", "Fare ($)"};
        searchResultsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(searchResultsModel);
        styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tableActions.setOpaque(false);
        JButton bookSelectedBtn = createPrimaryButton("Book Selected Ride");

        // Event: Search Action (Calls Overloaded searchRide methods)
        searchBtn.addActionListener(e -> {
            String src = srcField.getText().trim();
            String dst = dstField.getText().trim();
            String date = dateField.getText().trim();

            if (src.isEmpty() || dst.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter both From (source) and To (destination).", "Search Filter", JOptionPane.WARNING_MESSAGE);
                return;
            }

            searchResultsModel.setRowCount(0);
            ArrayList<Ride> results;

            // OOP Concept: Method Overloading execution
            if (!date.isEmpty()) {
                // Signature 2: 3 parameters (source, destination, date)
                rideService.searchRide(src, dst, date); // Call interface contract
                results = rideService.findRides(src, dst, date);
            } else {
                // Signature 1: 2 parameters (source, destination)
                rideService.searchRide(src, dst); // Call interface contract
                results = rideService.findRides(src, dst);
            }

            for (Ride r : results) {
                String driverName = (r.getDriver() != null) ? r.getDriver().getName() : "N/A";
                searchResultsModel.addRow(new Object[]{
                        r.getRideId(),
                        driverName,
                        r.getStartingLocation(),
                        r.getDestination(),
                        r.getDate(),
                        r.getDepartureTime(),
                        r.getAvailableSeats(),
                        String.format("%.2f", r.getCostPerPerson())
                });
            }

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "No rides found matching your route.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        showAllBtn.addActionListener(e -> {
            searchResultsModel.setRowCount(0);
            for (Ride r : rideService.getRides()) {
                String driverName = (r.getDriver() != null) ? r.getDriver().getName() : "N/A";
                searchResultsModel.addRow(new Object[]{
                        r.getRideId(),
                        driverName,
                        r.getStartingLocation(),
                        r.getDestination(),
                        r.getDate(),
                        r.getDepartureTime(),
                        r.getAvailableSeats(),
                        String.format("%.2f", r.getCostPerPerson())
                });
            }
        });

        bookSelectedBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(mainFrame, "Select a ride to book.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String rideId = (String) searchResultsModel.getValueAt(row, 0);
            try {
                boolean success = rideService.bookRide(rideId);
                if (success) {
                    fileService.saveAllRides(rideService.getRides());
                    JOptionPane.showMessageDialog(mainFrame, "Seat booked successfully for Ride " + rideId + "!", "Booked", JOptionPane.INFORMATION_MESSAGE);
                    // Update table display
                    Ride updated = rideService.findRideById(rideId);
                    searchResultsModel.setValueAt(updated.getAvailableSeats(), row, 6);
                }
            } catch (RideNotFoundException | InvalidRideException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        tableActions.add(bookSelectedBtn);

        JPanel centerArea = new JPanel(new BorderLayout(10, 10));
        centerArea.setOpaque(false);
        centerArea.add(filterCard, BorderLayout.NORTH);
        centerArea.add(scrollPane, BorderLayout.CENTER);
        centerArea.add(tableActions, BorderLayout.SOUTH);

        panel.add(centerArea, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // SCREEN 7: My Rides Screen
    // =========================================================================
    private JPanel createMyRidesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Rides Offered by You");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JButton backBtn = createOutlineButton("← Back to Dashboard");
        backBtn.addActionListener(e -> showScreen(SCREEN_DASHBOARD));
        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Ride ID", "From", "To", "Date", "Departure Time", "Seats Left", "Fare ($)"};
        myRidesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(myRidesModel);
        styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton refreshBtn = createSecondaryButton("Refresh Rides");
        refreshBtn.addActionListener(e -> refreshMyRides());
        bottom.add(refreshBtn);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshMyRides() {
        myRidesModel.setRowCount(0);
        Student current = authService.getCurrentLoggedInStudent();
        if (current == null) return;

        ArrayList<Ride> userRides = rideService.getRidesByDriver(current.getStudentId());
        for (Ride r : userRides) {
            myRidesModel.addRow(new Object[]{
                    r.getRideId(),
                    r.getStartingLocation(),
                    r.getDestination(),
                    r.getDate(),
                    r.getDepartureTime(),
                    r.getAvailableSeats(),
                    String.format("%.2f", r.getCostPerPerson())
            });
        }
    }

    // =========================================================================
    // SCREEN 8: Profile Screen
    // =========================================================================
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 245, 249));

        JPanel card = createCardPanel(450, 420);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Student Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 23, 42));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileNameLabel = new JLabel("Name: ");
        profileIdLabel = new JLabel("Student ID: ");
        profileEmailLabel = new JLabel("College Email: ");
        profileRoleLabel = new JLabel("Role: Registered NSSCE Student");
        profileVerifyStatusLabel = new JLabel("Verification: Verified Student ✔");

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        profileNameLabel.setFont(labelFont);
        profileIdLabel.setFont(labelFont);
        profileEmailLabel.setFont(labelFont);
        profileRoleLabel.setFont(labelFont);
        profileVerifyStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profileVerifyStatusLabel.setForeground(new Color(16, 185, 129));

        JButton backBtn = createPrimaryButton("Return to Dashboard");
        backBtn.setMaximumSize(new Dimension(360, 38));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> showScreen(SCREEN_DASHBOARD));

        card.add(Box.createVerticalStrut(15));
        card.add(title);
        card.add(Box.createVerticalStrut(25));
        card.add(profileNameLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(profileIdLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(profileEmailLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(profileRoleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(profileVerifyStatusLabel);
        card.add(Box.createVerticalStrut(35));
        card.add(backBtn);
        card.add(Box.createVerticalStrut(15));

        panel.add(card);
        return panel;
    }

    private void refreshProfile() {
        Student current = authService.getCurrentLoggedInStudent();
        if (current != null) {
            // Polymorphism & Encapsulation demonstration
            current.displayProfile();

            profileNameLabel.setText("Name: " + current.getName());
            profileIdLabel.setText("Student ID: " + current.getStudentId());
            profileEmailLabel.setText("College Email: " + current.getEmail());
            profileRoleLabel.setText("Role: Registered NSSCE Student");
            boolean isVerified = authService.isEmailVerified(current.getEmail());
            profileVerifyStatusLabel.setText("Verification: " + (isVerified ? "Verified ✔" : "Pending ⏳"));
        }
    }

    // =========================================================================
    // Logout Action
    // =========================================================================
    private void handleLogout() {
        int opt = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            authService.logout();
            showScreen(SCREEN_LOGIN);
        }
    }

    // =========================================================================
    // UI Helpers & Modern Component Styling
    // =========================================================================
    private JPanel createCardPanel(int width, int height) {
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(width, height));
        card.setMaximumSize(new Dimension(width, height));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 25, 25, 25)
        ));
        return card;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private void addFormField(JPanel container, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(400, 34));

        container.add(label);
        container.add(Box.createVerticalStrut(4));
        container.add(field);
        container.add(Box.createVerticalStrut(10));
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(13, 148, 136)); // Teal accent
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(15, 23, 42));
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(7, 14, 7, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(71, 85, 105));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(225, 29, 72)); // Rose red
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(241, 245, 249));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(51, 65, 85));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }
}
