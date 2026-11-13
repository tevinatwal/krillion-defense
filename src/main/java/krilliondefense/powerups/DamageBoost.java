package krilliondefense.powerups;

import java.util.List;

import krilliondefense.entities.Enemy;
import krilliondefense.entities.Tower;
import krilliondefense.game.GameState;

/**
 * +50% damage for every tower until the wave ends.
 */
public class DamageBoost implements PowerUp {

    /** {@inheritDoc} */
    @Override
    public void apply(GameState state, List<Tower> towers, List<Enemy> enemies) {
        state.setDamageBoost(true); // so towers built later this wave get it too
        for (Tower t : towers) {
            t.setBoosted(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Towers do +50% damage this wave";
    }
}
