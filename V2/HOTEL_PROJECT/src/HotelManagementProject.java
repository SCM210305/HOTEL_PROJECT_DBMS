import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// --- Main Application Frame (Visible after successful login) ---

public class HotelManagementProject extends JFrame {

    // Database credentials and connection
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String USER = "root";
    // ATTENTION: **CHANGE THIS TO YOUR ACTUAL MYSQL PASSWORD**
    private static final String PASS = "Mourya@1721"; 

    private Connection conn;
    private final String userRole; // Stores the role passed from the LoginFrame

    // GUI Components
    private JTabbedPane tabbedPane;
    private JPanel reservationPanel;
    private JPanel viewPanel;
    private JPanel paymentPanel; // Only initialized for Admin role

    // Reservation Panel Components
    private JTextField customerIdField, fNameField, mInitField, lNameField, proofField, genderField, emailField;
    private JTextField guestCountField, chkInField, chkOutField, roomPrefTypeField;
    private JButton submitReservationButton;

    // View Panel Components
    private JComboBox<String> viewTableComboBox;
    private JTable dataTable;
    private JButton refreshButton;
    private DefaultTableModel tableModel;

    // Payment Panel Components
    private JTextField paymentResIdField, paymentAmountField;
    private JComboBox<String> paymentMethodComboBox, paymentStatusComboBox;
    private JButton submitPaymentButton;


