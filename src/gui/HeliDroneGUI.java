package gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class HeliDroneGUI extends JFrame{
    private JTextArea textArea;

    public HeliDroneGUI() {
        setTitle("Helicopter Drone Output");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize the text area
        textArea = new JTextArea();
        textArea.setEditable(false); // Make it read-only
        textArea.setLineWrap(true);  // Enable line wrapping
        textArea.setWrapStyleWord(true); // Wrap at word boundaries

        // Add the text area to a scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add the scroll pane to the frame
        add(scrollPane, BorderLayout.CENTER);

        // Set the size of the frame
        setSize(600, 400);
        // Make the frame visible
        setVisible(true);
        setLocationRelativeTo(null);
    }
    // Method to append messages to the text area
    public void printMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.setForeground(Color.BLUE); // Set the text color
            textArea.append(message + "\n");
        });
    }

}
