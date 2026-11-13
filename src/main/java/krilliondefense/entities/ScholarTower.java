package krilliondefense.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import krilliondefense.game.GameState;

/**
 * Unlocked by a Legendary answer. Damage = 10 + knowledgePoints / 20,
 * so it gets stronger the more obscure stuff you know.
 */
public class ScholarTower extends Tower {
    /** Gold it costs to build (after it is unlocked). */
    public static final int COST = 100;
    /** Game state, used to read knowledge points. */
    private GameState state;

    /**
     * @param x     center x
     * @param y     center y
     * @param state game state (to read knowledge points)
     */
    public ScholarTower(int x, int y, GameState state) {
        super(x, y, 120, 10, 30, COST);
        this.state = state;
    }

    /** Updates damage from knowledge points then shoots like normal. */
    @Override
    public void update(List<Enemy> enemies, List<Projectile> shots) {
        // base damage grows with knowledge points, then upgrades add on top
        int base = 10 + state.getKnowledgePoints() / 20;
        damage = (int) Math.round(base * Math.pow(1.25, level - 1));
        super.update(enemies, shots);
    }

    /** {@inheritDoc} */
    @Override
    protected Projectile makeProjectile(Enemy target, List<Enemy> enemies) {
        return new Projectile(x, y, target, getDamage(), 0, Projectile.MAGIC);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Scholar Tower";
    }

    /** {@inheritDoc} */
    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(160, 100, 220));
        // little open book instead of a barrel
        g.setColor(Color.WHITE);
        g.fillRect(x - 8, y - 5, 7, 9);
        g.fillRect(x + 1, y - 5, 7, 9);
        g.setColor(new Color(90, 40, 140));
        g.drawLine(x, y - 5, x, y + 4);
        g.drawRect(x - 8, y - 5, 16, 9);
    }
}
