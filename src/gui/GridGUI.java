package gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import environment.Grid;

public class GridGUI extends JFrame {
    private Grid grid;
    // private JTextArea textArea;
    private JPanel[][] cellPanels; // 2D array to store references to cell panels
    private JTextPane textPane;
    private StyledDocument doc;
    private Icon roverIcon;
    private Icon droneIcon;
    private int cellSize = 50;
    private int[] roverPosition = {-1, -1}; // Initially, no position
    private int[] dronePosition = {-1, -1}; // Initially, no position


    public GridGUI(Grid grid) {
        this.grid = grid;
        roverIcon = new ImageIcon(new ImageIcon("images\\explorer.png").getImage().getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH));
        droneIcon = new ImageIcon(new ImageIcon("images\\drone.png").getImage().getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH));
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Grid Representation and Output");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Use BorderLayout for better arrangement

        JPanel gridPanel = new JPanel(new GridLayout(grid.getHeight(), grid.getWidth()));
        cellPanels = new JPanel[grid.getHeight()][grid.getWidth()];

        for (int i = 0; i < grid.getHeight(); i++) {
            for (int j = 0; j < grid.getWidth(); j++) {
                JPanel panel = new JPanel();
                panel.setBorder(BorderFactory.createLineBorder(Color.black));
                panel.setPreferredSize(new Dimension(cellSize, cellSize));
                gridPanel.add(panel);
                cellPanels[i][j] = panel;
            }
        }
        textPane = new JTextPane();
        textPane.setEditable(false);
        doc = textPane.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(400, 200)); // Set preferred size
        add(gridPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        setSize(800, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void printMessageColored(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = doc.addStyle("StyleName", null);
                StyleConstants.setForeground(style, color);
                doc.insertString(doc.getLength(), message + "\n", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateRoverPosition(int x, int y) {
        //resetAllCellPanels();
        clearPosition(roverPosition[0], roverPosition[1]);
        roverPosition[0] = x;
        roverPosition[1] = y;
        // Highlight the rover's current position
        printMessageColored("Rover is at (" + x + "," + y + ")", Color.RED);
        JLabel roverLabel = new JLabel(roverIcon);
        roverLabel.setVisible(rootPaneCheckingEnabled);
        roverLabel.setPreferredSize(new Dimension(50,50));
        cellPanels[y][x].add(roverLabel, BorderLayout.CENTER);
        cellPanels[y][x].revalidate();
        cellPanels[y][x].repaint();
        //cellPanels[y][x].setBackground(Color.RED);


    }
    public void updateDronePosition(int x , int y){
        //resetAllCellPanels();
        clearPosition(dronePosition[0], dronePosition[1]);
        dronePosition[0] = x;
        dronePosition[1] = y;
        JLabel droneLabel = new JLabel(droneIcon);
        droneLabel.setVisible(rootPaneCheckingEnabled);
        droneLabel.setPreferredSize(new Dimension(50,50));
        cellPanels[y][x].add(droneLabel, BorderLayout.CENTER);
        cellPanels[y][x].revalidate();
        cellPanels[y][x].repaint();
    }

/*    private void resetAllCellPanels() {
        // Reset all cells to default background
        for (int row = 0; row < grid.getHeight(); row++) {
            for (int col = 0; col < grid.getWidth(); col++) {
                cellPanels[row][col].removeAll();
                cellPanels[row][col].revalidate();
                cellPanels[row][col].repaint();
                //cellPanels[row][col].setBackground(null);
            }
        }
    }*/ 
    private void clearPosition(int x, int y) {
        if (x >= 0 && y >= 0) {
            cellPanels[y][x].removeAll();
            cellPanels[y][x].revalidate();
            cellPanels[y][x].repaint();
        }
    }
    public void clearDronePosition() {
        clearPosition(dronePosition[0], dronePosition[1]);
        dronePosition[0] = -1;
        dronePosition[1] = -1;
        printMessageColored("Drone has landed and is removed from the grid.", Color.BLUE);
    }

}
