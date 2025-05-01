package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Background {
    private GamePanel gp;
    private BufferedImage image;


    public Background(GamePanel gp) {
        this.gp = gp;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/background/space.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void draw(Graphics2D g2) {
        g2.drawImage(image, 0, 0, gp.getScreenWidth(), gp.getScreenHeight(), null);
    }
}