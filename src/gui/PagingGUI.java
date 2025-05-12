package gui;

import model.Page;
import model.Paging;
import model.Program;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class PagingGUI extends JPanel {

    private final JFrame window;
    private final Paging paging;
    private JList<String> rowHeader;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"", ""};
    private JButton addButton;
    private JTable activePagesTable;

    public PagingGUI(JFrame window) {
        this.window = window;
        this.paging = new Paging();

        window.setSize(1400, 500);
        window.setTitle("Minh họa giải thuật Phân trang");
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new GridBagLayout()); // Use GridBagLayout

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        // Create the three main panels
        JPanel tablePanel = createMemoryPanel();
        JPanel settingsAndAddProcessPanel = createSettingsAndAddProgramPanel();
        JPanel processesPanel = createPagesTablePanel();

        // Add the tablePanel with constraints
        gbc.gridx = 0;
        gbc.weightx = 0.3; // Adjust the weight as needed
        add(tablePanel, gbc);

        // Add the settingsAndAddProcessPanel with constraints
        gbc.gridx = 1;
        gbc.weightx = 0.1; // Adjust the weight as needed
        add(settingsAndAddProcessPanel, gbc);

        // Add the processesPanel with constraints
        gbc.gridx = 2;
        gbc.weightx = 0.6; // Give more weight to make it wider
        add(processesPanel, gbc);
    }

    private JPanel createMemoryPanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Bộ nhớ"));

        Object[][] data = new Object[0][2];
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

                // Check if the value is "OS" and color the row light grey
                if ("OS".equals(value)) {
                    c.setBackground(Color.LIGHT_GRAY);
                } else if (value != null && value.toString().contains("PID ")) {
                    try {
                        String pidStr = value.toString().split(",")[0].split(" ")[1]; // Extract PID
                        int pid = Integer.parseInt(pidStr);
                        Program process = paging.findProgramByPID(pid);
                        if (process != null) {
                            c.setBackground(process.getColor());
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        c.setBackground(Color.WHITE); // Fallback in case of parsing error
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
        rowHeader = new JList<>(rowHeaders); // Make it an instance variable
        rowHeader.setFixedCellWidth(50);
        rowHeader.setFixedCellHeight(table.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setRowHeaderView(rowHeader);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void updateTableData(int memorySize, int frameSize, int osSize) {
        // Initialize the table model with the correct number of rows
        tableModel.setDataVector(new Object[memorySize][1], columnNames);
        // Now initialize the OS process
        paging.initializeMemory(memorySize, frameSize, osSize);
        updateMemoryTable();
        updatePagesTable(); // Update the table
        // Update the row headers based on the new memory size
        updateRowHeaders(memorySize);
    }

    private void updateRowHeaders(int memorySize) {
        String[] newHeaders = new String[memorySize];
        for (int i = 0; i < memorySize; i++) {
            newHeaders[i] = "@" + i;
        }
        rowHeader.setListData(newHeaders);
    }

    private JPanel createSettingsAndAddProgramPanel() {
        JPanel settingsAndAddProcessPanel = new JPanel();
        settingsAndAddProcessPanel.setLayout(new BoxLayout(settingsAndAddProcessPanel, BoxLayout.Y_AXIS));

        // Add the settings panel
        JPanel settingsPanel = createSettingsPanel();
        settingsAndAddProcessPanel.add(settingsPanel);

        // Add the add process panel
        JPanel addProcessPanel = createAddProgramPanel();
        settingsAndAddProcessPanel.add(addProcessPanel);

        return settingsAndAddProcessPanel;
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setBorder(new TitledBorder("Cài đặt bộ nhớ"));
        settingsPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // Padding
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.weightx = 0; // No extra space distribution

        // Memory size label and spinner
        JLabel memorySizeLabel = new JLabel("Kích thước bộ nhớ:");
        JSpinner memorySizeSpinner = new JSpinner(new SpinnerNumberModel(64, 1, 1024, 1));
        settingsPanel.add(memorySizeLabel, gbc);

        gbc.gridx = 1; // Column 1
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1; // Extra space distributed to spinner
        settingsPanel.add(memorySizeSpinner, gbc);

        // Frame size label and spinner
        gbc.gridx = 0; // Reset to Column 0 for next component
        gbc.gridy = 1; // Next row
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; // Reset extra space distribution

        JLabel frameSizeLabel = new JLabel("Kích thước khung trang:");
        JSpinner frameSizeSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 1024, 1));
        settingsPanel.add(frameSizeLabel, gbc);

        gbc.gridx = 1; // Column 1 for spinner
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1; // Extra space distributed to spinner
        settingsPanel.add(frameSizeSpinner, gbc);

        // OS size label and spinner
        gbc.gridx = 0; // Reset to Column 0 for next component
        gbc.gridy = 2; // Next row
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0; // Reset extra space distribution

        JLabel osSizeLabel = new JLabel("Kích thước Hệ điều hành:");
        JSpinner osSizeSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 64, 1));
        settingsPanel.add(osSizeLabel, gbc);

        gbc.gridx = 1; // Column 1 for spinner
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1; // Extra space distributed to spinner
        settingsPanel.add(osSizeSpinner, gbc);

        // Confirm button
        gbc.gridx = 0; // Span across both columns
        gbc.gridy = 3; // Next row
        gbc.gridwidth = 2; // Span across two columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button

        JButton confirmButton = new JButton("Khởi tạo bộ nhớ");
        confirmButton.addActionListener(e -> {
            int memorySize = (Integer) memorySizeSpinner.getValue();
            int frameSize = (Integer) frameSizeSpinner.getValue();
            int osSize = (Integer) osSizeSpinner.getValue();
            updateTableData(memorySize, frameSize, osSize);
            addButton.setEnabled(true);
        });
        settingsPanel.add(confirmButton, gbc);

        return settingsPanel;
    }

    private JPanel createAddProgramPanel() {
        JPanel addProcessPanel = new JPanel(new GridBagLayout());
        addProcessPanel.setBorder(new TitledBorder("Nạp chương trình"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // Add some padding
        gbc.anchor = GridBagConstraints.WEST;

        // Name label and field
        JLabel nameLabel = new JLabel("Tên:");
        JTextField nameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        addProcessPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        addProcessPanel.add(nameField, gbc);

        // Size label and spinner
        JLabel sizeLabel = new JLabel("Kích thước:");
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 64, 1));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        addProcessPanel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        addProcessPanel.add(sizeSpinner, gbc);

        // Color label and button
        JLabel colorLabel = new JLabel("Màu:");
        JButton colorButton = new JButton();
        Random rand = new Random(); // Random object to generate random numbers
        // Set default color
        Color defaultColor = new Color(255,130,0);
        colorButton.setBackground(defaultColor);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false); // Needed on some look and feels

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1; // Take up only one column for the label
        addProcessPanel.add(colorLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1; // Take up only one column for the button
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addProcessPanel.add(colorButton, gbc);

        colorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(null, "Chọn màu", colorButton.getBackground());
            if (chosenColor != null) {
                colorButton.setBackground(chosenColor);
            }
        });

        addButton = new JButton("Nạp chương trình");
        addButton.setEnabled(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Span across two columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        addProcessPanel.add(addButton, gbc);

        // Action listener for create button
        addButton.addActionListener(e -> {
            int processSize = (Integer) sizeSpinner.getValue();
            String processName = nameField.getText().trim();
            Color processColor = colorButton.getBackground();

            try {
                paging.addProgram(processName, processSize, processColor);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            updateMemoryTable();
            updatePagesTable();

            Color randomColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            colorButton.setBackground(randomColor);
        });

        return addProcessPanel;
    }

    private void updateMemoryTable() {
        List<String> memory = paging.getMemory();
        for (int i = 0; i < memory.size(); i++) {
            tableModel.setValueAt(memory.get(i), i, 0);
        }
    }

    private JPanel createPagesTablePanel() {
        JPanel processesPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout
        processesPanel.setBorder(new TitledBorder("Bảng quản lý trang"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 3;

        // Table for active processes
        String[] activeProcessColumnNames = {"Số hiệu CT", "Tên chương trình", "Số hiệu trang", "Dấu hiệu (M)", "Địa chỉ (A)", "Màu"};
        activePagesTable = new JTable(new DefaultTableModel(new Object[0][6], activeProcessColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make table non-editable
                return false;
            }
        });
        
        // Apply the custom renderer to specific columns
        activePagesTable.getColumnModel().getColumn(0).setCellRenderer(new MergedCellRenderer()); // Program ID
        activePagesTable.getColumnModel().getColumn(1).setCellRenderer(new MergedCellRenderer()); // Program Name

        JScrollPane activeProcessesScrollPane = new JScrollPane(activePagesTable);

        TableColumn colorColumn = activePagesTable.getColumnModel().getColumn(5);
        colorColumn.setCellRenderer(new ColorRenderer());

        // Add the table with constraints
        processesPanel.add(activeProcessesScrollPane, gbc);

        JButton back = new JButton("Trở về Menu");
        back.addActionListener(e -> {
            Menu menu = new Menu(window);
            window.remove(this);
            window.add(menu);
        });

        // Adjust constraints for the back button
        gbc.weighty = 1; // Less weight compared to the table
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        // Add the back button with constraints
        processesPanel.add(back, gbc);

        return processesPanel;
    }

    private void updatePagesTable() {
        DefaultTableModel model = (DefaultTableModel) activePagesTable.getModel();
        model.setRowCount(0); // Clear existing rows

        for (Program program : paging.getPrograms()) {
            for (Page page : program.getPages()) {
                Object[] row = new Object[]{
                        (program.getPID() >= 0 ? program.getPID() : null),
                        program.getName(),
                        page.getPageID(),
                        page.getMark(),
                        page.getAddress(),
                        program.getColor()
                };
                model.addRow(row);
            }
        }
    }

    static class ColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Color color) {
                setBackground(color);
                setForeground(color); // You might want to adjust text color for visibility
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }
            return this;
        }
    }

    class MergedCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // Check if the current cell value matches the previous row's value
            if (row > 0 && value != null && value.equals(table.getValueAt(row - 1, column))) {
                // Hide the current cell's text and top border
                setText("");
            }
            return this;
        }
    }

    private static class RowHeaderRenderer extends JLabel implements ListCellRenderer<String> {
        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(LEFT); // Align to the left
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