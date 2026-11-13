package krilliondefense.game;

/**
 * Gold, lives, wave number and other stats for one game.
 */
public class GameState {
    /** Number of waves to win. */
    public static final int MAX_WAVES = 8;

    /** Gold the player has. */
    private int gold;
    /** Lives left. */
    private int lives;
    /** Current wave (0 before the first one). */
    private int waveNumber;
    /** Points from answering questions. */
    private int knowledgePoints;
    /** What the game is doing right now. */
    private GamePhase phase;
    /** Phase before the current one. */
    private GamePhase previousPhase;
    /** True after a Legendary answer. */
    private boolean scholarUnlocked;
    /** True while the Rare damage boost is on. */
    private boolean damageBoost;

    /** Makes a new game state with starting values. */
    public GameState() {
        reset();
    }

    /** Puts everything back to the starting values (easy mode). */
    public void reset() {
        reset(Difficulty.EASY);
    }

    /**
     * Puts everything back to the starting values for a difficulty.
     *
     * @param difficulty the difficulty picked on the menu
     */
    public void reset(Difficulty difficulty) {
        gold = difficulty.getStartGold(); // only a little gold, build your defense first
        lives = difficulty.getLives();
        waveNumber = 0;
        knowledgePoints = 0;
        phase = GamePhase.MENU;
        previousPhase = GamePhase.MENU;
        scholarUnlocked = false;
        damageBoost = false;
    }

    /**
     * Takes gold if the player has enough.
     *
     * @param amount how much to spend
     * @return true if it worked, false if not enough gold
     */
    public boolean spendGold(int amount) {
        if (amount > gold) {
            return false;
        }
        gold -= amount;
        return true;
    }

    /**
     * @param amount gold to add
     */
    public void addGold(int amount) {
        gold += amount;
    }

    /** Takes away one life (never goes below 0). */
    public void loseLife() {
        loseLives(1);
    }

    /**
     * Takes away some lives (the boss takes 5).
     *
     * @param amount lives to lose
     */
    public void loseLives(int amount) {
        lives = Math.max(0, lives - amount);
    }

    /** @return true if out of lives */
    public boolean isGameOver() {
        return lives <= 0;
    }

    /**
     * Changes phase and remembers the old one (so QUESTION can go back).
     *
     * @param p the new phase
     */
    public void setPhase(GamePhase p) {
        previousPhase = phase;
        phase = p;
    }

    /**
     * @param pts knowledge points to add
     */
    public void addKnowledgePoints(int pts) {
        knowledgePoints += pts;
    }

    /** Lets the player build Scholar Towers. */
    public void unlockScholarTower() {
        scholarUnlocked = true;
    }

    /** Moves to the next wave number. */
    public void nextWave() {
        waveNumber++;
    }

    /** @return current gold */
    public int getGold() {
        return gold;
    }

    /** @return lives left */
    public int getLives() {
        return lives;
    }

    /** @return the current wave number (0 before the first wave) */
    public int getWaveNumber() {
        return waveNumber;
    }

    /** @return knowledge points */
    public int getKnowledgePoints() {
        return knowledgePoints;
    }

    /** @return the current phase */
    public GamePhase getPhase() {
        return phase;
    }

    /** @return the phase before the current one */
    public GamePhase getPreviousPhase() {
        return previousPhase;
    }

    /**
     * @param on true while the Rare damage boost is active (it ends with the wave)
     */
    public void setDamageBoost(boolean on) {
        damageBoost = on;
    }

    /** @return true if towers should do +50% damage right now */
    public boolean isDamageBoost() {
        return damageBoost;
    }

    /** @return true if the Scholar Tower is unlocked */
    public boolean isScholarUnlocked() {
        return scholarUnlocked;
    }
}
