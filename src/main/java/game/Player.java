package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player {
    private GamePanel gp;
    private KeyHandler keyHandler;

    // Player position and stats
    private int x, y;
    private int speed = 4;
    private int width = 48;
    private int height = 48;
    private int lives = 3;

    // Player image
    private BufferedImage image;
    private Rectangle hitbox;

    // Shooting
    private int shootCooldown = 0;
    private final int shootCooldownMax = 15;

    public Player(GamePanel gp, KeyHandler keyHandler) {
        this.gp = gp;
        this.keyHandler = keyHandler;

        // Set initial position
        x = gp.getScreenWidth() / 2 - width / 2;
        y = gp.getScreenHeight() - height - 20;

        // Load player image
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/player/playership1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize hitbox
        hitbox = new Rectangle(x, y, width, height);
    }

    public void update() {
        // Movement
        if (keyHandler.upPressed && y > 0) y -= speed;
        if (keyHandler.downPressed && y < gp.getScreenHeight() - height) y += speed;
        if (keyHandler.leftPressed && x > 0) x -= speed;
        if (keyHandler.rightPressed && x < gp.getScreenWidth() - width) x += speed;

        // Update hitbox position
        hitbox.x = x;
        hitbox.y = y;

        // Shooting
        if (shootCooldown > 0) {
            shootCooldown--;
        }

        if (keyHandler.shootPressed && shootCooldown == 0) {
            gp.getProjectileManager().addPlayerProjectile(x + width / 2, y);
            gp.getSoundManager().playSound(SoundManager.SHOOT_SOUND);
            shootCooldown = shootCooldownMax;
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, width, height, null);
    }


    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            gp.getSoundManager().playSound(SoundManager.EXPLOSION_SOUND);
            gp.getProjectileManager().addExplosion(x + width / 2, y + height / 2);
            gp.gameOver();
            return;
        }
        gp.getSoundManager().playSound(SoundManager.HIT_SOUND);
    }

    public Rectangle getHitbox() { return hitbox; }
    public int getLives() { return lives; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}