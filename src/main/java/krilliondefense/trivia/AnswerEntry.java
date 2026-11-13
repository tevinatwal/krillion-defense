package krilliondefense.trivia;

/**
 * One known answer for a category and how rare it is (level 1 = common, 5 = legendary).
 */
public class AnswerEntry {
    /** The answer text (already cleaned up). */
    private String answer;
    /** Rarity level, 1 (common) to 5 (rarest). */
    private int level;

    /**
     * @param answer the answer (already normalized, e.g. "south africa")
     * @param level  rarity level from 1 (most common) to 5 (rarest)
     */
    public AnswerEntry(String answer, int level) {
        this.answer = answer;
        this.level = level;
    }

    /** @return the answer text */
    public String getAnswer() {
        return answer;
    }

    /** @return the rarity level 1-5 */
    public int getLevel() {
        return level;
    }
}
