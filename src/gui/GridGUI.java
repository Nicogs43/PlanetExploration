package gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import environment.Grid;
//TODO: make a differnt GUI FOr the Ground control and for the helidrone 
import rover.RoverAgent;

public class GridGUI extends JFrame {
    private Grid grid;
    // private JTextArea textArea;
    private JPanel[][] cellPanels; // 2D array to store references to cell panels
    private JTextPane textPane;
    private StyledDocument doc;
    private RoverAgent roverAgent;

    public GridGUI(Grid grid, RoverAgent roverAgent) {
        this.grid = grid;
        this.roverAgent = roverAgent;
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
        // textArea = new JTextArea(10, 30); // Height and width of the text area
        // textArea.setEditable(false);
        // JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.SOUTH);
        setSize(800, 800);
        setLocationRelativeTo(null);

        // Add window listener to handle closing operations
        /*
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Perform cleanup operations, e.g., deleting agents
                cleanupAgents();
                // Dispose the frame
                dispose();
            }
        }); */
        setVisible(true);
    }

    /*
     * public void printMessage(String message) {
     * SwingUtilities.invokeLater(() -> {
     * textArea.append(message + "\n");
     * });
     * }
     */
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
        // Reset all cells to default background
        for (int row = 0; row < grid.getHeight(); row++) {
            for (int col = 0; col < grid.getWidth(); col++) {
                cellPanels[row][col].setBackground(null);
            }
        }
        // Highlight the rover's current position
        printMessageColored("Rover is at (" + x + "," + y + ")", Color.RED);
        cellPanels[y][x].setBackground(Color.RED);

    }

        // Method to perform cleanup operations
        private void cleanupAgents() {
            roverAgent.doDelete();
        }

}
