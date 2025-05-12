import javax.swing.*;

import gui.Menu;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        Menu menu = new Menu(window);
        window.add(menu);
    }
}