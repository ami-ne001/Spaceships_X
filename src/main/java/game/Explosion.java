package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Explosion {
    private GamePanel gp;
    private int x, y;
    private int width = 64;
    private int height = 64;
    private int frameCounter = 0;
    private int animationSpeed = 5;
    private BufferedImage[] frames = new BufferedImage[3];
    private boolean finished = false;

    public Explosion(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x = x - width / 2;
        this.y = y - height / 2;

        try {
            frames[0] = ImageIO.read(getClass().getResourceAsStream("/explosion/explosion1.png"));
            frames[1] = ImageIO.read(getClass().getResourceAsStream("/explosion/explosion2.png"));
            frames[2] = ImageIO.read(getClass().getResourceAsStream("/explosion/explosion3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        frameCounter++;
        if (frameCounter >= frames.length * animationSpeed) {
            finished = true;
        }
    }

    public void draw(Graphics2D g2) {
        if (!finished) {
            int currentFrame = frameCounter / animationSpeed;
            if (currentFrame < frames.length) {
                g2.drawImage(frames[currentFrame], x, y, width, height, null);
            }
        }
    }

    public boolean isFinished() { return finished; }
}