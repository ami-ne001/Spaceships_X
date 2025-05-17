package game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 5001;
    private static ChatServer instance;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final List<ChatMessage> messageHistory = new ArrayList<>();
    private final int MAX_HISTORY = 50; // Maximum number of messages to store
    private boolean running = false;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    
    private ChatServer() {
        // Private constructor for singleton
    }
    
    public static synchronized ChatServer getInstance() {
        if (instance == null) {
            instance = new ChatServer();
        }
        return instance;
    }
    
    public void start() {
        if (running) return;
        
        try {
            serverSocket = new ServerSocket(PORT);
            threadPool = Executors.newCachedThreadPool();
            running = true;
            System.out.println("Chat Server started on port " + PORT);
            
            // Accept client connections
            threadPool.execute(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket);
                        threadPool.execute(handler);
                    } catch (IOException e) {
                        if (!running) break;
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            });
            
        } catch (IOException e) {
            System.err.println("Could not start chat server: " + e.getMessage());
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdown();
            }
            clients.clear();
        } catch (IOException e) {
            System.err.println("Error stopping chat server: " + e.getMessage());
        }
    }
    
    private void broadcast(ChatMessage message) {
        // Log the message for debugging
        System.out.println("Broadcasting message: " + message.getSenderName() + ": " + message.getMessage());
        
        // Add to history
        synchronized (messageHistory) {
            messageHistory.add(message);
            if (messageHistory.size() > MAX_HISTORY) {
                messageHistory.remove(0);
            }
        }
        
        // Send to all clients
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }
    
    private List<ChatMessage> getRecentMessages() {
        synchronized (messageHistory) {
            return new ArrayList<>(messageHistory);
        }
    }
    
    private class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String clientId;
        private String clientName;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Error creating streams: " + e.getMessage());
            }
        }
        
        @Override
        public void run() {
            try {
                // First message should be client ID
                clientId = (String) in.readObject();
                
                // Remove any existing handler with the same ID to prevent name conflicts
                if (clients.containsKey(clientId)) {
                    System.out.println("Client ID already exists, removing old connection: " + clientId);
                    ClientHandler oldHandler = clients.get(clientId);
                    oldHandler.disconnect();
                }
                
                clients.put(clientId, this);
                System.out.println("Chat client connected: " + clientId);
                
                // Send message history to new client
                List<ChatMessage> history = getRecentMessages();
                for (ChatMessage msg : history) {
                    sendMessage(msg);
                }
                
                // Handle incoming messages
                while (running) {
                    Object obj = in.readObject();
                    if (obj instanceof ChatMessage) {
                        ChatMessage message = (ChatMessage) obj;
                        // Store the client name for this connection
                        clientName = message.getSenderName();
                        broadcast(message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client disconnected: " + clientId);
            } finally {
                disconnect();
            }
        }
        
        public void sendMessage(ChatMessage message) {
            try {
                if (out != null) {
                    out.writeObject(message);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Error sending message to client " + clientId + ": " + e.getMessage());
                disconnect();
            }
        }
        
        public void disconnect() {
            if (clientId != null) {
                clients.remove(clientId);
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        // For running chat server standalone
        ChatServer.getInstance().start();
    }
} 