package krilliondefense.powerups;

import java.util.List;

import krilliondefense.entities.Enemy;
import krilliondefense.entities.Tower;
import krilliondefense.game.GameState;

/**
 * Freezes every enemy on the map for 6 seconds.
 */
public class FreezeEnemies implements PowerUp {
    /** How long enemies are frozen (360 ticks = 6 seconds). */
    private static final int FREEZE_TICKS = 360;

    /** {@inheritDoc} */
    @Override
    public void apply(GameState state, List<Tower> towers, List<Enemy> enemies) {
        for (Enemy e : enemies) {
            e.freeze(FREEZE_TICKS);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Freeze all enemies for 6 seconds";
    }
}
