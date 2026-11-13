package krilliondefense.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Low damage but slows enemies by 40% for 2 seconds.
 */
public class FrostTower extends Tower {
    /** Gold it costs to build. */
    public static final int COST = 75;
    /** How long a hit slows an enemy (120 ticks = 2 seconds). */
    private static final int SLOW_TICKS = 120;

    /**
     * @param x center x
     * @param y center y
     */
    public FrostTower(int x, int y) {
        super(x, y, 90, 4, 40, COST);
    }

    /** {@inheritDoc} */
    @Override
    protected Projectile makeProjectile(Enemy target, List<Enemy> enemies) {
        return new Projectile(x, y, target, getDamage(), SLOW_TICKS, Projectile.FROST);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Frost Tower";
    }

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(130, 200, 250));
        drawBarrel(g, 14, 6, new Color(220, 245, 255));
        // ice crystal on top
        g.setColor(Color.WHITE);
        g.fillPolygon(new int[] {x, x + 5, x, x - 5}, new int[] {y - 7, y, y + 7, y}, 4);
    }
}
