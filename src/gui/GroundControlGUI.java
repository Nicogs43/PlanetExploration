package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import groundcontrol.GroundControl;

public class GroundControlGUI extends JFrame {
    private JTextField targetXField;
    private JTextField targetYField;
    private JButton setTargetButton;
    // private JButton launchDroneButton;
    private GroundControl GroundControl;
    public int targetX, targetY;


    public GroundControlGUI(GroundControl agent) {
        this.GroundControl = agent;

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));

        inputPanel.add(new JLabel("Target X:"));
        targetXField = new JTextField(5);
        inputPanel.add(targetXField);

        inputPanel.add(new JLabel("Target Y:"));
        targetYField = new JTextField(5);
        inputPanel.add(targetYField);

        mainPanel.add(inputPanel, BorderLayout.CENTER);


        setTargetButton = new JButton("Set Target Coordinates");
        setTitle("Ground Control");
        setTargetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    targetX = Integer.parseInt(targetXField.getText().trim());
                    targetY = Integer.parseInt(targetYField.getText().trim());
                    // Send new target coordinates to agent

                    JOptionPane.showMessageDialog(GroundControlGUI.this,
                            "Target coordinates set to: (" + targetX + "," + targetY + ")",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(GroundControlGUI.this,
                            "Invalid input. Please enter integers for coordinates.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                showLaunchDialog();
                setCoordTartget(targetX, targetY);

            }

            private void setCoordTartget(int targetX, int targetY) {
                GroundControl.addBehaviour(new OneShotBehaviour(GroundControl) {
                    public void action() {
                        System.out.println("Target coordinates set to: (" + targetX + "," + targetY + ")");
                        // You can add other actions here like sending these coordinates to other agents
                        GroundControl.sendNewTarget(targetX, targetY);
                        GroundControl.setTargetX(targetX);
                        GroundControl.setTargetY(targetY);
                    }
                });
            }
        });
        mainPanel.add(setTargetButton, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        setSize(500, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void showLaunchDialog() {
        int response = JOptionPane.showConfirmDialog(this,
                "Do you want to launch the HeliDrone?",
                "Launch HeliDrone",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            GroundControl.addBehaviour(new OneShotBehaviour(GroundControl) {
                public void action() {
                    System.out.println("HeliDrone launch initiated.");
                    GroundControl.LaunchHeliDrone(); // Assume this method is defined in your agent
                }
            });
        }
        dispose();
    }

}

