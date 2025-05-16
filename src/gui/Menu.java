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

        // Title
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;
        add(new JLabel("<html><h1>Minh họa quản lý bộ nhớ</h1><hr></html>"), gbc);

        // Option buttons
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        JPanel buttons = new JPanel(new GridBagLayout());

        // Button to switch to paging simulation window
        JButton pagingButton = new JButton("Chiến lược Phân trang");
        pagingButton.addActionListener(e -> {
            PagingGUI pagingGUI = new PagingGUI(window);
            window.remove(this);
            window.add(pagingGUI);
        });

        // Button to switch to segmentation simulation window
        JButton segmentationButton = new JButton("Chiến lược Phân đoạn");
        segmentationButton.addActionListener(e -> {
            SegmentationGUI segmentationGUI = new SegmentationGUI(window);
            window.remove(this);
            window.add(segmentationGUI);
        });

        buttons.add(pagingButton, gbc);
        buttons.add(segmentationButton, gbc);

        add(buttons, gbc);
    }
}