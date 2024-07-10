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
    private JPanel[][] cellPanels; // 2D array to store references to cell panels
    private JTextPane textPane;
    private StyledDocument doc;
    private Icon roverIcon;
    private Icon droneIcon;
    private Icon alienIcon;
    private int cellSize = 50;
    private int[] roverPosition = {-1, -1}; // Initially, no position
    private int[] dronePosition = {-1, -1}; // Initially, no position
    private int[] alienPosition = {-1, -1}; // Initially, no position


    public GridGUI(Grid grid) {
        this.grid = grid;
        roverIcon = new ImageIcon(new ImageIcon("images\\explorer.png").getImage().getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH));
        droneIcon = new ImageIcon(new ImageIcon("images\\drone.png").getImage().getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH));
        alienIcon = new ImageIcon(new ImageIcon("images\\alien.png").getImage().getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH));
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
                JLayeredPane layeredPane = new JLayeredPane();
                layeredPane.setBorder(BorderFactory.createLineBorder(Color.black));
                layeredPane.setPreferredSize(new Dimension(cellSize, cellSize));
                cellPanels[i][j] = new JPanel(new BorderLayout());
                cellPanels[i][j].add(layeredPane);
                gridPanel.add(cellPanels[i][j]);
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
        clearPosition(roverPosition[0], roverPosition[1], roverIcon);
        roverPosition[0] = x;
        roverPosition[1] = y;
        // Highlight the rover's current position
        printMessageColored("Rover is at (" + x + "," + y + ")", Color.RED);
        addIconToCell(x, y, roverIcon);
    }

    public void updateDronePosition(int x , int y){
        clearPosition(dronePosition[0], dronePosition[1], droneIcon);
        dronePosition[0] = x;
        dronePosition[1] = y;
        addIconToCell(x, y, droneIcon);
    }

    public void updateAlienPosition(int x, int y) {
        clearPosition(alienPosition[0], alienPosition[1], alienIcon);
        alienPosition[0] = x;
        alienPosition[1] = y;
        addIconToCell(x, y, alienIcon);
    }

    private void clearPosition(int x, int y, Icon icon) {
        if (x >= 0 && y >= 0) {
            JLayeredPane layeredPane = (JLayeredPane) cellPanels[y][x].getComponent(0);
            for (Component component : layeredPane.getComponents()) {
                if (component instanceof JLabel && ((JLabel) component).getIcon() == icon) {
                    layeredPane.remove(component);
                }
            }
            layeredPane.revalidate();
            layeredPane.repaint();
        }
    }

    private void addIconToCell(int x, int y, Icon icon) {
        JLayeredPane layeredPane = (JLayeredPane) cellPanels[y][x].getComponent(0);
        JLabel label = new JLabel(icon);
        label.setBounds(0, 0, cellSize, cellSize);
        layeredPane.add(label, JLayeredPane.DEFAULT_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void clearDronePosition() {
        clearPosition(dronePosition[0], dronePosition[1], droneIcon);
        dronePosition[0] = -1;
        dronePosition[1] = -1;
        printMessageColored("Drone has landed and is removed from the grid.", Color.BLUE);
    }


}


