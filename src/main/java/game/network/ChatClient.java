package game.network;

import game.UI.ChatUI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    private static final int PORT = 5001;
    private static String SERVER_IP = "localhost";
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientId;
    private String username;
    private boolean connected = false;
    private ExecutorService threadPool;
    private ChatUI chatUI;
    private List<ChatMessage> messageCache = new ArrayList<>();
    
    public ChatClient(String clientId, String username, ChatUI chatUI) {
        this.clientId = clientId;
        this.username = username;
        this.chatUI = chatUI;
        this.threadPool = Executors.newCachedThreadPool();
    }
    
    public void connect() {
        if (connected) return;
        
        try {
            socket = new Socket(SERVER_IP, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Send client ID
            out.writeObject(clientId);
            out.flush();
            
            connected = true;
            System.out.println("Connected to chat server at " + SERVER_IP + ":" + PORT);
            System.out.println("Chat user: " + username);
            
            // Start listener thread for incoming messages
            threadPool.execute(this::listenForMessages);
            
        } catch (IOException e) {
            System.err.println("Failed to connect to chat server: " + e.getMessage());
            disconnect();
        }
    }
    
    private void listenForMessages() {
        try {
            while (connected) {
                Object obj = in.readObject();
                if (obj instanceof ChatMessage) {
                    ChatMessage message = (ChatMessage) obj;
                    if (chatUI != null) {
                        chatUI.addMessage(message);
                    } else {
                        // If UI not ready, cache the message
                        synchronized (messageCache) {
                            messageCache.add(message);
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                System.err.println("Connection to chat server lost: " + e.getMessage());
                disconnect();
            }
        }
    }
    
    public void sendMessage(String message) {
        if (!connected) return;
        
        try {
            // Always use current username when sending message
            ChatMessage chatMessage = new ChatMessage(clientId, this.username, message);
            out.writeObject(chatMessage);
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
            disconnect();
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
    
    public void setChatUI(ChatUI chatUI) {
        this.chatUI = chatUI;
        
        // If messages were cached before UI was ready, add them now
        if (!messageCache.isEmpty()) {
            synchronized (messageCache) {
                for (ChatMessage message : messageCache) {
                    chatUI.addMessage(message);
                }
                messageCache.clear();
            }
        }
    }
    
    public void setUsername(String username) {
        this.username = username;
        System.out.println("Updated chat username to: " + username);
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public static void setServerIP(String ip) {
        SERVER_IP = ip;
    }
} 