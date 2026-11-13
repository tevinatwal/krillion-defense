package krilliondefense.trivia;

import java.text.Normalizer;

/**
 * Cleans up player input, turns levels into tiers and checks for typos.
 */
public class RarityScorer {

    /**
     * Cleans up an answer so "  The Gambia!" and "gambia" match.
     * Lowercases, strips accents, removes punctuation, collapses spaces
     * and drops a leading "the ".
     *
     * @param input raw text from the player
     * @return cleaned up text (empty string if input is null)
     */
    public String normalize(String input) {
        if (input == null) {
            return "";
        }
        String s = input.toLowerCase().trim();
        // strip accents (é -> e) so "reykjavík" works
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replace("&", " and ");
        s = s.replace("'", "").replace("’", ""); // normal and curly apostrophes
        // we keep + and # so c++ and c# don't all turn into "c"
        s = s.replaceAll("[^a-z0-9+# ]", " ");
        s = s.replaceAll("\\s+", " ").trim();
        if (s.startsWith("the ")) {
            s = s.substring(4);
        }
        return s;
    }

    /**
     * @param level answer level 1-5
     * @return knowledge points for that level
     */
    public int score(int level) {
        return getTier(level).getPoints();
    }

    /**
     * @param level answer level 1-5
     * @return the matching tier
     */
    public RarityTier getTier(int level) {
        return RarityTier.fromLevel(level);
    }

    /**
     * Typo check. Short answers (4 letters or less) have to be exact so
     * "chad" != "char". Normal answers can be 1 letter off, and long answers
     * (9+ letters, like "madagascar") can be 2 letters off.
     *
     * @param a what the player typed
     * @param b the real answer
     * @return true if it should count as a match
     */
    public boolean isCloseMatch(String a, String b) {
        if (b.length() <= 4) {
            return false;
        }
        int allowed = b.length() >= 9 ? 2 : 1;
        if (Math.abs(a.length() - b.length()) > allowed) {
            return false; // can't be close, skip the slow check
        }
        return editDistance(a, b) <= allowed;
    }

    /**
     * Levenshtein distance (the standard dynamic programming version).
     *
     * @param a first string
     * @param b second string
     * @return number of edits to turn a into b
     */
    public int editDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
