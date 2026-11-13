package krilliondefense.ui;

import javax.swing.JFrame;

/**
 * The main window. Just holds the GamePanel.
 */
public class GameFrame extends JFrame {
    /** The panel the game is drawn on. */
    private GamePanel gamePanel;

    /** Makes and shows the window. */
    public GameFrame() {
        super("Krillion Defense");
        gamePanel = new GamePanel(this);
        add(gamePanel);
        pack();
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
