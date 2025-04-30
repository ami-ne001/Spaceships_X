package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ProjectileManager {
    private GamePanel gp;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private ArrayList<Explosion> explosions = new ArrayList<>();

    public ProjectileManager(GamePanel gp) {
        this.gp = gp;
    }

    public void update() {
        // Update projectiles
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile projectile = projectiles.get(i);
            projectile.update();

            if (!projectile.isActive()) {
                projectiles.remove(i);
                i--;
            }
        }

        // Update explosions
        for (int i = 0; i < explosions.size(); i++) {
            Explosion explosion = explosions.get(i);
            explosion.update();

            if (explosion.isFinished()) {
                explosions.remove(i);
                i--;
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
        projectiles.add(new Projectile(gp, x, y, true));
    }

    public void addEnemyProjectile(int x, int y) {
        projectiles.add(new Projectile(gp, x, y, false));
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

    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

    public ArrayList<Explosion> getExplosions() {
        return explosions;
    }


}