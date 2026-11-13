package krilliondefense.trivia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for QuestionManager: scoring answers, repeats, typos and empty answers.
 */
class QuestionManagerTest {
    private QuestionManager qm;
    private Question q = new Question("Name a country in Africa", "africa_countries");

    @BeforeEach
    void setUp() throws Exception {
        AnswerBank bank = new AnswerBank();
        bank.loadFromStream(getClass().getResourceAsStream("/test_answers.csv"));
        qm = new QuestionManager(bank);
    }

    @Test
    void rareAnswerIsLegendary() {
        AnswerResult r = qm.submitAnswer(q, "Eswatini");
        assertTrue(r.isValid());
        assertEquals(RarityTier.LEGENDARY, r.getTier());
        assertEquals(100, r.getScore());
    }

    @Test
    void commonAnswerIsCommon() {
        assertEquals(RarityTier.COMMON, qm.submitAnswer(q, "nigeria").getTier());
    }

    @Test
    void repeatIsInvalid() {
        qm.submitAnswer(q, "kenya");
        AnswerResult r = qm.submitAnswer(q, "Kenya");
        assertFalse(r.isValid());
        assertEquals(RarityTier.INVALID, r.getTier());
    }

    @Test
    void aliasCountsAsUsed() {
        qm.submitAnswer(q, "eswatini");
        assertFalse(qm.submitAnswer(q, "Swaziland").isValid());
    }

    @Test
    void emptyIsInvalid() {
        assertFalse(qm.submitAnswer(q, "   ").isValid());
        assertFalse(qm.submitAnswer(q, null).isValid());
    }

    @Test
    void typoStillCounts() {
        AnswerResult r = qm.submitAnswer(q, "madagaskar");
        assertTrue(r.isValid());
        assertEquals("madagascar", r.getMatchedAnswer());
    }

    @Test
    void missingSpaceStillCounts() {
        assertEquals("south africa", qm.submitAnswer(q, "SouthAfrica").getMatchedAnswer());
    }

    @Test
    void aliasTypoStillCounts() {
        assertEquals("eswatini", qm.submitAnswer(q, "swazilnd").getMatchedAnswer());
    }

    @Test
    void wrongAnswerIsInvalid() {
        assertFalse(qm.submitAnswer(q, "france").isValid());
    }

    @Test
    void questionsComeFromPrompts() {
        assertEquals(1, qm.getQuestionCount());
        assertEquals("africa_countries", qm.nextQuestion().getCategory());
    }
}
