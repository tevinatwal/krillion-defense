package krilliondefense.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The grid the game is played on. 0 = grass, 1 = path, 2 = base.
 * A map has one or more lanes (paths). Each lane is a list of corner points
 * from where enemies come in to a base. Harder maps have more lanes.
 * The background only gets drawn once into an image to keep the game fast.
 */
public class GameMap {
    /** Size of one tile in pixels. */
    public static final int TILE = 40;
    /** Number of tile columns. */
    public static final int COLS = 20;
    /** Number of tile rows. */
    public static final int ROWS = 12;

    /** Grid value for grass (can build here). */
    public static final int GRASS = 0;
    /** Grid value for path. */
    public static final int PATH = 1;
    /** Grid value for a base. */
    public static final int BASE = 2;

    /** Lane corners in tile coords {col, row}. the last corner of each lane is a base. */
    private static final int[][][] EASY_LANES = {
        {{0, 1}, {16, 1}, {16, 5}, {3, 5}, {3, 9}, {19, 9}}
    };
    /** Medium map: two paths that meet at one base. */
    private static final int[][][] MEDIUM_LANES = {
        {{0, 1}, {15, 1}, {15, 3}, {5, 3}, {5, 5}, {17, 5}, {17, 6}, {19, 6}},
        {{0, 10}, {3, 10}, {3, 8}, {10, 8}, {10, 10}, {15, 10}, {15, 7}, {17, 7}, {17, 6}, {19, 6}}
    };
    /** Hard map: three paths and two bases. */
    private static final int[][][] HARD_LANES = {
        {{0, 1}, {7, 1}, {7, 4}, {13, 4}, {13, 2}, {19, 2}},
        {{0, 6}, {9, 6}, {9, 8}, {16, 8}, {16, 9}, {19, 9}},
        {{5, 11}, {5, 10}, {17, 10}, {17, 9}, {19, 9}}
    };

    /** What is on each tile (GRASS, PATH or BASE). */
    private int[][] grid = new int[ROWS][COLS];
    /** Waypoints (in pixels) for each lane. */
    private List<List<Point>> lanes = new ArrayList<>();
    /** The map picture, drawn once and reused. */
    private BufferedImage background;

    /** Builds the easy map (one path). */
    public GameMap() {
        this(EASY_LANES);
    }

    /**
     * Builds a map from lane corner lists.
     *
     * @param laneCorners for each lane, its corners in tile coords {col, row}
     */
    public GameMap(int[][]... laneCorners) {
        for (int[][] corners : laneCorners) {
            List<Point> path = new ArrayList<>();
            for (int i = 0; i < corners.length - 1; i++) {
                fillPath(corners[i][0], corners[i][1], corners[i + 1][0], corners[i + 1][1]);
            }
            for (int[] c : corners) {
                // waypoints are the center of each corner tile in pixels
                path.add(new Point(c[0] * TILE + TILE / 2, c[1] * TILE + TILE / 2));
            }
            // enemies start just off the edge of the screen
            Point start = path.get(0);
            if (corners[0][0] == 0) {
                start.x = -TILE / 2;
            } else if (corners[0][1] == 0) {
                start.y = -TILE / 2;
            } else if (corners[0][1] == ROWS - 1) {
                start.y = ROWS * TILE + TILE / 2;
            }
            int[] last = corners[corners.length - 1];
            grid[last[1]][last[0]] = BASE;
            lanes.add(path);
        }
    }

    /**
     * @param difficulty the difficulty picked on the menu
     * @return the map for that difficulty
     */
    public static GameMap forDifficulty(Difficulty difficulty) {
        switch (difficulty) {
            case MEDIUM:
                return new GameMap(MEDIUM_LANES);
            case HARD:
                return new GameMap(HARD_LANES);
            default:
                return new GameMap(EASY_LANES);
        }
    }

    /** Marks a straight line of tiles as path (doesn't cover up a base) */
    private void fillPath(int c1, int r1, int c2, int r2) {
        int dc = Integer.signum(c2 - c1);
        int dr = Integer.signum(r2 - r1);
        int c = c1;
        int r = r1;
        while (true) {
            if (grid[r][c] != BASE) {
                grid[r][c] = PATH;
            }
            if (c == c2 && r == r2) {
                break;
            }
            c += dc;
            r += dr;
        }
    }

    /**
     * @param col column
     * @param row row
     * @return true if a tower can go here
     */
    public boolean isBuildable(int col, int row) {
        if (col < 0 || row < 0 || col >= COLS || row >= ROWS) {
            return false;
        }
        return grid[row][col] == GRASS;
    }

    /**
     * @param i index of the waypoint
     * @return the waypoint in pixels on the first lane
     */
    public Point getWaypoint(int i) {
        return getWaypoint(0, i);
    }

    /**
     * @param lane which lane
     * @param i    index of the waypoint
     * @return the waypoint in pixels
     */
    public Point getWaypoint(int lane, int i) {
        return lanes.get(lane).get(i);
    }

    /** @return how many waypoints the first lane has */
    public int getPathLength() {
        return getPathLength(0);
    }

    /**
     * @param lane which lane
     * @return how many waypoints that lane has
     */
    public int getPathLength(int lane) {
        return lanes.get(lane).size();
    }

    /** @return how many lanes (paths) this map has */
    public int getLaneCount() {
        return lanes.size();
    }

    /**
     * Draws the map.
     *
     * @param g graphics to draw with
     */
    public void draw(Graphics2D g) {
        g.drawImage(getBackground(), 0, 0, null);
    }

