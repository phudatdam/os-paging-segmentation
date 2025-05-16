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

    private final JFrame window; // Parent window
    private final Segmentation segmentation; // Segmentation model
    private DefaultTableModel memoryTableModel;
    private final String[] memoryColumnNames = {""};
    private JList<String> memoryRowHeader;
    private JButton addButton;
    private JTable segmentsTable;


    public SegmentationGUI(JFrame window) {
        this.window = window;
        this.segmentation = new Segmentation();

        // Set up window and layout
        window.setSize(1400, 500);
        window.setTitle("Minh họa giải thuật Phân đoạn");
        window.setLocationRelativeTo(null); // Center the window
        window.setVisible(true);

        setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding
        setLayout(new GridBagLayout()); // Use GridBagLayout

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; // Fill the entire cell
        gbc.weighty = 1; // Allow the panel to grow vertically

        // Create the three main panels
        // Add memory table panel
        gbc.weightx = 0.3;
        JPanel tablePanel = createMemoryPanel();
        add(tablePanel, gbc);

        // Add settings and add segment panel
        gbc.weightx = 0.1;
        JPanel settingsAndAddSegmentPanel = createSettingsAndAddSegmentPanel();
        add(settingsAndAddSegmentPanel, gbc);

        // Add segments table panel
        gbc.weightx = 0.6;
        JPanel segmentsPanel = createSegmentsTablePanel();
        add(segmentsPanel, gbc);
    }


    // Create the memory table panel
    private JPanel createMemoryPanel() {
        // Create memory table panel with a border titled "Bộ nhớ"
        JPanel memoryPanel = new JPanel(new BorderLayout());
        memoryPanel.setBorder(new TitledBorder("Bộ nhớ"));

        Object[][] data = new Object[0][];
        memoryTableModel = new DefaultTableModel(data, memoryColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Set the table and cell color renderer
        JTable memoryTable = new JTable(memoryTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Object value = getModel().getValueAt(row, column);

                if ("OS".equals(value)) { // Check if the value is "OS"
                    c.setBackground(Color.LIGHT_GRAY); // Color the row light grey
                } else if (value != null && value.toString().contains("Đoạn ")) { // Check if the value contains segment ID
                    try {
                        String sidStr = value.toString().split(",")[0].split(" ")[1]; // Extract segment ID
                        int sid = Integer.parseInt(sidStr);
                        Segment segment = segmentation.findSegmentBySID(sid);
                        if (segment != null) {
                            c.setBackground(segment.getColor()); // Set the background color to the segment's color
                        } else {
                            c.setBackground(Color.WHITE); // Default color if program not found
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        c.setBackground(Color.WHITE); // Fallback in case of parsing error
                    }
                } else {
                    c.setBackground(Color.WHITE); // Default color for other cells
                }
                return c;
            }
        };

        // Set table properties
        memoryTable.setFillsViewportHeight(true);
        memoryTable.setShowGrid(true);
        memoryTable.setGridColor(Color.GRAY);

        // Set row header (memory address)
        String[] rowHeaders = new String[data.length];
        memoryRowHeader = new JList<>(rowHeaders);
        memoryRowHeader.setFixedCellWidth(50);
        memoryRowHeader.setFixedCellHeight(memoryTable.getRowHeight());
        memoryRowHeader.setCellRenderer(new RowHeaderRenderer(memoryTable));

        // Add a scroll pane to the table
        JScrollPane scrollPane = new JScrollPane(memoryTable);
        scrollPane.setRowHeaderView(memoryRowHeader);

        memoryPanel.add(scrollPane, BorderLayout.CENTER);

        return memoryPanel;
    }


    // Initialize memory and segments table with the given memory size and OS size
    private void initializeMemory(int memorySize, int osSize) {
        // Initialize memory table model with the correct number of rows
        memoryTableModel.setDataVector(new Object[memorySize][1], memoryColumnNames);

        // Initialize memory, add OS segment, and update tables
        segmentation.initializeMemory(memorySize, osSize);
        updateMemoryTable();
        updateSegmentsTable();
        updateRowHeaders(memorySize);
    }


    // Update memory table row headers (memory address) with the current memory size
    private void updateRowHeaders(int memorySize) {
        String[] newHeaders = new String[memorySize];
        for (int i = 0; i < memorySize; i++) {
            newHeaders[i] = "@" + i;
        }
        memoryRowHeader.setListData(newHeaders);
    }


    // Update the memory table with the current state of the segmentation
    private void updateMemoryTable() {
        List<String> memory = segmentation.getMemory();
        for (int i = 0; i < memory.size(); i++) {
            memoryTableModel.setValueAt(memory.get(i), i, 0); // Update the content column (1st column)
        }
    }


    // Create the settings and add segment panel
    private JPanel createSettingsAndAddSegmentPanel() {
        JPanel settingsAndAddSegmentPanel = new JPanel();
        settingsAndAddSegmentPanel.setLayout(new BoxLayout(settingsAndAddSegmentPanel, BoxLayout.Y_AXIS)); // Vertical layout

        // Add settings panel
        JPanel settingsPanel = createSettingsPanel();
        settingsAndAddSegmentPanel.add(settingsPanel);

        // Add add segment panel
        JPanel addSegmentPanel = createAddSegmentPanel();
        settingsAndAddSegmentPanel.add(addSegmentPanel);

        return settingsAndAddSegmentPanel;
    }


    // Create the settings panel where user can set memory size and OS size
    private JPanel createSettingsPanel() {
        // Create settings panel with a border titled "Cài đặt bộ nhớ"
        JPanel settingsPanel = new JPanel();
        settingsPanel.setBorder(new TitledBorder("Cài đặt bộ nhớ"));
        settingsPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // Padding
        gbc.anchor = GridBagConstraints.WEST; // Left align

        // Memory size label and spinner
        JLabel memorySizeLabel = new JLabel("Kích thước bộ nhớ:");
        JSpinner memorySizeSpinner = new JSpinner(new SpinnerNumberModel(64, 1, 1024, 1));
        settingsPanel.add(memorySizeLabel, gbc);

        gbc.gridx = 1; // Column 1
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1; // Extra space distributed to spinner
        settingsPanel.add(memorySizeSpinner, gbc);

        // OS size label and spinner
        gbc.gridx = 0; // Reset to Column 0 for next component
        gbc.gridy = 1; // Next row
        JLabel osSizeLabel = new JLabel("Kích thước Hệ điều hành:");
        JSpinner osSizeSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 64, 1));
        settingsPanel.add(osSizeLabel, gbc);

        gbc.gridx = 1; // Column 1 for spinner
        settingsPanel.add(osSizeSpinner, gbc);

        // Confirm button
        gbc.gridx = 0; // Span across both columns
        gbc.gridy = 2; // Next row
        gbc.gridwidth = 2; // Span across two columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button

        JButton confirmButton = new JButton("Khởi tạo bộ nhớ");
        confirmButton.addActionListener(e -> {
            int memorySize = (Integer) memorySizeSpinner.getValue();
            int osSize = (Integer) osSizeSpinner.getValue();
            initializeMemory(memorySize, osSize);
            addButton.setEnabled(true); // Enable the add segment button after initialization
        });
        settingsPanel.add(confirmButton, gbc);

        return settingsPanel;
    }


    // Create the add segment panel where user can input segment details
    private JPanel createAddSegmentPanel() {
        // Create add segment panel with a border titled "Nạp đoạn"
        JPanel addSegmentPanel = new JPanel(new GridBagLayout());
        addSegmentPanel.setBorder(new TitledBorder("Nạp đoạn"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // Padding
        gbc.anchor = GridBagConstraints.WEST; // Left align

        // Name label and field
        JLabel nameLabel = new JLabel("Tên:");
        JTextField nameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        addSegmentPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        addSegmentPanel.add(nameField, gbc);

        // Size label and spinner
        JLabel sizeLabel = new JLabel("Kích thước:");
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 64, 1));
        gbc.gridx = 0;
        gbc.gridy = 1;
        addSegmentPanel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        addSegmentPanel.add(sizeSpinner, gbc);

        // Color label and button
        JLabel colorLabel = new JLabel("Màu:");
        JButton colorButton = new JButton();
        Random rand = new Random();
        // Set default color
        colorButton.setBackground(new Color(255, 192, 127));
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 2;
        addSegmentPanel.add(colorLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addSegmentPanel.add(colorButton, gbc);

        // Open color chooser dialog when color button is clicked
        colorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(null, "Chọn màu", colorButton.getBackground());
            if (chosenColor != null) {
                colorButton.setBackground(chosenColor);
            }
        });

        // Add button to add segment
        addButton = new JButton("Nạp đoạn");
        addButton.setEnabled(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Span across two columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        addSegmentPanel.add(addButton, gbc);

        // Add a new segment or show error message when button is clicked
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

            // Random color for the color button
            float hue = rand.nextFloat();
            float saturation = 0.3f + rand.nextFloat() * 0.3f;
            float brightness = 0.95f + rand.nextFloat() * 0.05f;
            colorButton.setBackground(Color.getHSBColor(hue, saturation, brightness));
        });

        return addSegmentPanel;
    }


    // Create the segments table panel
    private JPanel createSegmentsTablePanel() {
        // Create segments table panel with a border titled "Bảng quản lý đoạn"
        JPanel segmentsTablePanel = new JPanel(new GridBagLayout());
        segmentsTablePanel.setBorder(new TitledBorder("Bảng quản lý đoạn"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 3;

        // Table for segments in memory
        String[] segmentsColumnNames = {"Số hiệu đoạn", "Tên đoạn", "Dấu hiệu (M)", "Địa chỉ (A)", "Độ dài (L)", "Màu"};
        segmentsTable = new JTable(new DefaultTableModel(new Object[0][6], segmentsColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        // Display color in the color column
        segmentsTable.getColumnModel().getColumn(5).setCellRenderer((TableCellRenderer) new ColorRenderer());

        // Add scroll pane
        JScrollPane segmentsScrollPane = new JScrollPane(segmentsTable);
        segmentsTablePanel.add(segmentsScrollPane, gbc);

        // Back to menu button
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


    // Update the segments table with the current state of segmentation
    private void updateSegmentsTable() {
        DefaultTableModel model = (DefaultTableModel) segmentsTable.getModel();
        model.setRowCount(0); // Clear existing rows
        // Add a new row for each segment
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


    // Display color in color column
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


    // Set background color for row header
    private static class RowHeaderRenderer extends JLabel implements ListCellRenderer<String> {
        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
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