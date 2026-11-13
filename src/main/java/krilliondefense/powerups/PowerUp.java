package krilliondefense.powerups;

import java.util.List;

import krilliondefense.entities.Enemy;
import krilliondefense.entities.Tower;
import krilliondefense.game.GameState;

/**
 * A reward the player gets for a valid trivia answer.
 */
public interface PowerUp {

    /**
     * Does the effect.
     *
     * @param state   game state
     * @param towers  all towers
     * @param enemies all enemies on the map
     */
    void apply(GameState state, List<Tower> towers, List<Enemy> enemies);

    /**
     * @return text shown to the player
     */
    String getDescription();
}
