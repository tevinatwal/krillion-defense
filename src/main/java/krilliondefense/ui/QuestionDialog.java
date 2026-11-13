package krilliondefense.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import krilliondefense.powerups.RewardFactory;
import krilliondefense.trivia.AnswerResult;
import krilliondefense.trivia.Question;
import krilliondefense.trivia.RarityTier;

/**
 * Popup that shows a trivia question with a countdown, and then the result.
 * It's modal so the game waits (paused) until it closes.
 */
public class QuestionDialog extends JDialog {
    /** Background color. */
    private static final Color BG = new Color(35, 38, 48);
    /** Color for bars and rows. */
    private static final Color PANEL = new Color(50, 54, 68);
    /** Normal text font. */
    private static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 15);
    /** Bold text font. */
    private static final Font BOLD = new Font("SansSerif", Font.BOLD, 15);

    // question screen
    /** Screen that shows the question. */
    private JPanel questionPanel;
    /** The question text. */
    private JLabel questionLabel;
    /** Bar that shows the time left. */
    private TimeBar timeBar;
    /** Box where the player types. */
    private JTextField answerField;
    /** Submit button. */
    private JButton submitButton;
    /** Shows "Type something first!". */
    private JLabel warningLabel;

    // result screen
    /** Screen that shows the result. */
    private JPanel resultPanel;
    /** Big title like "EPIC ANSWER!". */
    private JLabel resultTitle;
    /** Shows what the player said. */
    private JLabel youSaidLabel;
    /** List of tiers with the player's tier highlighted. */
    private RewardLadder resultLadder;
    /** Shows the reward they got. */
    private JLabel rewardLabel;
    /** Shows other answers they could have said. */
    private JLabel examplesLabel;
    /** Continue button. */
    private JButton continueButton;

    /** Timer that counts down the time left. */
    private javax.swing.Timer countdown;
    /** Milliseconds left to answer. */
    private int msLeft;
    /** Total milliseconds for this question. */
    private int msTotal;
    /** What the player typed (null if time ran out). */
    private String answer;
    /** When the current screen opened, so a stray key press does not skip it. */
    private long shownAt;

    /**
     * @param owner the game window
     */
    public QuestionDialog(JFrame owner) {
        super(owner, "Trivia Question!", true);
        buildQuestionPanel();
        buildResultPanel();

        setSize(640, 520);
        setResizable(false);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        // make sure the text box gets the keyboard focus so you can type right away
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                focusAnswerBox();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                focusAnswerBox();
            }
        });

        // ticks 10 times a second so the time bar moves smoothly
        countdown = new Timer(100, e -> {
            msLeft -= 100;
            timeBar.setTime(msLeft, msTotal);
            if (msLeft <= 0) {
                answer = null; // time ran out
                close();
            }
        });
    }

    /**
     * The color for each tier. Used here and in the game screen.
     *
     * @param tier the tier
     * @return its color
     */
    public static Color tierColor(RarityTier tier) {
        switch (tier) {
            case COMMON:
                return new Color(170, 175, 185);
            case UNCOMMON:
                return new Color(90, 200, 100);
            case RARE:
                return new Color(70, 150, 245);
            case EPIC:
                return new Color(175, 95, 235);
            case LEGENDARY:
                return new Color(250, 185, 30);
            default:
                return new Color(230, 80, 80);
        }
    }

    /**
     * Makes an answer look nice: "south africa" -> "South Africa".
     *
     * @param s answer from the csv
     * @return the answer with capital letters
     */
    public static String niceName(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String word : s.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            if (word.length() == 3 && !word.matches(".*[aeiouy].*")) {
                sb.append(word.toUpperCase()); // short names with no vowels are acronyms, like BMW
            } else {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return sb.toString();
    }

    /** Makes a centered label with a font and color. */
    private JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(0.5f);
        return l;
    }

    /** Makes a flat colored button. */
    private JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setBackground(color);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Builds the screen that shows the question, timer, rewards and answer box. */
    private void buildQuestionPanel() {
        JLabel header = label("QUESTION TIME!   (game is paused)", new Font("SansSerif", Font.BOLD, 22), Color.BLACK);
        header.setOpaque(true);
        header.setBackground(new Color(255, 170, 40));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        questionLabel = label("", new Font("SansSerif", Font.BOLD, 28), Color.WHITE);
        timeBar = new TimeBar();

        JLabel ladderTitle = label("The rarer your answer, the better the reward:", BOLD, new Color(200, 205, 220));
        RewardLadder ladder = new RewardLadder();

        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setBackground(BG);
        middle.setBorder(BorderFactory.createEmptyBorder(14, 20, 6, 20));
        middle.add(questionLabel);
        middle.add(Box.createVerticalStrut(12));
        middle.add(timeBar);
        middle.add(Box.createVerticalStrut(16));
        middle.add(ladderTitle);
        middle.add(Box.createVerticalStrut(6));
        middle.add(ladder);

        answerField = new JTextField();
        answerField.setFont(new Font("SansSerif", Font.PLAIN, 22));
        answerField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 170, 40), 2),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        answerField.addActionListener(e -> submit()); // pressing Enter submits
        submitButton = button("Submit", new Color(255, 170, 40));
        submitButton.addActionListener(e -> submit());
        warningLabel = label(" ", NORMAL, new Color(255, 120, 120));

        JPanel bottom = new JPanel(new BorderLayout(8, 6));
        bottom.setBackground(BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 20, 12, 20));
        bottom.add(label("Type your answer and press Enter", BOLD, Color.WHITE), BorderLayout.NORTH);
        bottom.add(answerField, BorderLayout.CENTER);
        bottom.add(submitButton, BorderLayout.EAST);
        bottom.add(warningLabel, BorderLayout.SOUTH);

        questionPanel = new JPanel(new BorderLayout());
        questionPanel.setBackground(BG);
        questionPanel.add(header, BorderLayout.NORTH);
        questionPanel.add(middle, BorderLayout.CENTER);
        questionPanel.add(bottom, BorderLayout.SOUTH);
    }

    /** Builds the screen that shows how your answer did. */
    private void buildResultPanel() {
        resultTitle = label("", new Font("SansSerif", Font.BOLD, 32), Color.BLACK);
        resultTitle.setOpaque(true);
        resultTitle.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        youSaidLabel = label("", new Font("SansSerif", Font.BOLD, 22), Color.WHITE);
        resultLadder = new RewardLadder();
        rewardLabel = label("", new Font("SansSerif", Font.BOLD, 18), Color.WHITE);
        examplesLabel = label("", NORMAL, new Color(200, 205, 220));

        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setBackground(BG);
        middle.setBorder(BorderFactory.createEmptyBorder(12, 20, 6, 20));
        middle.add(youSaidLabel);
        middle.add(Box.createVerticalStrut(10));
        middle.add(resultLadder);
        middle.add(Box.createVerticalStrut(12));
        middle.add(rewardLabel);
        middle.add(Box.createVerticalStrut(10));
        middle.add(examplesLabel);

        continueButton = button("Continue", new Color(120, 210, 120));
        continueButton.addActionListener(e -> {
            if (justOpened()) {
                return;
            }
            close();
        });
        JPanel bottom = new JPanel();
        bottom.setBackground(BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 20, 14, 20));
        bottom.add(continueButton);

        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(BG);
        resultPanel.add(resultTitle, BorderLayout.NORTH);
        resultPanel.add(middle, BorderLayout.CENTER);
        resultPanel.add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Shows the question and waits for an answer.
     *
     * @param q the question
     * @return what the player typed, or null if time ran out / window closed
     */
    public String showQuestion(Question q) {
        answer = null;
        msTotal = q.getTimeLimitSeconds() * 1000;
        msLeft = msTotal;
        questionLabel.setText(q.getPrompt());
        timeBar.setTime(msLeft, msTotal);
        answerField.setText("");
        warningLabel.setText(" ");

        setContentPane(questionPanel);
        getRootPane().setDefaultButton(submitButton);
        setLocationRelativeTo(getOwner());
        bringGameToFront();
        shownAt = System.currentTimeMillis();
        countdown.restart();
        setVisible(true); // blocks here until close() since it's modal
        countdown.stop();
        return answer;
    }

    /**
     * Shows how the answer did. Waits until the player clicks Continue.
     *
     * @param result   the scored answer
     * @param examples text about other answers they could have said (can be empty)
     */
    public void showResult(AnswerResult result, String examples) {
        RarityTier tier = result.getTier();
        if (result.isValid()) {
            resultTitle.setText(tier.getLabel().toUpperCase() + " ANSWER!   +" + result.getScore() + " knowledge");
            youSaidLabel.setText("You said: " + niceName(result.getMatchedAnswer()));
            rewardLabel.setText("Your reward:  " + RewardFactory.create(tier).getDescription());
            rewardLabel.setForeground(tierColor(tier));
        } else {
            resultTitle.setText(result.getMessage());
            String typed = result.getTyped() == null ? "(nothing)" : "\"" + result.getTyped().trim() + "\"";
            youSaidLabel.setText("You said: " + typed);
            rewardLabel.setText("No reward this time");
            rewardLabel.setForeground(new Color(255, 130, 130));
        }
        resultTitle.setBackground(tierColor(tier));
        resultLadder.setHighlight(tier);
        examplesLabel.setText("<html><div style='text-align:center;width:480px'>" + examples + "</div></html>");

        setContentPane(resultPanel);
        getRootPane().setDefaultButton(continueButton);
        setLocationRelativeTo(getOwner());
        shownAt = System.currentTimeMillis();
        // grey out Continue for a moment so it doesn't close by accident
        continueButton.setEnabled(false);
        Timer enable = new Timer(700, e -> {
            continueButton.setEnabled(true);
            continueButton.requestFocusInWindow();
        });
        enable.setRepeats(false);
        enable.start();
        setVisible(true);
    }

    /** On Mac the game isn't always the active app (like when it's started from a terminal or IntelliJ), so your typing goes to the other app instead of the answer box. This asks the OS to bring the game to the front first. */
    private void bringGameToFront() {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.APP_REQUEST_FOREGROUND)) {
                Desktop.getDesktop().requestForeground(true);
            }
        } catch (Exception e) {
            // not supported on this computer, oh well
        }
        if (getOwner() != null) {
            getOwner().toFront();
        }
    }

    /** Puts the typing cursor in the answer box. */
    private void focusAnswerBox() {
        if (getContentPane() == questionPanel) {
            toFront();
            answerField.requestFocusInWindow();
        }
    }

    /** Ignore clicks/Enter right when the popup opens so it doesn't close instantly. */
    private boolean justOpened() {
        return System.currentTimeMillis() - shownAt < 500;
    }

    /** Called when the player presses Enter or Submit. */
    private void submit() {
        if (justOpened()) {
            return;
        }
        if (answerField.getText().trim().isEmpty()) {
            warningLabel.setText("Type something first!");
            answerField.requestFocusInWindow();
            return;
        }
        answer = answerField.getText();
        close();
    }

    /** Stops the timer and hides the popup (this lets setVisible(true) return). */
    private void close() {
        countdown.stop();
        setVisible(false);
    }

    /**
     * Bar that shrinks as time runs out. Turns red near the end.
     */
    private static class TimeBar extends JComponent {
        /** Milliseconds left. */
        private int left;
        /** Total milliseconds. */
        private int total = 1;

        /** Makes the bar. */
        TimeBar() {
            setPreferredSize(new Dimension(580, 26));
            setMaximumSize(new Dimension(580, 26));
            setAlignmentX(0.5f);
        }

        /**
         * Updates how much time is left.
         *
         * @param left  milliseconds left
         * @param total total milliseconds
         */
        void setTime(int left, int total) {
            this.left = Math.max(0, left);
            this.total = total;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(PANEL);
            g2.fillRoundRect(0, 0, w, h, h, h);
            double pct = (double) left / total;
            g2.setColor(left <= 5000 ? new Color(235, 70, 60) : new Color(255, 170, 40));
            g2.fillRoundRect(0, 0, (int) (w * pct), h, h, h);
            g2.setColor(Color.WHITE);
            g2.setFont(BOLD);
            String s = (left + 999) / 1000 + " seconds left";
            int sw = g2.getFontMetrics().stringWidth(s);
            g2.drawString(s, w / 2 - sw / 2, h / 2 + 5);
        }
    }

    /**
     * The list of the 5 tiers with their reward. Used in the question popup
     * (so you know what you're playing for) and the result (shows where you landed).
     */
    private static class RewardLadder extends JComponent {
        /** Tiers from best to worst. */
        private static final RarityTier[] TIERS = {
            RarityTier.LEGENDARY, RarityTier.EPIC, RarityTier.RARE, RarityTier.UNCOMMON, RarityTier.COMMON
        };
        /** Tier to highlight (the player's answer), or null. */
        private RarityTier highlight = null;

        /** Makes the ladder. */
        RewardLadder() {
            setPreferredSize(new Dimension(580, 5 * 30));
            setMaximumSize(new Dimension(580, 5 * 30));
            setAlignmentX(0.5f);
        }

        /**
         * Highlights the player's tier.
         *
         * @param tier the tier to highlight (or null for none)
         */
        void setHighlight(RarityTier tier) {
            highlight = tier;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < TIERS.length; i++) {
                RarityTier t = TIERS[i];
                int y = i * 30;
                boolean mine = t == highlight;
                g2.setColor(mine ? tierColor(t).darker() : PANEL);
                g2.fillRoundRect(0, y + 2, getWidth(), 26, 10, 10);
                if (mine) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, y + 2, getWidth() - 2, 25, 10, 10);
                    g2.setStroke(new BasicStroke(1));
                }
                // colored name tag with level dots
                g2.setColor(tierColor(t));
                g2.fillRoundRect(6, y + 5, 118, 20, 8, 8);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString(t.getLabel(), 13, y + 20);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", mine ? Font.BOLD : Font.PLAIN, 14));
                g2.drawString(RewardFactory.create(t).getDescription(), 136, y + 20);
                if (mine) {
                    g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                    String you = "<< YOU";
                    g2.drawString(you, getWidth() - g2.getFontMetrics().stringWidth(you) - 10, y + 20);
                }
            }
        }
    }
}
