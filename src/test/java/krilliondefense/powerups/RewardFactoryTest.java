package krilliondefense.powerups;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import krilliondefense.entities.BasicEnemy;
import krilliondefense.entities.Enemy;
import krilliondefense.game.GameState;
import krilliondefense.trivia.RarityTier;

/**
 * Tests for RewardFactory and the power-ups.
 */
class RewardFactoryTest {

    @Test
    void eachTierGivesItsReward() {
        assertTrue(RewardFactory.create(RarityTier.COMMON) instanceof GoldBonus);
        assertTrue(RewardFactory.create(RarityTier.UNCOMMON) instanceof GoldBonus);
        assertTrue(RewardFactory.create(RarityTier.RARE) instanceof DamageBoost);
        assertTrue(RewardFactory.create(RarityTier.EPIC) instanceof FreezeEnemies);
        assertTrue(RewardFactory.create(RarityTier.LEGENDARY) instanceof KnowledgeBlast);
    }

    @Test
    void invalidGivesNothing() {
        assertNull(RewardFactory.create(RarityTier.INVALID));
    }

    @Test
    void goldBonusAddsGold() {
        GameState s = new GameState();
        int before = s.getGold();
        RewardFactory.create(RarityTier.UNCOMMON).apply(s, new ArrayList<>(), new ArrayList<>());
        assertEquals(before + 60, s.getGold());
    }

    @Test
    void blastHurtsEnemiesAndUnlocksScholar() {
        GameState s = new GameState();
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new BasicEnemy(0, 0, 1));
        new KnowledgeBlast().apply(s, new ArrayList<>(), enemies);
        assertTrue(enemies.get(0).isDead());
        assertTrue(s.isScholarUnlocked());
    }
}
