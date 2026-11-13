package krilliondefense.entities;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * The boss at the end of the last wave. 1000 health, half speed, 100 gold,
 * and it takes 5 lives if it reaches the base.
 */
public class BossEnemy extends Enemy {

    /**
     * @param x       start x
     * @param y       start y
     * @param hpScale health multiplier for later waves
     */
    public BossEnemy(double x, double y, double hpScale) {
        super(x, y, (int) (1000 * hpScale), 0.6, 100);
        livesCost = 5;
        alwaysShowHealth = true; // so you can see how strong it is
    }

    /**
     * The boss doesn't freeze as long as everyone else.
     *
     * @param ticks how long to freeze for
     */
    @Override
    public void freeze(int ticks) {
        super.freeze(ticks / 2);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g) {
        drawBody(g, 20, new Color(140, 60, 170));
        // crown
        int cx = (int) x;
        int cy = (int) y - 20;
        g.setColor(new Color(255, 210, 40));
        g.fillPolygon(new int[] {cx - 10, cx - 10, cx - 5, cx, cx + 5, cx + 10, cx + 10},
                new int[] {cy + 2, cy - 8, cy - 3, cy - 10, cy - 3, cy - 8, cy + 2}, 7);
    }
}
