package game.entities;

import game.GamePanel;
import game.managers.SoundManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Enemy {
    private GamePanel gp;

    // Enemy properties
    private int x, y;
    private int speed;
    private int width = 48;
    private int height = 48;
    private int health;
    private int type;
    private int points;

    // Enemy image
    private BufferedImage image;
    private Rectangle hitbox;

    // Movement pattern
    private int movementPattern;
    private int movementCounter = 0;

    // Shooting
    private int shootTimer = 0;
    private int shootInterval = 120;

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
        // Basic movement
        y += speed;

        // Movement patterns
        switch (movementPattern) {
            case 0: // Straight down
                break;
            case 1: // Zigzag
                x += (int)(Math.sin(movementCounter * 0.1) * 2);
                break;
            case 2: // Zigzag
                x += (int)(Math.cos(movementCounter * 0.1) * 3);
                break;
        }
        movementCounter++;

        // Update hitbox
        hitbox.x = x;
        hitbox.y = y;

        // Shooting
        shootTimer++;
        if (shootTimer >= shootInterval) {
            gp.getProjectileManager().addEnemyProjectile(x + width / 2, y + height);
            shootTimer = 0;
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
            gp.getProjectileManager().addExplosion(x + width / 2, y + height / 2);
            return;
        }
        gp.getSoundManager().playSound(SoundManager.HIT_SOUND);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
    public boolean isOffScreen() {
        return y > gp.getScreenHeight();
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getType() { return type; }
    public int getHealth() { return health; }
}