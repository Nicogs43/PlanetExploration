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
    private JButton launchDroneButton;
    private GroundControl GroundControl;

    public GroundControlGUI(GroundControl agent) {
        this.GroundControl = agent;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        panel.add(new JLabel("Target X:"));
        targetXField = new JTextField(5);
        panel.add(targetXField);

        panel.add(new JLabel("Target Y:"));
        targetYField = new JTextField(5);
        panel.add(targetYField);

        setTargetButton = new JButton("Set Target Coordinates");
        setTargetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int targetX, targetY;
                try {
                    targetX = Integer.parseInt(targetXField.getText().trim());
                    targetY = Integer.parseInt(targetYField.getText().trim());
                    // Send new target coordinates to agent
                    GroundControl.addBehaviour(new OneShotBehaviour(GroundControl) {
                        public void action() {
                            System.out.println("Target coordinates set to: (" + targetX + "," + targetY + ")");
                            // You can add other actions here like sending these coordinates to other agents
                            GroundControl.sendNewTarget(targetX, targetY);
                        }
                    });
                    
                    JOptionPane.showMessageDialog(GroundControlGUI.this,
                            "Target coordinates set to: (" + targetX + "," + targetY + ")",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(GroundControlGUI.this,
                            "Invalid input. Please enter integers for coordinates.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(setTargetButton);

        launchDroneButton = new JButton("Launch HeliDrone");
        launchDroneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // Trigger the launch of the drone
                GroundControl.addBehaviour(new OneShotBehaviour(GroundControl) {
                    public void action() {
                        System.out.println("HeliDrone launch initiated.");
                        GroundControl.LaunchHeliDrone(); // Assume this method is defined in your agent
                    }
                });
            }
        });
        panel.add(launchDroneButton);

        add(panel, BorderLayout.CENTER);

        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
