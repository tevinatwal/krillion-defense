package krilliondefense.trivia;

/**
 * How rare an answer was. Every answer in answers.csv has a level from 1 to 5,
 * and each level is one tier. Rarer tiers give more points and better power-ups.
 */
public enum RarityTier {
    /** Wrong answer, no reward. */
    INVALID("Invalid", 0),
    /** Level 1 answer. */
    COMMON("Common", 10),
    /** Level 2 answer. */
    UNCOMMON("Uncommon", 25),
    /** Level 3 answer. */
    RARE("Rare", 50),
    /** Level 4 answer. */
    EPIC("Epic", 75),
    /** Level 5 answer (the rarest). */
    LEGENDARY("Legendary", 100);

    /** Name shown on screen. */
    private final String label;
    /** Knowledge points for this tier. */
    private final int points;

    /**
     * @param label  name shown on screen
     * @param points knowledge points for this tier
     */
    RarityTier(String label, int points) {
        this.label = label;
        this.points = points;
    }

    /** @return nice name to show the player, like "Legendary" */
    public String getLabel() {
        return label;
    }

    /** @return knowledge points for an answer of this tier */
    public int getPoints() {
        return points;
    }

    /**
     * @param level answer level 1-5 from the csv
     * @return the matching tier (INVALID if the level is out of range)
     */
    public static RarityTier fromLevel(int level) {
        switch (level) {
            case 1:
                return COMMON;
            case 2:
                return UNCOMMON;
            case 3:
                return RARE;
            case 4:
                return EPIC;
            case 5:
                return LEGENDARY;
            default:
                return INVALID;
        }
    }

    /** @return the level 1-5 for this tier (0 for INVALID) */
    public int getLevel() {
        return ordinal();
    }
}
