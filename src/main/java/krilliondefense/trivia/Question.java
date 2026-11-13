package krilliondefense.trivia;

/**
 * One open-ended trivia prompt, like "Name a country in Africa".
 * The category is used to look up answers in the AnswerBank.
 */
public class Question {
    /** Text shown to the player. */
    private String prompt;
    /** Category key in answers.csv. */
    private String category;
    /** Seconds the player gets to answer. */
    private int timeLimitSeconds;

    /**
     * Makes a question with the default 30 second time limit.
     *
     * @param prompt   text shown to the player
     * @param category category key in answers.csv
     */
    public Question(String prompt, String category) {
        this(prompt, category, 30);
    }

    /**
     * Makes a question with a custom time limit.
     *
     * @param prompt           text shown to the player
     * @param category         category key in answers.csv
     * @param timeLimitSeconds how long the player has to answer
     */
    public Question(String prompt, String category, int timeLimitSeconds) {
        this.prompt = prompt;
        this.category = category;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    /** @return the prompt text */
    public String getPrompt() {
        return prompt;
    }

    /** @return the category key */
    public String getCategory() {
        return category;
    }

    /** @return the time limit in seconds */
    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }
}
