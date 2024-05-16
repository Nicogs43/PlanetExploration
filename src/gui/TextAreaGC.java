package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TextAreaGC extends JFrame {
    private JTextArea textArea;

    public TextAreaGC() {
        // Set the title of the frame
        setTitle("Ground Control Output");
        // Set the default close operation
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set the layout of the frame
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
            //textArea.setForeground(color); // Set the text color
            textArea.append(message + "\n");
        });
    }

    public static void main(String[] args) {
        // Create an instance of TextAreaGC
        TextAreaGC textAreaGC = new TextAreaGC();
        
        // Simulate appending messages
        textAreaGC.printMessage("System initialized.");
        textAreaGC.printMessage("Rover moved to (5, 5).");
        textAreaGC.printMessage("Communication established.");
    }
}

