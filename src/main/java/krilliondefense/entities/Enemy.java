package krilliondefense.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import krilliondefense.game.GameMap;

/**
 * Something that walks along the path toward the base.
 */
public abstract class Enemy {
    /** X position in pixels. */
    protected double x;
    /** Y position in pixels. */
    protected double y;
    /** Health left. */
    protected int health;
    /** Health it started with (for the health bar). */
    protected int maxHealth;
    /** Pixels moved per tick. */
    protected double speed;
    /** Gold the player gets for killing it. */
    protected int reward;
    /** Index of the last waypoint it passed. */
    protected int waypointIndex;
    /** Which path it follows (harder maps have more than one). */
    protected int lane;
    /** How many lives you lose if it reaches a base. */
    protected int livesCost = 1;

    /** Ticks left of the Frost Tower slow. */
    protected int slowTicks;
    /** Ticks left of the Freeze power-up. */
    protected int freezeTicks;
    /** How far it has walked (towers aim at the one furthest along). */
    protected double distanceTraveled;
    /** True once it reaches a base. */
    protected boolean atEnd;
    /** Direction it is walking in x (for drawing the eyes). */
    protected double dirX = 1;
    /** Direction it is walking in y. */
    protected double dirY = 0;
    /** Ticks left of the white flash after getting hit. */
    protected int hitFlash;
    /** True if the health bar should show even at full health (the boss). */
    protected boolean alwaysShowHealth;

