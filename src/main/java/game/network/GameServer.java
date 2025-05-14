package game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 5000;
    private static final long UPDATE_INTERVAL = 16; // ~60 updates per second
    private ServerSocket serverSocket;
    private volatile boolean running;
    private ConcurrentHashMap<String, ClientHandler> clients;
    private List<GameState> gameStates;
    private static GameServer instance;
    private ScheduledExecutorService updateExecutor;

    private GameServer() {
        clients = new ConcurrentHashMap<>();
        gameStates = Collections.synchronizedList(new ArrayList<>());
        updateExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public static synchronized GameServer getInstance() {
        if (instance == null) {
            instance = new GameServer();
        }
        return instance;
    }

    public void start() {
        if (running) return;
        
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);

            // Start the update scheduler
            updateExecutor.scheduleAtFixedRate(this::update, 0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);

            // Accept client connections in a separate thread
            new Thread(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                        new Thread(clientHandler).start();
                    } catch (IOException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void update() {
        // Process any pending game states and broadcast updates
        synchronized (gameStates) {
            if (!gameStates.isEmpty()) {
                GameState latestState = gameStates.get(gameStates.size() - 1);
                broadcastState(latestState.getPlayerId(), latestState);
                gameStates.clear();
            }
        }
    }

    public void stop() {
        running = false;
        updateExecutor.shutdown();
        for (ClientHandler client : clients.values()) {
            client.disconnect();
        }
        clients.clear();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastState(String senderId, GameState state) {
        if (state == null) return;
        
        List<String> disconnectedClients = new ArrayList<>();
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            String clientId = entry.getKey();
            ClientHandler client = entry.getValue();
            if (!clientId.equals(senderId)) {
                try {
                    client.sendState(state);
                } catch (Exception e) {
                    System.out.println("Error broadcasting to client " + clientId + ": " + e.getMessage());
                    disconnectedClients.add(clientId);
                }
            }
        }
        
        // Remove disconnected clients
        for (String clientId : disconnectedClients) {
            removeClient(clientId);
        }
    }

    public synchronized void addClient(String id, ClientHandler handler) {
        if (id == null || handler == null) return;
        
        // Remove any existing client with the same ID
        if (clients.containsKey(id)) {
            removeClient(id);
        }
        
        clients.put(id, handler);
        System.out.println("Client connected: " + id + " (Total clients: " + clients.size() + ")");
    }

    public synchronized void removeClient(String id) {
        if (id == null) return;
        
        ClientHandler handler = clients.remove(id);
        if (handler != null) {
            handler.disconnect();
            System.out.println("Client disconnected: " + id + " (Total clients: " + clients.size() + ")");
        }
    }

    // Inner class to handle individual client connections
    private class ClientHandler implements Runnable {
        private Socket socket;
        private GameServer server;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String id;
        private volatile boolean running = true;
        private final Queue<GameState> stateQueue;

        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
            this.id = UUID.randomUUID().toString();
            this.stateQueue = new ConcurrentLinkedQueue<>();
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                server.addClient(id, this);

                while (running) {
                    try {
                        GameState state = (GameState) in.readObject();
                        if (state != null) {
                            // Add to queue instead of broadcasting immediately
                            stateQueue.offer(state);
                            synchronized (gameStates) {
                                gameStates.add(state);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Client " + id + " connection error: " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        public void disconnect() {
            running = false;
            server.removeClient(id);
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendState(GameState state) {
            try {
                out.writeObject(state);
                out.reset(); // Reset the object stream to prevent memory leaks
                out.flush();
            } catch (IOException e) {
                disconnect();
            }
        }

        public String getId() { return id; }
    }
} 