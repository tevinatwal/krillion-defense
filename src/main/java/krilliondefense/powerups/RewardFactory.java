package krilliondefense.powerups;

import krilliondefense.trivia.RarityTier;

/**
 * Makes the power-up for a rarity tier. Each tier always gives the same
 * reward so the player knows what they are playing for.
 */
public class RewardFactory {

    /**
     * @param tier how rare the answer was
     * @return a power-up, or null for INVALID
     */
    public static PowerUp create(RarityTier tier) {
        switch (tier) {
            case COMMON:
                return new GoldBonus(25);
            case UNCOMMON:
                return new GoldBonus(60);
            case RARE:
                return new DamageBoost();
            case EPIC:
                return new FreezeEnemies();
            case LEGENDARY:
                return new KnowledgeBlast();
            default:
                return null;
        }
    }
}