    /**
     * Draws the map stretched into a box (used for the menu background and previews).
     *
     * @param g      graphics to draw with
     * @param x      left
     * @param y      top
     * @param width  width to stretch to
     * @param height height to stretch to
     */
    public void drawScaled(Graphics2D g, int x, int y, int width, int height) {
        g.drawImage(getBackground(), x, y, width, height, null);
    }

    /** Gets the map picture, making it the first time it is needed. */
    private BufferedImage getBackground() {
        if (background == null) {
            background = makeBackground();
        }
        return background;
    }

    /** Draws grass, paths and bases into an image one time. */
    private BufferedImage makeBackground() {
        BufferedImage img = new BufferedImage(COLS * TILE, ROWS * TILE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Random rand = new Random(42); // same seed so the map looks the same every time

        // grass in a light/dark checker pattern
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                g.setColor((r + c) % 2 == 0 ? new Color(118, 178, 82) : new Color(110, 170, 76));
                g.fillRect(c * TILE, r * TILE, TILE, TILE);
            }
        }
        // little grass tufts and flowers
        for (int i = 0; i < 260; i++) {
            int x = rand.nextInt(COLS * TILE);
            int y = rand.nextInt(ROWS * TILE);
            if (grid[y / TILE][x / TILE] != GRASS) {
                continue;
            }
            if (i % 9 == 0) {
                g.setColor(i % 2 == 0 ? new Color(250, 240, 120) : Color.WHITE);
                g.fillOval(x, y, 4, 4);
            } else {
                g.setColor(new Color(85, 140, 60));
                g.drawLine(x, y, x - 2, y - 5);
                g.drawLine(x, y, x + 2, y - 5);
            }
        }
        // a few rocks
        for (int i = 0; i < 14; i++) {
            int c = rand.nextInt(COLS);
            int r = rand.nextInt(ROWS);
            if (grid[r][c] == GRASS) {
                int x = c * TILE + 8 + rand.nextInt(20);
                int y = r * TILE + 8 + rand.nextInt(20);
                g.setColor(new Color(130, 130, 125));
                g.fillOval(x, y, 10, 7);
                g.setColor(new Color(165, 165, 160));
                g.fillOval(x + 2, y + 1, 5, 3);
            }
        }

        // each path is a thick line through its waypoints. the dark edge goes
        // under every lane first so lanes that join look like one road
        for (int pass = 0; pass < 2; pass++) {
            for (List<Point> lane : lanes) {
                int[] xs = new int[lane.size()];
                int[] ys = new int[lane.size()];
                for (int i = 0; i < lane.size(); i++) {
                    xs[i] = lane.get(i).x;
                    ys[i] = lane.get(i).y;
                }
                if (pass == 0) {
                    g.setStroke(new BasicStroke(38, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
                    g.setColor(new Color(150, 115, 70));
                } else {
                    g.setStroke(new BasicStroke(32, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
                    g.setColor(new Color(214, 184, 128));
                }
                g.drawPolyline(xs, ys, xs.length);
            }
        }
        g.setStroke(new BasicStroke(1));
        // pebbles on the path
        for (int i = 0; i < 150; i++) {
            int x = rand.nextInt(COLS * TILE);
            int y = rand.nextInt(ROWS * TILE);
            if (grid[y / TILE][x / TILE] == PATH) {
                g.setColor(rand.nextBoolean() ? new Color(190, 160, 110) : new Color(230, 205, 155));
                g.fillOval(x, y, 3, 3);
            }
        }

        // red arrows where enemies come in, castles at the bases
        for (List<Point> lane : lanes) {
            drawSpawnArrow(g, lane.get(0), lane.get(1));
        }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == BASE) {
                    drawCastle(g, new Point(c * TILE + TILE / 2, r * TILE + TILE / 2));
                }
            }
        }
        g.dispose();
        return img;
    }

    /** Arrow on the edge of the map pointing the way enemies walk in. */
    private void drawSpawnArrow(Graphics2D g, Point start, Point next) {
        double angle = Math.atan2(next.y - start.y, next.x - start.x);
        // put it just inside the screen
        int x = Math.max(12, Math.min(COLS * TILE - 12, start.x));
        int y = Math.max(12, Math.min(ROWS * TILE - 12, start.y));
        int[] px = new int[3];
        int[] py = new int[3];
        double[] a = {angle, angle + 2.5, angle - 2.5};
        double[] len = {11, 9, 9};
        for (int i = 0; i < 3; i++) {
            px[i] = (int) (x + Math.cos(a[i]) * len[i]);
            py[i] = (int) (y + Math.sin(a[i]) * len[i]);
        }
        g.setColor(new Color(220, 60, 50));
        g.fillPolygon(px, py, 3);
        g.setColor(new Color(120, 20, 20));
        g.drawPolygon(px, py, 3);
    }

    /** Simple castle for a base. */
    private void drawCastle(Graphics2D g, Point p) {
        int x = p.x - 18;
        int y = p.y - 16;
        g.setColor(new Color(120, 120, 130));
        g.fillRect(x, y, 36, 32);
        // battlements on top
        for (int i = 0; i < 4; i++) {
            g.fillRect(x + i * 10, y - 6, 6, 6);
        }
        g.setColor(new Color(90, 90, 100));
        g.drawRect(x, y, 36, 32);
        // door
        g.setColor(new Color(80, 50, 30));
        g.fillRoundRect(x + 12, y + 14, 12, 18, 8, 8);
        // flag
        g.setColor(new Color(60, 60, 60));
        g.drawLine(x + 18, y - 6, x + 18, y - 20);
        g.setColor(new Color(60, 110, 230));
        g.fillPolygon(new int[] {x + 18, x + 30, x + 18}, new int[] {y - 20, y - 16, y - 12}, 3);
    }
}
