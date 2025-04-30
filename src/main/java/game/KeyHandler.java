package game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean shootPressed;
    public boolean spacePressed;
    public boolean enterPressed;
    public boolean rPressed;
    public boolean escapePressed;
    public boolean keyPressed = false;

    char lastKeyChar;
    int lastKeyCode;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        lastKeyCode = e.getKeyCode();
        keyPressed = true;
        lastKeyChar = e.getKeyChar();

        // Movement controls (both arrow keys and WASD)
        if (lastKeyCode == KeyEvent.VK_UP || lastKeyCode == KeyEvent.VK_W) upPressed = true;
        if (lastKeyCode == KeyEvent.VK_DOWN || lastKeyCode == KeyEvent.VK_S) downPressed = true;
        if (lastKeyCode == KeyEvent.VK_LEFT || lastKeyCode == KeyEvent.VK_A) leftPressed = true;
        if (lastKeyCode == KeyEvent.VK_RIGHT || lastKeyCode == KeyEvent.VK_D) rightPressed = true;
        if (lastKeyCode == KeyEvent.VK_ENTER) enterPressed = true;
        if (lastKeyCode == KeyEvent.VK_ESCAPE) escapePressed = true;

        // Shooting controls
        if (lastKeyCode == KeyEvent.VK_SPACE) {
            if (!spacePressed) {
                shootPressed = true;
            }
            spacePressed = true;
        }

        // Restart game when game over
        if (lastKeyCode == KeyEvent.VK_R) rPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        lastKeyCode = e.getKeyCode();

        if (lastKeyCode == KeyEvent.VK_UP || lastKeyCode == KeyEvent.VK_W) upPressed = false;
        if (lastKeyCode == KeyEvent.VK_DOWN || lastKeyCode == KeyEvent.VK_S) downPressed = false;
        if (lastKeyCode == KeyEvent.VK_LEFT || lastKeyCode == KeyEvent.VK_A) leftPressed = false;
        if (lastKeyCode == KeyEvent.VK_RIGHT || lastKeyCode == KeyEvent.VK_D) rightPressed = false;
        if (lastKeyCode == KeyEvent.VK_SPACE) {
            spacePressed = false;
            shootPressed = false;
        }
        if (lastKeyCode == KeyEvent.VK_R) rPressed = false;
        if (lastKeyCode == KeyEvent.VK_ESCAPE) escapePressed = false;
    }
}