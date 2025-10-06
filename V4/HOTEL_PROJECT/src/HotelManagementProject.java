import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

class ImagePanel extends JPanel {
    private final Image img;
    public ImagePanel(Image img) {
        this.img = img;
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

public class HotelManagementProject extends JFrame {

    // Database credentials and connection
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String USER = "root";
    private static final String PASS = "Mourya@1721";

    private Connection conn;
    private final String userRole;
    private JTabbedPane tabbedPane;
    private JPanel reservationPanel, viewPanel, paymentPanel, billingPanel; // Added billingPanel
    private JTextField customerIdField, fNameField, mInitField, lNameField, proofField, genderField, emailField;
    private JTextField guestCountField, chkInField, chkOutField, roomPrefTypeField;
    private JButton submitReservationButton;
    private JComboBox<String> viewTableComboBox;
    private JTable dataTable;
    private JButton refreshButton, updateButton, deleteButton;
    private DefaultTableModel tableModel;
    private JTextField paymentResIdField, paymentAmountField;
    private JComboBox<String> paymentMethodComboBox, paymentStatusComboBox;
    private JButton submitPaymentButton, logoutButton;
    private static Image bgImg;
    
    // Billing Panel Components
    private JTextField billingResIdField;
    private JButton generateBillButton;
    private JTextArea billDisplayArea;

    public HotelManagementProject(String userRole, Image img) {
        super("Hotel Reservation System (" + userRole + ")");
        this.userRole = userRole;
        this.bgImg = img;
        connectToDatabase();
        initializeUI();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Database connection successful!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeUI() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));

        createReservationPanel();
        createViewPanel();

        tabbedPane.addTab("Make Reservation", reservationPanel);
        tabbedPane.addTab("View Data", viewPanel);

        if ("ADMIN".equals(userRole) || "EMPLOYEE".equals(userRole)) { // Added for EMPLOYEES too
            createPaymentPanel();
            tabbedPane.addTab("Record Payment", paymentPanel);
            createBillingPanel(); // New billing panel
            tabbedPane.addTab("Generate Bill", billingPanel); // New billing tab
        }

        logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(200, 30, 30));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoutButton.setPreferredSize(new Dimension(140, 45));
        logoutButton.addActionListener(e -> {
            dispose();
            LoginFrame.showLoginScreen(bgImg);
        });

