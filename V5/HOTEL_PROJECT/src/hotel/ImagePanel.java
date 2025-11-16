package hotel;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {

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
