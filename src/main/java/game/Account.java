package game;

import game.DAO.DatabaseManager;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Account{
    private GamePanel gp;
    private DatabaseManager db;
    private Font titleFont = new Font("Arial", Font.BOLD, 40);
    private Font inputFont = new Font("Arial", Font.PLAIN, 30);

    private String username = "";
    private int score = 0;
    private String statusMessage = "";
    private Color statusColor = Color.WHITE;

    public Account(GamePanel gp, DatabaseManager db) {
        this.gp = gp;
        this.db = db;
    }

    public void update() {
        KeyHandler keyHandler = gp.getKeyHandler();

        if (keyHandler.escapePressed) {
            gp.setGameState(0);
            keyHandler.escapePressed = false;
            resetInput();
            return;
        }

        if (keyHandler.keyPressed){
            char keyChar = keyHandler.lastKeyChar;
            if (keyChar >= 32 && keyChar <= 126 && username.length() < 12) {
                username += keyChar;
            } else if (keyHandler.lastKeyCode == KeyEvent.VK_BACK_SPACE && username.length() > 0) {
                username = username.substring(0, username.length() - 1);
            } else if (keyHandler.lastKeyCode == KeyEvent.VK_ENTER && !username.isEmpty()) {
                keyHandler.enterPressed = false;
                handlePlayer();
                resetInput();
            }
            keyHandler.keyPressed = false;
        }
    }

    private void handlePlayer() {
        if (!db.playerExists(username)) {
            db.createPlayer(username, score);
            statusMessage = "New player created!";
        } else {
            statusMessage = "Welcome back!";
            score = db.getHighscore(username);
        }
        gp.setCurrentUser(username);
        statusColor = Color.GREEN;

        gp.setCurrentUser(username);
        gp.setGameState(GamePanel.STATE_SHIP_SELECTION);
    }

    private void resetInput() {
        username = "";
        statusMessage = "";
    }

    public void draw(Graphics2D g2) {
        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.getScreenWidth(), gp.getScreenHeight());

        // Title
        g2.setFont(titleFont);
        g2.setColor(Color.BLUE);
        String title = "ENTER USERNAME";
        int titleX = getXForCenteredText(title, g2);
        g2.drawString(title, titleX, 100);

        // Input field
        g2.setFont(inputFont);
        g2.setColor(Color.WHITE);
        g2.drawString("Username:", 150, 200);
        g2.drawRect(350, 170, 300, 40);
        g2.drawString(username, 360, 200);

        // Status message
        g2.setColor(statusColor);
        g2.drawString(statusMessage, getXForCenteredText(statusMessage, g2), 300);

        // Instructions
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Press ENTER to confirm",
                getXForCenteredText("Press ENTER to confirm", g2), 400);
        g2.drawString("ESC to return to menu",
                getXForCenteredText("ESC to return to menu", g2), 450);
    }

    private int getXForCenteredText(String text, Graphics2D g2) {
        return gp.getScreenWidth() / 2 - g2.getFontMetrics().stringWidth(text) / 2;
    }

    public String getUsername(){return this.username;}
    public int getScore(){return this.score;}

    public void setscore(int score){this.score = score;}
}