package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class OtherPlayer {
    private GamePanel gp;
    private String playerId;
    private int x, y;
    private int width = 48;
    private int height = 48;
    private BufferedImage image;
    private Rectangle hitbox;

    public OtherPlayer(GamePanel gp, String playerId) {
        this.gp = gp;
        this.playerId = playerId;
        this.x = gp.getScreenWidth() / 2 - width / 2;
        this.y = gp.getScreenHeight() - height - 20;

        try {
            image = ImageIO.read(getClass().getResourceAsStream("/player/playership1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        hitbox = new Rectangle(x, y, width, height);
    }

    public void update() {
        // Update hitbox position
        hitbox.x = x;
        hitbox.y = y;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, width, height, null);
        
        // Draw player ID above the ship
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString(playerId, x, y - 5);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
} 