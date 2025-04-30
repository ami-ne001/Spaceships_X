package game;

public class CollisionChecker {
    private GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkCollisions() {
        Player player = gp.getPlayer();
        EnemyManager enemyManager = gp.getEnemyManager();
        ProjectileManager projectileManager = gp.getProjectileManager();

        // Check player-projectile collisions with enemies
        for (Projectile projectile : projectileManager.getProjectiles()) {
            if (projectile.isPlayerProjectile()) {
                for (Enemy enemy : enemyManager.getEnemies()) {
                    if (projectile.getHitbox().intersects(enemy.getHitbox())) {
                        enemy.takeDamage();
                        projectile.setActive(false);
                        break;
                    }
                }
            }
        }

        // Check enemy-projectile collisions with player
        for (Projectile projectile : projectileManager.getProjectiles()) {
            if (!projectile.isPlayerProjectile() && projectile.getHitbox().intersects(player.getHitbox())) {
                player.takeDamage();
                projectile.setActive(false);
                gp.decreasePlayerLives();
                break;
            }
        }

        // Check player-enemy collisions
        for (Enemy enemy : enemyManager.getEnemies()) {
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