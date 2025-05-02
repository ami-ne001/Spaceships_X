package game;

import game.network.ChatClient;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatState {
    private GamePanel gp;
    private ChatClient chatClient;
    private List<String> messages = new ArrayList<>();
    private String currentMessage = "";
    private boolean active = false;

    public ChatState(GamePanel gp) {
        this.gp = gp;
    }

    public void activate() {
        if (!active) {
            chatClient = new ChatClient(gp.getCurrentUser());
            if (chatClient.connect("localhost", 5555)) {
                active = true;
                messages.add("Connected to chat server!");
            } else {
                messages.add("Failed to connect to chat server");
            }
        }
    }

    public void update() {
        KeyHandler keyHandler = gp.getKeyHandler();

        if (keyHandler.escapePressed) {
            gp.setGameState(GamePanel.STATE_PLAYING);
            keyHandler.escapePressed = false;
            return;
        }

        // Handle incoming messages
        String newMessage;
        while ((newMessage = chatClient.getNextMessage()) != null) {
            messages.add(newMessage);
            if (messages.size() > 20) {
                messages.remove(0);
            }
        }

        // Handle outgoing messages
        if (keyHandler.keyPressed) {
            char keyChar = keyHandler.lastKeyChar;
            if (keyChar >= 32 && keyChar <= 126 && currentMessage.length() < 50) {
                currentMessage += keyChar;
            } else if (keyHandler.lastKeyCode == KeyEvent.VK_BACK_SPACE && currentMessage.length() > 0) {
                currentMessage = currentMessage.substring(0, currentMessage.length() - 1);
            } else if (keyHandler.lastKeyCode == KeyEvent.VK_ENTER && !currentMessage.isEmpty()) {
                chatClient.sendMessage(currentMessage);
                currentMessage = "";
            }
            keyHandler.keyPressed = false;
        }
    }

    public void draw(Graphics2D g2) {
        // Semi-transparent background
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, gp.getScreenWidth(), gp.getScreenHeight());

        // Draw chat messages
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));

        int y = 100;
        for (String message : messages) {
            g2.drawString(message, 50, y);
            y += 25;
        }

        // Draw current message being typed
        g2.drawString("> " + currentMessage, 50, gp.getScreenHeight() - 50);

        // Draw instructions
        g2.drawString("Press ESC to close chat", 50, gp.getScreenHeight() - 20);
    }

    public void deactivate() {
        if (active) {
            chatClient.disconnect();
            active = false;
        }
    }
}