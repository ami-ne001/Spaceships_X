package game;

import game.network.GameState;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EnemyManager {
    private GamePanel gp;
    private List<Enemy> enemies;
    private int spawnTimer;
    private int spawnInterval = 60;
    private int level = 1;
    private int maxEnemies = 5;

    public EnemyManager(GamePanel gp) {
        this.gp = gp;
        enemies = Collections.synchronizedList(new ArrayList<>());
        spawnTimer = 0;
    }

    public void update() {
        synchronized (enemies) {
            // Spawn enemies in single player or if we're the host in multiplayer
            if (!gp.isMultiplayer() || (gp.getGameClient() != null && gp.getGameClient().isHost())) {
                spawnTimer++;
                if (spawnTimer >= spawnInterval && enemies.size() < maxEnemies) {
                    spawnEnemy();
                    spawnTimer = 0;
                }
            }

            // Update enemies
            Iterator<Enemy> it = enemies.iterator();
            while (it.hasNext()) {
                Enemy enemy = it.next();
                enemy.update();
                
                // Remove enemies that are off screen
                if (enemy.isOffScreen()) {
                    it.remove();
                }
            }
        }
    }

    private void spawnEnemy() {
        int x = (int) (Math.random() * (gp.getScreenWidth() - 48));
        int type = (int) (Math.random() * level) + 1;
        synchronized (enemies) {
            enemies.add(new Enemy(gp, x, -48, type));
        }
    }

    public void draw(Graphics2D g2) {
        synchronized (enemies) {
            for (Enemy enemy : new ArrayList<>(enemies)) {
                enemy.draw(g2);
            }
        }
    }

    public List<Enemy> getEnemies() {
        synchronized (enemies) {
            return new ArrayList<>(enemies);
        }
    }

    public void removeEnemy(Enemy enemy) {
        synchronized (enemies) {
            enemies.remove(enemy);
        }
    }

    public void clearEnemies() {
        synchronized (enemies) {
            enemies.clear();
        }
    }

    public void setLevel(int level) {
        this.level = level;
        // Adjust difficulty based on level
        switch (level) {
            case 1:
                spawnInterval = 60;
                maxEnemies = 5;
                break;
            case 2:
                spawnInterval = 45;
                maxEnemies = 7;
                break;
            case 3:
                spawnInterval = 30;
                maxEnemies = 10;
                break;
        }
    }

    // Multiplayer synchronization
    public void syncEnemies(List<GameState.EnemyState> enemyStates) {
        if (enemyStates == null) return;
        
        synchronized (enemies) {
            // Clear existing enemies and recreate them from the synchronized state
            enemies.clear();
            
            for (GameState.EnemyState state : enemyStates) {
                Enemy enemy = new Enemy(gp, state.getX(), state.getY(), state.getType());
                enemies.add(enemy);
            }
        }
    }
}