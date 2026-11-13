package krilliondefense.game;

/**
 * The difficulty you pick on the main menu. Harder modes have more paths
 * (and on Hard, two bases), enemies spawn faster and there are more of them.
 */
public enum Difficulty {
    /** One path and one base. */
    EASY("Easy", "1 path, 1 base. Good for learning.", 100, 20, 60, 1.0),
    /** Two paths into one base, more enemies. */
    MEDIUM("Medium", "2 paths into 1 base. More enemies.", 150, 20, 52, 1.2),
    /** Three paths and two bases, the most enemies. */
    HARD("Hard", "3 paths, 2 bases to defend!", 250, 15, 46, 1.45);

    /** Name shown on screen. */
    private final String label;
    /** One line description for the menu. */
    private final String description;
    /** Gold at the start. */
    private final int startGold;
    /** Lives at the start. */
    private final int lives;
    /** Ticks between enemy spawns. */
    private final int spawnDelay;
    /** How many more enemies per wave than easy. */
    private final double enemyMultiplier;

    /**
     * @param label           name shown on screen
     * @param description     one line description for the menu
     * @param startGold       gold at the start
     * @param lives           lives at the start
     * @param spawnDelay      ticks between enemy spawns
     * @param enemyMultiplier how many more enemies per wave than easy
     */
    Difficulty(String label, String description, int startGold, int lives, int spawnDelay,
            double enemyMultiplier) {
        this.label = label;
        this.description = description;
        this.startGold = startGold;
        this.lives = lives;
        this.spawnDelay = spawnDelay;
        this.enemyMultiplier = enemyMultiplier;
    }

    /** @return name to show, like "Hard" */
    public String getLabel() {
        return label;
    }

    /** @return one line description for the menu */
    public String getDescription() {
        return description;
    }

    /** @return gold you start with */
    public int getStartGold() {
        return startGold;
    }

    /** @return lives you start with */
    public int getLives() {
        return lives;
    }

    /** @return ticks between enemy spawns */
    public int getSpawnDelay() {
        return spawnDelay;
    }

    /** @return multiplier on the number of enemies per wave */
    public double getEnemyMultiplier() {
        return enemyMultiplier;
    }
}
