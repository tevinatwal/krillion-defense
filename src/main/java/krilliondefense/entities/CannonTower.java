package krilliondefense.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Slow, but its shots explode and hurt every enemy close by.
 */
public class CannonTower extends Tower {
    /** Gold it costs to build. */
    public static final int COST = 100;
    /** How far the explosion reaches, in pixels. */
    private static final double SPLASH = 45;

    /**
     * @param x center x
     * @param y center y
     */
    public CannonTower(int x, int y) {
        super(x, y, 95, 22, 75, COST);
    }

    /** {@inheritDoc} */
    @Override
    protected Projectile makeProjectile(Enemy target, List<Enemy> enemies) {
        double bx = x + Math.cos(angle) * 16;
        double by = y + Math.sin(angle) * 16;
        Projectile p = new Projectile(bx, by, target, getDamage(), 0, Projectile.CANNON);
        p.setSplash(SPLASH, enemies);
        return p;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Cannon Tower";
    }

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(90, 90, 100));
        drawBarrel(g, 20, 10, new Color(40, 40, 45));
    }
}
