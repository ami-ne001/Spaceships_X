package game;

import game.DAO.DatabaseManager;

import javax.swing.*;
import java.awt.*;

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
    private int enemiesDefeated = 0;
    private int gameState = STATE_MENU;
    private MenuState menuState;
    private DatabaseManager dbManager;
    private Account account;
    private ShipSelectionState shipSelectionState;
    private String currentUser;
    private Ship selectedShip;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        dbManager = new DatabaseManager();
        account = new Account(this, dbManager);
        shipSelectionState = new ShipSelectionState(this);

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

        // Play background music
        soundManager.playBackgroundMusic();
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

    private void update() {
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
            case STATE_PLAYING:
                if (!gameOver) {
                    player.update();
                    enemyManager.update();
                    projectileManager.update();
                    collisionChecker.checkCollisions();

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
                    dbManager.updateHighscore(currentUser, score);
                }
                break;
            case STATE_GAME_OVER:
                if (keyHandler.rPressed) {
                    startGame();
                    keyHandler.rPressed = false;
                }else if(keyHandler.escapePressed){
                    startGame();
                    gameState = STATE_MENU;
                }
                break;
        }
    }

    private void levelUp() {
        level++;
        score += 100 * level;
        enemyManager.increaseDifficulty(level);
    }

    public void gameOver() {
        gameOver = true;
        soundManager.stopBackgroundMusic();
        soundManager.playSound(SoundManager.EXPLOSION_SOUND);
    }

    public void startGame() {
        score = 0;
        level = 1;
        gameOver = false;
        gameState = STATE_PLAYING;
        enemiesDefeated = 0;

        // Clear all game objects
        if (enemyManager != null) {
            enemyManager.getEnemies().clear();
        }
        if (projectileManager != null) {
            projectileManager.getProjectiles().clear();
            projectileManager.getExplosions().clear();
        }

        // Reinitialize game
        initGame();

        // Restart background music if sound is enabled
        if (soundManager != null) {
            soundManager.playBackgroundMusic();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        switch(gameState) {
            case STATE_MENU:
                menuState.draw(g2);
                break;
            case STATE_PLAYING:
                background.draw(g2);

                player.draw(g2);
                enemyManager.draw(g2);
                projectileManager.draw(g2);

                // Draw UI
                ui.draw(g2);
                break;
            case STATE_GAME_OVER:
                // Draw background
                background.draw(g2);

                // Draw game elements
                player.draw(g2);
                enemyManager.draw(g2);
                projectileManager.draw(g2);

                // Draw UI
                ui.draw(g2);
                break;
            case STATE_ACCOUNT:
                account.draw(g2);
                break;
            case STATE_SHIP_SELECTION:
                shipSelectionState.draw(g2);
                break;
        }

        g2.dispose();
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
    public void setGameState(int gameState) {this.gameState = gameState;}
    public void setCurrentUser(String username) {
        this.currentUser = username;
    }
    public void setSelectedShip(Ship ship) {
        this.selectedShip = ship;

        player.applyShipStats(ship);
    }
    public void setPlayerLives(int playerLives) { this.playerLives = playerLives; }
}