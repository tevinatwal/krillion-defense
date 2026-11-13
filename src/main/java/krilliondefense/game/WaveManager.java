package krilliondefense.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import krilliondefense.entities.BasicEnemy;
import krilliondefense.entities.BossEnemy;
import krilliondefense.entities.Enemy;
import krilliondefense.entities.FastEnemy;
import krilliondefense.entities.TankEnemy;

/**
 * Makes the enemies for each wave and spawns them one at a time.
 * On maps with more than one lane the enemies take turns picking a lane.
 */
public class WaveManager {
    /** How many of each enemy are in each wave on easy: {basic, fast, tank, boss}. */
    private static final int[][] WAVES = {
        {6, 0, 0, 0},
        {10, 0, 0, 0},
        {10, 5, 0, 0},
        {12, 7, 2, 0},
        {14, 9, 3, 0},
        {15, 11, 5, 0},
        {16, 13, 7, 0},
        {16, 13, 7, 1}
    };

    /** Enemies waiting to come out. */
    private Queue<Enemy> spawnQueue = new LinkedList<>();
    /** Ticks until the next enemy comes out. */
    private int spawnTimer;
    /** How many enemies are in this wave. */
    private int totalThisWave;
    /** The map (for lane start points). */
    private GameMap map;
    /** Difficulty (changes spawn speed and enemy count). */
    private Difficulty difficulty;

    /**
     * @param map the map (enemies start at its first waypoint)
     */
    public WaveManager(GameMap map) {
        this(map, Difficulty.EASY);
    }

    /**
     * @param map        the map
     * @param difficulty changes how many enemies come and how fast
     */
    public WaveManager(GameMap map, Difficulty difficulty) {
        this.map = map;
        this.difficulty = difficulty;
    }

    /**
     * Fills the spawn queue for a wave. Health goes up 25% each wave.
     *
     * @param waveNumber which wave (1-8)
     */
    public void startWave(int waveNumber) {
        spawnQueue.clear();
        spawnTimer = 0;
        int[] counts = WAVES[Math.min(waveNumber, WAVES.length) - 1];
        double hpScale = Math.pow(1.25, waveNumber - 1);
        double more = difficulty.getEnemyMultiplier();

        List<Enemy> list = new ArrayList<>();
        for (int i = 0; i < Math.round(counts[0] * more); i++) {
            list.add(new BasicEnemy(0, 0, hpScale));
        }
        for (int i = 0; i < Math.round(counts[1] * more); i++) {
            list.add(new FastEnemy(0, 0, hpScale));
        }
        for (int i = 0; i < Math.round(counts[2] * more); i++) {
            list.add(new TankEnemy(0, 0, hpScale));
        }
        // mix them up, but the first few are always basic so the wave starts easy
        Collections.shuffle(list.subList(Math.min(3, list.size()), list.size()), new Random(waveNumber));
        for (int i = 0; i < counts[3]; i++) {
            list.add(new BossEnemy(0, 0, hpScale)); // boss comes last
        }
        // take turns on the lanes
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setLane(i % map.getLaneCount(), map);
        }
        spawnQueue.addAll(list);
        totalThisWave = list.size();
    }

    /**
     * Spawns the next enemy when the timer runs out.
     *
     * @param enemies list of enemies on the map
     */
    public void update(List<Enemy> enemies) {
        if (spawnQueue.isEmpty()) {
            return;
        }
        spawnTimer--;
        if (spawnTimer <= 0) {
            enemies.add(spawnQueue.poll());
            spawnTimer = difficulty.getSpawnDelay();
        }
    }

    /** @return true when every enemy in the wave has been spawned */
    public boolean isWaveFinished() {
        return spawnQueue.isEmpty();
    }

    /** @return how many enemies are still waiting to spawn */
    public int getRemainingToSpawn() {
        return spawnQueue.size();
    }

    /** @return how many enemies have spawned so far this wave */
    public int getSpawnedCount() {
        return totalThisWave - spawnQueue.size();
    }

    /** @return total enemies in the current wave */
    public int getTotalThisWave() {
        return totalThisWave;
    }
}
