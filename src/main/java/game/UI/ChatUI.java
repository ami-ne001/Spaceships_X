package game.UI;

import game.GamePanel;
import game.network.ChatClient;
import game.network.ChatMessage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatUI {
    // UI Constants
    private static final int CHAT_MARGIN = 10;
    private static final int CHAT_WIDTH = 300;
    private static final int CHAT_HEIGHT = 200;
    private static final int INPUT_HEIGHT = 30;
    private static final int MAX_VISIBLE_MESSAGES = 10;
    private static final Color CHAT_BG_COLOR = new Color(0, 0, 0, 180);
    private static final Color CHAT_INPUT_COLOR = new Color(50, 50, 50, 200);
    private static final Color CHAT_TEXT_COLOR = Color.WHITE;
    
    // Chat state
    private boolean isVisible = false;
    private String inputText = "";
    private List<ChatMessage> messages = new ArrayList<>();
    private int scrollPosition = 0;
    
    // References
    private GamePanel gamePanel;
    private ChatClient chatClient;
    
    public ChatUI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    public void update() {
        KeyHandler keyHandler = gamePanel.getKeyHandler();
        
        // Open chat with Shift key
        if (keyHandler.lastKeyCode == KeyEvent.VK_SHIFT && keyHandler.keyPressed && !isVisible) {
            isVisible = true;
            keyHandler.keyPressed = false;
        }
        
        // Close chat with Escape key
        if (keyHandler.lastKeyCode == KeyEvent.VK_ESCAPE && keyHandler.keyPressed && isVisible) {
            isVisible = false;
            keyHandler.keyPressed = false;
            return;
        }
        
        // Only process input when chat is visible
        if (!isVisible) return;
        
        // Process text input
        if (keyHandler.keyPressed) {
            keyHandler.keyPressed = false;
            
            if (keyHandler.lastKeyCode == KeyEvent.VK_ENTER && !inputText.trim().isEmpty()) {
                // Send message
                if (chatClient != null && chatClient.isConnected()) {
                    chatClient.sendMessage(inputText.trim());
                }
                inputText = "";
            } else if (keyHandler.lastKeyCode == KeyEvent.VK_BACK_SPACE && !inputText.isEmpty()) {
                // Delete last character
                inputText = inputText.substring(0, inputText.length() - 1);
            } else if (keyHandler.lastKeyChar >= 32 && keyHandler.lastKeyChar <= 126) {
                // Add character to input text (only printable ASCII)
                if (inputText.length() < 50) { // Limit input length
                    inputText += keyHandler.lastKeyChar;
                }
            }
        }
    }
    
    public void draw(Graphics2D g2) {
        if (!isVisible) return;
        
        int screenWidth = gamePanel.getScreenWidth();
        int screenHeight = gamePanel.getScreenHeight();
        
        // Position chat in bottom left
        int chatX = CHAT_MARGIN;
        int chatY = screenHeight - CHAT_HEIGHT - CHAT_MARGIN;
        
        // Draw chat background
        g2.setColor(CHAT_BG_COLOR);
        g2.fillRoundRect(chatX, chatY, CHAT_WIDTH, CHAT_HEIGHT, 10, 10);
        
        // Draw chat messages
        g2.setColor(CHAT_TEXT_COLOR);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        
        int messageY = chatY + CHAT_HEIGHT - INPUT_HEIGHT - 10;
        int displayCount = Math.min(MAX_VISIBLE_MESSAGES, messages.size() - scrollPosition);
        
        for (int i = scrollPosition; i < scrollPosition + displayCount; i++) {
            int index = messages.size() - 1 - i;
            if (index >= 0) {
                ChatMessage message = messages.get(index);
                String text = message.toString();
                g2.drawString(text, chatX + 10, messageY);
                messageY -= 20; // Move up for next message
            }
        }
        
        // Draw input box
        g2.setColor(CHAT_INPUT_COLOR);
        g2.fillRoundRect(chatX + 5, chatY + CHAT_HEIGHT - INPUT_HEIGHT - 5, 
                         CHAT_WIDTH - 10, INPUT_HEIGHT, 5, 5);
        
        // Draw input text
        g2.setColor(CHAT_TEXT_COLOR);
        g2.drawString(inputText + (System.currentTimeMillis() % 1000 < 500 ? "|" : ""), 
                     chatX + 10, chatY + CHAT_HEIGHT - 10);
        
        // Draw border
        g2.setColor(new Color(150, 150, 150, 200));
        g2.drawRoundRect(chatX, chatY, CHAT_WIDTH, CHAT_HEIGHT, 10, 10);
        
        // Draw instructions
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("Press ESC to close", chatX + 5, chatY + 15);
    }
    
    public void addMessage(ChatMessage message) {
        messages.add(message);
        // Auto-scroll to bottom when new messages arrive
        scrollPosition = Math.max(0, messages.size() - MAX_VISIBLE_MESSAGES);
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
} 