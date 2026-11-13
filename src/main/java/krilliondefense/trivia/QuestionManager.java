package krilliondefense.trivia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Picks questions and checks the player's answers.
 * No Swing in here so it can be tested on its own.
 */
public class QuestionManager {
    /** Every question that can be asked. */
    private List<Question> questions;
    /** Where the answers come from. */
    private AnswerBank bank;
    /** Cleans up and scores answers. */
    private RarityScorer scorer;
    /** Answers already given this game ("category:answer"). */
    private Set<String> usedAnswers = new HashSet<>();
    /** Shuffled questions left to ask before repeating. */
    private List<Question> deck = new ArrayList<>();

    /**
     * @param bank      where the answers come from
     * @param questions all the questions that can be asked
     */
    public QuestionManager(AnswerBank bank, List<Question> questions) {
        this.bank = bank;
        this.questions = questions;
        this.scorer = new RarityScorer();
    }

    /**
     * Makes a question manager using every #prompt line in the bank.
     *
     * @param bank the loaded answer bank
     */
    public QuestionManager(AnswerBank bank) {
        this(bank, makeQuestions(bank));
    }

    /** Makes one Question for every #prompt line in the bank. */
    private static List<Question> makeQuestions(AnswerBank bank) {
        List<Question> list = new ArrayList<>();
        for (Map.Entry<String, String> e : bank.getPrompts().entrySet()) {
            list.add(new Question(e.getValue(), e.getKey()));
        }
        return list;
    }

    /**
     * Gets the next question. Goes through a shuffled deck so questions
     * don't repeat until all of them have been asked.
     *
     * @return a question
     */
    public Question nextQuestion() {
        if (deck.isEmpty()) {
            deck.addAll(questions);
            Collections.shuffle(deck);
        }
        return deck.remove(0);
    }

    /**
     * Checks an answer and scores it. Follows the activity diagram:
     * empty -> normalize -> alias -> already used -> exact/close match -> tier.
     *
     * @param q     the question that was asked
     * @param input what the player typed (null if time ran out)
     * @return the result
     */
    public AnswerResult submitAnswer(Question q, String input) {
        if (input == null) {
            return AnswerResult.invalid(null, "Time's up!");
        }
        String category = q.getCategory();
        String answer = scorer.normalize(input);
        if (answer.isEmpty()) {
            return AnswerResult.invalid(input, "No answer given");
        }
        answer = bank.resolveAlias(category, answer);

        AnswerEntry entry = bank.find(category, answer);
        if (entry == null) {
            entry = findCloseMatch(category, answer);
        }
        if (entry == null) {
            return AnswerResult.invalid(input, "Not on our list");
        }
        if (usedAnswers.contains(category + ":" + entry.getAnswer())) {
            return AnswerResult.invalid(input, "Already used this game");
        }

        RarityTier tier = scorer.getTier(entry.getLevel());
        usedAnswers.add(category + ":" + entry.getAnswer());
        return new AnswerResult(true, tier, entry.getAnswer(), input, tier.getLabel() + "!");
    }

    /** Looks for a typo match in the answers and the aliases. returns null if nothing is close. */
    private AnswerEntry findCloseMatch(String category, String answer) {
        String noSpaces = answer.replace(" ", "");
        AnswerEntry best = null;
        int bestDist = Integer.MAX_VALUE;
        for (AnswerEntry e : bank.getEntries(category)) {
            // "southafrica" should still match "south africa"
            if (e.getAnswer().replace(" ", "").equals(noSpaces)) {
                return e;
            }
            if (scorer.isCloseMatch(answer, e.getAnswer())) {
                int d = scorer.editDistance(answer, e.getAnswer());
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
        }
        for (Map.Entry<String, String> alias : bank.getAliases(category).entrySet()) {
            if (scorer.isCloseMatch(answer, alias.getKey())) {
                int d = scorer.editDistance(answer, alias.getKey());
                if (d < bestDist) {
                    bestDist = d;
                    best = bank.find(category, alias.getValue());
                }
            }
        }
        return best;
    }

    /**
     * Picks a few example answers from one level, for showing the player
     * what else they could have said.
     *
     * @param category category key
     * @param level    level 1-5
     * @param count    how many to pick
     * @param skip     an answer to leave out (can be null)
     * @return up to count answers
     */
    public List<String> getExamples(String category, int level, int count, String skip) {
        List<String> list = bank.getAnswersAtLevel(category, level);
        list.remove(skip);
        Collections.shuffle(list);
        return list.subList(0, Math.min(count, list.size()));
    }

    /** Clears used answers (for starting a new game). */
    public void reset() {
        usedAnswers.clear();
        deck.clear();
    }

    /** @return how many different questions there are */
    public int getQuestionCount() {
        return questions.size();
    }
}
