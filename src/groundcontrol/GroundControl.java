package groundcontrol;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import gui.*;

public class GroundControl extends Agent {
    private int targetX, targetY = 0;
    // private Scanner scanner = new Scanner(System.in);
    private List<Behaviour> activeBehaviours = new ArrayList<>();
    public TextAreaGC textArea;

    protected void setup() {
        setTargetGUI();
        textArea = new TextAreaGC();

        Behaviour messageReceiver = new MessageReceiver();
        Behaviour finishedDigging = new finishedDigging();
        Behaviour receiveTheAnalysis = new receiveTheAnalysis();
        Behaviour receiveWorkDone = new receiveWorkDone();

        addBehaviour(messageReceiver);
        addBehaviour(finishedDigging);
        addBehaviour(receiveTheAnalysis);
        addBehaviour(receiveWorkDone);

        activeBehaviours.add(messageReceiver);
        activeBehaviours.add(finishedDigging);
        activeBehaviours.add(receiveTheAnalysis);
        activeBehaviours.add(receiveWorkDone);

    }

    public int getTargetX() {
        return targetX;

    }

    public void setTargetX(int targetX) {
        this.targetX = targetX;

    }

    // make get and setter also for targetY
    public int getTargetY() {
        return targetY;
    }

    public void setTargetY(int targetY) {
        this.targetY = targetY;
    }

    private void setTargetGUI() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {

                    new GroundControlGUI(GroundControl.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void LaunchHeliDrone() {
        // Create a new ACLMessage to send to the rover
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("rover", AID.ISLOCALNAME));
        msg.setConversationId("GroundControl-Launch-Helidrone");
        msg.setContent("launch-helidrone");
        // Send the message
        send(msg);
    }

    protected void takeDown() {
        textArea = new TextAreaGC();
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("rover", AID.ISLOCALNAME));
        msg.setConversationId("GroundControl-Shutdown");
        msg.setContent("DELETE");
        send(msg);

        // Cancel all behaviours
        for (Behaviour behaviour : activeBehaviours) {
            removeBehaviour(behaviour);
            System.out.println("Cancelled behaviour: " + behaviour.getClass().getSimpleName());
        }

        /*
         * System.out.println("Ground Control agent " + getAID().getName() +
         * "  is preparing to shut down.");
         * try {
         * getContainerController().kill();
         * System.out.println("Container successfully killed.");
         * } catch (StaleProxyException e) {
         * System.err.println("Failed to kill the container due to a stale proxy.");
         * e.printStackTrace();
         * } catch (Exception e) {
         * System.err.println("An unexpected error occurred during container shutdown."
         * );
         * e.printStackTrace();
         * }
         */
        // System.out.println("Ground Control agent " + getAID().getName() + " has shut
        // down.");
        textArea.printMessage("Ground Control agent " + getAID().getName() + " has shut down.");
        textArea.dispose();
    }

    private class receiveWorkDone extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("Rover-Work-Finished"));
            ACLMessage msg = receive(mt);
            if (msg != null) {
                String messageContent = msg.getContent();
                // System.out.println("Confirmed message: " + messageContent);
                textArea.printMessage("Confirmed message: " + messageContent);
                setTargetGUI();
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
                //System.out.println("Received: " + msg.getContent());
                textArea.printMessage("Received: " + msg.getContent());
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
                //System.out.println("Received: " + msg.getContent());
                textArea.printMessage("Received: " + msg.getContent());
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
                switch (msg.getContent()) {
                    case "stone":
                        //System.out.println("The rover found a stone, Yet another stone.");
                        textArea.printMessage("The rover found a stone, Yet another stone." );
                        break;
                    case "water":
                        //System.out.println("The rover found water. Interesting, water is the basis of our life.");
                        textArea.printMessage("The rover found water. Interesting, water is the basis of our life.");
                        break;
                    case "unknown manufacture":
                        textArea.printMessage("The rover found an unknown manufacture, Very intersting. the rover will start to dig again.");
                        //System.out.println(
                        //        "The rover found an unknown manufacture, Very intersting. the rover will start to dig again.");

                    default:
                        break;
                }
            } else {
                block();
            }
        }
    }

    // Method to send new target coordinates to the rover
    public void sendNewTarget(int newTargetX, int newTargetY) {
        //System.out.println("Sending new target coordinates to Rover: (" + newTargetX + "," + newTargetY + ")");
        textArea.printMessage("Sending new target coordinates to Rover: (" + newTargetX + "," + newTargetY + ")");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId("GroundControl-Target-Coordinates");
        msg.addReceiver(new jade.core.AID("rover", jade.core.AID.ISLOCALNAME));
        msg.setContent(newTargetX + "," + newTargetY);
        send(msg);
    }

}
