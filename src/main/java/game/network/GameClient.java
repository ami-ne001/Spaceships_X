package game.network;

import game.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameClient {
    private static String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final int RECONNECT_DELAY = 1000; // 1 second
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private static final long UPDATE_INTERVAL = 16; // ~60 updates per second
    private static final int STATE_BUFFER_SIZE = 10;
    
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String clientId;
    private GamePanel gamePanel;
    private volatile boolean connected;
    private Thread listenThread;
    private Thread updateThread;
    private boolean isHost;
    private int reconnectAttempts = 0;
    
    // State interpolation
    private final Queue<GameState> stateBuffer;
    private GameState previousState;
    private GameState targetState;
    private float interpolationAlpha = 0;
    private long lastUpdateTime;
    
    public GameClient(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.clientId = UUID.randomUUID().toString();
        this.stateBuffer = new ConcurrentLinkedQueue<>();
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public synchronized void connect() {
        if (connected) return;
        
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
            reconnectAttempts = 0;
            
            // Start listening for server updates
            listenThread = new Thread(this::listenForUpdates);
            listenThread.setDaemon(true); // Make thread daemon so it doesn't prevent JVM shutdown
            listenThread.start();
            
            // Start update thread
            startUpdateThread();
            
            System.out.println("Connected to server with ID: " + clientId);
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            connected = false;
            tryReconnect();
        }
    }
    
    private void tryReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            System.out.println("Max reconnection attempts reached");
            return;
        }
        
        reconnectAttempts++;
        System.out.println("Attempting to reconnect... (Attempt " + reconnectAttempts + ")");
        
        try {
            Thread.sleep(RECONNECT_DELAY);
            connect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public synchronized void disconnect() {
        connected = false;
        try {
            if (listenThread != null) {
                listenThread.interrupt();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void listenForUpdates() {
        while (connected && !Thread.currentThread().isInterrupted()) {
            try {
                GameState state = (GameState) in.readObject();
                if (state != null) {
                    updateGameState(state);
                }
            } catch (IOException e) {
                System.out.println("Lost connection to server: " + e.getMessage());
                handleDisconnect();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }
    
    private void startUpdateThread() {
        updateThread = new Thread(() -> {
            while (connected && !Thread.currentThread().isInterrupted()) {
                long currentTime = System.currentTimeMillis();
                long deltaTime = currentTime - lastUpdateTime;
                
                if (deltaTime >= UPDATE_INTERVAL) {
                    sendGameState();
                    lastUpdateTime = currentTime;
                }
                
                interpolateStates();
                
                try {
                    Thread.sleep(1); // Prevent thread from hogging CPU
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
    private void interpolateStates() {
        if (previousState == null || targetState == null) {
            if (!stateBuffer.isEmpty()) {
                targetState = stateBuffer.poll();
                previousState = targetState;
            }
            return;
        }
        
        interpolationAlpha += 0.1f; // Adjust this value to control interpolation speed
        
        if (interpolationAlpha >= 1.0f) {
            previousState = targetState;
            targetState = stateBuffer.poll();
            interpolationAlpha = 0;
            return;
        }
        
        if (targetState != null && previousState != null) {
            // Interpolate position
            int interpolatedX = (int) lerp(previousState.getPlayerX(), targetState.getPlayerX(), interpolationAlpha);
            int interpolatedY = (int) lerp(previousState.getPlayerY(), targetState.getPlayerY(), interpolationAlpha);
            
            // Update game state with interpolated values
            gamePanel.updateOtherPlayer(targetState.getPlayerId(), interpolatedX, interpolatedY);
        }
    }
    
    private float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }
    
    private synchronized void updateGameState(GameState state) {
        if (!connected || state == null || state.getPlayerId().equals(clientId)) return;
        
        try {
            // Add state to buffer instead of immediately applying
            if (stateBuffer.size() >= STATE_BUFFER_SIZE) {
                stateBuffer.poll(); // Remove oldest state if buffer is full
            }
            stateBuffer.offer(state);
            
            // Extract player info
            String playerId = state.getPlayerId();
            String username = state.getUsername();
            String shipPath = state.getShipImagePath();
            
            // Add other player if they don't exist yet
            if (!gamePanel.getOtherPlayers().containsKey(playerId)) {
                gamePanel.addOtherPlayer(playerId);
            }
            
            // Update the player info
            OtherPlayer otherPlayer = gamePanel.getOtherPlayers().get(playerId);
            if (otherPlayer != null) {
                // Update username if available
                if (username != null) {
                    otherPlayer.setUsername(username);
                }
                
                // Update ship image if available
                if (shipPath != null) {
                    otherPlayer.setShipImagePath(shipPath);
                }
            }
            
            // Handle non-interpolated updates
            if (!isHost && state.getEnemies() != null) {
                gamePanel.getEnemyManager().clearEnemies();
                gamePanel.getEnemyManager().syncEnemies(state.getEnemies());
            }
            
            // Handle projectile synchronization
            if (state.getProjectiles() != null) {
                // Keep all projectiles from other players and enemy projectiles from host
                List<GameState.ProjectileState> validProjectiles = state.getProjectiles().stream()
                    .filter(p -> {
                        String shooterId = p.getShooterId();
                        boolean isEnemyProjectile = shooterId != null && shooterId.startsWith("enemy_");
                        // Accept projectiles that are:
                        // 1. From other players (not our own)
                        // 2. Enemy projectiles from host (for non-host players)
                        return !clientId.equals(shooterId) && // Not our own projectiles
                               ((!isHost && isEnemyProjectile) || // Non-host should see enemy projectiles
                                !isEnemyProjectile); // Everyone sees player projectiles
                    })
                    .toList();
                
                if (!validProjectiles.isEmpty()) {
                    System.out.println("Received " + validProjectiles.size() + " projectiles from network");
                    gamePanel.getProjectileManager().syncProjectiles(validProjectiles, clientId);
                }
            }
        } catch (Exception e) {
            System.out.println("Error updating game state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public synchronized void sendGameState() {
        if (!connected || gamePanel == null) return;
        
        try {
            GameState state = new GameState(clientId);
            Player player = gamePanel.getPlayer();
            if (player == null) return;
            
            // Update player state
            state.setPlayerPosition(player.getX(), player.getY());
            state.setLives(player.getLives());
            state.setScore(gamePanel.getScore());
            state.setLevel(gamePanel.getLevel());
            
            // Set username (use current user from game panel)
            state.setUsername(gamePanel.getCurrentUser());
            
            // Update ship image path
            if (player.getImage() != null && gamePanel.getSelectedShip() != null) {
                state.setShipImagePath(gamePanel.getSelectedShip().getImagePath());
            }
            
            // Only host sends enemy states
            if (isHost) {
                List<GameState.EnemyState> enemyStates = new ArrayList<>();
                for (Enemy enemy : gamePanel.getEnemyManager().getEnemies()) {
                    if (enemy != null) {
                        enemyStates.add(new GameState.EnemyState(
                            enemy.getX(),
                            enemy.getY(),
                            enemy.getType(),
                            enemy.getHealth()
                        ));
                    }
                }
                state.setEnemies(enemyStates);
            }
            
            // Update projectile states
            List<GameState.ProjectileState> projectileStates = new ArrayList<>();
            for (Projectile projectile : gamePanel.getProjectileManager().getProjectiles()) {
                if (projectile != null && projectile.isActive()) {
                    // Only send projectiles that we own (our player projectiles or, if host, enemy projectiles)
                    String shooterId = projectile.getShooterId();
                    if (clientId.equals(shooterId) || 
                        (isHost && shooterId != null && shooterId.startsWith("enemy_"))) {
                        projectileStates.add(new GameState.ProjectileState(
                            projectile.getX(),
                            projectile.getY(),
                            projectile.isPlayerProjectile(),
                            shooterId
                        ));
                    }
                }
            }
            state.setProjectiles(projectileStates);
            
            // Send state to server
            if (out != null) {
                out.writeObject(state);
                out.reset(); // Reset the object stream to prevent memory leaks
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Failed to send game state: " + e.getMessage());
            handleDisconnect();
        } catch (Exception e) {
            System.out.println("Unexpected error sending game state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleDisconnect() {
        System.out.println("Handling disconnect for client: " + clientId);
        disconnect();
        if (isHost) {
            // If we're the host, try to restart the server
            new Thread(() -> {
                try {
                    GameServer.getInstance().start();
                    System.out.println("Server restarted, attempting to reconnect...");
                    Thread.sleep(1000);
                    connect();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.out.println("Error during server restart: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        } else {
            tryReconnect();
        }
    }
    
    public boolean isConnected() { return connected; }
    public String getClientId() { return clientId; }
    public boolean isHost() { return isHost; }
    public void setHost(boolean isHost) { this.isHost = isHost; }
    public static void setServerIP(String ip) { SERVER_IP = ip; }
} 