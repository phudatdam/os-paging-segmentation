import javax.swing.*;

import gui.Menu;

public class Main {
    public static void main(String[] args) {
        // Create window
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        // Start with menu window
        Menu menu = new Menu(window);
        window.add(menu);
    }
}