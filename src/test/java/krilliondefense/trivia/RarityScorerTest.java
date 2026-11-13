package krilliondefense.trivia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for RarityScorer: cleaning up input, levels to tiers, points and typo matching.
 */
class RarityScorerTest {
    private RarityScorer scorer = new RarityScorer();

    @Test
    void normalizeCleansInput() {
        assertEquals("gambia", scorer.normalize("  The Gambia! "));
        assertEquals("cote divoire", scorer.normalize("Côte d'Ivoire"));
        assertEquals("guinea bissau", scorer.normalize("Guinea-Bissau"));
        assertEquals("c++", scorer.normalize("C++"));
        assertEquals("", scorer.normalize(null));
    }

    @Test
    void levelsMapToTiers() {
        assertEquals(RarityTier.COMMON, scorer.getTier(1));
        assertEquals(RarityTier.UNCOMMON, scorer.getTier(2));
        assertEquals(RarityTier.RARE, scorer.getTier(3));
        assertEquals(RarityTier.EPIC, scorer.getTier(4));
        assertEquals(RarityTier.LEGENDARY, scorer.getTier(5));
        assertEquals(RarityTier.INVALID, scorer.getTier(9));
    }

    @Test
    void rarerIsWorthMore() {
        assertEquals(10, scorer.score(1));
        assertEquals(100, scorer.score(5));
        assertTrue(scorer.score(4) > scorer.score(3));
    }

    @Test
    void closeMatch() {
        assertTrue(scorer.isCloseMatch("madagaskar", "madagascar"));
        assertTrue(scorer.isCloseMatch("madagaskr", "madagascar")); // long words can be 2 off
        assertTrue(scorer.isCloseMatch("ghanaa", "ghana"));
        assertFalse(scorer.isCloseMatch("ganna", "ghana") && scorer.editDistance("ganna", "ghana") > 1);
        assertFalse(scorer.isCloseMatch("char", "chad")); // too short for typo matching
    }
}
