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

    private final JFrame window; // Parent window
    private final Paging paging; // Paging model
    private DefaultTableModel memoryTableModel;
    private JTable memoryTable;
    private final String[] memoryColumnNames = {"", ""};
    private JList<String> memoryRowHeader;
    private JButton addButton;
    private JButton accessButton;
    private JButton deleteButton;
    private JTable pagesTable;


    public PagingGUI(JFrame window) {
        this.window = window;
        this.paging = new Paging();

        // Set up window and layout
        window.setSize(1400, 500);
        window.setTitle("Minh họa giải thuật Phân trang");
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

        // Add settings and add program panel
        gbc.weightx = 0.1;
        JPanel settingsAndAddProgramPanel = createSettingsAndAddProgramPanel();
        add(settingsAndAddProgramPanel, gbc);

        // Add pages table panel
        gbc.weightx = 0.6;
        JPanel pagesTableAndAccessMemoryPanel = createPagesTableAndAccessMemoryPanel();
        add(pagesTableAndAccessMemoryPanel, gbc);

        // Thêm listener cho pagesTable để enable nút xóa khi chọn chương trình
        // (sau khi pagesTable đã được khởi tạo trong createPagesTablePanel)
        pagesTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = pagesTable.getSelectedRow();
            if (selectedRow >= 0 && !e.getValueIsAdjusting()) {
                String programName = (String) pagesTable.getValueAt(selectedRow, 1);
                // Không cho xóa OS
                deleteButton.setEnabled(!"OS".equals(programName));
            } else {
                deleteButton.setEnabled(false);
            }
        });
        // Thêm action cho deleteButton
        deleteButton.addActionListener(e -> deleteSelectedProgram());
    }


    // Create the memory table panel
    private JPanel createMemoryPanel() {
        // Create memory table panel with a border titled "Bộ nhớ"
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Bộ nhớ"));

        Object[][] data = new Object[0][2];
        memoryTableModel = new DefaultTableModel(data, memoryColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Set the table and cell color renderer
        memoryTable = new JTable(memoryTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Object value = getModel().getValueAt(row, column);

                if ("OS".equals(value)) { // Check if the value is "OS"
                    c.setBackground(Color.LIGHT_GRAY); // Color the row light grey
                } else if (value != null && value.toString().contains("CT ")) { // Check if the value contains program ID
                    try {
                        String pidStr = value.toString().split(",")[0].split(" ")[1]; // Extract program ID
                        int pid = Integer.parseInt(pidStr);
                        Program program = paging.findProgramByPID(pid);
                        if (program != null) {
                            c.setBackground(program.getColor()); // Set the background color to the program's color
                            if (isRowSelected(row)) {
                                c.setBackground(c.getBackground().darker()); // Make selected row darker
                            }
                        } else {
                            c.setBackground(Color.WHITE); // Default color if program not found
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        c.setBackground(Color.WHITE); // Fallback in case of parsing error
                    }
                } else if (value.toString().contains("@")) { // Check if the value is a memory address
                    c.setBackground(getTableHeader().getBackground()); // Set to default header color
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

        // Set row header (frame index)
        String[] rowHeaders = new String[data.length];
        memoryRowHeader = new JList<>(rowHeaders);
        memoryRowHeader.setFixedCellWidth(40);
        memoryRowHeader.setFixedCellHeight(memoryTable.getRowHeight());
        memoryRowHeader.setCellRenderer(new RowHeaderRenderer(memoryTable));

        // Add a scroll pane to the table
        JScrollPane scrollPane = new JScrollPane(memoryTable);
        scrollPane.setRowHeaderView(memoryRowHeader);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }


    // Initialize memory and pages table with the given memory size, frame size, and OS size
    private void initializeMemory(int memorySize, int frameSize, int osSize) {
        // Initialize memory table model with the correct number of rows
        memoryTableModel.setDataVector(new Object[memorySize][1], memoryColumnNames);
        
        // Initialize memory, add OS program, and update tables
        paging.initializeMemory(memorySize, frameSize, osSize);
        updateMemoryRowHeaders(memorySize);
        updateMemoryTable();
        updatePagesTable();

        // Set width for memory address column, resize content column accordingly
        memoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        memoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        memoryTable.getColumnModel().getColumn(1).setPreferredWidth(179);
    }


    // Update memory table row headers (frame index) with the current memory size
    private void updateMemoryRowHeaders(int memorySize) {
        String[] newHeaders = new String[memorySize];
        for (int i = 0; i < memorySize; i++) {
            newHeaders[i] = "F" + i / paging.getFrameSize();
            memoryTableModel.setValueAt("@" + i, i, 0);
        }
        memoryRowHeader.setListData(newHeaders);
    }


    // Update the memory table with the current state of the paging
    private void updateMemoryTable() {
        List<String> memory = paging.getMemory();
        for (int i = 0; i < memory.size(); i++) {
            memoryTableModel.setValueAt(memory.get(i), i, 1); // Update the content column (2nd column)
        }
    }


    // Create the settings and add program panel
    private JPanel createSettingsAndAddProgramPanel() {
        JPanel settingsAndAddProgramPanel = new JPanel();
        settingsAndAddProgramPanel.setLayout(new BoxLayout(settingsAndAddProgramPanel, BoxLayout.Y_AXIS)); // Vertical layout

        // Add settings panel
        JPanel settingsPanel = createSettingsPanel();
        settingsAndAddProgramPanel.add(settingsPanel);

        // Add add program panel
        JPanel addProgramPanel = createAddProgramPanel();
        settingsAndAddProgramPanel.add(addProgramPanel);

        return settingsAndAddProgramPanel;
    }


    // Create the settings panel where user can set memory size, frame size, and OS size
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

        // Frame size label and spinner
        gbc.gridx = 0; // Reset to Column 0 for next component
        gbc.gridy = 1; // Next row
        JLabel frameSizeLabel = new JLabel("Kích thước khung trang:");
        JSpinner frameSizeSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 1024, 1));
        settingsPanel.add(frameSizeLabel, gbc);

        gbc.gridx = 1; // Column 1 for spinner
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
            initializeMemory(memorySize, frameSize, osSize);
            addButton.setEnabled(true); // Enable the add program button after initialization
        });
        settingsPanel.add(confirmButton, gbc);

        return settingsPanel;
    }


    // Create the add program panel where user can input program details
    private JPanel createAddProgramPanel() {
        // Create add program panel with a border titled "Nạp chương trình"
        JPanel addProgramPanel = new JPanel(new GridBagLayout());
        addProgramPanel.setBorder(new TitledBorder("Nạp chương trình"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4); // Padding
        gbc.anchor = GridBagConstraints.WEST; // Left align

        // Name label and field
        JLabel nameLabel = new JLabel("Tên:");
        JTextField nameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        addProgramPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        addProgramPanel.add(nameField, gbc);

        // Size label and spinner
        JLabel sizeLabel = new JLabel("Kích thước:");
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 64, 1));
        gbc.gridx = 0;
        gbc.gridy = 1;
        addProgramPanel.add(sizeLabel, gbc);

        gbc.gridx = 1;
        addProgramPanel.add(sizeSpinner, gbc);

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
        addProgramPanel.add(colorLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addProgramPanel.add(colorButton, gbc);

        // Open color chooser dialog when color button is clicked
        colorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(null, "Chọn màu", colorButton.getBackground());
            if (chosenColor != null) {
                colorButton.setBackground(chosenColor);
            }
        });

        // Add button to add program
        addButton = new JButton("Nạp chương trình");
        addButton.setEnabled(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Span across two columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        addProgramPanel.add(addButton, gbc);

        // Add a new program or show error message when button is clicked
        addButton.addActionListener(e -> {
            String programName = nameField.getText().trim();
            int programSize = (Integer) sizeSpinner.getValue();
            Color programColor = colorButton.getBackground();

            try {
                paging.addProgram(programName, programSize, programColor);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            updateMemoryTable();
            updatePagesTable();

            // Random color for the color button
            float hue = rand.nextFloat();
            float saturation = 0.3f + rand.nextFloat() * 0.3f;
            float brightness = 0.95f + rand.nextFloat() * 0.05f;
            colorButton.setBackground(Color.getHSBColor(hue, saturation, brightness));

            accessButton.setEnabled(true); // Enable the access memory button after adding a segment
        });

        return addProgramPanel;
    }


    // Create the pages table and access memory panel
    private JPanel createPagesTableAndAccessMemoryPanel() {
        JPanel pagesTableAndAccessMemoryPanel = new JPanel();
        pagesTableAndAccessMemoryPanel.setLayout(new BoxLayout(pagesTableAndAccessMemoryPanel, BoxLayout.Y_AXIS)); // Vertical layout

        // Add segments table panel
        JPanel pagesTablePanel = createPagesTablePanel();
        pagesTableAndAccessMemoryPanel.add(pagesTablePanel);

        // Add access memory panel
        JPanel accessMemoryPanel = createAccessMemoryPanel();
        pagesTableAndAccessMemoryPanel.add(accessMemoryPanel);

        return pagesTableAndAccessMemoryPanel;
    }


    // Create the pages table panel
    private JPanel createPagesTablePanel() {
        // Create pages table panel with a border titled "Bảng quản lý trang"
        JPanel pagesTablePanel = new JPanel(new GridBagLayout());
        pagesTablePanel.setBorder(new TitledBorder("Bảng quản lý trang"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        // Table for programs in memory and their pages
        String[] pagesColumnNames = {"Số hiệu CT", "Tên chương trình", "Số hiệu trang", "Dấu hiệu (M)", "Địa chỉ (A)", "Màu"};
        pagesTable = new JTable(new DefaultTableModel(new Object[0][6], pagesColumnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        
        // Merge cells with the same value in the first two columns
        pagesTable.getColumnModel().getColumn(0).setCellRenderer(new MergedCellRenderer()); // Program ID
        pagesTable.getColumnModel().getColumn(1).setCellRenderer(new MergedCellRenderer()); // Program Name
        // Display color in the color column
        pagesTable.getColumnModel().getColumn(5).setCellRenderer(new ColorRenderer());

        // Add scroll pane
        JScrollPane pagesScrollPane = new JScrollPane(pagesTable);
        pagesTablePanel.add(pagesScrollPane, gbc);

        return pagesTablePanel;
    }


    // Update the pages table with the current state of paging
    private void updatePagesTable() {
        DefaultTableModel model = (DefaultTableModel) pagesTable.getModel();
        model.setRowCount(0); // Clear existing rows
        // Add a new row for each page
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


    // Create the access memory panel where user can input program ID and offset and receive physical address
    private JPanel createAccessMemoryPanel() {
        // Create access memory panel with a border titled "Truy nhập bộ nhớ"
        JPanel accessMemoryPanel = new JPanel(new GridBagLayout());
        accessMemoryPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Truy nhập bộ nhớ"),
            new EmptyBorder(20, 0, 0, 0) // Add a 20px gap at the top
        ));
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST; // Left align
    
        // Program ID label and input
        JLabel programIDLabel = new JLabel("Số hiệu chương trình:");
        JTextField programIDField = new JTextField(20);
        accessMemoryPanel.add(programIDLabel, gbc);
    
        gbc.gridx = 1;
        gbc.weightx = 0.1; // Extra space distributed to input
        accessMemoryPanel.add(programIDField, gbc);
    
        // Offset label and input
        JLabel offsetLabel = new JLabel("Độ lệch trong chương trình:");
        JTextField offsetField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 1;
        accessMemoryPanel.add(offsetLabel, gbc);
    
        gbc.gridx = 1;
        accessMemoryPanel.add(offsetField, gbc);
    
        // Button to translate logical address to physical address
        accessButton = new JButton("Truy nhập bộ nhớ");
        accessButton.setEnabled(false); // Initially disabled
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        accessMemoryPanel.add(accessButton, gbc);
    
        // Label to display the physical address
        JLabel resultLabel = new JLabel("Địa chỉ vật lý: ");
        gbc.gridy = 3;
        accessMemoryPanel.add(resultLabel, gbc);
    
        // Display the physical address and highlight the corresponding row in the memory table when button is clicked
        accessButton.addActionListener(e -> {
            try {
                int segmentID = Integer.parseInt(programIDField.getText().trim());
                int offset = Integer.parseInt(offsetField.getText().trim());
                int physicalAddress = paging.translateAddress(segmentID, offset);
    
                // Display the physical address
                resultLabel.setText("Địa chỉ vật lý: " + physicalAddress);
    
                // Highlight and scroll to the corresponding row in the memory table
                memoryTable.setRowSelectionInterval(physicalAddress, physicalAddress);
                memoryTable.scrollRectToVisible(memoryTable.getCellRect(physicalAddress, 0, true));
    
                // Highlight and scroll to the corresponding row in the segments table
                for (int i = 1; i < pagesTable.getRowCount(); i++) {
                    if ((int) pagesTable.getValueAt(i, 0) == segmentID) {
                        pagesTable.setRowSelectionInterval(i, i);
                        pagesTable.scrollRectToVisible(pagesTable.getCellRect(i, 0, true));
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Back to menu button
        JButton back = new JButton("Trở về Menu");
        back.addActionListener(e -> {
            Menu menu = new Menu(window);
            window.remove(this);
            window.add(menu);
        });
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        
        accessMemoryPanel.add(back, gbc);

        deleteButton = new JButton("Xóa chương trình");
        deleteButton.setEnabled(false); // Ẩn ban đầu
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span across two columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        accessMemoryPanel.add(deleteButton, gbc);

        return accessMemoryPanel;
    }


    // Xóa chương trình được chọn trong bảng quản lý trang
    private void deleteSelectedProgram() {
        int selectedRow = pagesTable.getSelectedRow();
        if (selectedRow >= 0) {
            Integer pid = (Integer) pagesTable.getValueAt(selectedRow, 0);
            String name = (String) pagesTable.getValueAt(selectedRow, 1);
            if (pid == null || "OS".equals(name)) {
                JOptionPane.showMessageDialog(this, "Không thể xóa chương trình hệ điều hành.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Program program = paging.findProgramByPID(pid);
            if (program == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy chương trình.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa chương trình '" + name + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                paging.deleteProgram(program.getName());
                updateMemoryTable();
                updatePagesTable();
                deleteButton.setEnabled(false);
            }
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


    // Merge cells with the same value
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


    // Set background color for row header
    private static class RowHeaderRenderer extends JLabel implements ListCellRenderer<String> {
        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setBackground(header.getBackground());
            setFont(header.getFont());
        }
        // Merge cells with the same value
        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            // Check if the current frame index matches the previous row's frame index
            if (index > 0 && value.equals(list.getModel().getElementAt(index - 1))) {
                setText(""); // Hide the text for subsequent rows with the same frame index
            } else {
                setText(value); // Display the frame index
            }
            return this;
        }
    }

}