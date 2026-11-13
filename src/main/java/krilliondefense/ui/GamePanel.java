package krilliondefense.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import krilliondefense.entities.ArrowTower;
import krilliondefense.entities.CannonTower;
import krilliondefense.entities.Enemy;
import krilliondefense.entities.FloatingText;
import krilliondefense.entities.FrostTower;
import krilliondefense.entities.Projectile;
import krilliondefense.entities.RingEffect;
import krilliondefense.entities.ScholarTower;
import krilliondefense.entities.Tower;
import krilliondefense.game.Difficulty;
import krilliondefense.game.GameMap;
import krilliondefense.game.GamePhase;
import krilliondefense.game.GameState;
import krilliondefense.game.WaveManager;
import krilliondefense.powerups.PowerUp;
import krilliondefense.powerups.RewardFactory;
import krilliondefense.trivia.AnswerBank;
import krilliondefense.trivia.AnswerResult;
import krilliondefense.trivia.Question;
import krilliondefense.trivia.QuestionManager;
import krilliondefense.trivia.RarityTier;

/**
 * Where the game runs. Has the game loop, draws everything and handles input.
 * Layout: map in the top left, tower shop along the bottom, info on the right.
 */
public class GamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    /** Map width in pixels. */
    private static final int MAP_W = GameMap.COLS * GameMap.TILE;
    /** Map height in pixels. */
    private static final int MAP_H = GameMap.ROWS * GameMap.TILE;
    /** Width of the info panel on the right. */
    private static final int SIDEBAR_W = 230;
    /** Height of the tower shop at the bottom. */
    private static final int SHOP_H = 110;
    /** Total width of the panel. */
    private static final int WIDTH = MAP_W + SIDEBAR_W;
    /** Total height of the panel. */
    private static final int HEIGHT = MAP_H + SHOP_H;

    /** How long the "question incoming" banner shows (90 ticks = 1.5 s). */
    private static final int WARNING_TICKS = 90; // ~1.5 s "question incoming" banner

    /** Sidebar background color. */
    private static final Color SIDEBAR_BG = new Color(35, 38, 48);
    /** Background color for cards and boxes. */
    private static final Color CARD_BG = new Color(50, 54, 68);
    /** Main highlight color. */
    private static final Color ORANGE = new Color(255, 170, 40);
    /** Color used for gold. */
    private static final Color GOLD = new Color(255, 205, 60);

    // shop: the 4 towers you can buy, in order (keys 1-4)
    /** Tower ids in shop order (keys 1-4). */
    private static final String[] TOWER_TYPES = {"arrow", "frost", "cannon", "scholar"};
    /** Tower names shown in the shop. */
    private static final String[] TOWER_NAMES = {"Arrow", "Frost", "Cannon", "Scholar"};
    /** One line description of each tower for the shop. */
    private static final String[] TOWER_INFO = {
        "Fast single shots",
        "Slows enemies 40%",
        "Explodes, hits groups",
        "Grows with Knowledge"
    };
    /** Click areas for the 4 shop cards. */
    private Rectangle[] shopCards = new Rectangle[4];
    /** Start Wave button. */
    private Rectangle startWaveButton = new Rectangle(MAP_W + 15, 230, SIDEBAR_W - 30, 46);
    /** Speed (1x / 2x) button. */
    private Rectangle speedButton = new Rectangle(MAP_W + 15, 284, SIDEBAR_W - 30, 28);
    // used for both "Start Game" and "Play Again"
    /** Big button used for "Start Game" and "Play Again". */
    private Rectangle bigButton = new Rectangle(WIDTH / 2 - 110, 490, 220, 52);
    // difficulty choices on the menu
    /** Click areas for the 3 difficulty cards on the menu. */
    private Rectangle[] difficultyCards = new Rectangle[3];
    /** Small map previews for the difficulty cards. */
    private GameMap[] previewMaps = new GameMap[3];
    /** Difficulty picked on the menu. */
    private Difficulty difficulty = Difficulty.EASY;

    /** Game loop timer (about 60 ticks per second). */
    private javax.swing.Timer timer;
    /** Gold, lives, wave and phase. */
    private GameState state;
    /** The map for the current game. */
    private GameMap map;
    /** Spawns the enemies for each wave. */
    private WaveManager waveManager;
    /** Picks questions and scores answers. */
    private QuestionManager questionManager;
    /** Towers on the map. */
    private List<Tower> towers = new ArrayList<>();
    /** Enemies on the map. */
    private List<Enemy> enemies = new ArrayList<>();
    /** Shots that are flying. */
    private List<Projectile> projectiles = new ArrayList<>();
    /** Floating texts like "+5g". */
    private List<FloatingText> texts = new ArrayList<>();
    /** Ring effects (explosions, upgrades). */
    private List<RingEffect> rings = new ArrayList<>();

    /** The popup for questions. */
    private QuestionDialog questionDialog;
    /** Shop tower you are about to place ("arrow", "frost", ...) or null. */
    private String selectedTower = null; // shop tower you're about to place ("arrow", "frost", ...) or null
    /** Tower on the map you clicked on (to upgrade or sell), or null. */
    private Tower selectedPlaced = null;
    /** True after the first Sell click (selling needs two clicks). */
    private boolean confirmSell = false;
    /** Upgrade button in the tower panel. */
    private Rectangle upgradeButton = new Rectangle(MAP_W + 15, 466, 98, 32);
    /** Sell button in the tower panel. */
    private Rectangle sellButton = new Rectangle(MAP_W + 117, 466, 98, 32);

    // banner at the top of the map that says what just happened (like what power-up you got)
    /** Text of the banner at the top of the map. */
    private String bannerText = "";
    /** Color of the banner. */
    private Color bannerColor = Color.WHITE;
    /** Ticks left to show the banner. */
    private int bannerTicks;
    /** Last message shown in the sidebar. */
    private String message = "";
    /** Mouse x (-1 if the mouse is off the panel). */
    private int mouseX = -1;
    /** Mouse y (-1 if the mouse is off the panel). */
    private int mouseY = -1;
    /** Game speed: 1 = normal, 2 = fast forward. */
    private int speed = 1;
    /** Ticks left of the red flash when you lose a life. */
    private int damageFlash;
    /** Ticks left of the white flash from the Knowledge Blast. */
    private int blastFlash;

    /** Counts down while the "question incoming" banner shows. */
    private int questionWarning = 0;  // counts down while the "incoming" banner shows
    /** How many questions this wave has. */
    private int questionsThisWave;
    /** How many have been asked so far this wave. */
    private int questionsAsked;

    // for the end screen
    /** Valid answers this game (for the end screen). */
    private int answersRight;
    /** Rarest answer tier this game. */
    private RarityTier bestTier = RarityTier.INVALID;
    /** Rarest answer this game. */
    private String bestAnswer = "";

    /**
     * Sets up the game and starts the timer.
     *
     * @param frame the window (needed for the question popup)
     */
    public GamePanel(JFrame frame) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        addMouseMotionListener(this);
        for (int i = 0; i < shopCards.length; i++) {
            shopCards[i] = new Rectangle(8 + i * 198, MAP_H + 10, 190, SHOP_H - 20);
        }
        for (int i = 0; i < 3; i++) {
            difficultyCards[i] = new Rectangle(WIDTH / 2 - 480 + i * 325, 262, 310, 170);
            previewMaps[i] = GameMap.forDifficulty(Difficulty.values()[i]);
        }
        setupKeys();

        state = new GameState();
        map = new GameMap();
        waveManager = new WaveManager(map);
        questionManager = loadTrivia();
        questionDialog = new QuestionDialog(frame);

        timer = new javax.swing.Timer(16, this); // ~60 fps
        timer.start();
    }

    /** Loads all the questions and answers from answers.csv. */
    private QuestionManager loadTrivia() {
        AnswerBank bank = new AnswerBank();
        try {
            bank.loadFromStream(getClass().getResourceAsStream("/answers.csv"));
        } catch (IOException | NullPointerException e) {
            System.out.println("Problem loading answers.csv: " + e);
        }
        return new QuestionManager(bank);
    }

    /** Keyboard shortcuts: 1-4 pick a tower, space starts the wave, esc cancels, s = speed. */
    private void setupKeys() {
        for (int i = 0; i < TOWER_TYPES.length; i++) {
            final String type = TOWER_TYPES[i];
            final int index = i;
            bindKey(KeyStroke.getKeyStroke((char) ('1' + i)), "tower" + i, () -> {
                if (state.getPhase() == GamePhase.MENU && index < 3) {
                    difficulty = Difficulty.values()[index]; // on the menu 1-3 pick the difficulty
                } else {
                    selectShopTower(type);
                }
            });
        }
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "space", () -> {
            if (state.getPhase() == GamePhase.BUILD) {
                startWave();
            } else if (state.getPhase() == GamePhase.MENU) {
                startGame();
            }
        });
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc", this::clearSelection);
        bindKey(KeyStroke.getKeyStroke('s'), "speed", this::toggleSpeed);
    }

    /** Connects a key to an action (works no matter what has focus in the window). */
    private void bindKey(KeyStroke key, String name, Runnable action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (state.getPhase() != GamePhase.QUESTION) {
                    action.run();
                    repaint();
                }
            }
        });
    }

    /**
     * Called by the timer every tick.
     *
     * @param e timer event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // the game freezes as soon as the "question incoming" banner shows
        for (int i = 0; i < speed; i++) {
            if (state.getPhase() == GamePhase.WAVE && questionWarning == 0) {
                updateGame();
            }
        }
        texts.removeIf(t -> !t.update());
        rings.removeIf(r -> !r.update());
        if (damageFlash > 0) {
            damageFlash--;
        }
        if (blastFlash > 0) {
            blastFlash--;
        }
        if (bannerTicks > 0 && questionWarning == 0) {
            bannerTicks--;
        }
        // show the warning banner for a bit, then pop up the question
        if (questionWarning > 0) {
            questionWarning--;
            if (questionWarning == 0) {
                timer.stop(); // game pauses
                SwingUtilities.invokeLater(this::askQuestion);
            }
        }
        repaint();
    }

    /** One tick of the wave: spawn -> move -> shoot -> hit -> check end. */
    private void updateGame() {
        waveManager.update(enemies);

        for (Enemy en : enemies) {
            en.move(map);
        }
        for (Tower t : towers) {
            t.update(enemies, projectiles);
        }
        for (Projectile p : projectiles) {
            p.update();
            if (p.hasHit() && p.didImpact() && p.getSplashRadius() > 0) {
                rings.add(new RingEffect(p.getX(), p.getY(), p.getSplashRadius(), ORANGE, 18));
            }
        }
        projectiles.removeIf(Projectile::hasHit);
        removeDeadEnemies();

        if (state.isGameOver()) {
            questionWarning = 0;
            state.setPhase(GamePhase.GAME_OVER);
            return;
        }

        // questions pop up when part of the wave has spawned
        // (waves 1-3 get one question, wave 4+ get two)
        if (questionsAsked < questionsThisWave) {
            double[] when = {0.35, 0.8};
            int spawned = waveManager.getSpawnedCount();
            if (spawned >= Math.ceil(waveManager.getTotalThisWave() * when[questionsAsked])) {
                questionsAsked++;
                questionWarning = WARNING_TICKS;
            }
        }

        if (waveManager.isWaveFinished() && enemies.isEmpty()) {
            endWave();
        }
    }

    /** Gives gold for kills and takes lives for enemies that got through. */
    private void removeDeadEnemies() {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy en = it.next();
            if (en.isDead()) {
                state.addGold(en.getReward());
                texts.add(new FloatingText("+" + en.getReward() + "g", en.getX(), en.getY() - 10, GOLD));
                it.remove();
            } else if (en.reachedEnd()) {
                state.loseLives(en.getLivesCost());
                texts.add(new FloatingText("-" + en.getLivesCost() + " life", en.getX() - 20, en.getY() - 20,
                        new Color(255, 90, 90)));
                damageFlash = 20;
                it.remove();
            }
        }
    }

    /** Called when every enemy in the wave is gone. Gives bonus gold or ends the game. */
    private void endWave() {
        projectiles.clear();
        state.setDamageBoost(false); // damage boost only lasts one wave
        for (Tower t : towers) {
            t.setBoosted(false);
        }
        if (state.getWaveNumber() >= GameState.MAX_WAVES) {
            questionWarning = 0;
            state.setPhase(GamePhase.VICTORY);
        } else {
            int bonus = 15 + state.getWaveNumber() * 5;
            state.addGold(bonus);
            message = "Wave " + state.getWaveNumber() + " cleared! +" + bonus
                    + " bonus gold. Build up, then press Start Wave.";
            showBanner("Wave " + state.getWaveNumber() + " cleared!  +" + bonus + " bonus gold", GOLD, 150);
            state.setPhase(GamePhase.BUILD);
        }
    }

    /** Starts the next wave and figures out how many questions it gets. */
    private void startWave() {
        if (questionWarning > 0) {
            return;
        }
        state.nextWave();
        waveManager.startWave(state.getWaveNumber());
        state.setPhase(GamePhase.WAVE);
        questionsAsked = 0;
        questionsThisWave = state.getWaveNumber() >= 4 ? 2 : 1;
        message = "Wave " + state.getWaveNumber() + " started! Get ready for "
                + (questionsThisWave == 1 ? "a question" : "2 questions") + " this wave.";
        showBanner("Wave " + state.getWaveNumber() + " - " + (questionsThisWave == 1 ? "1 question" : "2 questions")
                + " coming up", Color.WHITE, 120);
    }

    /** Resets everything and starts a new game on the picked difficulty. */
    private void startGame() {
        state.reset(difficulty);
        map = GameMap.forDifficulty(difficulty);
        waveManager = new WaveManager(map, difficulty);
        towers.clear();
        enemies.clear();
        projectiles.clear();
        texts.clear();
        rings.clear();
        questionManager.reset();
        selectedTower = null;
        selectedPlaced = null;
        bannerTicks = 0;
        speed = 1;
        answersRight = 0;
        bestTier = RarityTier.INVALID;
        bestAnswer = "";
        message = "Build your defense first! Pick a tower in the shop below, then click the grass.";
        state.setPhase(GamePhase.BUILD);
    }

    /** Switches between normal and 2x speed. */
    private void toggleSpeed() {
        speed = speed == 1 ? 2 : 1;
    }

    /** Shows a question, scores it and gives the reward (see sequence diagram) */
    private void askQuestion() {
        GamePhase comeBackTo = state.getPhase();
        state.setPhase(GamePhase.QUESTION);
        paintImmediately(0, 0, getWidth(), getHeight()); // shows the PAUSED screen behind the popup

        Question q = questionManager.nextQuestion();
        String input = questionDialog.showQuestion(q);
        AnswerResult result = questionManager.submitAnswer(q, input);

        String examples;
        if (result.isValid()) {
            RarityTier tier = result.getTier();
            state.addKnowledgePoints(result.getScore());
            PowerUp p = RewardFactory.create(tier);
            p.apply(state, towers, enemies);
            showRewardEffect(tier, p.getDescription());
            answersRight++;
            if (tier.getLevel() > bestTier.getLevel()) {
                bestTier = tier;
                bestAnswer = result.getMatchedAnswer();
            }
            message = tier.getLabel() + " answer! " + p.getDescription() + ".";
            examples = makeExamples(q, tier, result.getMatchedAnswer());
        } else {
            message = result.getMessage() + " - no reward this time.";
            showBanner("No reward: " + result.getMessage(), new Color(230, 110, 110), 150);
            examples = makeExamples(q, RarityTier.INVALID, null);
        }
        questionDialog.showResult(result, examples);

        state.setPhase(comeBackTo);
        timer.start(); // game resumes
    }

    /** The "you could have said..." line on the result screen. */
    private String makeExamples(Question q, RarityTier tier, String skip) {
        String cat = q.getCategory();
        if (tier == RarityTier.INVALID) {
            String[] names = {"", "Common", "Uncommon", "Rare", "Epic", "Legendary"};
            List<String> parts = new ArrayList<>();
            for (int lvl = 1; lvl <= 5; lvl += 2) {
                for (String a : questionManager.getExamples(cat, lvl, 1, null)) {
                    parts.add("<b>" + QuestionDialog.niceName(a) + "</b> (" + names[lvl] + ")");
                }
            }
            return "Answers that would have worked: " + String.join(", ", parts);
        }
        List<String> legendary = questionManager.getExamples(cat, 5, 3, skip);
        if (tier == RarityTier.LEGENDARY) {
            return "Other Legendary answers: <b>" + joinNice(legendary) + "</b>";
        }
        return "Want Legendary next time? Answers like: <b>" + joinNice(legendary) + "</b>";
    }

    /** Turns a list of answers into "A, B, C" with capital letters. */
    private String joinNice(List<String> list) {
        List<String> nice = new ArrayList<>();
        for (String s : list) {
            nice.add(QuestionDialog.niceName(s));
        }
        return String.join(", ", nice);
    }

    /** Shows what the power-up did on the map. */
    private void showRewardEffect(RarityTier tier, String description) {
        Color c = QuestionDialog.tierColor(tier);
        showBanner(tier.getLabel() + " answer!  " + description, c, 220);
        if (tier == RarityTier.LEGENDARY) {
            blastFlash = 25;
            for (Enemy en : enemies) {
                rings.add(new RingEffect(en.getX(), en.getY(), 30, c, 25));
            }
        } else if (tier == RarityTier.EPIC) {
            for (Enemy en : enemies) {
                rings.add(new RingEffect(en.getX(), en.getY(), 22, new Color(150, 220, 255), 30));
            }
        }
    }

    /** Shows a banner at the top of the map for a few seconds. */
    private void showBanner(String text, Color color, int ticks) {
        bannerText = text;
        bannerColor = color;
        bannerTicks = ticks;
    }

    /** Cancels the shop tower you were placing and closes the tower panel. */
    private void clearSelection() {
        selectedTower = null;
        selectedPlaced = null;
        confirmSell = false;
    }

    // ---------------------------------------------------------------- input

    /**
     * Handles clicks. (We use mousePressed because mouseClicked doesn't fire
     * if the mouse moves a tiny bit while clicking, which felt broken.)
     *
     * @param e mouse event
     */
    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        GamePhase phase = state.getPhase();

        if (phase == GamePhase.MENU) {
            for (int i = 0; i < difficultyCards.length; i++) {
                if (difficultyCards[i].contains(mx, my)) {
                    difficulty = Difficulty.values()[i];
                }
            }
            if (bigButton.contains(mx, my)) {
                startGame();
            }
            return;
        }
        if (phase == GamePhase.GAME_OVER || phase == GamePhase.VICTORY) {
            if (bigButton.contains(mx, my)) {
                state.setPhase(GamePhase.MENU);
            }
            return;
        }
        if (phase != GamePhase.BUILD && phase != GamePhase.WAVE) {
            return;
        }

        boolean onMap = mx < MAP_W && my < MAP_H;
        // right click always cancels whatever is selected
        if (SwingUtilities.isRightMouseButton(e)) {
            clearSelection();
            return;
        }

        for (int i = 0; i < shopCards.length; i++) {
            if (shopCards[i].contains(mx, my)) {
                selectShopTower(TOWER_TYPES[i]);
                return;
            }
        }
        if (selectedPlaced != null && upgradeButton.contains(mx, my)) {
            upgradeSelected();
        } else if (selectedPlaced != null && sellButton.contains(mx, my)) {
            if (confirmSell) {
                sellTower(selectedPlaced);
            } else {
                confirmSell = true;
                message = "Click Sell again to sell this tower for " + selectedPlaced.getSellValue() + " gold.";
            }
        } else if (startWaveButton.contains(mx, my) && phase == GamePhase.BUILD) {
            clearSelection();
            startWave();
        } else if (speedButton.contains(mx, my)) {
            toggleSpeed();
        } else if (onMap) {
            clickMap(mx / GameMap.TILE, my / GameMap.TILE);
        }
    }

    /** Clicking the map: pick a tower that's there, or build the shop tower you picked. */
    private void clickMap(int col, int row) {
        Tower existing = towerAt(col, row);
        if (existing != null) {
            selectedTower = null;
            selectedPlaced = existing;
            confirmSell = false;
            message = existing.getName() + " selected. Use the buttons on the right to upgrade or sell it.";
        } else if (selectedTower != null) {
            placeTower(col, row);
        } else {
            selectedPlaced = null; // clicked empty ground, close the tower panel
        }
    }

    /** Picks a tower from the shop so you can place it on the map. */
    private void selectShopTower(String type) {
        if (state.getPhase() != GamePhase.BUILD && state.getPhase() != GamePhase.WAVE) {
            return;
        }
        if (type.equals("scholar") && !state.isScholarUnlocked()) {
            message = "Scholar Tower is locked. Get a LEGENDARY answer to unlock it!";
            return;
        }
        selectedPlaced = null;
        if (type.equals(selectedTower)) {
            selectedTower = null; // click again to unselect
            return;
        }
        confirmSell = false;
        if (state.getGold() < costOf(type)) {
            message = "Not enough gold for that tower yet. Kill enemies or answer questions for more.";
            return;
        }
        selectedTower = type;
        message = "Click a green spot on the grass to build it. Right click or Esc to cancel.";
    }

    /** Makes a tower of the given type (used for placing and for the preview) */
    private Tower makeTower(String type, int cx, int cy) {
        if (type.equals("arrow")) {
            return new ArrowTower(cx, cy);
        } else if (type.equals("frost")) {
            return new FrostTower(cx, cy);
        } else if (type.equals("cannon")) {
            return new CannonTower(cx, cy);
        } else {
            return new ScholarTower(cx, cy, state);
        }
    }

    /** Gold cost of a tower type. */
    private int costOf(String type) {
        if (type.equals("arrow")) {
            return ArrowTower.COST;
        } else if (type.equals("frost")) {
            return FrostTower.COST;
        } else if (type.equals("cannon")) {
            return CannonTower.COST;
        }
        return ScholarTower.COST;
    }

    /** Finds the tower on a tile, or returns null if there isn't one. */
    private Tower towerAt(int col, int row) {
        int cx = col * GameMap.TILE + GameMap.TILE / 2;
        int cy = row * GameMap.TILE + GameMap.TILE / 2;
        for (Tower t : towers) {
            if (t.getX() == cx && t.getY() == cy) {
                return t;
            }
        }
        return null;
    }

    /** Sells a tower and gives back some of the gold. */
    private void sellTower(Tower t) {
        state.addGold(t.getSellValue());
        towers.remove(t);
        selectedPlaced = null;
        texts.add(new FloatingText("+" + t.getSellValue() + "g", t.getX(), t.getY() - 10, GOLD));
        message = "Sold " + t.getName() + " for " + t.getSellValue() + " gold.";
    }

    /** Upgrades the tower you clicked on if you have enough gold. */
    private void upgradeSelected() {
        Tower t = selectedPlaced;
        confirmSell = false;
        if (t.getLevel() >= Tower.MAX_LEVEL) {
            message = "That tower is already max level.";
        } else if (state.spendGold(t.getUpgradeCost())) {
            t.upgrade();
            rings.add(new RingEffect(t.getX(), t.getY(), 24, GOLD, 18));
            message = t.getName() + " upgraded to level " + t.getLevel() + "!";
        } else {
            message = "Need " + t.getUpgradeCost() + " gold to upgrade.";
        }
    }

    /** Builds the tower picked in the shop on this tile. */
    private void placeTower(int col, int row) {
        if (!map.isBuildable(col, row)) {
            message = "Can't build on the path. Pick a green spot on the grass.";
            return;
        }
        if (state.spendGold(costOf(selectedTower))) {
            int cx = col * GameMap.TILE + GameMap.TILE / 2;
            int cy = row * GameMap.TILE + GameMap.TILE / 2;
            Tower t = makeTower(selectedTower, cx, cy);
            t.setBoosted(state.isDamageBoost());
            towers.add(t);
            rings.add(new RingEffect(cx, cy, 22, Color.WHITE, 15));
            message = t.getName() + " built for " + costOf(selectedTower)
                    + " gold! Click it any time to upgrade or sell.";
            selectedTower = null; // one tower per pick, so the preview doesn't keep following the mouse
        } else {
            message = "Not enough gold for that tower.";
            selectedTower = null;
        }
    }

    // ---------------------------------------------------------------- drawing

    /**
     * Draws everything.
     *
     * @param g graphics
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (state.getPhase() == GamePhase.MENU) {
            drawMenu(g2);
            return;
        }

        map.draw(g2);
        drawPlacementGrid(g2);
        for (Tower t : towers) {
            t.draw(g2);
        }
        for (Enemy en : enemies) {
            en.draw(g2);
        }
        for (Projectile p : projectiles) {
            p.draw(g2);
        }
        for (RingEffect r : rings) {
            r.draw(g2);
        }
        drawPlacementPreview(g2);
        for (FloatingText t : texts) {
            t.draw(g2);
        }
        drawMapOverlays(g2);
        drawShop(g2);
        drawSidebar(g2);

        if (questionWarning > 0) {
            drawQuestionWarning(g2);
        } else if (state.getPhase() == GamePhase.QUESTION) {
            drawPaused(g2);
        }
        if (state.getPhase() == GamePhase.GAME_OVER) {
            drawEndScreen(g2, "GAME OVER", new Color(230, 80, 80));
        } else if (state.getPhase() == GamePhase.VICTORY) {
            drawEndScreen(g2, "YOU WIN!", GOLD);
        }
        Toolkit.getDefaultToolkit().sync(); // makes animation smoother on some computers
    }

    /** Red flash when you lose lives, white flash for the blast, blue tint when frozen. */
    private void drawMapOverlays(Graphics2D g) {
        if (damageFlash > 0) {
            g.setColor(new Color(255, 0, 0, damageFlash * 4));
            g.setStroke(new BasicStroke(8));
            g.drawRect(4, 4, MAP_W - 8, MAP_H - 8);
            g.setStroke(new BasicStroke(1));
        }
        if (blastFlash > 0) {
            g.setColor(new Color(255, 240, 180, blastFlash * 6));
            g.fillRect(0, 0, MAP_W, MAP_H);
        }
        if (anyFrozen()) {
            g.setColor(new Color(150, 210, 255, 35));
            g.fillRect(0, 0, MAP_W, MAP_H);
        }
        if (bannerTicks > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            int w = g.getFontMetrics().stringWidth(bannerText) + 40;
            int alpha = Math.min(255, bannerTicks * 8);
            g.setColor(new Color(20, 22, 30, Math.min(220, alpha)));
            g.fillRoundRect(MAP_W / 2 - w / 2 - 4, 8, w + 8, 42, 16, 16);
            g.setColor(new Color(bannerColor.getRed(), bannerColor.getGreen(), bannerColor.getBlue(), alpha));
            g.fillRoundRect(MAP_W / 2 - w / 2, 12, w, 34, 14, 14);
            g.setColor(new Color(0, 0, 0, alpha));
            centerText(g, bannerText, MAP_W / 2, 35);
        }
        if (speed == 2 && state.getPhase() == GamePhase.WAVE) {
            drawPill(g, "2x SPEED", MAP_W - 90, 8, 80, ORANGE, Color.BLACK);
        }
    }

    /** True if any enemy is frozen right now. */
    private boolean anyFrozen() {
        for (Enemy en : enemies) {
            if (en.isFrozen()) {
                return true;
            }
        }
        return false;
    }

    /** Faint outline on every buildable tile while you're placing a tower. */
    private void drawPlacementGrid(Graphics2D g) {
        if (selectedTower == null) {
            return;
        }
        g.setColor(new Color(255, 255, 255, 35));
        for (int r = 0; r < GameMap.ROWS; r++) {
            for (int c = 0; c < GameMap.COLS; c++) {
                if (map.isBuildable(c, r) && towerAt(c, r) == null) {
                    g.drawRect(c * GameMap.TILE + 2, r * GameMap.TILE + 2, GameMap.TILE - 4, GameMap.TILE - 4);
                }
            }
        }
    }

    /** While placing: green/red square + range circle so you can see where the tower goes. also shows the range of the tower you clicked on. */
    private void drawPlacementPreview(Graphics2D g) {
        if (selectedPlaced != null) {
            drawRange(g, selectedPlaced.getX(), selectedPlaced.getY(), selectedPlaced.getRange(), Color.WHITE);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(selectedPlaced.getX() - 19, selectedPlaced.getY() - 19, 38, 38, 12, 12);
            g.setStroke(new BasicStroke(1));
        }
        if (selectedTower == null || mouseX < 0 || mouseX >= MAP_W || mouseY < 0 || mouseY >= MAP_H) {
            return;
        }
        int col = mouseX / GameMap.TILE;
        int row = mouseY / GameMap.TILE;
        int cx = col * GameMap.TILE + GameMap.TILE / 2;
        int cy = row * GameMap.TILE + GameMap.TILE / 2;
        boolean ok = map.isBuildable(col, row) && towerAt(col, row) == null;
        g.setColor(ok ? new Color(0, 255, 0, 70) : new Color(255, 0, 0, 70));
        g.fillRect(col * GameMap.TILE, row * GameMap.TILE, GameMap.TILE, GameMap.TILE);
        g.setColor(ok ? Color.GREEN : Color.RED);
        g.setStroke(new BasicStroke(2));
        g.drawRect(col * GameMap.TILE, row * GameMap.TILE, GameMap.TILE, GameMap.TILE);
        g.setStroke(new BasicStroke(1));

        // the see-through tower sits on the tile it would be built on
        Tower ghost = makeTower(selectedTower, cx, cy);
        drawRange(g, cx, cy, ghost.getRange(), ok ? Color.WHITE : Color.RED);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        ghost.draw(g);
        g.setComposite(old);
    }

    /** Draws a see-through circle showing how far a tower can shoot. */
    private void drawRange(Graphics2D g, int x, int y, double range, Color c) {
        int r = (int) range;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 35));
        g.fillOval(x - r, y - r, r * 2, r * 2);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 180));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x - r, y - r, r * 2, r * 2);
        g.setStroke(new BasicStroke(1));
    }

    /** The tower shop along the bottom of the screen. */
    private void drawShop(Graphics2D g) {
        g.setColor(new Color(28, 30, 38));
        g.fillRect(0, MAP_H, MAP_W, SHOP_H);

        for (int i = 0; i < shopCards.length; i++) {
            Rectangle r = shopCards[i];
            String type = TOWER_TYPES[i];
            boolean locked = type.equals("scholar") && !state.isScholarUnlocked();
            boolean canAfford = state.getGold() >= costOf(type);
            boolean selected = type.equals(selectedTower);
            boolean hover = r.contains(mouseX, mouseY);

            g.setColor(selected ? new Color(80, 70, 40) : hover ? new Color(62, 66, 82) : CARD_BG);
            g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
            g.setColor(selected ? ORANGE : new Color(80, 85, 100));
            g.setStroke(new BasicStroke(selected ? 3 : 1));
            g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
            g.setStroke(new BasicStroke(1));

            // picture of the tower
            makeTower(type, r.x + 32, r.y + 38).draw(g);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.drawString(TOWER_NAMES[i], r.x + 62, r.y + 22);
            g.setColor(new Color(160, 165, 180));
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("[" + (i + 1) + "]", r.x + r.width - 26, r.y + 18);
            g.setColor(new Color(200, 205, 220));
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString(TOWER_INFO[i], r.x + 62, r.y + 42);

            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            if (locked) {
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
                drawPill(g, "LOCKED", r.x + 62, r.y + 54, 64, new Color(120, 120, 130), Color.WHITE);
                g.setColor(QuestionDialog.tierColor(RarityTier.LEGENDARY));
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                g.drawString("Needs a Legendary answer", r.x + 12, r.y + 86);
            } else {
                drawCoin(g, r.x + 70, r.y + 66);
                g.setColor(canAfford ? GOLD : new Color(240, 90, 90));
                g.drawString(costOf(type) + " gold", r.x + 80, r.y + 71);
            }
        }
    }

    /** Draws the info panel on the right (stats, wave info, buttons, messages). */
    private void drawSidebar(Graphics2D g) {
        int x = MAP_W;
        g.setColor(SIDEBAR_BG);
        g.fillRect(x, 0, SIDEBAR_W, HEIGHT);

        // stats
        g.setColor(CARD_BG);
        g.fillRoundRect(x + 10, 10, SIDEBAR_W - 20, 120, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        drawCoin(g, x + 30, 35);
        g.setColor(GOLD);
        g.drawString(state.getGold() + " gold", x + 45, 41);
        drawHeart(g, x + 30, 62);
        g.setColor(new Color(255, 120, 120));
        g.drawString(state.getLives() + " lives", x + 45, 68);
        g.setColor(Color.WHITE);
        g.drawString("Wave " + state.getWaveNumber() + " / " + GameState.MAX_WAVES, x + 22, 95);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(difficultyColor(difficulty));
        g.drawString(difficulty.getLabel().toUpperCase(), x + 150, 95);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.setColor(new Color(200, 160, 255));
        g.drawString("Knowledge " + state.getKnowledgePoints(), x + 22, 120);

        // what's happening right now
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        if (state.getPhase() == GamePhase.BUILD) {
            g.setColor(new Color(180, 185, 200));
            g.drawString("NEXT WAVE:", x + 15, 152);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(Color.WHITE);
            g.drawString(nextWaveText(), x + 15, 170);
            g.drawString("Questions: " + (state.getWaveNumber() + 1 >= 4 ? 2 : 1)
                    + " during the wave", x + 15, 188);
            drawButton(g, startWaveButton, "START WAVE " + (state.getWaveNumber() + 1) + "  (Space)",
                    new Color(100, 210, 110));
        } else {
            g.setColor(new Color(180, 185, 200));
            g.drawString("WAVE IN PROGRESS", x + 15, 152);
            int left = enemies.size() + waveManager.getRemainingToSpawn();
            int total = Math.max(1, waveManager.getTotalThisWave());
            g.setColor(CARD_BG);
            g.fillRoundRect(x + 15, 162, SIDEBAR_W - 30, 14, 14, 14);
            g.setColor(new Color(230, 90, 80));
            g.fillRoundRect(x + 15, 162, (SIDEBAR_W - 30) * left / total, 14, 14, 14);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(Color.WHITE);
            g.drawString(left + " enemies left", x + 15, 194);
            // dots for questions: orange = still coming, grey = done
            g.drawString("Questions:", x + 15, 214);
            for (int i = 0; i < questionsThisWave; i++) {
                g.setColor(i < questionsAsked ? new Color(110, 110, 120) : ORANGE);
                g.fillOval(x + 85 + i * 18, 203, 12, 12);
            }
            g.setColor(new Color(70, 74, 90));
            g.fillRoundRect(startWaveButton.x, startWaveButton.y, startWaveButton.width,
                    startWaveButton.height, 12, 12);
            g.setColor(new Color(170, 175, 190));
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            centerText(g, "Defend!", startWaveButton.x + startWaveButton.width / 2, startWaveButton.y + 29);
        }
        drawButton(g, speedButton, "Speed: " + speed + "x  (S)", speed == 2 ? ORANGE : new Color(150, 155, 170));

        // active power-ups
        g.setColor(new Color(180, 185, 200));
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("ACTIVE POWER-UPS:", x + 15, 336);
        int y = 344;
        boolean none = true;
        if (state.isDamageBoost()) {
            drawPill(g, "+50% DAMAGE (this wave)", x + 15, y, SIDEBAR_W - 30, QuestionDialog.tierColor(RarityTier.RARE),
                    Color.WHITE);
            y += 25;
            none = false;
        }
        if (anyFrozen()) {
            drawPill(g, "ENEMIES FROZEN", x + 15, y, SIDEBAR_W - 30, QuestionDialog.tierColor(RarityTier.EPIC),
                    Color.WHITE);
            none = false;
        }
        if (none) {
            g.setColor(new Color(120, 125, 140));
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.drawString("None yet - answer questions!", x + 15, 360);
        }

        // bottom box: the tower you clicked, the tower you're placing, or the last message
        g.setColor(CARD_BG);
        g.fillRoundRect(x + 10, 395, SIDEBAR_W - 20, 112, 14, 14);
        if (selectedPlaced != null) {
            drawTowerPanel(g, selectedPlaced, x);
        } else if (selectedTower != null) {
            int i = java.util.Arrays.asList(TOWER_TYPES).indexOf(selectedTower);
            g.setColor(ORANGE);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.drawString("Placing: " + TOWER_NAMES[i] + " Tower", x + 20, 418);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            drawWrapped(g, "Click a green spot on the grass to build it for " + costOf(selectedTower)
                    + " gold. Right click or Esc to cancel.", x + 20, 440, SIDEBAR_W - 40);
        } else {
            g.setColor(new Color(255, 225, 120));
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            drawWrapped(g, message, x + 20, 416, SIDEBAR_W - 40);
        }

        g.setColor(new Color(150, 155, 170));
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("Click a tower to upgrade or sell it", x + 12, 530);
        g.drawString("1-4: buy tower   Esc/right click: cancel", x + 12, 546);
        g.drawString("Space: start wave   S: speed", x + 12, 562);
    }

    /** Info + upgrade/sell buttons for the tower you clicked on. */
    private void drawTowerPanel(Graphics2D g, Tower t, int x) {
        g.setColor(GOLD);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString(t.getName(), x + 20, 418);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("Level " + t.getLevel() + " / " + Tower.MAX_LEVEL, x + 20, 437);
        g.drawString("Damage " + t.getDamage() + "    Range " + (int) t.getRange(), x + 20, 454);

        boolean maxed = t.getLevel() >= Tower.MAX_LEVEL;
        boolean canAfford = state.getGold() >= t.getUpgradeCost();
        Color upColor = maxed || !canAfford ? new Color(110, 115, 125) : new Color(100, 210, 110);
        drawButton(g, upgradeButton, maxed ? "MAX LEVEL" : "Upgrade " + t.getUpgradeCost() + "g", upColor);
        drawButton(g, sellButton, confirmSell ? "Sure? Click again" : "Sell +" + t.getSellValue() + "g",
                confirmSell ? new Color(255, 80, 70) : new Color(235, 120, 100));
    }

    /** Short text about what enemies are in the next wave. */
    private String nextWaveText() {
        int w = state.getWaveNumber() + 1;
        if (w == GameState.MAX_WAVES) {
            return "Final wave + the BOSS!";
        } else if (w >= 4) {
            return "Basic, fast and TANK enemies";
        } else if (w == 3) {
            return "Basic + fast enemies";
        }
        return "Basic enemies";
    }

    /** Green for easy, orange for medium, red for hard. */
    private Color difficultyColor(Difficulty d) {
        if (d == Difficulty.EASY) {
            return new Color(110, 210, 110);
        } else if (d == Difficulty.MEDIUM) {
            return ORANGE;
        }
        return new Color(240, 80, 80);
    }

    /** Draws the main menu with the difficulty picker. */
    private void drawMenu(Graphics2D g) {
        map.drawScaled(g, 0, 0, getWidth(), getHeight());
        g.setColor(new Color(15, 18, 28, 220));
        g.fillRect(0, 0, getWidth(), getHeight());

        int cx = getWidth() / 2;
        g.setFont(new Font("SansSerif", Font.BOLD, 50));
        g.setColor(new Color(0, 0, 0, 150));
        centerText(g, "KRILLION DEFENSE", cx + 3, 73);
        g.setColor(ORANGE);
        centerText(g, "KRILLION DEFENSE", cx, 70);
        g.setFont(new Font("SansSerif", Font.PLAIN, 17));
        g.setColor(Color.WHITE);
        centerText(g, "A tower defense game where knowing obscure stuff saves you", cx, 102);

        // 3 steps
        String[][] steps = {
            {"1. BUILD", "Place towers next to the path."},
            {"2. ANSWER", "Questions pop up mid-wave (game pauses)."},
            {"3. GO NICHE", "Rarer answers = better rewards!"}
        };
        for (int i = 0; i < 3; i++) {
            int bx = cx - 480 + i * 325;
            g.setColor(CARD_BG);
            g.fillRoundRect(bx, 122, 310, 62, 14, 14);
            g.setColor(ORANGE);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString(steps[i][0], bx + 14, 146);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.drawString(steps[i][1], bx + 14, 168);
        }

        // difficulty picker with a little preview of each map
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(new Color(200, 205, 220));
        centerText(g, "Choose a difficulty:", cx, 248);
        for (int i = 0; i < 3; i++) {
            Difficulty d = Difficulty.values()[i];
            Rectangle r = difficultyCards[i];
            boolean picked = d == difficulty;
            boolean hover = r.contains(mouseX, mouseY);
            g.setColor(picked ? new Color(70, 64, 44) : hover ? new Color(62, 66, 82) : CARD_BG);
            g.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);
            g.setColor(picked ? difficultyColor(d) : new Color(80, 85, 100));
            g.setStroke(new BasicStroke(picked ? 4 : 1));
            g.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);
            g.setStroke(new BasicStroke(1));

            previewMaps[i].drawScaled(g, r.x + 12, r.y + 12, 140, 84);
            g.setColor(new Color(0, 0, 0, 80));
            g.drawRect(r.x + 12, r.y + 12, 140, 84);

            g.setColor(difficultyColor(d));
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString(d.getLabel().toUpperCase(), r.x + 164, r.y + 34);
            g.setColor(new Color(160, 165, 180));
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("[" + (i + 1) + "]", r.x + r.width - 28, r.y + 22);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString(d.getLives() + " lives", r.x + 164, r.y + 56);
            g.drawString(d.getStartGold() + " starting gold", r.x + 164, r.y + 73);
            String speed = d == Difficulty.EASY ? "Normal" : d == Difficulty.MEDIUM ? "Faster" : "Fastest";
            g.drawString(speed + " enemies", r.x + 164, r.y + 90);
            g.setColor(new Color(200, 205, 220));
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString(d.getDescription(), r.x + 14, r.y + 124);
            if (picked) {
                drawPill(g, "SELECTED", r.x + r.width / 2 - 45, r.y + 138, 90, difficultyColor(d), Color.BLACK);
            }
        }

        // what each answer level gives (short version)
        RarityTier[] tiers = {RarityTier.COMMON, RarityTier.UNCOMMON, RarityTier.RARE, RarityTier.EPIC,
            RarityTier.LEGENDARY};
        String[] shortRewards = {"+25 gold", "+60 gold", "+50% damage", "Freeze all", "Blast all!"};
        for (int i = 0; i < tiers.length; i++) {
            drawPill(g, tiers[i].getLabel() + ": " + shortRewards[i], cx - 480 + i * 194, 450, 184,
                    QuestionDialog.tierColor(tiers[i]), Color.BLACK);
        }

        drawButton(g, bigButton, "START " + difficulty.getLabel().toUpperCase() + " GAME", ORANGE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(170, 175, 190));
        centerText(g, "1-3 to pick, Space to start   -   " + questionManager.getQuestionCount()
                + " different questions", cx, 570);
    }

    /** Darkens the map and says the game is paused (behind the question popup). */
    private void drawPaused(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, MAP_W, MAP_H);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        centerText(g, "GAME PAUSED", MAP_W / 2, 60);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        centerText(g, "Answer the question to keep playing", MAP_W / 2, 90);
    }

    /** Big flashing banner so you know a question is about to pop up. */
    private void drawQuestionWarning(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, MAP_W, MAP_H);
        boolean flash = (questionWarning / 10) % 2 == 0;
        g.setColor(flash ? ORANGE : new Color(255, 210, 110));
        g.fillRoundRect(120, MAP_H / 2 - 60, MAP_W - 240, 120, 24, 24);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(120, MAP_H / 2 - 60, MAP_W - 240, 120, 24, 24);
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        centerText(g, "QUESTION TIME!", MAP_W / 2, MAP_H / 2 - 5);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        centerText(g, "Game paused. Get ready to type your answer!", MAP_W / 2, MAP_H / 2 + 30);
    }

    /** Draws the game over / you win screen with some stats. */
    private void drawEndScreen(Graphics2D g, String title, Color color) {
        g.setColor(new Color(10, 12, 20, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        int cx = getWidth() / 2;
        g.setFont(new Font("SansSerif", Font.BOLD, 54));
        g.setColor(color);
        centerText(g, title, cx, 150);

        g.setColor(CARD_BG);
        g.fillRoundRect(cx - 220, 190, 440, 200, 18, 18);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        int cleared = state.getPhase() == GamePhase.VICTORY ? state.getWaveNumber() : state.getWaveNumber() - 1;
        centerText(g, "Waves cleared: " + cleared + " / " + GameState.MAX_WAVES
                + "   (" + difficulty.getLabel() + ")", cx, 230);
        centerText(g, "Questions answered: " + answersRight, cx, 262);
        centerText(g, "Knowledge points: " + state.getKnowledgePoints(), cx, 294);
        if (bestTier != RarityTier.INVALID) {
            g.setColor(QuestionDialog.tierColor(bestTier));
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            centerText(g, "Best answer: " + QuestionDialog.niceName(bestAnswer) + " (" + bestTier.getLabel() + ")",
                    cx, 340);
        }
        drawButton(g, bigButton, "PLAY AGAIN", ORANGE);
    }

    // ---------------------------------------------------------------- small drawing helpers

    /** Draws a rounded button that gets lighter when the mouse is over it. */
    private void drawButton(Graphics2D g, Rectangle r, String text, Color color) {
        boolean hover = r.contains(mouseX, mouseY);
        g.setColor(hover ? color.brighter() : color);
        g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g.setColor(color.darker());
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, r.height > 40 ? 16 : 13));
        centerText(g, text, r.x + r.width / 2, r.y + r.height / 2 + 5);
    }

    /** Draws a small rounded label with text in the middle. */
    private void drawPill(Graphics2D g, String text, int x, int y, int w, Color bg, Color fg) {
        g.setColor(bg);
        g.fillRoundRect(x, y, w, 22, 12, 12);
        g.setColor(fg);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        centerText(g, text, x + w / 2, y + 16);
    }

    /** Draws a little gold coin icon. */
    private void drawCoin(Graphics2D g, int x, int y) {
        g.setColor(GOLD);
        g.fillOval(x - 7, y - 7, 14, 14);
        g.setColor(new Color(190, 140, 20));
        g.drawOval(x - 7, y - 7, 14, 14);
        g.drawOval(x - 4, y - 4, 8, 8);
    }

    /** Draws a little heart icon for lives. */
    private void drawHeart(Graphics2D g, int x, int y) {
        g.setColor(new Color(235, 70, 80));
        g.fillOval(x - 7, y - 6, 8, 8);
        g.fillOval(x - 1, y - 6, 8, 8);
        g.fillPolygon(new int[] {x - 7, x + 7, x}, new int[] {y - 1, y - 1, y + 7}, 3);
    }

    /** Draws text centered on an x position. */
    private void centerText(Graphics2D g, String text, int cx, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, cx - w / 2, y);
    }

    /** Splits long messages onto multiple lines so they fit. */
    private void drawWrapped(Graphics2D g, String text, int x, int y, int maxWidth) {
        String line = "";
        for (String word : text.split(" ")) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (g.getFontMetrics().stringWidth(test) > maxWidth) {
                g.drawString(line, x, y);
                y += 17;
                line = word;
            } else {
                line = test;
            }
        }
        g.drawString(line, x, y);
    }

    // ---------------------------------------------------------------- mouse

    /**
     * Keeps track of the mouse for the placement preview.
     *
     * @param e mouse event
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint(); // redraw right away so the preview keeps up with the mouse
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseX = -1;
        mouseY = -1;
        repaint();
    }

    // don't need these but MouseListener makes us have them
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }
}
