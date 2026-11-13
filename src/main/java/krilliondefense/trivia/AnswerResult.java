package krilliondefense.trivia;

/**
 * What happened when the player submitted an answer.
 */
public class AnswerResult {
    /** True if the answer counted. */
    private boolean valid;
    /** How rare it was (INVALID if it did not count). */
    private RarityTier tier;
    /** The answer from the bank it matched. */
    private String matchedAnswer;
    /** What the player typed. */
    private String typed;
    /** Short message for the player. */
    private String message;

    /**
     * @param valid         true if the answer counted
     * @param tier          rarity tier (INVALID if it didn't count)
     * @param matchedAnswer the answer from the bank it matched (null if invalid)
     * @param typed         what the player actually typed (null if time ran out)
     * @param message       short text to show the player
     */
    public AnswerResult(boolean valid, RarityTier tier, String matchedAnswer, String typed, String message) {
        this.valid = valid;
        this.tier = tier;
        this.matchedAnswer = matchedAnswer;
        this.typed = typed;
        this.message = message;
    }

    /**
     * Shortcut for an invalid result.
     *
     * @param typed   what the player typed
     * @param message why it was invalid
     * @return an INVALID result
     */
    public static AnswerResult invalid(String typed, String message) {
        return new AnswerResult(false, RarityTier.INVALID, null, typed, message);
    }

    /** @return true if the answer counted */
    public boolean isValid() {
        return valid;
    }

    /** @return knowledge points earned (0 if invalid) */
    public int getScore() {
        return tier.getPoints();
    }

    /** @return the rarity tier */
    public RarityTier getTier() {
        return tier;
    }

    /** @return the bank answer that was matched, or null */
    public String getMatchedAnswer() {
        return matchedAnswer;
    }

    /** @return what the player typed (null if the timer ran out) */
    public String getTyped() {
        return typed;
    }

    /** @return message for the player */
    public String getMessage() {
        return message;
    }
}
