package hotel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class HotelManagementProject extends JFrame {

    private final String userRole;

    private JTabbedPane tabbedPane;
    private JPanel reservationPanel, viewPanel, paymentPanel, billingPanel, manageRoomsPanelContainer;

    private JTextField customerIdField, fNameField, mInitField, lNameField, proofField, genderField, emailField;
    private JTextField guestCountField, chkInField, chkOutField;
    private JComboBox<String> roomPrefComboBox;
    private JButton submitReservationButton;

    private JComboBox<String> viewTableComboBox;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    private JTextField paymentResIdField, paymentAmountField;
    private JComboBox<String> paymentMethodComboBox, paymentStatusComboBox;
    private JButton submitPaymentButton;

    private JTextField billingResIdField;
    private JButton generateBillButton;
    private JTextArea billDisplayArea;

    private JButton logoutButton;

    private Connection conn;
    private Image bgImg;

    public HotelManagementProject(String userRole, Image img) {
        super("Hotel Reservation System (" + userRole + ")");
        this.userRole = userRole;
        this.bgImg = img;

        try {
            conn = DBUtils.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed:\n" + e.getMessage());
            System.exit(1);
        }

        initializeUI();

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initializeUI() {

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));

        createReservationPanel();
        createViewPanel();

        tabbedPane.addTab("Make Reservation", reservationPanel);
        tabbedPane.addTab("View Data", viewPanel);

        if ("ADMIN".equals(userRole)) {
            createPaymentPanel();
            tabbedPane.addTab("Record Payment", paymentPanel);

            createBillingPanel();
            tabbedPane.addTab("Generate Bill", billingPanel);

            createManageRoomsContainer();
            tabbedPane.addTab("Manage Rooms", manageRoomsPanelContainer);
        }

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoutButton.setBackground(new Color(200, 40, 40));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setPreferredSize(new Dimension(140, 45));
        logoutButton.addActionListener(e -> {
            dispose();
            LoginFrame.showLoginScreen(bgImg);
        });

        JPanel content = new JPanel(new BorderLayout()) {
            @Override public boolean isOpaque() { return false; }
        };
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        content.add(tabbedPane, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
            @Override public boolean isOpaque() { return false; }
        };
        south.add(logoutButton);

        content.add(south, BorderLayout.SOUTH);

        setContentPane(new ImagePanel(bgImg));
        getContentPane().add(content, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------
    // RESERVATION PANEL
    // ------------------------------------------------------------

    private void createReservationPanel() {

        reservationPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override public boolean isOpaque() { return false; }
        };
        reservationPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel form = new JPanel(new GridLayout(0, 2, 14, 14)) {
            @Override public boolean isOpaque() { return false; }
        };

        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 16);

        JLabel lbl;

        lbl = new JLabel("Customer ID:"); lbl.setFont(labelFont);
        customerIdField = new JTextField(UUID.randomUUID().toString().substring(0, 8));
        customerIdField.setEnabled(false);
        customerIdField.setFont(fieldFont);
        form.add(lbl); form.add(customerIdField);

        lbl = new JLabel("First Name:"); lbl.setFont(labelFont);
        fNameField = new JTextField(); fNameField.setFont(fieldFont);
        form.add(lbl); form.add(fNameField);

        lbl = new JLabel("Middle Initial:"); lbl.setFont(labelFont);
        mInitField = new JTextField(); mInitField.setFont(fieldFont);
        form.add(lbl); form.add(mInitField);

        lbl = new JLabel("Last Name:"); lbl.setFont(labelFont);
        lNameField = new JTextField(); lNameField.setFont(fieldFont);
        form.add(lbl); form.add(lNameField);

        lbl = new JLabel("Proof ID:"); lbl.setFont(labelFont);
        proofField = new JTextField(); proofField.setFont(fieldFont);
        form.add(lbl); form.add(proofField);

        lbl = new JLabel("Gender:"); lbl.setFont(labelFont);
        genderField = new JTextField(); genderField.setFont(fieldFont);
        form.add(lbl); form.add(genderField);

        lbl = new JLabel("Email:"); lbl.setFont(labelFont);
        emailField = new JTextField(); emailField.setFont(fieldFont);
        form.add(lbl); form.add(emailField);

        lbl = new JLabel("Guest Count:"); lbl.setFont(labelFont);
        guestCountField = new JTextField(); guestCountField.setFont(fieldFont);
        form.add(lbl); form.add(guestCountField);

        lbl = new JLabel("Check-in (YYYY-MM-DD):"); lbl.setFont(labelFont);
        chkInField = new JTextField(); chkInField.setFont(fieldFont);
        form.add(lbl); form.add(chkInField);

        lbl = new JLabel("Check-out (YYYY-MM-DD):"); lbl.setFont(labelFont);
        chkOutField = new JTextField(); chkOutField.setFont(fieldFont);
        form.add(lbl); form.add(chkOutField);

        lbl = new JLabel("Room Pref Type:"); lbl.setFont(labelFont);
        roomPrefComboBox = new JComboBox<>(); roomPrefComboBox.setFont(fieldFont);
        form.add(lbl); form.add(roomPrefComboBox);

        populateRoomTypes();

        submitReservationButton = new JButton("Submit Reservation");
        submitReservationButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        submitReservationButton.setBackground(new Color(0, 123, 255));
        submitReservationButton.setForeground(Color.WHITE);
        submitReservationButton.setPreferredSize(new Dimension(250, 50));
        submitReservationButton.addActionListener(e -> submitReservation());

        JPanel south = new JPanel() {
            @Override public boolean isOpaque() { return false; }
        };
        south.add(submitReservationButton);

        reservationPanel.add(form, BorderLayout.CENTER);
        reservationPanel.add(south, BorderLayout.SOUTH);
    }

    private void populateRoomTypes() {
        roomPrefComboBox.removeAllItems();

        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT DISTINCT ROOM_TYPE FROM ROOMS WHERE STATUS='Available'")) {

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                roomPrefComboBox.addItem(rs.getString("ROOM_TYPE"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	private void submitReservation() {
    if (conn == null) {
        JOptionPane.showMessageDialog(this, "Database connection failed.");
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

    if (fName.isEmpty() || lName.isEmpty() || proof.isEmpty() ||
        email.isEmpty() || guestCount.isEmpty() ||
        chkIn.isEmpty() || chkOut.isEmpty() ||
        roomPrefComboBox.getItemCount() == 0) {

        JOptionPane.showMessageDialog(this,
                "Please fill all fields and ensure room types are available.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        conn.setAutoCommit(false);

        // INSERT CUSTOMER
        String sql1 = "INSERT INTO CUSTOMER " +
                "(CUSTOMER_ID, F_NAME, M_INIT, L_NAME, PROOF, GENDER, EMAIL)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = conn.prepareStatement(sql1)) {
            pst.setString(1, customerId);
            pst.setString(2, fName);
            pst.setString(3, mInit);
            pst.setString(4, lName);
            pst.setString(5, proof);
            pst.setString(6, gender);
            pst.setString(7, email);
            pst.executeUpdate();
        }

        // CREATE RESERVATION
        String resId = "RES" + UUID.randomUUID().toString().substring(0, 5);

        String sql2 = "INSERT INTO RESERVATION " +
                "(RESER_ID, GUEST_COUNT, CHK_IN, CHK_OUT, ROOM_PREF_TYPE, CUSTOMER_ID, PROOF, EMAIL) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String roomType = roomPrefComboBox.getSelectedItem().toString();

        try (PreparedStatement pst = conn.prepareStatement(sql2)) {
            pst.setString(1, resId);
            pst.setInt(2, Integer.parseInt(guestCount));
            pst.setDate(3, java.sql.Date.valueOf(chkIn));
            pst.setDate(4, java.sql.Date.valueOf(chkOut));
            pst.setString(5, roomType);
            pst.setString(6, customerId);
            pst.setString(7, proof);
            pst.setString(8, email);
            pst.executeUpdate();
        }

        // ASSIGN ROOM
        String assignedRoom = null;

        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT ROOM_NO FROM ROOMS WHERE ROOM_TYPE=? AND STATUS='Available' LIMIT 1 FOR UPDATE")) {
            pst.setString(1, roomType);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                assignedRoom = rs.getString("ROOM_NO");
            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                        "No available room found for selected type.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // INSERT INTO BOOKED_VIA
        try (PreparedStatement pst = conn.prepareStatement(
                "INSERT INTO BOOKED_VIA (ROOM_NO, RESER_ID, MODE) VALUES (?, ?, ?)")) {
            pst.setString(1, assignedRoom);
            pst.setString(2, resId);
            pst.setString(3, "Online");
            pst.executeUpdate();
        }

        // CONFIRMATION
        String confId = "CONF" + UUID.randomUUID().toString().substring(0,5);

        try (PreparedStatement pst = conn.prepareStatement(
                "INSERT INTO CONFIRMATION (CONF_ID, CONF_MODE, CONF_DATE, STATUS, RESER_ID) VALUES (?, ?, ?, ?, ?)")) {
            pst.setString(1, confId);
            pst.setString(2, "Online");
            pst.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pst.setString(4, "Pending");
            pst.setString(5, resId);
            pst.executeUpdate();
        }

        conn.commit();

        JOptionPane.showMessageDialog(this,
                "Reservation complete! Your Reservation ID: " + resId);

        // Reset fields
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

        populateRoomTypes();

    } catch (Exception ex) {
        try { conn.rollback(); } catch (SQLException ignore) {}
        JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    } finally {
        try { conn.setAutoCommit(true); } catch (SQLException ignore) {}
    }
}

    // ------------------------------------------------------------
    // VIEW PANEL
    // ------------------------------------------------------------

    private void createViewPanel() {

        viewPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override public boolean isOpaque() { return false; }
        };
        viewPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8)) {
            @Override public boolean isOpaque() { return false; }
        };

        String[] tables;
        if ("ADMIN".equals(userRole)) {
            tables = new String[]{
                    "CUSTOMER", "RESERVATION", "CONFIRMATION",
                    "ROOMS", "HOTEL", "PAYMENT", "BOOKED_VIA"
            };
        } else {
            tables = new String[]{"ROOMS", "PAYMENT"};
        }

        viewTableComboBox = new JComboBox<>(tables);
        viewTableComboBox.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JButton refresh = new JButton("Refresh");
        JButton update = new JButton("Update Row");
        JButton delete = new JButton("Delete Row");

        refresh.setPreferredSize(new Dimension(140, 40));
        update.setPreferredSize(new Dimension(140, 40));
        delete.setPreferredSize(new Dimension(140, 40));

        refresh.setBackground(new Color(30, 150, 70));
        update.setBackground(new Color(30, 150, 70));
        delete.setBackground(new Color(30, 150, 70));

        refresh.setForeground(Color.WHITE);
        update.setForeground(Color.WHITE);
        delete.setForeground(Color.WHITE);

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);

        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        dataTable.setRowHeight(28);

        JTableHeader header = dataTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 17));
        header.setBackground(new Color(52, 152, 219));
        header.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(dataTable);

        refresh.addActionListener(e -> {
            String table = viewTableComboBox.getSelectedItem().toString();

            if ("USER".equals(userRole) && "PAYMENT".equals(table)) {

                String custId = JOptionPane.showInputDialog(
                        this,
                        "Enter your Customer ID to view payments:"
                );

                if (custId != null && !custId.trim().isEmpty()) {
                    loadUserPayments(custId.trim());
                }

            } else {
                loadTableData(table);
            }
        });

        viewTableComboBox.addActionListener(e -> {
            String sel = viewTableComboBox.getSelectedItem().toString();

            if ("PAYMENT".equals(sel) && "USER".equals(userRole)) {
                tableModel.setRowCount(0);
                tableModel.setColumnIdentifiers(new String[]{"Message"});
                tableModel.addRow(new Object[]{
                        "Click Refresh and enter your Customer ID"
                });
            } else {
                loadTableData(sel);
            }
        });

        if ("ADMIN".equals(userRole)) {
            update.addActionListener(e ->
                    updateSelectedRow(viewTableComboBox.getSelectedItem().toString()));

            delete.addActionListener(e ->
                    deleteSelectedRow(viewTableComboBox.getSelectedItem().toString()));

            control.add(update);
            control.add(delete);
        }

        control.add(new JLabel("Select Table:"));
        control.add(viewTableComboBox);
        control.add(refresh);

        JPanel content = new JPanel(new BorderLayout()) {
            @Override public boolean isOpaque() { return false; }
        };

        content.add(scrollPane, BorderLayout.CENTER);

        viewPanel.add(control, BorderLayout.NORTH);
        viewPanel.add(content, BorderLayout.CENTER);

        loadTableData("CUSTOMER");
    }

    private void loadUserPayments(String customerId) {

        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT p.* FROM PAYMENT p " +
                        "JOIN RESERVATION r ON p.RESER_ID = r.RESER_ID " +
                        "WHERE r.CUSTOMER_ID = ?")) {

            pst.setString(1, customerId);

            ResultSet rs = pst.executeQuery();
            ResultSetMetaData md = rs.getMetaData();

            int cols = md.getColumnCount();
            String[] names = new String[cols];

            for (int i = 1; i <= cols; i++) {
                names[i - 1] = md.getColumnName(i);
            }

            tableModel.setColumnIdentifiers(names);
            tableModel.setRowCount(0);

            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 1; i <= cols; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading payments: " + e.getMessage());
        }
    }

    private void loadTableData(String tableName) {

        try (Statement stmt = conn.createStatement()) {

            String sql;

            if ("USER".equals(userRole) && "ROOMS".equalsIgnoreCase(tableName)) {
                sql = "SELECT * FROM ROOMS WHERE STATUS='Available'";
            } else {
                sql = "SELECT * FROM " + tableName;
            }

            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();

            int cols = md.getColumnCount();
            String[] names = new String[cols];

            for (int i = 1; i <= cols; i++) {
                names[i - 1] = md.getColumnName(i);
            }

            tableModel.setColumnIdentifiers(names);
            tableModel.setRowCount(0);

            while (rs.next()) {

                Object[] row = new Object[cols];

                for (int i = 1; i <= cols; i++) {
                    row[i - 1] = rs.getObject(i);
                }

                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load table: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // PAYMENT PANEL
    // ------------------------------------------------------------

    private void createPaymentPanel() {

        paymentPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override public boolean isOpaque() { return false; }
        };
        paymentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel form = new JPanel(new GridLayout(0, 2, 14, 14)) {
            @Override public boolean isOpaque() { return false; }
        };

        JLabel lbl;

        lbl = new JLabel("Reservation ID:");
        paymentResIdField = new JTextField();
        form.add(lbl); form.add(paymentResIdField);

        lbl = new JLabel("Amount:");
        paymentAmountField = new JTextField();
        form.add(lbl); form.add(paymentAmountField);

        lbl = new JLabel("Payment Method:");
        paymentMethodComboBox =
                new JComboBox<>(new String[]{"Credit Card", "Debit Card", "UPI", "Cash"});
        form.add(lbl); form.add(paymentMethodComboBox);

        lbl = new JLabel("Status:");
        paymentStatusComboBox =
                new JComboBox<>(new String[]{"Completed", "Pending", "Failed"});
        form.add(lbl); form.add(paymentStatusComboBox);

        submitPaymentButton = new JButton("Record Payment");
        submitPaymentButton.setBackground(new Color(40, 167, 69));
        submitPaymentButton.setForeground(Color.WHITE);
        submitPaymentButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        submitPaymentButton.addActionListener(e -> submitPayment());

        JPanel south = new JPanel() {
            @Override public boolean isOpaque() { return false; }
        };

        south.add(submitPaymentButton);

        paymentPanel.add(form, BorderLayout.CENTER);
        paymentPanel.add(south, BorderLayout.SOUTH);
    }

    private void submitPayment() {

        String resId = paymentResIdField.getText();
        String amount = paymentAmountField.getText();

        if (resId.isEmpty() || amount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Missing fields.");
            return;
        }

        try {

            double amt = Double.parseDouble(amount);

            String payId = "PAY" + UUID.randomUUID().toString().substring(0,5);
            String payRef = "REF" + UUID.randomUUID().toString().substring(0,5);
            String method = paymentMethodComboBox.getSelectedItem().toString();
            String status = paymentStatusComboBox.getSelectedItem().toString();

            try (PreparedStatement pst = conn.prepareStatement(
                    "INSERT INTO PAYMENT VALUES (?, ?, ?, ?, ?, ?)")) {

                pst.setString(1, payId);
                pst.setString(2, payRef);
                pst.setDouble(3, amt);
                pst.setString(4, method);
                pst.setString(5, status);
                pst.setString(6, resId);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Payment Recorded!");

                paymentResIdField.setText("");
                paymentAmountField.setText("");
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Payment Failed: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------
    // BILLING PANEL
    // ------------------------------------------------------------

    private void createBillingPanel() {

        billingPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override public boolean isOpaque() { return false; }
        };

        billingPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel top = new JPanel(new FlowLayout()) {
            @Override public boolean isOpaque() { return false; }
        };

        billingResIdField = new JTextField(15);
        generateBillButton = new JButton("Generate Bill");

        generateBillButton.addActionListener(e -> generateBill());

        top.add(new JLabel("Reservation ID:"));
        top.add(billingResIdField);
        top.add(generateBillButton);

        billDisplayArea = new JTextArea(16, 40);
        billDisplayArea.setEditable(false);
        billDisplayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));

        billingPanel.add(top, BorderLayout.NORTH);
        billingPanel.add(new JScrollPane(billDisplayArea), BorderLayout.CENTER);
    }

    private void generateBill() {

        String resId = billingResIdField.getText().trim();

        if (resId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Reservation ID.");
            return;
        }

        try (PreparedStatement pst = conn.prepareStatement(
                "SELECT c.F_NAME, c.L_NAME, r.CHK_IN, r.CHK_OUT, r.ROOM_PREF_TYPE " +
                        "FROM RESERVATION r JOIN CUSTOMER c " +
                        "ON r.CUSTOMER_ID = c.CUSTOMER_ID WHERE r.RESER_ID = ?")) {

            pst.setString(1, resId);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Reservation not found.");
                return;
            }

            String fname = rs.getString("F_NAME");
            String lname = rs.getString("L_NAME");

            LocalDate in = rs.getDate("CHK_IN").toLocalDate();
            LocalDate out = rs.getDate("CHK_OUT").toLocalDate();

            long nights = ChronoUnit.DAYS.between(in, out);
            String roomType = rs.getString("ROOM_PREF_TYPE");

            double price = 0;

            try (PreparedStatement p2 = conn.prepareStatement(
                    "SELECT PRICE FROM ROOMS WHERE ROOM_TYPE=? LIMIT 1")) {

                p2.setString(1, roomType);

                ResultSet rp = p2.executeQuery();
                if (rp.next()) {
                    price = rp.getDouble("PRICE");
                }
            }

            double total = nights * price;

            String bill =
                    "==============================\n" +
                    "         HOTEL BILL\n" +
                    "==============================\n" +
                    "Reservation ID: " + resId + "\n" +
                    "Customer Name:  " + fname + " " + lname + "\n" +
                    "------------------------------\n" +
                    "Check-in:  " + in + "\n" +
                    "Check-out: " + out + "\n" +
                    "Nights:    " + nights + "\n" +
                    "------------------------------\n" +
                    "Room Type: " + roomType + "\n" +
                    "Rate/Night: ₹" + price + "\n" +
                    "------------------------------\n" +
                    "TOTAL:     ₹" + total + "\n" +
                    "==============================";

            billDisplayArea.setText(bill);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error generating bill: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // MANAGE ROOMS (ADMIN ONLY)
    // ------------------------------------------------------------

    private void createManageRoomsContainer() {
        manageRoomsPanelContainer = new JPanel(new BorderLayout());
        manageRoomsPanelContainer.add(new ManageRoomsPanel(conn), BorderLayout.CENTER);
    }

    // ------------------------------------------------------------
    // UPDATE / DELETE ROWS (ADMIN)
    // ------------------------------------------------------------

    private void deleteSelectedRow(String table) {

        int row = dataTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row.");
            return;
        }

        String key = dataTable.getValueAt(row, 0).toString();
        String col = dataTable.getColumnName(0);

        try (PreparedStatement pst = conn.prepareStatement(
                "DELETE FROM " + table + " WHERE " + col + "=?")) {

            pst.setString(1, key);
            pst.executeUpdate();

            loadTableData(table);

            JOptionPane.showMessageDialog(this, "Row Deleted.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Delete Failed: " + e.getMessage());
        }
    }

    private void updateSelectedRow(String table) {

        int row = dataTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }

        int cols = dataTable.getColumnCount();
        JTextField[] fields = new JTextField[cols];

        JPanel panel = new JPanel(new GridLayout(cols, 2));

        for (int i = 0; i < cols; i++) {
            panel.add(new JLabel(dataTable.getColumnName(i)));
            fields[i] = new JTextField(dataTable.getValueAt(row, i).toString());
            panel.add(fields[i]);
        }

        int ans = JOptionPane.showConfirmDialog(
                this, panel, "Update Row", JOptionPane.OK_CANCEL_OPTION);

        if (ans != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            StringBuilder sb = new StringBuilder("UPDATE " + table + " SET ");

            for (int i = 1; i < cols; i++) {
                sb.append(dataTable.getColumnName(i)).append("=?, ");
            }

            sb.setLength(sb.length() - 2);

            sb.append(" WHERE ")
              .append(dataTable.getColumnName(0))
              .append("=?");

            try (PreparedStatement pst = conn.prepareStatement(sb.toString())) {

                for (int i = 1; i < cols; i++) {
                    pst.setString(i, fields[i].getText());
                }

                pst.setString(cols, fields[0].getText());

                pst.executeUpdate();
            }

            loadTableData(table);

            JOptionPane.showMessageDialog(this, "Row Updated.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Update Failed: " + e.getMessage());
        }
    }
}
