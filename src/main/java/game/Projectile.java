package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Projectile {
    private GamePanel gp;
    protected int x, y;
    protected int speed;
    private int width = 12;
    private int height = 24;
    private boolean isPlayerProjectile;
    private BufferedImage image;
    protected Rectangle hitbox;
    private boolean active = true;
    private String shooterId;

    public Projectile(GamePanel gp, int x, int y, boolean isPlayerProjectile) {
        this.gp = gp;
        this.x = x - width / 2;
        this.y = y;
        this.isPlayerProjectile = isPlayerProjectile;
        
        // Handle shooter ID for both single-player and multiplayer modes
        if (isPlayerProjectile) {
            if (gp.isMultiplayer() && gp.getGameClient() != null && gp.getGameClient().isConnected()) {
                this.shooterId = gp.getGameClient().getClientId();
            } else {
                this.shooterId = "single_player";
            }
        } else {
            this.shooterId = "enemy";
        }

        try {
            if (isPlayerProjectile) {
                image = ImageIO.read(getClass().getResourceAsStream("/projectile/playerprojectile.png"));
                speed = -8;
            } else {
                image = ImageIO.read(getClass().getResourceAsStream("/projectile/enemyprojectile.png"));
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

        // Check if off screen
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
    public int getX() { return x; }
    public int getY() { return y; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public String getShooterId() { return shooterId; }
    public void setShooterId(String shooterId) { this.shooterId = shooterId; }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.hitbox.x = x;
        this.hitbox.y = y;
    }

}