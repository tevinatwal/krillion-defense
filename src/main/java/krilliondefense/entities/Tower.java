package krilliondefense.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * Base class for all towers. Towers turn to face and shoot at the
 * enemy that is furthest along the path.
 */
public abstract class Tower {
    /** Highest level a tower can be upgraded to. */
    public static final int MAX_LEVEL = 3;

    /** Center x in pixels. */
    protected int x;
    /** Center y in pixels. */
    protected int y;
    /** How far it can shoot, in pixels. */
    protected double range;
    /** Damage per shot. */
    protected int damage;
    /** Ticks between shots. */
    protected int fireRate;
    /** Upgrade level (1-3). */
    protected int level;
    /** Gold it cost to build. */
    protected int cost;

    /** Ticks until it can shoot again. */
    protected int cooldown;
    /** True while the damage boost power-up is on. */
    protected boolean boosted;
    /** Which way the barrel points. */
    protected double angle;
    /** Ticks left of the barrel kicking back after a shot. */
    protected int recoil;
    /** Total gold spent on it (used for the sell price). */
    protected int goldSpent;

    /**
     * @param x        center x in pixels
     * @param y        center y in pixels
     * @param range    range in pixels
     * @param damage   damage per shot
     * @param fireRate ticks between shots
     * @param cost     gold cost
     */
    public Tower(int x, int y, double range, int damage, int fireRate, int cost) {
        this.x = x;
        this.y = y;
        this.range = range;
        this.damage = damage;
        this.fireRate = fireRate;
        this.cost = cost;
        this.level = 1;
        this.goldSpent = cost;
        this.angle = -Math.PI / 2; // start pointing up
    }

    /**
     * Counts down and fires at a target when ready.
     *
     * @param enemies enemies on the map
     * @param shots   list to add new projectiles to
     */
    public void update(List<Enemy> enemies, List<Projectile> shots) {
        if (recoil > 0) {
            recoil--;
        }
        if (cooldown > 0) {
            cooldown--;
        }
        Enemy target = findTarget(enemies);
        if (target == null) {
            return;
        }
        angle = Math.atan2(target.getY() - y, target.getX() - x);
        if (cooldown <= 0) {
            Projectile p = makeProjectile(target, enemies);
            shots.add(p);
            cooldown = fireRate;
            recoil = 5;
        }
    }

    /**
     * Makes the projectile this tower shoots. Subclasses change it.
     *
     * @param target  enemy to shoot
     * @param enemies all enemies (for splash)
     * @return the projectile
     */
    protected Projectile makeProjectile(Enemy target, List<Enemy> enemies) {
        // start the shot at the end of the barrel
        double bx = x + Math.cos(angle) * 14;
        double by = y + Math.sin(angle) * 14;
        return new Projectile(bx, by, target, getDamage(), 0, Projectile.ARROW);
    }

    /** Level up: +25% damage, +10% range. Max level 3. */
    public void upgrade() {
        if (level >= MAX_LEVEL) {
            return;
        }
        goldSpent += getUpgradeCost();
        level++;
        damage = (int) Math.round(damage * 1.25);
        range = range * 1.1;
    }

    /**
     * Picks the enemy in range that is furthest along the path.
     *
     * @param enemies enemies on the map
     * @return the target, or null if nothing is in range
     */
    protected Enemy findTarget(List<Enemy> enemies) {
        Enemy best = null;
        for (Enemy e : enemies) {
            if (e.isDead() || e.reachedEnd()) {
                continue;
            }
            double dx = e.getX() - x;
            double dy = e.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) <= range) {
                if (best == null || e.getDistanceTraveled() > best.getDistanceTraveled()) {
                    best = e;
                }
            }
        }
        return best;
    }

    /** @return damage with the boost included */
    public int getDamage() {
        if (boosted) {
            return (int) Math.round(damage * 1.5);
        }
        return damage;
    }

    /**
     * @param boosted turn the damage boost on or off
     */
    public void setBoosted(boolean boosted) {
        this.boosted = boosted;
    }

    /** @return gold to upgrade to the next level */
    public int getUpgradeCost() {
        return cost * 4 / 5 * level;
    }

    /** @return gold you get back for selling (60% of what you paid) */
    public int getSellValue() {
        return goldSpent * 3 / 5;
    }

    /** @return the level (1-3) */
    public int getLevel() {
        return level;
    }

    /** @return the range in pixels */
    public double getRange() {
        return range;
    }

    /** @return center x */
    public int getX() {
        return x;
    }

    /** @return center y */
    public int getY() {
        return y;
    }

    /** @return the tower's name for the UI */
    public abstract String getName();

    /**
     * Draws the stone base, the colored top and the level dots.
     *
     * @param g     graphics
     * @param color main color of this tower
     */
    protected void drawBase(Graphics2D g, Color color) {
        // orange glow when boosted
        if (boosted) {
            g.setColor(new Color(255, 150, 40, 90));
            g.fillOval(x - 22, y - 22, 44, 44);
        }
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x - 16, y + 8, 32, 12);
        g.setColor(new Color(150, 145, 140));
        g.fillRoundRect(x - 16, y - 16, 32, 32, 10, 10);
        g.setColor(new Color(105, 100, 95));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x - 16, y - 16, 32, 32, 10, 10);
        g.setColor(color);
        g.fillOval(x - 11, y - 11, 22, 22);
        g.setColor(color.darker());
        g.drawOval(x - 11, y - 11, 22, 22);
        g.setStroke(new BasicStroke(1));

        // level dots along the bottom
        for (int i = 0; i < level; i++) {
            int dx = x - (level - 1) * 4 + i * 8;
            g.setColor(new Color(255, 215, 0));
            g.fillOval(dx - 3, y + 11, 6, 6);
            g.setColor(new Color(150, 110, 0));
            g.drawOval(dx - 3, y + 11, 6, 6);
        }
    }

    /**
     * Draws the barrel pointing at the current target.
     *
     * @param g      graphics
     * @param length barrel length
     * @param width  barrel width
     * @param color  barrel color
     */
    protected void drawBarrel(Graphics2D g, int length, int width, Color color) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);
        int back = recoil > 0 ? 3 : 0;
        g.setColor(color);
        g.fillRoundRect(-back, -width / 2, length, width, 4, 4);
        g.setColor(color.darker());
        g.drawRoundRect(-back, -width / 2, length, width, 4, 4);
        g.setTransform(old);
    }

    /**
     * @param g graphics to draw with
     */
    public abstract void draw(Graphics2D g);
}
