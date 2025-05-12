package gui;

import model.Segmentation;
import model.Segment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class SegmentationGUI extends JPanel {

    private final JFrame window;
    private final Segmentation segmentation;
    private DefaultTableModel tableModel;
    private JList<String> rowHeader;
    private JTable activeSegmentsTable;
    private JButton addButton;
    private final String[] columnNames = {""};

    public SegmentationGUI(JFrame window) {
        this.window = window;
        this.segmentation = new Segmentation();

        window.setSize(1400, 500);
        window.setTitle("Minh họa giải thuật Phân đoạn");
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        JPanel tablePanel = createMemoryPanel();
        JPanel settingsAndAddSegmentPanel = createSettingsAndAddSegmentPanel();
        JPanel segmentsPanel = createSegmentsTablePanel();

        gbc.gridx = 0;
        gbc.weightx = 0.3;
        add(tablePanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.1;
        add(settingsAndAddSegmentPanel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.6;
        add(segmentsPanel, gbc);
    }

    private JPanel createMemoryPanel() {
        JPanel memoryPanel = new JPanel(new BorderLayout());
        memoryPanel.setBorder(new TitledBorder("Bộ nhớ"));

        Object[][] data = new Object[0][];
        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Object value = getModel().getValueAt(row, column);

                if ("OS".equals(value)) {
                    c.setBackground(Color.LIGHT_GRAY);
                } else if (value != null && value.toString().contains("SID ")) {
                    try {
                        String sidStr = value.toString().split(",")[0].split(" ")[1];
                        int sid = Integer.parseInt(sidStr);
                        Segment segment = segmentation.findSegmentBySID(sid);
                        if (segment != null) {
                            c.setBackground(segment.getColor());
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);

        String[] rowHeaders = new String[data.length];
        rowHeader = new JList<>(rowHeaders);
        rowHeader.setFixedCellWidth(50);
        rowHeader.setFixedCellHeight(table.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setRowHeaderView(rowHeader);

        memoryPanel.add(scrollPane, BorderLayout.CENTER);

        return memoryPanel;
    }

    private void updateTableData(int memorySize, int osSize) {
        tableModel.setDataVector(new Object[memorySize][1], columnNames);

        segmentation.reset(memorySize);
        segmentation.initializeOSSegments(osSize);
        updateMemoryTable();
        updateSegmentsTable();
        updateRowHeaders(memorySize);
    }

    private void updateRowHeaders(int memorySize) {
        String[] newHeaders = new String[memorySize];
        for (int i = 0; i < memorySize; i++) {
            newHeaders[i] = "@" + i;
        }
        rowHeader.setListData(newHeaders);
    }

    private JPanel createSettingsAndAddSegmentPanel() {
        JPanel settingsAndAddSegmentPanel = new JPanel();
        settingsAndAddSegmentPanel.setLayout(new BoxLayout(settingsAndAddSegmentPanel, BoxLayout.Y_AXIS));

        JPanel settingsPanel = createSettingsPanel();

        settingsAndAddSegmentPanel.add(settingsPanel);

        JPanel addSegmentPanel = createAddSegmentPanel();
        settingsAndAddSegmentPanel.add(addSegmentPanel);

        return settingsAndAddSegmentPanel;
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setBorder(new TitledBorder("Cài đặt bộ nhớ"));
        settingsPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel memorySizeLabel = new JLabel("Kích thước bộ nhớ:");
        JSpinner memorySizeSpinner = new JSpinner(new SpinnerNumberModel(64, 1, 1024, 1));
        settingsPanel.add(memorySizeLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        settingsPanel.add(memorySizeSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel osSizeLabel = new JLabel("Kích thước Hệ điều hành:");
        JSpinner osSizeSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 64, 1));
        settingsPanel.add(osSizeLabel, gbc);

        gbc.gridx = 1;
        settingsPanel.add(osSizeSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton confirmButton = new JButton("Khởi tạo bộ nhớ");
        confirmButton.addActionListener(e -> {
            int memorySize = (Integer) memorySizeSpinner.getValue();
            int osSize = (Integer) osSizeSpinner.getValue();
            updateTableData(memorySize, osSize);
            addButton.setEnabled(true);
        });
        settingsPanel.add(confirmButton, gbc);

        return settingsPanel;
    }

    private JPanel createAddSegmentPanel() {
        JPanel addSegmentPanel = new JPanel(new GridBagLayout());
        addSegmentPanel.setBorder(new TitledBorder("Nạp đoạn"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel("Tên:");
        JTextField nameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        addSegmentPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        addSegmentPanel.add(nameField, gbc);

        JLabel sizeLabel = new JLabel("Kích thước:");
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 64, 1));
        gbc.gridx = 0;
        gbc.gridy = 1;
        addSegmentPanel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        addSegmentPanel.add(sizeSpinner, gbc);

        JLabel colorLabel = new JLabel("Màu:");
        JButton colorButton = new JButton();
        Random rand = new Random(); // Random object to generate random numbers
        // Set default color
        colorButton.setBackground(new Color(255, 130, 0));
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 2;
        addSegmentPanel.add(colorLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addSegmentPanel.add(colorButton, gbc);

        colorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(null, "Chọn màu", colorButton.getBackground());
            if (chosenColor != null) {
                colorButton.setBackground(chosenColor);
            }
        });

        addButton = new JButton("Nạp đoạn");
        addButton.setEnabled(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        addSegmentPanel.add(addButton, gbc);

        addButton.addActionListener(e -> {
            String segmentName = nameField.getText().trim();
            int segmentSize = (Integer) sizeSpinner.getValue();
            Color segmentColor = colorButton.getBackground();

            try {
                segmentation.addSegment(segmentName, segmentSize, segmentColor);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            updateMemoryTable();
            updateSegmentsTable();

            Color randomColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            colorButton.setBackground(randomColor);
        });

        return addSegmentPanel;
    }

    private void updateMemoryTable() {
        List<String> memory = segmentation.getMemory();
        for (int i = 0; i < memory.size(); i++) {
            tableModel.setValueAt(memory.get(i), i, 0);
        }
    }

    private JPanel createSegmentsTablePanel() {
        JPanel segmentsTablePanel = new JPanel(new GridBagLayout());
        segmentsTablePanel.setBorder(new TitledBorder("Bảng quản lý đoạn"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 3;

        String[] activeSegmentsColumnNames = {"Số hiệu đoạn", "Tên đoạn", "Dấu hiệu (M)", "Địa chỉ (A)", "Độ dài (L)", "Màu"};
        activeSegmentsTable = new JTable(new DefaultTableModel(new Object[0][6], activeSegmentsColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        JScrollPane activeSegmentsScrollPane = new JScrollPane(activeSegmentsTable);

        TableColumn colorColumn = activeSegmentsTable.getColumnModel().getColumn(5);
        colorColumn.setCellRenderer((TableCellRenderer) new ColorRenderer());

        segmentsTablePanel.add(activeSegmentsScrollPane, gbc);

        JButton back = new JButton("Trở về Menu");
        back.addActionListener(e -> {
            Menu menu = new Menu(window);
            window.remove(this);
            window.add(menu);
        });

        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        segmentsTablePanel.add(back, gbc);

        return segmentsTablePanel;
    }

    private void updateSegmentsTable() {
        DefaultTableModel model = (DefaultTableModel) activeSegmentsTable.getModel();
        model.setRowCount(0);

        for (Segment segment : segmentation.getSegments()) {
            Object[] row = new Object[]{
                    (segment.getSID() >= 0 ? segment.getSID() : null),
                    segment.getName(),
                    segment.getMark(),
                    segment.getAddress(),
                    segment.getLength(),
                    segment.getColor()
            };
            model.addRow(row);
        }
    }

    static class ColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Color color) {
                setBackground(color);
                setForeground(color);
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }
            return this;
        }
    }

    private static class RowHeaderRenderer extends JLabel implements ListCellRenderer<String> {
        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(LEFT);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText((value != null) ? value : "");
            return this;
        }
    }
}