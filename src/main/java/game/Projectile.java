package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Projectile {
    private GamePanel gp;
    private int x, y;
    private int speed;
    private int width = 12;
    private int height = 24;
    private boolean isPlayerProjectile;
    private BufferedImage image;
    private Rectangle hitbox;
    private boolean active = true;

    public Projectile(GamePanel gp, int x, int y, boolean isPlayerProjectile) {
        this.gp = gp;
        this.x = x - width / 2;
        this.y = y;
        this.isPlayerProjectile = isPlayerProjectile;

        try {
            if (isPlayerProjectile) {
                image = ImageIO.read(getClass().getResourceAsStream("/projectile/playerprojectile.png"));
                speed = -8;
            } else {
                image = ImageIO.read(getClass().getResourceAsStream("/projectile/fireball_down.png"));
                speed = 5;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        hitbox = new Rectangle(x, y, width, height);
    }

    public void update() {
        y += speed;
        hitbox.y = y;

        // Check if off-screen
        if (y < -height || y > gp.getScreenHeight()) {
            active = false;
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getHitbox() { return hitbox; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isPlayerProjectile() { return isPlayerProjectile; }
}