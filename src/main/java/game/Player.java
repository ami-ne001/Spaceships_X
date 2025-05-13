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
    private int speed;
    private int maxHealth;
    private int width ;
    private int height;
    private int lives;

    // Player image
    private BufferedImage image;
    private Rectangle hitbox;

    // Shooting
    private int shootCooldown = 0;
    private final int shootCooldownMax = 15;
    private long lastShootTime = 0;
    private static final long SHOOT_DELAY = 200; // 200ms between shots

    public Player(GamePanel gp, KeyHandler keyHandler) {
        this.gp = gp;
        this.keyHandler = keyHandler;

        // Set initial position
        width = gp.getTileSize();
        height = gp.getTileSize();
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
    public void applyShipStats(Ship ship) {
        this.speed = ship.getSpeed();          // Set movement speed
        this.maxHealth = ship.getHealth();     // Set max health
        this.lives = maxHealth;              // For shooting
        setImage(ship.getImage());            // Change appearance
    }

    public void setImage(BufferedImage image) {
        if (image != null) {
            this.image = image; // Make sure hitbox matches new size
        }
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

        // Shooting with rate limiting
        long currentTime = System.currentTimeMillis();
        if (keyHandler.shootPressed && currentTime - lastShootTime >= SHOOT_DELAY) {
            gp.getProjectileManager().addPlayerProjectile(x + width / 2, y);
            gp.getSoundManager().playSound(SoundManager.SHOOT_SOUND);
            lastShootTime = currentTime;
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            gp.gameOver();
        }
    }

    public int getLives() {
        return lives;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getMaxHealth() { return maxHealth; }

    public void setX(int x){this.x = x;}
    public void setY(int y){this.y = y;}
    public void setlives(int lives){this.lives = lives;}
}