package game.network;

import java.io.Serializable;

/**
 * Represents a chat message sent between clients
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String senderId;
    private String senderName;
    private String message;
    private long timestamp;
    
    public ChatMessage(String senderId, String senderName, String message) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return senderName + ": " + message;
    }
} 