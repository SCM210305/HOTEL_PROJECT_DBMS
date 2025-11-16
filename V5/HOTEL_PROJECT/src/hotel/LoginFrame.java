package hotel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private Image bgImg;

    private final Map<String, String> USERS = new HashMap<>() {{
        put("root", "Mourya@1721");
        put("emp", "5678");
    }};

    private final Map<String, String> ROLES = new HashMap<>() {{
        put("root", "ADMIN");
        put("emp", "USER");
    }};

    public LoginFrame(Image img) {
        super("System Login");
        this.bgImg = img;

        setContentPane(new ImagePanel(bgImg));

        initializeUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeUI() {

        JPanel main = new JPanel(new GridBagLayout()) {
            @Override public boolean isOpaque() { return false; }
        };

        main.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Hotel System Login");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 26, 0);
        main.add(heading, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 12, 8);

        gbc.gridy++; gbc.gridx = 0;
        main.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        main.add(usernameField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        main.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        main.add(passwordField, gbc);

        gbc.gridy++; gbc.gridx = 1;
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        main.add(loginButton, gbc);

        add(main, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (USERS.containsKey(username) && USERS.get(username).equals(password)) {
            String role = ROLES.get(username);

            JOptionPane.showMessageDialog(this, "Login Successful! Role: " + role);

            dispose();

            SwingUtilities.invokeLater(() -> new HotelManagementProject(role, bgImg).setVisible(true));

        } else {
            JOptionPane.showMessageDialog(this, "Invalid login.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void showLoginScreen(Image img) {
        SwingUtilities.invokeLater(() -> new LoginFrame(img));
    }
}
