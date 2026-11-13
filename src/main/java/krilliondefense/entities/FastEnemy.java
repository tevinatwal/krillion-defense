package krilliondefense.entities;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Fast enemy. 30 health, 2x speed, 7 gold. Shows up from wave 3.
 */
public class FastEnemy extends Enemy {

    /**
     * @param x       start x
     * @param y       start y
     * @param hpScale health multiplier for later waves
     */
    public FastEnemy(double x, double y, double hpScale) {
        super(x, y, (int) (30 * hpScale), 3.0, 7);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g) {
        // speed lines behind it
        if (freezeTicks == 0) {
            g.setColor(new Color(255, 255, 255, 120));
            for (int i = -1; i <= 1; i++) {
                int sx = (int) (x - dirX * 12 + dirY * i * 4);
                int sy = (int) (y - dirY * 12 - dirX * i * 4);
                g.drawLine(sx, sy, (int) (sx - dirX * 8), (int) (sy - dirY * 8));
            }
        }
        drawBody(g, 8, new Color(255, 160, 40));
    }
}
