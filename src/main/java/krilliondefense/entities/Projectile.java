package krilliondefense.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * A shot that flies at an enemy and hits it. Cannon shots also hurt
 * everything near where they land (splash).
 */
public class Projectile {
    /** Kind of shot: arrow from an Arrow Tower. */
    public static final int ARROW = 0;
    /** Kind of shot: ice ball from a Frost Tower. */
    public static final int FROST = 1;
    /** Kind of shot: cannon ball (splash damage). */
    public static final int CANNON = 2;
    /** Kind of shot: magic orb from a Scholar Tower. */
    public static final int MAGIC = 3;

    /** X position in pixels. */
    private double x;
    /** Y position in pixels. */
    private double y;
    /** The enemy it is flying at. */
    private Enemy target;
    /** Damage it does when it hits. */
    private int damage;
    /** Pixels moved per tick. */
    private double speed;
    /** How long it slows the target (0 = no slow). */
    private int slowTicks;
    /** ARROW, FROST, CANNON or MAGIC. */
    private int kind;
    /** Explosion size in pixels (0 = no splash). */
    private double splashRadius;
    /** Every enemy on the map (only used for splash). */
    private List<Enemy> allEnemies;
    /** True once it is done and can be removed. */
    private boolean hit;
    /** True if it actually hit something. */
    private boolean impacted;
    /** Direction it is flying (for drawing arrows). */
    private double angle;

    /**
     * Makes a normal single target shot.
     *
     * @param x         start x
     * @param y         start y
     * @param target    enemy to hit
     * @param damage    damage to do
     * @param slowTicks how long to slow the target (0 for no slow)
     */
    public Projectile(double x, double y, Enemy target, int damage, int slowTicks) {
        this(x, y, target, damage, slowTicks, slowTicks > 0 ? FROST : ARROW);
    }

    /**
     * @param x         start x
     * @param y         start y
     * @param target    enemy to hit
     * @param damage    damage to do
     * @param slowTicks how long to slow the target (0 for no slow)
     * @param kind      ARROW, FROST, CANNON or MAGIC (changes how it looks)
     */
    public Projectile(double x, double y, Enemy target, int damage, int slowTicks, int kind) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.damage = damage;
        this.slowTicks = slowTicks;
        this.kind = kind;
        this.speed = kind == CANNON ? 5 : 8;
    }

    /**
     * Turns this into a splash shot.
     *
     * @param radius     splash size in pixels
     * @param allEnemies every enemy on the map
     */
    public void setSplash(double radius, List<Enemy> allEnemies) {
        this.splashRadius = radius;
        this.allEnemies = allEnemies;
    }

    /** Moves toward the target and does damage when it gets there. */
    public void update() {
        if (hit) {
            return;
        }
        // target already died or got to the base, so just remove this shot
        if (target.isDead() || target.reachedEnd()) {
            hit = true;
            return;
        }
        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        angle = Math.atan2(dy, dx);
        if (dist <= speed) {
            x = target.getX();
            y = target.getY();
            if (splashRadius > 0 && allEnemies != null) {
                for (Enemy e : allEnemies) {
                    double ex = e.getX() - x;
                    double ey = e.getY() - y;
                    if (Math.sqrt(ex * ex + ey * ey) <= splashRadius) {
                        e.takeDamage(damage);
                    }
                }
            } else {
                target.takeDamage(damage);
            }
            if (slowTicks > 0) {
                target.slow(slowTicks);
            }
            hit = true;
            impacted = true;
        } else {
            x += dx / dist * speed;
            y += dy / dist * speed;
        }
    }

    /** @return true once the shot is done (hit or target gone) and can be removed */
    public boolean hasHit() {
        return hit;
    }

    /** @return true if it actually hit something (not just fizzled out) */
    public boolean didImpact() {
        return impacted;
    }

    /** @return the splash radius (0 if it isn't a splash shot) */
    public double getSplashRadius() {
        return splashRadius;
    }

    /** @return the kind of shot */
    public int getKind() {
        return kind;
    }

    /** @return x in pixels */
    public double getX() {
        return x;
    }

    /** @return y in pixels */
    public double getY() {
        return y;
    }

    /**
     * @param g graphics to draw with
     */
    public void draw(Graphics2D g) {
        int ix = (int) x;
        int iy = (int) y;
        if (kind == ARROW) {
            // a short line pointing the way it's flying
            int tx = (int) (x - Math.cos(angle) * 10);
            int ty = (int) (y - Math.sin(angle) * 10);
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(90, 60, 30));
            g.drawLine(tx, ty, ix, iy);
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.LIGHT_GRAY);
            g.fillOval(ix - 2, iy - 2, 4, 4);
        } else if (kind == FROST) {
            g.setColor(new Color(150, 220, 255, 120));
            g.fillOval(ix - 6, iy - 6, 12, 12);
            g.setColor(Color.WHITE);
            g.fillOval(ix - 3, iy - 3, 6, 6);
        } else if (kind == CANNON) {
            g.setColor(new Color(40, 40, 40));
            g.fillOval(ix - 5, iy - 5, 10, 10);
            g.setColor(Color.GRAY);
            g.fillOval(ix - 3, iy - 4, 3, 3);
        } else {
            g.setColor(new Color(200, 120, 255, 130));
            g.fillOval(ix - 7, iy - 7, 14, 14);
            g.setColor(new Color(255, 230, 255));
            g.fillOval(ix - 3, iy - 3, 6, 6);
        }
    }
}
