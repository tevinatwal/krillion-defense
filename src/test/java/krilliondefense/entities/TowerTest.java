package krilliondefense.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for Tower: upgrading and picking a target.
 */
class TowerTest {

    @Test
    void upgradeAddsDamageAndMaxesAtThree() {
        ArrowTower t = new ArrowTower(100, 100);
        assertEquals(10, t.getDamage());
        t.upgrade();
        assertEquals(13, t.getDamage()); // +25% (12.5 rounds up)
        t.upgrade();
        t.upgrade(); // should do nothing
        assertEquals(3, t.getLevel());
    }

    @Test
    void findTargetPicksFurthest() {
        ArrowTower t = new ArrowTower(100, 100);
        Enemy behind = new BasicEnemy(100, 110, 1);
        Enemy ahead = new BasicEnemy(110, 100, 1);
        Enemy outOfRange = new BasicEnemy(700, 700, 1);
        behind.distanceTraveled = 50;
        ahead.distanceTraveled = 200;
        outOfRange.distanceTraveled = 999;

        List<Enemy> list = new ArrayList<>(List.of(behind, ahead, outOfRange));
        assertEquals(ahead, t.findTarget(list));
        assertNull(t.findTarget(List.of(outOfRange)));
    }
}
