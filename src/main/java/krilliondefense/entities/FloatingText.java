package krilliondefense.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Text that floats up and fades out, like "+5g" when an enemy dies.
 */
public class FloatingText {
    /** How many ticks the text stays on screen. */
    private static final int LIFE = 50;
    /** Font for the text. */
    private static final Font FONT = new Font("SansSerif", Font.BOLD, 14);

    /** What it says. */
    private String text;
    /** X position. */
    private double x;
    /** Y position (goes up over time). */
    private double y;
    /** Text color. */
    private Color color;
    /** Ticks since it was made. */
    private int age;

    /**
     * @param text  what to show
     * @param x     start x
     * @param y     start y
     * @param color text color
     */
    public FloatingText(String text, double x, double y, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    /**
     * Moves it up a little.
     *
     * @return false when it's done and can be removed
     */
    public boolean update() {
        age++;
        y -= 0.7;
        return age < LIFE;
    }

    /**
     * @param g graphics to draw with
     */
    public void draw(Graphics2D g) {
        int alpha = 255 - age * 255 / LIFE;
        g.setFont(FONT);
        int w = g.getFontMetrics().stringWidth(text);
        g.setColor(new Color(0, 0, 0, alpha / 2));
        g.drawString(text, (int) x - w / 2 + 1, (int) y + 1);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.drawString(text, (int) x - w / 2, (int) y);
    }
}
