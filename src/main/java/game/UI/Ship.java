package game.UI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Ship{
    private final String name;
    private final int speed;
    private final int health;
    private final BufferedImage image;
    private final String imagePath;

    public Ship(String name, int speed, int health, String imagePath) {
        this.name = name;
        this.speed = speed;
        this.health = health;
        this.imagePath = imagePath;
        this.image = loadImage();
    }

    private BufferedImage loadImage() {
        try {
            return ImageIO.read(getClass().getResourceAsStream(imagePath));
        } catch (IOException e) {
            System.err.println("Error loading ship image: " + imagePath);
            e.printStackTrace();
            return null;
        }
    }

    // Getters
    public String getName() { return name; }
    public int getSpeed() { return speed; }
    public int getHealth() { return health; }
    public BufferedImage getImage() { return image; }
    public String getImagePath() { return imagePath; }

    @Override
    public String toString() {
        return String.format("%s (Speed: %d, Health: %d)",
                name, speed, health);
    }
}