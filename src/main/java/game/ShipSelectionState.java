package game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ShipSelectionState {
    private GamePanel gp;
    private ShipManager shipManager;
    private List<Ship> ships;
    private int currentSelection = 0;

    private Font titleFont = new Font("Arial", Font.BOLD, 40);
    private Font statFont = new Font("Arial", Font.PLAIN, 20);

    public ShipSelectionState(GamePanel gp) {
        this.gp = gp;
        this.shipManager = new ShipManager();
        this.ships = shipManager.getAvailableShips();
    }

    public void update() {
        KeyHandler keyHandler = gp.getKeyHandler();

        if (keyHandler.leftPressed) {
            currentSelection--;
            if (currentSelection < 0) currentSelection = ships.size() - 1;
            keyHandler.leftPressed = false;
        }

        if (keyHandler.rightPressed) {
            currentSelection++;
            if (currentSelection >= ships.size()) currentSelection = 0;
            keyHandler.rightPressed = false;
        }

        if (keyHandler.enterPressed) {
            selectShip();
            keyHandler.enterPressed = false;
        }

        if (keyHandler.escapePressed) {
            gp.setGameState(GamePanel.STATE_ACCOUNT);
            keyHandler.escapePressed = false;
        }
    }

    private void selectShip() {
        Ship selectedShip = ships.get(currentSelection);
        gp.setSelectedShip(selectedShip);
        gp.setPlayerLives(selectedShip.getHealth());
        gp.setGameState(GamePanel.STATE_PLAYING);
    }

    public void draw(Graphics2D g2) {
        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.getScreenWidth(), gp.getScreenHeight());

        // Title
        g2.setFont(titleFont);
        g2.setColor(Color.BLUE);
        String title = "SELECT YOUR SHIP";
        int titleX = getXForCenteredText(title, g2);
        g2.drawString(title, titleX, 80);

        Ship currentShip = ships.get(currentSelection);

        // Ship name
        g2.drawString(currentShip.getName(), getXForCenteredText(currentShip.getName(), g2), 180);

        // Ship image
        BufferedImage img = currentShip.getImage();
        if (img != null) {
            int shipX = gp.getScreenWidth() / 2 - gp.getTileSize();
            g2.drawImage(img, shipX, 200, gp.getTileSize()*2, gp.getTileSize()*2 , null);
        }

        // Stats
        int statY = 400;
        drawStatBar(g2, "Speed", currentShip.getSpeed(), 10, 150, statY);
        drawStatBar(g2, "Health", currentShip.getHealth(), 10, 150, statY + 40);

        // Instructions
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Use LEFT/RIGHT to select, SPACE to confirm",
                getXForCenteredText("Use LEFT/RIGHT to select, ENTER to confirm", g2), 550);
        g2.drawString("Press ESC to go back",
                getXForCenteredText("Press ESC to go back", g2), 580);
    }

    private void drawStatBar(Graphics2D g2, String label, int value, int max, int x, int y) {
        g2.setFont(statFont);
        g2.setColor(Color.WHITE);
        g2.drawString(label + ":", x, y + 15);

        int barWidth = 200;
        int fillWidth = (int)((value / (double)max) * barWidth);

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x + 100, y, barWidth, 20);

        g2.setColor(getStatColor(label));
        g2.fillRect(x + 100, y, fillWidth, 20);

        g2.setColor(Color.WHITE);
        g2.drawRect(x + 100, y, barWidth, 20);
        g2.drawString(value + "/" + max, x + 100 + barWidth + 10, y + 15);
    }

    private Color getStatColor(String stat) {
        switch (stat) {
            case "Speed": return new Color(100, 200, 255);
            case "Health": return new Color(100, 255, 100);
            case "Attack": return new Color(255, 100, 100);
            default: return Color.WHITE;
        }
    }

    private int getXForCenteredText(String text, Graphics2D g2) {
        return gp.getScreenWidth() / 2 - g2.getFontMetrics().stringWidth(text) / 2;
    }
}