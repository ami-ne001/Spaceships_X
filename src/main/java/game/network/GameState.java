package game.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Player state
    private String playerId;
    private int x;
    private int y;
    private int lives;
    private int score;
    
    // Game state
    private List<EnemyState> enemies;
    private List<ProjectileState> projectiles;
    private int level;
    
    public GameState(String playerId) {
        this.playerId = playerId;
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
    }
    
    // Player state setters and getters
    public void setPlayerPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getPlayerX() { return x; }
    public int getPlayerY() { return y; }
    public String getPlayerId() { return playerId; }
    public void setLives(int lives) { this.lives = lives; }
    public int getLives() { return lives; }
    public void setScore(int score) { this.score = score; }
    public int getScore() { return score; }
    
    // Game state management
    public void setEnemies(List<EnemyState> enemies) {
        this.enemies = enemies;
    }
    
    public List<EnemyState> getEnemies() {
        return enemies;
    }
    
    public void setProjectiles(List<ProjectileState> projectiles) {
        this.projectiles = projectiles;
    }
    
    public List<ProjectileState> getProjectiles() {
        return projectiles;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
    
    // Inner classes for enemy and projectile states
    public static class EnemyState implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x;
        private int y;
        private int type;
        private int health;
        
        public EnemyState(int x, int y, int type, int health) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.health = health;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getType() { return type; }
        public int getHealth() { return health; }
    }
    
    public static class ProjectileState implements Serializable {
        private static final long serialVersionUID = 1L;
        private int x;
        private int y;
        private boolean isPlayerProjectile;
        private String shooterId;
        
        public ProjectileState(int x, int y, boolean isPlayerProjectile, String shooterId) {
            this.x = x;
            this.y = y;
            this.isPlayerProjectile = isPlayerProjectile;
            this.shooterId = shooterId;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public boolean isPlayerProjectile() { return isPlayerProjectile; }
        public String getShooterId() { return shooterId; }
    }
} 