package game.UI;

import game.GamePanel;

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
    private boolean hostingGame = false;
    private String multiplayerStatus = "";

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

        // Handle escape from multiplayer menu
        if (gp.getKeyHandler().escapePressed && hostingGame) {
            hostingGame = false;
            multiplayerStatus = "";
            gp.getKeyHandler().escapePressed = false;
        }

        // Handle multiplayer input
        if (hostingGame) {
            char key = gp.getKeyHandler().lastKeyChar;
            if (key == 'h' || key == 'H') {
                startMultiplayerGame(true);
            } else if (key == 'j' || key == 'J') {
                startMultiplayerGame(false);
            }
        }
    }

    private void selectOption() {
        switch(currentChoice) {
            case 0: // NEW GAME
                gp.setGameState(GamePanel.STATE_ACCOUNT);
                break;
            case 1: // MULTIPLAYER
                if (!hostingGame) {
                    hostingGame = true;
                    multiplayerStatus = "Press H to host or J to join";
                }
                break;
            case 2: // QUIT
                System.exit(0);
                break;
        }
    }

    private void startMultiplayerGame(boolean isHost) {
        if (isHost) {
            // Initialize multiplayer as host
            gp.startMultiplayerGame(true, null);
            multiplayerStatus = "Hosting game... Waiting for players";
            gp.setGameState(GamePanel.STATE_ACCOUNT);
        } else {
            // Switch to IP input state
            gp.setGameState(GamePanel.STATE_IP_INPUT);
            multiplayerStatus = "";
            hostingGame = false;
        }
    }

    public void draw(Graphics2D g2) {
        // Draw background
        g2.drawImage(backgroundImage, 0, 0, gp.getScreenWidth(), gp.getScreenHeight(), null);

        // Draw title
        g2.setFont(titleFont);
        g2.setColor(Color.YELLOW);
        String text = "SPACESHIPS_X";
        int x = getXForCenteredText(text, g2);
        g2.drawString(text, x, 175);

        // Draw menu options
        g2.setFont(menuFont);
        for (int i = 0; i < options.length; i++) {
            if (i == currentChoice) {
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(Color.GRAY);
            }
            text = options[i];
            x = getXForCenteredText(text, g2);
            g2.drawString(text, x, 300 + i * 50);
        }

        // Draw multiplayer status if active
        if (hostingGame) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            x = getXForCenteredText(multiplayerStatus, g2);
            g2.drawString(multiplayerStatus, x, 475);
        }
    }

    private int getXForCenteredText(String text, Graphics2D g2) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.getScreenWidth() / 2 - length / 2;
    }
}