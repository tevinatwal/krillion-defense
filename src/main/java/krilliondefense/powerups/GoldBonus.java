package krilliondefense.powerups;

import java.util.List;

import krilliondefense.entities.Enemy;
import krilliondefense.entities.Tower;
import krilliondefense.game.GameState;

/**
 * Gives the player some gold.
 */
public class GoldBonus implements PowerUp {
    /** Gold to give. */
    private int amount;

    /**
     * @param amount gold to give
     */
    public GoldBonus(int amount) {
        this.amount = amount;
    }

    /** {@inheritDoc} */
    @Override
    public void apply(GameState state, List<Tower> towers, List<Enemy> enemies) {
        state.addGold(amount);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "+" + amount + " gold";
    }
}
