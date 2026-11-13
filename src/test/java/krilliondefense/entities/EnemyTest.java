package krilliondefense.entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import krilliondefense.game.GameMap;

/**
 * Tests for Enemy: dying and walking to the end of a path.
 */
class EnemyTest {

    @Test
    void diesAtZeroHealth() {
        BasicEnemy e = new BasicEnemy(0, 0, 1);
        e.takeDamage(49);
        assertFalse(e.isDead());
        e.takeDamage(1);
        assertTrue(e.isDead());
    }

    @Test
    void reachesEndOfThreeWaypointPath() {
        // small map with one lane that has 3 waypoints
        GameMap map = new GameMap(new int[][] {{0, 0}, {3, 0}, {3, 3}});
        BasicEnemy e = new BasicEnemy(0, 0, 1);
        e.setLane(0, map);
        for (int i = 0; i < 1000 && !e.reachedEnd(); i++) {
            e.move(map);
        }
        assertTrue(e.reachedEnd());
    }

    @Test
    void hardMapHasMoreLanes() {
        assertTrue(GameMap.forDifficulty(krilliondefense.game.Difficulty.HARD).getLaneCount() == 3);
        assertTrue(GameMap.forDifficulty(krilliondefense.game.Difficulty.EASY).getLaneCount() == 1);
    }
}
