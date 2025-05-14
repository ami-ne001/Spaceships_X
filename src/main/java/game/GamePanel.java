package game;

import game.DAO.DatabaseManager;
import game.network.ChatClient;
import game.network.ChatServer;
import game.network.GameClient;
import game.network.GameServer;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel implements Runnable {
    // Screen settings
    private final int tileSize = 48;
    private final int maxScreenCol = 15;
    private final int maxScreenRow = 15;
    private final int screenWidth = tileSize * maxScreenCol;
    private final int screenHeight = tileSize * maxScreenRow;
    public static final int STATE_MENU = 0;
    public static final int STATE_ACCOUNT = 1;
    public static final int STATE_SHIP_SELECTION = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_GAME_OVER = 4;
    public static final int STATE_IP_INPUT = 5;

    // Game state
    private int FPS = 60;
    private Thread gameThread;
    private KeyHandler keyHandler = new KeyHandler();
    private Player player;
    private EnemyManager enemyManager;
    private ProjectileManager projectileManager;
    private CollisionChecker collisionChecker;
    private Background background;
    private SoundManager soundManager;
    private UI ui;
    private int score = 0;
    private int playerLives;
    private int level = 1;
    private boolean gameOver = false;
    private int gameState = STATE_MENU;
    private MenuState menuState;
    private DatabaseManager dbManager;
    private Account account;
    private ShipSelectionState shipSelectionState;
    private IPInputState ipInputState;
    private String currentUser;
    private Ship selectedShip;

    // Multiplayer components
    private GameClient gameClient;
    private boolean isMultiplayer = false;
    private Map<String, OtherPlayer> otherPlayers = new HashMap<>();
    
    // Chat components
    private ChatUI chatUI;
    private ChatClient chatClient;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        dbManager = new DatabaseManager();
        account = new Account(this, dbManager);
        shipSelectionState = new ShipSelectionState(this);
        ipInputState = new IPInputState(this);

        // Initialize game components
        initGame();
    }

    public KeyHandler getKeyHandler() {
        return keyHandler;
    }

    private void initGame() {
        player = new Player(this, keyHandler);
        enemyManager = new EnemyManager(this);
        projectileManager = new ProjectileManager(this);
        collisionChecker = new CollisionChecker(this);
        background = new Background(this);
        soundManager = new SoundManager();
        ui = new UI(this);
        menuState = new MenuState(this);
        chatUI = new ChatUI(this);  // Initialize Chat UI

        // Play background music
        soundManager.playBackgroundMusic();
    }

    public void startMultiplayerGame(boolean isHost, String serverIP) {
        // Clear any existing multiplayer state
        if (gameClient != null) {
            gameClient.disconnect();
        }
        if (chatClient != null) {
            chatClient.disconnect();
        }
        otherPlayers.clear();
        
        isMultiplayer = true;
        
        // Set up game client
        gameClient = new GameClient(this);
        gameClient.setHost(isHost);
        
        if (isHost) {
            // Start game server in a new thread
            new Thread(() -> {
                GameServer.getInstance().start();
                System.out.println("Game Server started on port 5000");
            }).start();
            
            // Start chat server in a new thread
            new Thread(() -> {
                ChatServer.getInstance().start();
                System.out.println("Chat Server started on port 5001");
            }).start();
            
            // Give the servers a moment to start
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            GameClient.setServerIP("localhost"); // Host uses localhost
            ChatClient.setServerIP("localhost");
        } else {
            // Client uses provided server IP
            GameClient.setServerIP(serverIP);
            ChatClient.setServerIP(serverIP);
        }
        
        // Connect to game server
        gameClient.connect();
        
        // Set up and connect chat client
        chatClient = new ChatClient(gameClient.getClientId(), currentUser, chatUI);
        chatUI.setChatClient(chatClient);
        chatClient.connect();
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        switch(gameState) {
            case STATE_MENU:
                menuState.update();
                break;
            case STATE_ACCOUNT:
                account.update();
                break;
            case STATE_SHIP_SELECTION:
                shipSelectionState.update();
                break;
            case STATE_IP_INPUT:
                ipInputState.update();
                break;
            case STATE_PLAYING:
                if (!gameOver) {
                    // Update chat UI
                    chatUI.update();
                    
                    // Only process game controls if chat isn't visible
                    if (!chatUI.isVisible()) {
                        player.update();
                    }
                    
                    enemyManager.update();
                    projectileManager.update();
                    collisionChecker.checkCollisions();

                    // Update other players in multiplayer mode
                    if (isMultiplayer && gameClient != null && gameClient.isConnected()) {
                        for (OtherPlayer otherPlayer : otherPlayers.values()) {
                            otherPlayer.update();
                        }
                        gameClient.sendGameState();
                    }

                    // Level progression
                    if (score >= 100 && level < 2) {
                        level = 2;
                        enemyManager.setLevel(level);
                    } else if (score >= 300 && level < 3) {
                        level = 3;
                        enemyManager.setLevel(level);
                    }
                } else {
                    gameState = STATE_GAME_OVER;
                    if(score > dbManager.getHighscore(currentUser)) {
                        dbManager.updateHighscore(currentUser, score);
                    }
                }
                break;
            case STATE_GAME_OVER:
                if (keyHandler.rPressed) {
                    restartGame();
                    keyHandler.rPressed = false;
                } else if (keyHandler.escapePressed) {
                    startGame();
                    gameState = STATE_MENU;
                    keyHandler.escapePressed = false;
                }
                break;
        }
    }

    public void gameOver() {
        gameOver = true;
        soundManager.playGameOverSound();
    }

    public void startGame() {
        // Clear multiplayer state if exists
        if (gameClient != null) {
            gameClient.disconnect();
            gameClient = null;
        }
        isMultiplayer = false;
        otherPlayers.clear();

        score = 0;
        level = 1;
        gameOver = false;
        gameState = STATE_PLAYING;

        // Clear all game objects
        if (enemyManager != null) {
            enemyManager.clearEnemies();
        }
        if (projectileManager != null) {
            projectileManager.clearProjectiles();
            projectileManager.clearExplosions();
        }

        // Reinitialize game
        initGame();

        // Restart background music if sound is enabled
        if (soundManager != null) {
            soundManager.playBackgroundMusic();
        }
    }

    public void restartGame(){
        score = 0;
        level = 1;
        enemyManager.setLevel(level);
        gameOver = false;
        gameState = STATE_PLAYING;
        playerLives = selectedShip.getHealth();
        player.setX(getScreenWidth() / 2 - getTileSize() / 2);
        player.setY(getScreenHeight() - getTileSize() - 20);
        player.setlives(playerLives);

        // Clear all game objects
        if (enemyManager != null) {
            enemyManager.clearEnemies();
        }
        if (projectileManager != null) {
            projectileManager.clearProjectiles();
            projectileManager.clearExplosions();
        }

        // Restart background music if sound is enabled
        if (soundManager != null) {
            soundManager.playBackgroundMusic();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        switch (gameState) {
            case STATE_MENU:
                menuState.draw(g2);
                break;
            case STATE_ACCOUNT:
                account.draw(g2);
                break;
            case STATE_SHIP_SELECTION:
                shipSelectionState.draw(g2);
                break;
            case STATE_IP_INPUT:
                ipInputState.draw(g2);
                break;
            case STATE_PLAYING:
                // Draw the game elements
                background.draw(g2);
                enemyManager.draw(g2);
                
                // Draw other players in multiplayer mode
                if (isMultiplayer) {
                    for (OtherPlayer otherPlayer : otherPlayers.values()) {
                        otherPlayer.draw(g2);
                    }
                }
                
                player.draw(g2);
                projectileManager.draw(g2);
                ui.draw(g2);
                
                // Draw chat UI
                chatUI.draw(g2);
                break;
            case STATE_GAME_OVER:
                background.draw(g2);
                ui.drawGameOverScreen(g2);
                break;
        }

        g2.dispose();
    }

    // Multiplayer methods
    public void addOtherPlayer(String playerId) {
        if (!otherPlayers.containsKey(playerId)) {
            otherPlayers.put(playerId, new OtherPlayer(this, playerId));
            System.out.println("Added player: " + playerId);
        }
    }

    public void removeOtherPlayer(String playerId) {
        otherPlayers.remove(playerId);
        System.out.println("Removed player: " + playerId);
    }

    public void updateOtherPlayer(String playerId, int x, int y) {
        if (!otherPlayers.containsKey(playerId)) {
            addOtherPlayer(playerId);
        }
        OtherPlayer player = otherPlayers.get(playerId);
        if (player != null) {
            player.setPosition(x, y);
            System.out.println("Updated player position: " + playerId + " at " + x + "," + y);
        }
    }

    // Getters and setters
    public int getTileSize() { return tileSize; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public Player getPlayer() { return player; }
    public EnemyManager getEnemyManager() { return enemyManager; }
    public ProjectileManager getProjectileManager() { return projectileManager; }
    public SoundManager getSoundManager() { return soundManager; }
    public int getScore() { return score; }
    public void addScore(int points) { score += points; }
    public int getPlayerLives() { return playerLives; }
    public void decreasePlayerLives() { playerLives--; }
    public int getLevel() { return level; }
    public boolean isGameOver() { return gameOver; }
    public void setGameState(int gameState) { this.gameState = gameState; }
    public void setCurrentUser(String username) { 
        this.currentUser = username; 
        // Update chat client username if it exists
        if (chatClient != null) {
            chatClient.setUsername(username);
        }
    }
    public String getCurrentUser() { return currentUser; }
    public void setSelectedShip(Ship ship) {
        this.selectedShip = ship;
        player.applyShipStats(ship);
    }
    public Ship getSelectedShip() { return selectedShip; }
    public Map<String, OtherPlayer> getOtherPlayers() { return otherPlayers; }
    public void setPlayerLives(int playerLives) { this.playerLives = playerLives; }
    public boolean isMultiplayer() { return isMultiplayer; }
    public GameClient getGameClient() { return gameClient; }
    public ChatUI getChatUI() {
        return chatUI;
    }
    public ChatClient getChatClient() {
        return chatClient;
    }
}