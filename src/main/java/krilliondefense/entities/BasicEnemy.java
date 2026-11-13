package krilliondefense.entities;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Normal enemy. 50 health, normal speed, 5 gold.
 */
public class BasicEnemy extends Enemy {

    /**
     * @param x       start x
     * @param y       start y
     * @param hpScale health multiplier for later waves
     */
    public BasicEnemy(double x, double y, double hpScale) {
        super(x, y, (int) (50 * hpScale), 1.5, 5);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g) {
        drawBody(g, 11, new Color(220, 70, 70));
    }
}
