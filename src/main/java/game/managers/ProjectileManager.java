package game.managers;

import game.GamePanel;
import game.entities.Explosion;
import game.entities.Projectile;
import game.network.GameState;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProjectileManager {
    private GamePanel gp;
    private final CopyOnWriteArrayList<Projectile> projectiles;
    private final CopyOnWriteArrayList<Explosion> explosions;
    private final Map<String, Long> lastProjectileUpdateTime;
    private static final int SYNC_THRESHOLD = 100; // Distance threshold for syncing projectiles

    public ProjectileManager(GamePanel gp) {
        this.gp = gp;
        projectiles = new CopyOnWriteArrayList<>();
        explosions = new CopyOnWriteArrayList<>();
        lastProjectileUpdateTime = new ConcurrentHashMap<>();
    }

    public void update() {
        // Update and remove inactive projectiles
        for (Projectile projectile : projectiles) {
            projectile.update();
            if (!projectile.isActive()) {
                projectiles.remove(projectile);
            }
        }

        // Update and remove finished explosions
        for (Explosion explosion : explosions) {
            explosion.update();
            if (explosion.isFinished()) {
                explosions.remove(explosion);
            }
        }
    }

    public void draw(Graphics2D g2) {
        // Draw projectiles
        for (Projectile projectile : projectiles) {
            projectile.draw(g2);
        }

        // Draw explosions
        for (Explosion explosion : explosions) {
            explosion.draw(g2);
        }
    }

    public void addPlayerProjectile(int x, int y) {
        if (!gp.isMultiplayer() || gp.getGameClient() == null) {
        projectiles.add(new Projectile(gp, x, y, true));
            return;
        }

        Projectile projectile = new Projectile(gp, x, y, true);
        projectile.setShooterId(gp.getGameClient().getClientId());
        projectiles.add(projectile);
        System.out.println("Added player projectile with ID: " + projectile.getShooterId());
    }

    public void addEnemyProjectile(int x, int y) {
        if (!gp.isMultiplayer() || gp.getGameClient() == null) {
        projectiles.add(new Projectile(gp, x, y, false));
            return;
        }

        // In multiplayer, only the host creates enemy projectiles
        if (gp.getGameClient().isHost()) {
            Projectile projectile = new Projectile(gp, x, y, false);
            projectile.setShooterId("enemy_" + gp.getGameClient().getClientId());
            projectiles.add(projectile);
            System.out.println("Host added enemy projectile");
        }
    }

    public void addExplosion(int x, int y) {
        explosions.add(new Explosion(gp, x, y));
    }

    public void clearProjectiles() {
        projectiles.clear();
    }

    public void clearExplosions() {
        explosions.clear();
    }

    public List<Projectile> getProjectiles() {
        return new ArrayList<>(projectiles);
    }

    // Improved multiplayer synchronization
    public void syncProjectiles(List<GameState.ProjectileState> projectileStates, String clientId) {
        if (projectileStates == null) return;
        
        long currentTime = System.currentTimeMillis();
        System.out.println("Syncing " + projectileStates.size() + " projectiles");

        // Create a list of projectiles to add
        List<Projectile> newProjectiles = new ArrayList<>();
        
        // Process each projectile state
        for (GameState.ProjectileState state : projectileStates) {
            String shooterId = state.getShooterId();
            
            // Skip our own projectiles
            if (clientId.equals(shooterId)) {
                continue;
    }

            // Handle enemy projectiles
            boolean isEnemyProjectile = shooterId.startsWith("enemy_");
            if (isEnemyProjectile && !gp.getGameClient().isHost()) {
                System.out.println("Processing enemy projectile from host");
            } else if (!isEnemyProjectile) {
                System.out.println("Processing player projectile from: " + shooterId);
            }

            // Find existing projectile or prepare to create new one
            boolean found = false;
            for (Projectile projectile : projectiles) {
                if (shooterId.equals(projectile.getShooterId()) &&
                    Math.abs(projectile.getX() - state.getX()) < SYNC_THRESHOLD &&
                    Math.abs(projectile.getY() - state.getY()) < SYNC_THRESHOLD) {
                    
                    // Update existing projectile with smoother interpolation
                    int deltaX = state.getX() - projectile.getX();
                    int deltaY = state.getY() - projectile.getY();
                    projectile.setPosition(
                        projectile.getX() + (deltaX / 3),
                        projectile.getY() + (deltaY / 3)
                    );
                    found = true;
                    break;
                }
            }

            // Create new projectile if not found
            if (!found) {
                Projectile newProjectile = new Projectile(gp, state.getX(), state.getY(), state.isPlayerProjectile());
                newProjectile.setShooterId(shooterId);
                newProjectiles.add(newProjectile);
                System.out.println("Created new " + (state.isPlayerProjectile() ? "player" : "enemy") + 
                                 " projectile for shooter: " + shooterId);
            }
        }
        
        // Add all new projectiles at once
        projectiles.addAll(newProjectiles);

        // Clean up inactive projectiles
        projectiles.removeIf(p -> {
            boolean shouldRemove = !p.isActive() && 
                                 (p.getY() < -p.getHeight() * 2 || p.getY() > gp.getScreenHeight() + p.getHeight() * 2);
            if (shouldRemove) {
                System.out.println("Removing inactive " + (p.isPlayerProjectile() ? "player" : "enemy") + 
                                 " projectile from shooter: " + p.getShooterId());
            }
            return shouldRemove;
        });
    }
}