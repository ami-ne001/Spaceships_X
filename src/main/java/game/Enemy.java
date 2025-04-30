package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class Enemy {
    private GamePanel gp;

    // Enemy properties
    private int x, y;
    private int speed;
    private int width = 48;
    private int height = 48;
    private int health;
    private int points;
    private int type;

    // Enemy image
    private BufferedImage image;
    private Rectangle hitbox;

    // Movement pattern
    private int movementPattern;
    private int movementCounter = 0;
    private Random random = new Random();

    // Shooting
    private int shootCooldown = 0;
    private int shootCooldownMax = 40;

    public Enemy(GamePanel gp, int x, int y, int type) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.type = type;

        // Set enemy type (1 = easy, 2 = medium, 3 = hard)
        switch (type) {
            case 1:
                speed = 1;
                health = 1;
                points = 10;
                movementPattern = 0;
                try {
                    image = ImageIO.read(getClass().getResourceAsStream("/enemy/enemyship1.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                speed = 2;
                health = 2;
                points = 20;
                movementPattern = 1;
                try {
                    image = ImageIO.read(getClass().getResourceAsStream("/enemy/enemyship2.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                speed = 2;
                health = 3;
                points = 30;
                movementPattern = 2;
                try {
                    image = ImageIO.read(getClass().getResourceAsStream("/enemy/enemyship3.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        hitbox = new Rectangle(x, y, width, height);
    }

    public void update() {
        switch (movementPattern) {
            case 0: // Straight down
                y += speed;
                break;
            case 1: // Zigzag
                y += speed;
                x += (int)(Math.sin(movementCounter * 0.1) * 2);
                break;
            case 2: // Zigzag
                y += speed;
                x += (int)(Math.cos(movementCounter * 0.1) * 3);
                break;
        }
        movementCounter++;

        // Update hitbox
        hitbox.x = x;
        hitbox.y = y;

        // Shooting
        if (shootCooldown > 0) {
            shootCooldown--;
        } else if (type == 1 && random.nextInt(200) < 1) {   // 0.5% de chance de tirer
            gp.getProjectileManager().addEnemyProjectile(x + width / 2, y + height);
            shootCooldown = shootCooldownMax;
        } else if (type == 2 && random.nextInt(100) < 1) {   // 1% de chance de tirer
            gp.getProjectileManager().addEnemyProjectile(x + width / 2, y + height);
            shootCooldown = shootCooldownMax;
        } else if (type == 3 && random.nextInt(50) < 1) {   // 2% de chance de tirer
            gp.getProjectileManager().addEnemyProjectile(x + width / 2, y + height);
            shootCooldown = shootCooldownMax;
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, width, height, null);
    }

    public void takeDamage() {
        health--;
        if (health <= 0) {
            gp.getSoundManager().playSound(SoundManager.EXPLOSION_SOUND);
            gp.addScore(points);
            gp.getEnemyManager().removeEnemy(this);

            // Create explosion animation
            gp.getProjectileManager().addExplosion(x + width / 2, y + height / 2);
            return;
        }
        gp.getSoundManager().playSound(SoundManager.HIT_SOUND);
    }


    public boolean isOffScreen() {
        return y > gp.getScreenHeight();
    }
    public Rectangle getHitbox() { return hitbox; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}