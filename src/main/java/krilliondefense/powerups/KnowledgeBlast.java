package krilliondefense.powerups;

import java.util.List;

import krilliondefense.entities.Enemy;
import krilliondefense.entities.Tower;
import krilliondefense.game.GameState;

/**
 * The Legendary reward. Does 200 damage to every enemy on the map
 * and unlocks the Scholar Tower.
 */
public class KnowledgeBlast implements PowerUp {
    /** Damage done to every enemy. */
    public static final int DAMAGE = 200;

    /** {@inheritDoc} */
    @Override
    public void apply(GameState state, List<Tower> towers, List<Enemy> enemies) {
        for (Enemy e : enemies) {
            e.takeDamage(DAMAGE);
        }
        state.unlockScholarTower();
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Blast every enemy for 200 + unlock Scholar Tower";
    }
}
