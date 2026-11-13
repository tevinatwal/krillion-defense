package krilliondefense.entities;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Slow but tough. 200 health, half speed, 15 gold. Shows up from wave 4.
 */
public class TankEnemy extends Enemy {

    /**
     * @param x       start x
     * @param y       start y
     * @param hpScale health multiplier for later waves
     */
    public TankEnemy(double x, double y, double hpScale) {
        super(x, y, (int) (200 * hpScale), 0.75, 15);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g) {
        drawBody(g, 15, new Color(90, 130, 90));
        // armor plate on its head
        if (freezeTicks == 0) {
            g.setColor(new Color(150, 150, 160));
            g.fillRect((int) x - 9, (int) y - 15, 18, 5);
        }
    }
}
