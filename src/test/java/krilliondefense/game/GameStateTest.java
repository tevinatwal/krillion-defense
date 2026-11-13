package krilliondefense.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for GameState: spending gold and running out of lives.
 */
class GameStateTest {

    @Test
    void spendGoldFailsWhenTooLow() {
        GameState s = new GameState();
        assertFalse(s.spendGold(500));
        assertEquals(100, s.getGold());
        assertTrue(s.spendGold(50));
        assertEquals(50, s.getGold());
    }

    @Test
    void livesToZeroIsGameOver() {
        GameState s = new GameState();
        for (int i = 0; i < 20; i++) {
            assertFalse(s.isGameOver());
            s.loseLife();
        }
        assertTrue(s.isGameOver());
    }
}
