package krilliondefense.trivia;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds every question prompt and every known answer, loaded from answers.csv.
 *
 * File format:
 *   #prompt,category,Name a country in Africa
 *   category,answer,level,alias1|alias2
 */
public class AnswerBank {
    /** category -> (answer -> entry) for every known answer. */
    private Map<String, Map<String, AnswerEntry>> answers = new LinkedHashMap<>();
    // aliases are per category: category -> (alias -> real answer)
    /** category -> (other name -> real answer), like "swaziland" -> "eswatini". */
    private Map<String, Map<String, String>> aliases = new HashMap<>();
    /** category -> question text, like "Name a country in Africa". */
    private Map<String, String> prompts = new LinkedHashMap<>();

    /**
     * Loads answers from a csv file on disk.
     *
     * @param path path to the csv file
     * @throws IOException if the file can't be read
     */
    public void loadFromFile(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))) {
            readLines(reader);
        }
    }

    /**
     * Loads answers from a stream (used for the csv inside resources).
     *
     * @param in the stream to read
     * @throws IOException if reading fails
     */
    public void loadFromStream(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            readLines(reader);
        }
    }

    /** Reads every line of the csv and adds the prompts, answers and aliases. */
    private void readLines(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts[0].equals("#prompt") && parts.length >= 3) {
                prompts.put(parts[1].trim(), parts[2].trim());
                continue;
            }
            if (line.startsWith("#") || parts.length < 3) {
                continue; // comment or bad line
            }
            String category = parts[0].trim();
            String answer = parts[1].trim();
            int level;
            try {
                level = Integer.parseInt(parts[2].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            answers.putIfAbsent(category, new LinkedHashMap<>());
            answers.get(category).put(answer, new AnswerEntry(answer, level));

            if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                aliases.putIfAbsent(category, new HashMap<>());
                for (String alias : parts[3].split("\\|")) {
                    aliases.get(category).put(alias.trim(), answer);
                }
            }
        }
    }

    /**
     * Swaps an alias for the real answer ("swaziland" -> "eswatini").
     *
     * @param category category key
     * @param answer   normalized answer
     * @return the real answer, or the same text if it isn't an alias
     */
    public String resolveAlias(String category, String answer) {
        Map<String, String> map = aliases.get(category);
        if (map == null) {
            return answer;
        }
        return map.getOrDefault(answer, answer);
    }

    /**
     * @param category category key
     * @return all aliases for the category (alias -> real answer)
     */
    public Map<String, String> getAliases(String category) {
        return aliases.getOrDefault(category, new HashMap<>());
    }

    /**
     * Finds an exact answer in a category (aliases work too).
     *
     * @param category category key
     * @param answer   normalized answer
     * @return the entry, or null if it isn't in the bank
     */
    public AnswerEntry find(String category, String answer) {
        Map<String, AnswerEntry> cat = answers.get(category);
        if (cat == null) {
            return null;
        }
        return cat.get(resolveAlias(category, answer));
    }

    /**
     * @param category category key
     * @return all entries in the category (empty if unknown category)
     */
    public Collection<AnswerEntry> getEntries(String category) {
        Map<String, AnswerEntry> cat = answers.get(category);
        if (cat == null) {
            return new ArrayList<>();
        }
        return cat.values();
    }

    /**
     * @param category category key
     * @param level    level 1-5
     * @return all answers in the category with that level
     */
    public List<String> getAnswersAtLevel(String category, int level) {
        List<String> list = new ArrayList<>();
        for (AnswerEntry e : getEntries(category)) {
            if (e.getLevel() == level) {
                list.add(e.getAnswer());
            }
        }
        return list;
    }

    /** @return category -> prompt text for every question in the file */
    public Map<String, String> getPrompts() {
        return prompts;
    }

    /** @return list of all category keys that have answers */
    public List<String> getCategories() {
        return new ArrayList<>(answers.keySet());
    }
}
