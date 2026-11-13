package krilliondefense.game;

/**
 * The different states the game can be in (see the state diagram).
 */
public enum GamePhase {
    /** Main menu. */
    MENU,
    /** Between waves, building towers. */
    BUILD,
    /** Enemies are coming. */
    WAVE,
    /** Game is paused for a trivia question. */
    QUESTION,
    /** Out of lives. */
    GAME_OVER,
    /** Beat every wave. */
    VICTORY
}
