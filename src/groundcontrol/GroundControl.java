package groundcontrol;

import java.util.Scanner;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;

public class GroundControl extends Agent {
    private int targetX, targetY = 0;
    private Scanner scanner = new Scanner(System.in);

    protected void setup() {
        System.out.println("Hello! Ground Control: " + getAID().getName() + " is ready.");
        // Retrieving target position from arguments
        boolean validTarget = false;    
        // Loop until valid coordinates are provided
        while (!validTarget) {
            System.out.println("Please enter target coordinates in the format 'x,y':");
            String input = scanner.nextLine();  // Read user input from console
    
            if (input != null && !input.isEmpty()) {
                String[] parts = input.split(",");
                if (parts.length == 2) { // Ensure there are exactly two parts
                    try {
                        targetX = Integer.parseInt(parts[0].trim());
                        targetY = Integer.parseInt(parts[1].trim());
                        validTarget = true; // Set the flag to true to break the loop
                    } catch (NumberFormatException e) {
                        System.out.println("Failed to parse coordinates. Ensure they are integers.");
                    }
                } else {
                    System.out.println("Invalid coordinates format. Expected format: 'x,y'");
                }
            } else {
                System.out.println("No input detected. Please enter target coordinates.");
            }
        }
        System.out.println("Target coordinates set to: (" + targetX + "," + targetY + ")");
    

        addBehaviour(new OneShotBehaviour(this) {
            public void action() {
                sendNewTarget(targetX, targetY);
            }
        });

        addBehaviour(new MessageReceiver());
        addBehaviour(new finishedDigging());
        addBehaviour(new receiveTheAnalysis());
        addBehaviour(new receiveWorkDone());

    }
 
    protected void takeDown() {
        /*
        System.out.println("Ground Control agent " + getAID().getName() + "  is preparing to shut dwn." );
        try {
            getContainerController().kill();
            System.out.println("Container successfully killed.");
        } catch (StaleProxyException e) {
            System.err.println("Failed to kill the container due to a stale proxy.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during container shutdown.");
            e.printStackTrace();
        }
        */
        System.out.println("Ground Control agent " + getAID().getName() + " has shut down.");
    }

    private class receiveWorkDone extends CyclicBehaviour {

            public void action() {
                int x = 0;
                int y = 0;
                boolean validInput = false;

                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                 MessageTemplate.MatchConversationId("Rover-Work-Finished"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    String messageContent = msg.getContent();
                        System.out.println("Confirmed message: " + messageContent);
                        while (!validInput) {
                            try {

                                System.out.println("Enter new X coordinate (or type 'exit' to stop):");
                                String inputX = scanner.nextLine();
                                if ("exit".equalsIgnoreCase(inputX.trim())) {
                                    myAgent.doDelete();
                                    return;
                                }
                                x = Integer.parseInt(inputX.trim());
                                validInput = true;
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input for new X. Please enter an integer.");
                            }
                        }
                        validInput = false;
                        while (!validInput) {
                            try {
                                System.out.println("Enter new Y coordinate:");
                                String inputY = scanner.nextLine();
                                y = Integer.parseInt(inputY.trim());
                                validInput = true;
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input for new Y. Please enter integers.");
                            }
                        }
                        sendNewTarget(x, y);
                        targetX = x;
                        targetY = y;
                } else {
                    block();
                }
            }
    }

    private class MessageReceiver extends CyclicBehaviour {
        public void action() {
            MessageTemplate mtReach = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("Rover-Target-Reach"));
            ACLMessage msg = myAgent.receive(mtReach);
            if (msg != null) {
                // Existing message handling
                System.out.println("Received: " + msg.getContent());
            } else {
                block();
            }
        }
    }

    private class finishedDigging extends CyclicBehaviour {
        public void action() {
            MessageTemplate mtDig = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("Rover-Dig-Finished"));
            ACLMessage msg = myAgent.receive(mtDig);
            if (msg != null) {
                // Existing message handling
                System.out.println("Received: " + msg.getContent());
            } else {
                block();
            }
        }
    }

    private class receiveTheAnalysis extends CyclicBehaviour {
        public void action() {
            MessageTemplate mtAnalysis = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("Rover-Analysis-Result"));
            ACLMessage msg = myAgent.receive(mtAnalysis);
            if (msg != null) {
                //TODO: create different behaviours for different analysis results
                System.out.println("Received: " + msg.getContent());
            } else {
                block();
            }
        }
    }

    // Method to send new target coordinates to the rover
    private void sendNewTarget(int newTargetX, int newTargetY) {
        System.out.println("Sending new target coordinates to Rover: (" + newTargetX + "," + newTargetY + ")");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new jade.core.AID("rover", jade.core.AID.ISLOCALNAME));
        msg.setContent(newTargetX + "," + newTargetY); // Sending coordinates as "x,y"
        send(msg);
    }

}
