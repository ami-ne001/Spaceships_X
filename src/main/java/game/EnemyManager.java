package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class EnemyManager {
    private GamePanel gp;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private Random random = new Random();
    private int spawnCooldown = 0;
    private int spawnCooldownMax = 60;
    private int level = 1;

    public EnemyManager(GamePanel gp) {
        this.gp = gp;
    }

    public void setLevel(int newLevel) {
        this.level = newLevel;
        this.spawnCooldownMax = Math.max(20, 60 - level * 5);
    }

    public void update() {
        // Spawn new enemies
        if (spawnCooldown > 0) {
            spawnCooldown--;
        } else if (random.nextInt(100) < 15) {  // 15% de chance de spawn
            spawnEnemy();
            spawnCooldown = spawnCooldownMax;
        }

        // Update all enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.update();

            // Remove if off-screen
            if (enemy.isOffScreen()) {
                enemies.remove(i);
                i--;
            }
        }
    }

    private void spawnEnemy() {
        int x = random.nextInt(gp.getScreenWidth() - 48);
        int y = -48;

        // Determine enemy type based on level
        int enemyType;
        int rand = random.nextInt(100);

        if (level == 1) {
            enemyType = 1;  // Seulement type 1 pour niveau 1
        } else if (level == 2) {
            enemyType = random.nextBoolean() ? 1 : 2;  // Types 1 et 2 pour niveau 2
        } else {
            // Tous les types pour niveau 3
            rand = random.nextInt(3);
            enemyType = rand + 1;
        }

        enemies.add(new Enemy(gp, x, y, enemyType));
    }

    public void draw(Graphics2D g2) {
        for (Enemy enemy : enemies) {
            enemy.draw(g2);
        }
    }

    public void clearEnemies() {
        enemies.clear();
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
    }

    public boolean allEnemiesDefeated() {
        return enemies.isEmpty();
    }

    public void increaseDifficulty(int newLevel) {
        level = newLevel;
        spawnCooldownMax = Math.max(20, 60 - level * 5);
    }
}