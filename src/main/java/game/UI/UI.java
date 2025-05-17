package game.UI;

import game.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class UI {
    private GamePanel gp;
    private Font arial_20;
    private BufferedImage heartImage;

    public UI(GamePanel gp) {
        this.gp = gp;
        arial_20 = new Font("Arial", Font.PLAIN, 20);

        try {
            heartImage = ImageIO.read(getClass().getResourceAsStream("/player/heart.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        g2.setFont(arial_20);
        g2.setColor(Color.WHITE);

        // Draw score
        g2.drawString("Score: " + gp.getScore(), 20, 30);

        // Draw level
        g2.drawString("Level: " + gp.getLevel(), 20, 60);

        // Draw player lives
        for (int i = 0; i < gp.getPlayerLives(); i++) {
            g2.drawImage(heartImage, 20 + i * 30, gp.getScreenHeight() - 40, 24, 24, null);
        }

        // Draw game over screen
        if (gp.isGameOver()) {
            drawGameOverScreen(g2);
        }
    }

    public void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.getScreenWidth(), gp.getScreenHeight());

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));

        String text = "GAME OVER";
        int x = getXForCenteredText(text, g2);
        int y = gp.getScreenHeight() / 2 - 50;
        g2.drawString(text, x, y);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Final Score: " + gp.getScore();
        x = getXForCenteredText(text, g2);
        y += 50;
        g2.drawString(text, x, y);

        text = "Press R to restart";
        x = getXForCenteredText(text, g2);
        y += 30;
        g2.drawString(text, x, y);
    }

    private int getXForCenteredText(String text, Graphics2D g2) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.getScreenWidth() / 2 - length / 2;
    }
}