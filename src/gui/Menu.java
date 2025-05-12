package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Menu extends JPanel {

    public Menu(JFrame window) {
        window.setSize(500, 200);
        window.setTitle("Minh họa quản lý bộ nhớ");
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;

        add(new JLabel("<html><h1>Minh họa quản lý bộ nhớ</h1><hr></html>"), gbc);

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel buttons = new JPanel(new GridBagLayout());
        JButton pagingButton = new JButton("Chiến lược Phân trang");
        JButton segmentationButton = new JButton("Chiến lược Phân đoạn");

        // Add action listener to pagingButton
        pagingButton.addActionListener(e -> {
            PagingGUI pagingGUI = new PagingGUI(window);
            window.remove(this);
            window.add(pagingGUI);
        });

        // Add action listener to segmentationButton
        segmentationButton.addActionListener(e -> {
            SegmentationGUI segmentationGUI = new SegmentationGUI(window);
            window.remove(this);
            window.add(segmentationGUI);
        });

        buttons.add(pagingButton, gbc);
        buttons.add(segmentationButton, gbc);

        pagingButton.setFocusable(false);
        segmentationButton.setFocusable(false);

        gbc.weighty = 1;
        add(buttons, gbc);
    }
}