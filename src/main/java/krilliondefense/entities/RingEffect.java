package krilliondefense.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A circle that grows and fades. Used for cannon explosions and the Knowledge Blast.
 */
public class RingEffect {
    /** Center x. */
    private double x;
    /** Center y. */
    private double y;
    /** How big the ring gets. */
    private double maxRadius;
    /** Ring color. */
    private Color color;
    /** Ticks since it was made. */
    private int age;
    /** How many ticks it lasts. */
    private int life;

    /**
     * @param x         center x
     * @param y         center y
     * @param maxRadius how big it gets
     * @param color     ring color
     * @param life      how many ticks it lasts
     */
    public RingEffect(double x, double y, double maxRadius, Color color, int life) {
        this.x = x;
        this.y = y;
        this.maxRadius = maxRadius;
        this.color = color;
        this.life = life;
    }

    /**
     * @return false when it's done and can be removed
     */
    public boolean update() {
        age++;
        return age < life;
    }

    /**
     * @param g graphics to draw with
     */
    public void draw(Graphics2D g) {
        double t = (double) age / life;
        int r = (int) (maxRadius * (0.3 + 0.7 * t));
        int alpha = (int) (200 * (1 - t));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha / 3));
        g.fillOval((int) x - r, (int) y - r, r * 2, r * 2);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.setStroke(new BasicStroke(3));
        g.drawOval((int) x - r, (int) y - r, r * 2, r * 2);
        g.setStroke(new BasicStroke(1));
    }
}
