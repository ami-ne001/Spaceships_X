package game;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {
    // Screen settings
    private final int originalTileSize = 48;
    private final int scale = 1;
    private final int tileSize = originalTileSize * scale;
    private final int maxScreenCol = 15;
    private final int maxScreenRow = 15;
    private final int screenWidth = tileSize * maxScreenCol;
    private final int screenHeight = tileSize * maxScreenRow;

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
    private int playerLives = 3;
    private int level = 1;
    private boolean gameOver = false;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);

        // Initialize game components
        initGame();
    }

    private void initGame() {
        player = new Player(this, keyHandler);
        enemyManager = new EnemyManager(this);
        projectileManager = new ProjectileManager(this);
        collisionChecker = new CollisionChecker(this);
        background = new Background(this);
        soundManager = new SoundManager();
        ui = new UI(this);

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
        if (!gameOver && !keyHandler.pausePressed) {
            player.update();
            enemyManager.update();
            projectileManager.update();
            collisionChecker.checkCollisions();

            // Level progression
            if (score >= 100 && level < 2) {
                level = 2;
                enemyManager.setLevel(level);
            } else if (score >= 200 && level < 3) {
                level = 3;
                enemyManager.setLevel(level);
            }
        } else if (keyHandler.rPressed) {
            restartGame();
            keyHandler.rPressed = false; // Reset the key state
        }
    }

    private void levelUp() {
        level++;
        score += 100 * level;
        enemyManager.increaseDifficulty(level);
    }

    public void gameOver() {
        gameOver = true;
        soundManager.playSound(SoundManager.GAMEOVER_SOUND);
        soundManager.stopBackgroundMusic();
    }

    public void restartGame() {
        playerLives = 3;
        score = 0;
        level = 1;
        gameOver = false;

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

        // Draw background
        background.draw(g2);

        // Draw game elements
        player.draw(g2);
        enemyManager.draw(g2);
        projectileManager.draw(g2);

        // Draw UI
        ui.draw(g2);

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
}