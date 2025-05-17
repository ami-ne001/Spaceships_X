package game.UI;

import game.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class IPInputState {
    private GamePanel gp;
    private Font titleFont = new Font("Arial", Font.BOLD, 40);
    private Font inputFont = new Font("Arial", Font.PLAIN, 30);
    private Font instructionsFont = new Font("Arial", Font.PLAIN, 25);
    private BufferedImage backgroundImage;
    private String ipAddress = "";
    private String statusMessage = "";
    private Color statusColor = Color.WHITE;

    public IPInputState(GamePanel gp) {
        this.gp = gp;
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/background/space.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        KeyHandler keyHandler = gp.getKeyHandler();

        if (keyHandler.escapePressed) {
            gp.setGameState(GamePanel.STATE_MENU);
            keyHandler.escapePressed = false;
            resetInput();
            return;
        }

        if (keyHandler.keyPressed) {
            char keyChar = keyHandler.lastKeyChar;
            int keyCode = keyHandler.lastKeyCode;

            // Allow only numbers, dots, and backspace
            if ((keyChar >= '0' && keyChar <= '9') || keyChar == '.' && ipAddress.length() < 15) {
                ipAddress += keyChar;
            } else if (keyCode == KeyEvent.VK_BACK_SPACE && ipAddress.length() > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.length() - 1);
            } else if (keyCode == KeyEvent.VK_ENTER && !ipAddress.isEmpty()) {
                if (validateIPAddress(ipAddress)) {
                    // Initialize multiplayer as client with the entered IP
                    gp.startMultiplayerGame(false, ipAddress);
                    
                    if (gp.getGameClient().isConnected()) {
                        resetInput();
                        gp.setGameState(GamePanel.STATE_ACCOUNT);
                    } else {
                        statusMessage = "Failed to connect to server!";
                        statusColor = Color.RED;
                    }
                } else {
                    statusMessage = "Invalid IP address format!";
                    statusColor = Color.RED;
                }
            }
            keyHandler.keyPressed = false;
        }
    }

    private boolean validateIPAddress(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        
        try {
            for (String part : parts) {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void resetInput() {
        ipAddress = "";
        statusMessage = "";
    }

    public void draw(Graphics2D g2) {
        // Background
        g2.drawImage(backgroundImage, 0, 0, gp.getScreenWidth(), gp.getScreenHeight(), null);

        // Title
        g2.setFont(titleFont);
        g2.setColor(Color.YELLOW);
        String title = "ENTER SERVER IP";
        int titleX = getXForCenteredText(title, g2);
        g2.drawString(title, titleX, 175);

        // Input field
        g2.setFont(inputFont);
        g2.setColor(Color.WHITE);
        g2.drawString("IP Address:", 150, 300);
        g2.drawRect(350, 270, 300, 40);
        g2.drawString(ipAddress, 360, 300);

        // Status message
        g2.setColor(statusColor);
        g2.drawString(statusMessage, getXForCenteredText(statusMessage, g2), 300);

        // Instructions
        g2.setFont(instructionsFont);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Press ENTER to connect",
                getXForCenteredText("Press ENTER to connect", g2), 450);
        g2.drawString("ESC to return to menu",
                getXForCenteredText("ESC to return to menu", g2), 475);
    }

    private int getXForCenteredText(String text, Graphics2D g2) {
        return gp.getScreenWidth() / 2 - g2.getFontMetrics().stringWidth(text) / 2;
    }
} 