    public HotelManagementProject(String userRole) { // Corrected constructor name to match class name
        super("Hotel Reservation System (" + userRole + ")");
        this.userRole = userRole;
        connectToDatabase();
        initializeUI();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Check if password is still the default placeholder
            if (PASS.equals("your_password_here")) {
                throw new SQLException("Database password is not set. Please update the PASS variable in HotelManagementProject.java.");
            }
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Database connection successful!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to the database. " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            // Do not exit here, allow LoginFrame to handle startup errors if necessary
        }
    }

    private void initializeUI() {
        tabbedPane = new JTabbedPane();

        // These tabs are visible to all authenticated users (Admin and Employee)
        createReservationPanel();
        createViewPanel();

        tabbedPane.addTab("Make a Reservation", reservationPanel);
        tabbedPane.addTab("View Data", viewPanel);

        // Conditional Tab Visibility: Only Admin sees the Payment Tab
        if (userRole.equals("ADMIN")) {
            createPaymentPanel();
            tabbedPane.addTab("Record Payment", paymentPanel);
        }

        add(tabbedPane);
    }

    private void createReservationPanel() {
        reservationPanel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Customer fields
        formPanel.add(new JLabel("Customer ID:"));
        customerIdField = new JTextField();
        customerIdField.setText(UUID.randomUUID().toString().substring(0, 8));
        customerIdField.setEnabled(false);
        formPanel.add(customerIdField);

        formPanel.add(new JLabel("First Name:"));
        fNameField = new JTextField();
        formPanel.add(fNameField);

        formPanel.add(new JLabel("Middle Initial:"));
        mInitField = new JTextField();
        formPanel.add(mInitField);

        formPanel.add(new JLabel("Last Name:"));
        lNameField = new JTextField();
        formPanel.add(lNameField);

        formPanel.add(new JLabel("Proof ID:"));
        proofField = new JTextField();
        formPanel.add(proofField);

        formPanel.add(new JLabel("Gender:"));
        genderField = new JTextField();
        formPanel.add(genderField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        // Reservation fields
        formPanel.add(new JLabel("Guest Count:"));
        guestCountField = new JTextField();
        formPanel.add(guestCountField);

        formPanel.add(new JLabel("Check-in (YYYY-MM-DD):"));
        chkInField = new JTextField();
        formPanel.add(chkInField);

        formPanel.add(new JLabel("Check-out (YYYY-MM-DD):"));
        chkOutField = new JTextField();
        formPanel.add(chkOutField);

        formPanel.add(new JLabel("Room Pref. Type:"));
        roomPrefTypeField = new JTextField();
        formPanel.add(roomPrefTypeField);

        submitReservationButton = new JButton("Submit Reservation");
        submitReservationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitReservation();
            }
        });

        reservationPanel.add(formPanel, BorderLayout.CENTER);
        reservationPanel.add(submitReservationButton, BorderLayout.SOUTH);
    }

    private void createViewPanel() {
        viewPanel = new JPanel(new BorderLayout(10, 10));
        JPanel controlPanel = new JPanel(new FlowLayout());

        String[] tables = {"CUSTOMER", "RESERVATION", "CONFIRMATION", "ROOMS", "HOTEL", "PAYMENT", "BOOKED_VIA"};
        viewTableComboBox = new JComboBox<>(tables);
        refreshButton = new JButton("Refresh");

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTableData(viewTableComboBox.getSelectedItem().toString());
            }
        });

        viewTableComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTableData(viewTableComboBox.getSelectedItem().toString());
            }
        });

        controlPanel.add(new JLabel("Select a table:"));
        controlPanel.add(viewTableComboBox);
        controlPanel.add(refreshButton);

        viewPanel.add(controlPanel, BorderLayout.NORTH);
        viewPanel.add(scrollPane, BorderLayout.CENTER);

        // Load initial data for the default table (CUSTOMER) if connection is active
        if (conn != null) {
            loadTableData("CUSTOMER");
        }
    }

    private void createPaymentPanel() {
        paymentPanel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Payment fields
        formPanel.add(new JLabel("Reservation ID:"));
        paymentResIdField = new JTextField();
        formPanel.add(paymentResIdField);

        formPanel.add(new JLabel("Amount:"));
        paymentAmountField = new JTextField();
        formPanel.add(paymentAmountField);

        formPanel.add(new JLabel("Payment Method:"));
        String[] payMethods = {"Credit Card", "Debit Card", "UPI", "Cash"};
        paymentMethodComboBox = new JComboBox<>(payMethods);
        formPanel.add(paymentMethodComboBox);

        formPanel.add(new JLabel("Payment Status:"));
        String[] payStatuses = {"Completed", "Pending", "Failed"};
        paymentStatusComboBox = new JComboBox<>(payStatuses);
        formPanel.add(paymentStatusComboBox);

        submitPaymentButton = new JButton("Record Payment");
        submitPaymentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitPayment();
            }
        });

        paymentPanel.add(formPanel, BorderLayout.CENTER);
        paymentPanel.add(submitPaymentButton, BorderLayout.SOUTH);
    }

    private void submitReservation() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Cannot proceed.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Existing submitReservation logic
        String customerId = customerIdField.getText();
        String fName = fNameField.getText();
        String mInit = mInitField.getText();
        String lName = lNameField.getText();
        String proof = proofField.getText();
        String gender = genderField.getText();
        String email = emailField.getText();

        String guestCount = guestCountField.getText();
        String chkIn = chkInField.getText();
        String chkOut = chkOutField.getText();
        String roomPrefType = roomPrefTypeField.getText();

        // Basic validation
        if (fName.isEmpty() || lName.isEmpty() || proof.isEmpty() || email.isEmpty() ||
            guestCount.isEmpty() || chkIn.isEmpty() || chkOut.isEmpty() || roomPrefType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into CUSTOMER
            String insertCustomerSQL = "INSERT INTO CUSTOMER (CUSTOMER_ID, F_NAME, M_INIT, L_NAME, PROOF, GENDER, EMAIL) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmtCustomer = conn.prepareStatement(insertCustomerSQL);
            pstmtCustomer.setString(1, customerId);
            pstmtCustomer.setString(2, fName);
            pstmtCustomer.setString(3, mInit);
            pstmtCustomer.setString(4, lName);
            pstmtCustomer.setString(5, proof);
            pstmtCustomer.setString(6, gender);
            pstmtCustomer.setString(7, email);
            pstmtCustomer.executeUpdate();

            // 2. Insert into RESERVATION
            String resId = "RES" + UUID.randomUUID().toString().substring(0, 5);
            String insertReservationSQL = "INSERT INTO RESERVATION (RESER_ID, GUEST_COUNT, CHK_IN, CHK_OUT, ROOM_PREF_TYPE, CUSTOMER_ID, PROOF, EMAIL) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmtReservation = conn.prepareStatement(insertReservationSQL);
            pstmtReservation.setString(1, resId);
            pstmtReservation.setInt(2, Integer.parseInt(guestCount));
            pstmtReservation.setDate(3, java.sql.Date.valueOf(chkIn));
            pstmtReservation.setDate(4, java.sql.Date.valueOf(chkOut));
            pstmtReservation.setString(5, roomPrefType);
            pstmtReservation.setString(6, customerId);
            pstmtReservation.setString(7, proof);
            pstmtReservation.setString(8, email);
            pstmtReservation.executeUpdate();

            // 3. Insert into CONFIRMATION
            String confId = "CONF" + UUID.randomUUID().toString().substring(0, 5);
            String insertConfirmationSQL = "INSERT INTO CONFIRMATION (CONF_ID, CONF_MODE, CONF_DATE, STATUS, RESER_ID) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmtConfirmation = conn.prepareStatement(insertConfirmationSQL);
            pstmtConfirmation.setString(1, confId);
            pstmtConfirmation.setString(2, "Online");
            pstmtConfirmation.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstmtConfirmation.setString(4, "Pending");
            pstmtConfirmation.setString(5, resId);
            pstmtConfirmation.executeUpdate();

            conn.commit(); // Commit the transaction
            JOptionPane.showMessageDialog(this, "Reservation submitted successfully! Your Reservation ID is: " + resId, "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear fields for a new entry
            customerIdField.setText(UUID.randomUUID().toString().substring(0, 8));
            fNameField.setText("");
            mInitField.setText("");
            lNameField.setText("");
            proofField.setText("");
            genderField.setText("");
            emailField.setText("");
            guestCountField.setText("");
            chkInField.setText("");
            chkOutField.setText("");
            roomPrefTypeField.setText("");

        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
             JOptionPane.showMessageDialog(this, "Invalid Date or Guest Count Format.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitPayment() {
         if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Cannot proceed.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Existing submitPayment logic
        String resId = paymentResIdField.getText();
        String amountStr = paymentAmountField.getText();
        String payMethod = paymentMethodComboBox.getSelectedItem().toString();
        String payStatus = paymentStatusComboBox.getSelectedItem().toString();

        // Basic validation
        if (resId.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter reservation ID and amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String payId = "PAY" + UUID.randomUUID().toString().substring(0, 5);
            String payRef = "REF" + UUID.randomUUID().toString().substring(0, 5);

            String sql = "INSERT INTO PAYMENT (PAYMENT_ID, PAY_REF, AMOUNT, PAY_METHOD, PAY_STATUS, RESER_ID) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, payId);
            pstmt.setString(2, payRef);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, payMethod);
            pstmt.setString(5, payStatus);
            pstmt.setString(6, resId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Payment for Reservation ID " + resId + " recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record payment. Check if Reservation ID exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            // Clear fields for new entry
            paymentResIdField.setText("");
            paymentAmountField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadTableData(String tableName) {
         if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Cannot load data.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();

            // Get column names
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = metaData.getColumnName(i);
            }
            tableModel.setColumnIdentifiers(columnNames);

            // Get data rows
            tableModel.setRowCount(0); // Clear existing data
            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(rowData);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load data from table: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // --- Login Frame Implementation (Nested Class) ---
    private static class LoginFrame extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton loginButton;

        // Hardcoded Users and Roles Map
        private final Map<String, String> USERS = new HashMap<>() {{
            put("root", "Mourya@1721");
            put("emp", "5678");
        }};
        private final Map<String, String> ROLES = new HashMap<>() {{
            put("root", "ADMIN");
            put("emp", "EMPLOYEE");
        }};

        public LoginFrame() {
            super("System Login");
            initializeLoginUI();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(350, 200);
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void initializeLoginUI() {
            // FIX: Removed incorrect setLayout/ClassCastException lines
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            ((GridLayout)panel.getLayout()).setVgap(15); // Setting vgap on the panel's layout

            panel.add(new JLabel("Username:"));
            usernameField = new JTextField();
            panel.add(usernameField);

            panel.add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            panel.add(passwordField);

            panel.add(new JLabel("")); // Placeholder for grid alignment

            loginButton = new JButton("Login");
            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    attemptLogin();
                }
            });
            panel.add(loginButton);

            // Add the content panel to the center of the LoginFrame
            add(panel, BorderLayout.CENTER); 
        }

        private void attemptLogin() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (USERS.containsKey(username) && USERS.get(username).equals(password)) {
                // Successful login
                String role = ROLES.get(username);
                JOptionPane.showMessageDialog(this, "Login Successful! Role: " + role, "Success", JOptionPane.INFORMATION_MESSAGE);

                // Close login window and open the main application frame
                dispose();
                // FIX: Corrected class name reference to HotelManagementProject
                HotelManagementProject mainApp = new HotelManagementProject(role); 
                mainApp.setVisible(true);

            } else {
                // Failed login
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Main Method (Application Entry Point) ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}
