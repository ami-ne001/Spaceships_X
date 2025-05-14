package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class OtherPlayer {
    private GamePanel gp;
    private String playerId;
    private String username;
    private int x, y;
    private int width = 48;
    private int height = 48;
    private BufferedImage image;
    private Rectangle hitbox;
    private String shipImagePath;

    public OtherPlayer(GamePanel gp, String playerId) {
        this.gp = gp;
        this.playerId = playerId;
        this.username = playerId;
        this.x = gp.getScreenWidth() / 2 - width / 2;
        this.y = gp.getScreenHeight() - height - 20;
        this.shipImagePath = "/player/playership1.png"; // Default ship

        loadImage();
        hitbox = new Rectangle(x, y, width, height);
    }

    private void loadImage() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream(shipImagePath));
        } catch (IOException e) {
            System.out.println("Error loading image: " + shipImagePath);
            e.printStackTrace();
            // Try to load default ship if the specified image fails
            try {
                image = ImageIO.read(getClass().getResourceAsStream("/player/playership1.png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void update() {
        // Update hitbox position
        hitbox.x = x;
        hitbox.y = y;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, width, height, null);
        
        // Draw username above the ship
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString(username, x + 8, y + height + 10);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setShipImagePath(String path) {
        if (path != null && !path.equals(this.shipImagePath)) {
            this.shipImagePath = path;
            loadImage();
        }
    }

    public void setUsername(String username) {
        if (username != null) {
            this.username = username;
        }
    }

    public Rectangle getHitbox() { return hitbox; }
    public String getPlayerId() { return playerId; }
    public String getUsername() { return username; }
    public int getX() { return x; }
    public int getY() { return y; }
} 