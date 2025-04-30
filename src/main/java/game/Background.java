package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Background {
    private GamePanel gp;
    private BufferedImage image;
    private int y1;
    private int y2;
    private int speed = 1;

    public Background(GamePanel gp) {
        this.gp = gp;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/background/space111.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Initialisez y1 et y2 après avoir défini gp
        y1 = 0;
        y2 = -gp.getScreenHeight();
    }

    public void update() {
        y1 += speed;
        y2 += speed;

        if (y1 >= gp.getScreenHeight()) {
            y1 = y2 - gp.getScreenHeight();
        }
        if (y2 >= gp.getScreenHeight()) {
            y2 = y1 - gp.getScreenHeight();
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, 0, y1, gp.getScreenWidth(), gp.getScreenHeight(), null);
    }
}