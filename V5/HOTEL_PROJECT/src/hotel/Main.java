package hotel;

import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // Load background image from resources
        Image bgImg = Toolkit.getDefaultToolkit().getImage(
            Main.class.getResource("/hotel/images/bg.jpg")
        );

        if (bgImg == null) {
            JOptionPane.showMessageDialog(null,
                "Background image not found! Make sure bg.jpg is inside src/hotel/images/",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Launch login screen
        LoginFrame.showLoginScreen(bgImg);
    }
}
