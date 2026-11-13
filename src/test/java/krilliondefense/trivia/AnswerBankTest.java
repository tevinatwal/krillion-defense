package krilliondefense.trivia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for AnswerBank: loading the csv, finding answers, aliases and levels.
 */
class AnswerBankTest {
    private AnswerBank bank;

    @BeforeEach
    void setUp() throws Exception {
        bank = new AnswerBank();
        bank.loadFromStream(getClass().getResourceAsStream("/test_answers.csv"));
    }

    @Test
    void loadsAndFindsKnownAnswer() {
        AnswerEntry e = bank.find("africa_countries", "kenya");
        assertNotNull(e);
        assertEquals(2, e.getLevel());
    }

    @Test
    void unknownAnswerIsNull() {
        assertNull(bank.find("africa_countries", "france"));
        assertNull(bank.find("not_a_category", "kenya"));
    }

    @Test
    void aliasWorks() {
        AnswerEntry e = bank.find("africa_countries", "swaziland");
        assertNotNull(e);
        assertEquals("eswatini", e.getAnswer());
    }

    @Test
    void loadsPrompts() {
        assertEquals("Name a country in Africa", bank.getPrompts().get("africa_countries"));
    }

    @Test
    void answersAtLevel() {
        assertTrue(bank.getAnswersAtLevel("africa_countries", 5).contains("djibouti"));
        assertEquals(3, bank.getAnswersAtLevel("africa_countries", 1).size());
    }

    @Test
    void realFileLoads() throws Exception {
        AnswerBank real = new AnswerBank();
        real.loadFromStream(getClass().getResourceAsStream("/answers.csv"));
        assertTrue(real.getPrompts().size() >= 40);
        // every question needs answers at every level
        for (String cat : real.getPrompts().keySet()) {
            for (int lvl = 1; lvl <= 5; lvl++) {
                assertTrue(real.getAnswersAtLevel(cat, lvl).size() >= 2, cat + " level " + lvl);
            }
        }
    }
}