        JPanel content = new JPanel(new BorderLayout()) {
            @Override
            public boolean isOpaque() { return false; }
        };
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.add(tabbedPane, BorderLayout.CENTER);
        JPanel logoutBar = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
            @Override
            public boolean isOpaque() { return false; }
        };
        logoutBar.add(logoutButton);
        content.add(logoutBar, BorderLayout.SOUTH);

        setContentPane(new ImagePanel(bgImg));
        getContentPane().add(content, BorderLayout.CENTER);
    }

    private void createReservationPanel() {
        reservationPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            public boolean isOpaque() { return false; }
        };
        reservationPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 14, 14)) {
            @Override
            public boolean isOpaque() { return false; }
        };
        formPanel.setBackground(new Color(255, 255, 255, 150));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);

        JLabel lbl;
        lbl = new JLabel("Customer ID:"); lbl.setFont(labelFont); formPanel.add(lbl);
        customerIdField = new JTextField(UUID.randomUUID().toString().substring(0, 8));
        customerIdField.setEnabled(false); customerIdField.setFont(fieldFont); formPanel.add(customerIdField);

        lbl = new JLabel("First Name:"); lbl.setFont(labelFont); formPanel.add(lbl);
        fNameField = new JTextField(); fNameField.setFont(fieldFont); formPanel.add(fNameField);

        lbl = new JLabel("Middle Initial:"); lbl.setFont(labelFont); formPanel.add(lbl);
        mInitField = new JTextField(); mInitField.setFont(fieldFont); formPanel.add(mInitField);

        lbl = new JLabel("Last Name:"); lbl.setFont(labelFont); formPanel.add(lbl);
        lNameField = new JTextField(); lNameField.setFont(fieldFont); formPanel.add(lNameField);

        lbl = new JLabel("Proof ID:"); lbl.setFont(labelFont); formPanel.add(lbl);
        proofField = new JTextField(); proofField.setFont(fieldFont); formPanel.add(proofField);

        lbl = new JLabel("Gender:"); lbl.setFont(labelFont); formPanel.add(lbl);
        genderField = new JTextField(); genderField.setFont(fieldFont); formPanel.add(genderField);

        lbl = new JLabel("Email:"); lbl.setFont(labelFont); formPanel.add(lbl);
        emailField = new JTextField(); emailField.setFont(fieldFont); formPanel.add(emailField);

        lbl = new JLabel("Guest Count:"); lbl.setFont(labelFont); formPanel.add(lbl);
        guestCountField = new JTextField(); guestCountField.setFont(fieldFont); formPanel.add(guestCountField);

        lbl = new JLabel("Check-in (YYYY-MM-DD):"); lbl.setFont(labelFont); formPanel.add(lbl);
        chkInField = new JTextField(); chkInField.setFont(fieldFont); formPanel.add(chkInField);

        lbl = new JLabel("Check-out (YYYY-MM-DD):"); lbl.setFont(labelFont); formPanel.add(lbl);
        chkOutField = new JTextField(); chkOutField.setFont(fieldFont); formPanel.add(chkOutField);

        lbl = new JLabel("Room Pref. Type:"); lbl.setFont(labelFont); formPanel.add(lbl);
        roomPrefTypeField = new JTextField(); roomPrefTypeField.setFont(fieldFont); formPanel.add(roomPrefTypeField);

        submitReservationButton = new JButton("Submit Reservation");
        submitReservationButton.setBackground(new Color(0, 123, 255));
        submitReservationButton.setForeground(Color.WHITE);
        submitReservationButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        submitReservationButton.setPreferredSize(new Dimension(250, 50));
        submitReservationButton.addActionListener(e -> submitReservation());

        JPanel southPanel = new JPanel() {
            @Override
            public boolean isOpaque() { return false; }
        };
        southPanel.add(submitReservationButton);

        reservationPanel.add(formPanel, BorderLayout.CENTER);
        reservationPanel.add(southPanel, BorderLayout.SOUTH);
    }

    private void createViewPanel() {
        viewPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            public boolean isOpaque() { return false; }
        };
        viewPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8)) {
            @Override
            public boolean isOpaque() { return false; }
        };

        String[] tables = {"CUSTOMER", "RESERVATION", "CONFIRMATION", "ROOMS", "HOTEL", "PAYMENT", "BOOKED_VIA"};
        viewTableComboBox = new JComboBox<>(tables);
        viewTableComboBox.setFont(new Font("Segoe UI", Font.BOLD, 15));
        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        updateButton = new JButton("Update Row");
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        deleteButton = new JButton("Delete Row");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JButton[] viewBtns = {refreshButton, updateButton, deleteButton};
        for (JButton b : viewBtns) {
            b.setPreferredSize(new Dimension(140, 40));
            b.setBackground(new Color(30, 150, 70));
            b.setForeground(Color.WHITE);
        }

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(29);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JTableHeader header = dataTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 17));
        header.setBackground(new Color(52, 152, 219));
        header.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(dataTable);

        refreshButton.addActionListener(e -> loadTableData(viewTableComboBox.getSelectedItem().toString()));
        viewTableComboBox.addActionListener(e -> loadTableData(viewTableComboBox.getSelectedItem().toString()));
        
        if ("ADMIN".equals(userRole)) {
            deleteButton.addActionListener(e -> deleteSelectedRow(viewTableComboBox.getSelectedItem().toString()));
            updateButton.addActionListener(e -> updateSelectedRow(viewTableComboBox.getSelectedItem().toString()));
            controlPanel.add(updateButton);
            controlPanel.add(deleteButton);
        }

        controlPanel.add(new JLabel("Select Table:"));
        controlPanel.add(viewTableComboBox);
        controlPanel.add(refreshButton);
        

        JPanel transparentMid = new JPanel(new BorderLayout()) {
            @Override
            public boolean isOpaque() { return false; }
        };
        transparentMid.add(scrollPane, BorderLayout.CENTER);

        viewPanel.add(controlPanel, BorderLayout.NORTH);
        viewPanel.add(transparentMid, BorderLayout.CENTER);

        if (conn != null) {
            loadTableData("CUSTOMER");
        }
    }

    private void createPaymentPanel() {
        paymentPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            public boolean isOpaque() { return false; }
        };
        paymentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 14, 14)) {
            @Override
            public boolean isOpaque() { return false; }
        };
        formPanel.setBackground(new Color(255, 255, 255, 150));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);

        JLabel lbl;
        lbl = new JLabel("Reservation ID:"); lbl.setFont(labelFont); formPanel.add(lbl);
        paymentResIdField = new JTextField(); paymentResIdField.setFont(fieldFont); formPanel.add(paymentResIdField);

        lbl = new JLabel("Amount:"); lbl.setFont(labelFont); formPanel.add(lbl);
        paymentAmountField = new JTextField(); paymentAmountField.setFont(fieldFont); formPanel.add(paymentAmountField);

        lbl = new JLabel("Payment Method:"); lbl.setFont(labelFont); formPanel.add(lbl);
        paymentMethodComboBox = new JComboBox<>(new String[]{"Credit Card", "Debit Card", "UPI", "Cash"});
        paymentMethodComboBox.setFont(fieldFont); formPanel.add(paymentMethodComboBox);

        lbl = new JLabel("Payment Status:"); lbl.setFont(labelFont); formPanel.add(lbl);
        paymentStatusComboBox = new JComboBox<>(new String[]{"Completed", "Pending", "Failed"});
        paymentStatusComboBox.setFont(fieldFont); formPanel.add(paymentStatusComboBox);

        submitPaymentButton = new JButton("Record Payment");
        submitPaymentButton.setBackground(new Color(40, 167, 69));
        submitPaymentButton.setForeground(Color.WHITE);
        submitPaymentButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        submitPaymentButton.setPreferredSize(new Dimension(220, 50));
        submitPaymentButton.addActionListener(e -> submitPayment());

        JPanel southPanel = new JPanel() {
            @Override
            public boolean isOpaque() { return false; }
        };
        southPanel.add(submitPaymentButton);

        paymentPanel.add(formPanel, BorderLayout.CENTER);
        paymentPanel.add(southPanel, BorderLayout.SOUTH);
    }
    
    // --- New Billing Panel Method ---
    private void createBillingPanel() {
        billingPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            public boolean isOpaque() { return false; }
        };
        billingPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)) {
            @Override
            public boolean isOpaque() { return false; }
        };
        
        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);

        inputPanel.add(new JLabel("Reservation ID:"));
        billingResIdField = new JTextField(15);
        billingResIdField.setFont(fieldFont);
        inputPanel.add(billingResIdField);

        generateBillButton = new JButton("Generate Bill");
        generateBillButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        generateBillButton.setBackground(new Color(255, 193, 7));
        generateBillButton.setForeground(Color.BLACK);
        generateBillButton.addActionListener(e -> generateBill());
        inputPanel.add(generateBillButton);

        billDisplayArea = new JTextArea(15, 40);
        billDisplayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        billDisplayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(billDisplayArea);

        billingPanel.add(inputPanel, BorderLayout.NORTH);
        billingPanel.add(scrollPane, BorderLayout.CENTER);
    }
    // --- End of New Method ---

    private void submitReservation() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Cannot proceed.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
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

        if (fName.isEmpty() || lName.isEmpty() || proof.isEmpty() || email.isEmpty() ||
            guestCount.isEmpty() || chkIn.isEmpty() || chkOut.isEmpty() || roomPrefType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            conn.setAutoCommit(false);
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

            String confId = "CONF" + UUID.randomUUID().toString().substring(0, 5);
            String insertConfirmationSQL = "INSERT INTO CONFIRMATION (CONF_ID, CONF_MODE, CONF_DATE, STATUS, RESER_ID) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmtConfirmation = conn.prepareStatement(insertConfirmationSQL);
            pstmtConfirmation.setString(1, confId);
            pstmtConfirmation.setString(2, "Online");
            pstmtConfirmation.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstmtConfirmation.setString(4, "Pending");
            pstmtConfirmation.setString(5, resId);
            pstmtConfirmation.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(this, "Reservation submitted successfully! Your Reservation ID is: " + resId, "Success", JOptionPane.INFORMATION_MESSAGE);

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
        String resId = paymentResIdField.getText();
        String amountStr = paymentAmountField.getText();
        String payMethod = paymentMethodComboBox.getSelectedItem().toString();
        String payStatus = paymentStatusComboBox.getSelectedItem().toString();
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

            paymentResIdField.setText("");
            paymentAmountField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    // --- New Generate Bill Method ---
    private void generateBill() {
        String resId = billingResIdField.getText().trim();
        if (resId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Reservation ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // SQL to get reservation and customer details
            String sql = "SELECT c.F_NAME, c.L_NAME, r.CHK_IN, r.CHK_OUT, r.ROOM_PREF_TYPE " +
                         "FROM RESERVATION r " +
                         "JOIN CUSTOMER c ON r.CUSTOMER_ID = c.CUSTOMER_ID " +
                         "WHERE r.RESER_ID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, resId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fName = rs.getString("F_NAME");
                String lName = rs.getString("L_NAME");
                LocalDate checkIn = rs.getDate("CHK_IN").toLocalDate();
                LocalDate checkOut = rs.getDate("CHK_OUT").toLocalDate();
                String roomType = rs.getString("ROOM_PREF_TYPE");
                
                // Calculate number of nights
                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                
                // Get room price from ROOMS table
                String priceSql = "SELECT PRICE FROM ROOMS WHERE ROOM_TYPE = ?";
                PreparedStatement pricePstmt = conn.prepareStatement(priceSql);
                pricePstmt.setString(1, roomType);
                ResultSet priceRs = pricePstmt.executeQuery();
                
                double roomPrice = 0.0;
                if (priceRs.next()) {
                    roomPrice = priceRs.getDouble("PRICE");
                } else {
                    JOptionPane.showMessageDialog(this, "Room type price not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double totalAmount = nights * roomPrice;
                
                // Construct the bill text
                String bill = String.format(
                    "=========================================\n" +
                    "           HOTEL RESERVATION BILL\n" +
                    "=========================================\n" +
                    "Reservation ID: %s\n" +
                    "Customer Name:  %s %s\n" +
                    "-----------------------------------------\n" +
                    "Check-in Date:  %s\n" +
                    "Check-out Date: %s\n" +
                    "Nights Stayed:  %d\n" +
                    "-----------------------------------------\n" +
                    "Room Type:      %s\n" +
                    "Room Rate/Night: $%.2f\n" +
                    "-----------------------------------------\n" +
                    "Total Amount:   $%.2f\n" +
                    "=========================================\n",
                    resId, fName, lName, checkIn, checkOut, nights, roomType, roomPrice, totalAmount
                );
                
                billDisplayArea.setText(bill);
            } else {
                JOptionPane.showMessageDialog(this, "Reservation ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                billDisplayArea.setText("");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while generating the bill: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            billDisplayArea.setText("");
        }
    }
    // --- End of New Method ---

    private void loadTableData(String tableName) {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Cannot load data.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = metaData.getColumnName(i);
            }
            tableModel.setColumnIdentifiers(columnNames);

            tableModel.setRowCount(0);
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
    
    private void deleteSelectedRow(String tableName) {
        int row = dataTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row.");
            return;
        }
        String primaryKey = dataTable.getValueAt(row, 0).toString();
        try {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM " + tableName + " WHERE " + dataTable.getColumnName(0) + "=?");
            pstmt.setString(1, primaryKey);
            pstmt.executeUpdate();
            loadTableData(tableName);
            JOptionPane.showMessageDialog(this, "Row Deleted.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Delete Error: " + e.getMessage());
        }
    }

    private void updateSelectedRow(String tableName) {
        int row = dataTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row.");
            return;
        }
        int colCount = dataTable.getColumnCount();
        JTextField[] fields = new JTextField[colCount];
        JPanel panel = new JPanel(new GridLayout(colCount, 2));
        for (int i = 0; i < colCount; i++) {
            panel.add(new JLabel(dataTable.getColumnName(i)));
            fields[i] = new JTextField(dataTable.getValueAt(row, i).toString());
            panel.add(fields[i]);
        }
        int result = JOptionPane.showConfirmDialog(this, panel, "Update Row", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
                for (int i = 1; i < colCount; i++) sql.append(dataTable.getColumnName(i)).append("=?, ");
                sql.setLength(sql.length() - 2);
                sql.append(" WHERE ").append(dataTable.getColumnName(0)).append("=?");
                PreparedStatement pstmt = conn.prepareStatement(sql.toString());
                for (int i = 1; i < colCount; i++) pstmt.setString(i, fields[i].getText());
                pstmt.setString(colCount, fields[0].getText());
                pstmt.executeUpdate();
                loadTableData(tableName);
                JOptionPane.showMessageDialog(this, "Row Updated.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Update Error: " + e.getMessage());
            }
        }
    }

    // --- Login Frame Implementation (Nested Class) ---
    private static class LoginFrame extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton loginButton;
        private static Image bgImg;

        // Hardcoded Users and Roles Map
        private final Map<String, String> USERS = new HashMap<>() {{
            put("root", "Mourya@1721");
            put("emp", "5678");
        }};
        private final Map<String, String> ROLES = new HashMap<>() {{
            put("root", "ADMIN");
            put("emp", "EMPLOYEE");
        }};

        public LoginFrame(Image img) {
            super("System Login");
            LoginFrame.bgImg = img;
            setContentPane(new ImagePanel(bgImg));
            initializeLoginUI();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(420, 300);
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void initializeLoginUI() {
            JPanel mainPanel = new JPanel(new GridBagLayout()) {
                @Override
                public boolean isOpaque() { return false; }
            };
            mainPanel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel titleLbl = new JLabel("Hotel System Login");
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 26, 0);
            mainPanel.add(titleLbl, gbc);

            gbc.insets = new Insets(0, 0, 12, 8); gbc.gridwidth = 1;
            gbc.gridy++; gbc.gridx = 0;
            mainPanel.add(new JLabel("Username:"), gbc);

            gbc.gridx = 1;
            usernameField = new JTextField(20);
            usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            mainPanel.add(usernameField, gbc);

            gbc.gridy++; gbc.gridx = 0;
            mainPanel.add(new JLabel("Password:"), gbc);

            gbc.gridx = 1;
            passwordField = new JPasswordField(20);
            passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            mainPanel.add(passwordField, gbc);

            gbc.gridy++; gbc.gridx = 1;
            loginButton = new JButton("Login");
            loginButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
            loginButton.setPreferredSize(new Dimension(130, 45));
            loginButton.setBackground(new Color(23, 162, 184));
            loginButton.setForeground(Color.WHITE);
            loginButton.addActionListener(e -> attemptLogin());
            mainPanel.add(loginButton, gbc);

            add(mainPanel, BorderLayout.CENTER);
        }

        private void attemptLogin() {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (USERS.containsKey(username) && USERS.get(username).equals(password)) {
                String role = ROLES.get(username);
                JOptionPane.showMessageDialog(this, "Login Successful! Role: " + role, "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new HotelManagementProject(role, bgImg).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }

        public static void showLoginScreen(Image img) {
            SwingUtilities.invokeLater(() -> new LoginFrame(img));
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf"); } catch (Exception ignored) {}
        bgImg = Toolkit.getDefaultToolkit().getImage("D:/HOTEL_PROJECT_DBMS/V4/HOTEL_PROJECT/src/image.jpg");
        LoginFrame.showLoginScreen(bgImg);
    }
}