    /**
     * @param x      start x in pixels
     * @param y      start y in pixels
     * @param health starting health
     * @param speed  pixels per tick
     * @param reward gold given when killed
     */
    public Enemy(double x, double y, int health, double speed, int reward) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        this.speed = speed;
        this.reward = reward;
        this.waypointIndex = 0;
    }

    /**
     * Moves toward the next waypoint. Frozen enemies don't move and
     * slowed enemies move 40% slower.
     *
     * @param map the map with the waypoints
     */
    public void move(GameMap map) {
        if (hitFlash > 0) {
            hitFlash--;
        }
        if (freezeTicks > 0) {
            freezeTicks--;
            return;
        }
        double moveLeft = speed;
        if (slowTicks > 0) {
            slowTicks--;
            moveLeft = speed * 0.6;
        }

        // loop in case we pass a corner in the middle of a step
        while (moveLeft > 0 && !atEnd) {
            if (waypointIndex + 1 >= map.getPathLength(lane)) {
                atEnd = true;
                break;
            }
            Point next = map.getWaypoint(lane, waypointIndex + 1);
            double dx = next.x - x;
            double dy = next.y - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                dirX = dx / dist;
                dirY = dy / dist;
            }
            if (dist <= moveLeft) {
                x = next.x;
                y = next.y;
                moveLeft -= dist;
                distanceTraveled += dist;
                waypointIndex++;
            } else {
                x += dx / dist * moveLeft;
                y += dy / dist * moveLeft;
                distanceTraveled += moveLeft;
                moveLeft = 0;
            }
        }
    }

    /**
     * @param amount damage to take
     */
    public void takeDamage(int amount) {
        health -= amount;
        hitFlash = 4;
    }

    /** @return true if health is 0 or less */
    public boolean isDead() {
        return health <= 0;
    }

    /** @return true if it made it to the base */
    public boolean reachedEnd() {
        return atEnd;
    }

    /**
     * @param ticks how long to slow for
     */
    public void slow(int ticks) {
        slowTicks = Math.max(slowTicks, ticks);
    }

    /**
     * @param ticks how long to freeze for
     */
    public void freeze(int ticks) {
        freezeTicks = Math.max(freezeTicks, ticks);
    }

    /**
     * Puts the enemy on a lane and moves it to that lane's start.
     *
     * @param lane which lane
     * @param map  the map
     */
    public void setLane(int lane, GameMap map) {
        this.lane = lane;
        Point start = map.getWaypoint(lane, 0);
        x = start.x;
        y = start.y;
    }

    /** @return true if frozen by the power-up */
    public boolean isFrozen() {
        return freezeTicks > 0;
    }

    /** @return how far along the path this enemy is (used for targeting) */
    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    /** @return x in pixels */
    public double getX() {
        return x;
    }

    /** @return y in pixels */
    public double getY() {
        return y;
    }

    /** @return current health */
    public int getHealth() {
        return health;
    }

    /** @return gold for killing it */
    public int getReward() {
        return reward;
    }

    /** @return lives lost if this enemy reaches the base */
    public int getLivesCost() {
        return livesCost;
    }

    /**
     * Draws a round body with a shadow, eyes and a health bar.
     * All the enemies use this, they just pick a size and color.
     *
     * @param g      graphics
     * @param radius size of the body
     * @param color  body color
     */
    protected void drawBody(Graphics2D g, int radius, Color color) {
        int ix = (int) x;
        int iy = (int) y;
        // shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(ix - radius, iy + radius - 4, radius * 2, 8);

        Color body = color;
        if (hitFlash > 0) {
            body = Color.WHITE;
        } else if (freezeTicks > 0) {
            body = new Color(170, 225, 255);
        }
        g.setColor(body);
        g.fillOval(ix - radius, iy - radius, radius * 2, radius * 2);
        // lighter spot so it looks a bit round
        g.setColor(new Color(255, 255, 255, 70));
        g.fillOval(ix - radius / 2, iy - radius + 2, radius, radius / 2 + 2);
        g.setColor(body.darker().darker());
        g.setStroke(new BasicStroke(2));
        g.drawOval(ix - radius, iy - radius, radius * 2, radius * 2);
        g.setStroke(new BasicStroke(1));

        // eyes look the way it's walking
        int ex = (int) (dirX * radius * 0.35);
        int ey = (int) (dirY * radius * 0.35);
        int eyeR = Math.max(3, radius / 3);
        int gap = radius / 3 + 1;
        g.setColor(Color.WHITE);
        g.fillOval(ix + ex - gap - eyeR / 2, iy + ey - eyeR, eyeR, eyeR + 1);
        g.fillOval(ix + ex + gap - eyeR / 2, iy + ey - eyeR, eyeR, eyeR + 1);
        g.setColor(Color.BLACK);
        g.fillOval(ix + ex - gap - 1 + (int) dirX, iy + ey - eyeR / 2 - 1 + (int) dirY, 3, 3);
        g.fillOval(ix + ex + gap - 1 + (int) dirX, iy + ey - eyeR / 2 - 1 + (int) dirY, 3, 3);

        // blue ring when slowed by frost
        if (slowTicks > 0 && freezeTicks == 0) {
            g.setColor(new Color(120, 200, 255));
            g.setStroke(new BasicStroke(2));
            g.drawOval(ix - radius - 3, iy - radius - 3, radius * 2 + 6, radius * 2 + 6);
            g.setStroke(new BasicStroke(1));
        }
        drawHealthBar(g, radius);
    }

    /**
     * Draws a health bar above the enemy (green -> yellow -> red).
     *
     * @param g      graphics
     * @param radius size of the enemy
     */
    protected void drawHealthBar(Graphics2D g, int radius) {
        if (health >= maxHealth && !alwaysShowHealth) {
            return; // don't show it until it gets hurt
        }
        int w = Math.max(20, radius * 2);
        int bx = (int) x - w / 2;
        int by = (int) y - radius - 9;
        double pct = Math.max(0, (double) health / maxHealth);
        g.setColor(new Color(40, 40, 40));
        g.fillRect(bx - 1, by - 1, w + 2, 6);
        if (pct > 0.6) {
            g.setColor(new Color(80, 220, 80));
        } else if (pct > 0.3) {
            g.setColor(new Color(240, 200, 50));
        } else {
            g.setColor(new Color(230, 60, 50));
        }
        g.fillRect(bx, by, (int) (w * pct), 4);
    }

    /**
     * @param g graphics to draw with
     */
    public abstract void draw(Graphics2D g);
}
