package game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MenuState {
    private GamePanel gp;
    private Font titleFont;
    private Font menuFont;
    private int currentChoice = 0;
    private String[] options = {"NEW GAME", "MULTIPLAYER", "QUIT"};
    private BufferedImage backgroundImage;


    public MenuState(GamePanel gp) {
        this.gp = gp;
        try {
            titleFont = new Font("Arial", Font.BOLD, 40);
            menuFont = new Font("Arial", Font.PLAIN, 30);
             backgroundImage = ImageIO.read(getClass().getResourceAsStream("/background/space.png"));
            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        // Handle menu navigation
        if (gp.getKeyHandler().upPressed) {
            currentChoice--;
            if (currentChoice < 0) currentChoice = options.length - 1;
            gp.getKeyHandler().upPressed = false;
        }
        if (gp.getKeyHandler().downPressed) {
            currentChoice++;
            if (currentChoice >= options.length) currentChoice = 0;
            gp.getKeyHandler().downPressed = false;
        }

        // Handle selection
        if (gp.getKeyHandler().enterPressed) {
            selectOption();
            gp.getKeyHandler().enterPressed = false;
        }
    }

    private void selectOption() {
        switch(currentChoice) {
            case 0: // NEW GAME
                gp.setGameState(GamePanel.STATE_ACCOUNT);
                break;
            case 1: //MULTIPLAYER
                break;
            case 2: // QUIT
                System.exit(0);
                break;
        }
    }

    public void draw(Graphics2D g2) {
        // Draw background
        g2.drawImage(backgroundImage, 0, 0, gp.getScreenWidth(), gp.getScreenHeight(), null);

        // Draw title
        g2.setFont(titleFont);
        g2.setColor(Color.YELLOW);
        String title = "Breath of Relief";
        int x = getXForCenteredText(title, g2);
        g2.drawString(title, x, gp.getScreenHeight() / 4);

        // Draw menu options
        g2.setFont(menuFont);
        for (int i = 0; i < options.length; i++) {
            if (i == currentChoice) {
                g2.setColor(Color.WHITE);
                g2.drawString("> " + options[i], getXForCenteredText("> " + options[i], g2),
                        gp.getScreenHeight() / 2 + i * 40);
            } else {
                g2.setColor(Color.GRAY);
                g2.drawString(options[i], getXForCenteredText(options[i], g2),
                        gp.getScreenHeight() / 2 + i * 40);
            }
        }
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        g2.setColor(Color.WHITE);
        g2.drawString("Use Up/Down arrows to navigate, Press Enter to select", 170,
                gp.getScreenHeight() - 20);
    }

    private int getXForCenteredText(String text, Graphics2D g2) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.getScreenWidth() / 2 - length / 2;
    }
}