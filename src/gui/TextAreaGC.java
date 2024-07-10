package gui;

import javax.swing.*;
import java.awt.*;

public class TextAreaGC extends JFrame {
    private JTextArea textArea;

    public TextAreaGC() {
        setTitle("Ground Control Output");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize the text area
        textArea = new JTextArea();
        textArea.setEditable(false); // Make it read-only
        textArea.setLineWrap(true); // Enable line wrapping
        textArea.setWrapStyleWord(true); // Wrap at word boundaries

        // Add the text area to a scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add the scroll pane to the frame
        add(scrollPane, BorderLayout.CENTER);

        setSize(600, 400);
        setVisible(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Calculate the new location
        int x = screenSize.width - getWidth();
        int y = screenSize.height - getHeight();

        // Set the new location
        setLocation(x, y);
    }

    // Method to append messages to the text area
    public void printMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
        });
    }
}
