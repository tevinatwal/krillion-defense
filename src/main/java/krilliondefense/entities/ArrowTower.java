package krilliondefense.entities;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Basic tower. Shoots fast at one enemy.
 */
public class ArrowTower extends Tower {
    /** Gold it costs to build. */
    public static final int COST = 50;

    /**
     * @param x center x
     * @param y center y
     */
    public ArrowTower(int x, int y) {
        super(x, y, 100, 10, 30, COST);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Arrow Tower";
    }

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(170, 120, 70));
        drawBarrel(g, 18, 5, new Color(90, 60, 35));
    }
}
