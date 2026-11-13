package krilliondefense.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Starts the game.
 */
public class Main {

    /**
     * @param args not used
     */
    public static void main(String[] args) {
        // the Mac look and feel ignores button colors, so use the plain Java one
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Couldn't set look and feel, using the default");
        }
        SwingUtilities.invokeLater(() -> new GameFrame());
    }
}
