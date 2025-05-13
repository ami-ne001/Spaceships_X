package game;

import java.util.List;

public class CollisionChecker {
    private GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkCollisions() {
        Player player = gp.getPlayer();
        EnemyManager enemyManager = gp.getEnemyManager();
        ProjectileManager projectileManager = gp.getProjectileManager();
        
        // Get thread-safe copies of collections
        List<Projectile> projectiles = projectileManager.getProjectiles();
        List<Enemy> enemies = enemyManager.getEnemies();

        // Check player-projectile collisions with enemies
        for (Projectile projectile : projectiles) {
            if (projectile.isPlayerProjectile()) {
                for (Enemy enemy : enemies) {
                    if (projectile.getHitbox().intersects(enemy.getHitbox())) {
                        enemy.takeDamage();
                        projectile.setActive(false);
                        break;
                    }
                }
            }
        }

        // Check enemy-projectile collisions with player
        for (Projectile projectile : projectiles) {
            if (!projectile.isPlayerProjectile() && projectile.getHitbox().intersects(player.getHitbox())) {
                player.takeDamage();
                projectile.setActive(false);
                gp.decreasePlayerLives();
                break;
            }
        }

        // Check player-enemy collisions
        for (Enemy enemy : enemies) {
            if (enemy.getHitbox().intersects(player.getHitbox())) {
                player.takeDamage();
                enemyManager.removeEnemy(enemy);
                gp.decreasePlayerLives();
                projectileManager.addExplosion(enemy.getX(), enemy.getY());
                break;
            }
        }
    }
}