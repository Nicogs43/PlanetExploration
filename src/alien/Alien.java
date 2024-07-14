package alien;

import java.util.Random;

import environment.Grid;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import gui.GridGUI;
import java.awt.Color;

public class Alien extends Agent {
    private Grid grid;
    private GridGUI gridGui;
    private Random random = new Random();
    private int x, y;
    private int targetX, targetY;

    protected void setup() {
        Object[] args = getArguments();
        grid = (Grid) args[0];
        gridGui = (GridGUI) args[1];
        setNewRandomtargets();
        moveToTargetBehaviour();
        handleProposalRejection();
        handleProposalAcceptance();
    }

    private void setNewRandomtargets() {
        // set the target position randomly
        targetX = random.nextInt(grid.getWidth());
        targetY = random.nextInt(grid.getHeight());
    }

    // check if the alien reach the target
    private boolean isAtTarget() {
        return x == targetX && y == targetY;
    }

    private void moveToTargetBehaviour() {
        // ticker behaviour
        // the alien wait 10 second before moving to the target
        addBehaviour(new WakerBehaviour(this, 10000) {
            protected void onWake() {
                // Now add the TickerBehaviour after the delay
                addBehaviour(new TickerBehaviour(myAgent, 1000) {
                    protected void onTick() {
                        if (isAtTarget()) {
                            sendProposal();
                            setNewRandomtargets();
                            moveToTarget();
                        }
                        moveToTarget();
                        gridGui.updateAlienPosition(x, y);

                    }
                });
            }
        });
    }

    private void sendProposal() {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.addReceiver(new AID("rover", AID.ISLOCALNAME));
                msg.setContent("Let's collaborate");
                msg.setConversationId("Alien-Rover-Proposal");
                send(msg);
                System.out.println("AlienAgent sent a proposal to rover.");
            }
        });
    }

    // Method to handle the rejection of proposals
    private void handleProposalRejection() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println("AlienAgent received proposal rejection: " + msg.getContent());
                    System.out.println("Nobody wants me! I'm going to a new target.");
                    setNewRandomtargets();
                } else {
                    block();
                }
            }
        });
    }

    // Method to handle the acceptance of proposals
    private void handleProposalAcceptance() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println("AlienAgent received proposal acceptance: " + msg.getContent());
                    System.out.println("Great! Let's collaborate and work together.");
                    // Perform some actions upon acceptance
                    performCollaborationTasks();
                    setNewRandomtargets();
                } else {
                    block();
                }
            }
        });
    }

    // Method to perform tasks upon acceptance of the proposal
    private void performCollaborationTasks() {
        addBehaviour(new OneShotBehaviour() {
        // it is a simply toy-behaivour to show the collaboration. No one reply to the alien
            public void action() {
                // Example task: Send a confirmation message back to the rover
                ACLMessage confirmMsg = new ACLMessage(ACLMessage.INFORM);
                confirmMsg.addReceiver(new AID("rover", AID.ISLOCALNAME));
                confirmMsg.setContent("Acknowledged. Starting collaboration tasks.");
                confirmMsg.setConversationId("Alien-Rover-Collaboration");
                send(confirmMsg);

                // Example task: Log the collaboration start in the GUI
                gridGui.printMessageColored("Starting collaboration with the rover.", Color.GREEN);

            }
        });
    }

    private void moveToTarget() {
        if (x < targetX && grid.isValidPosition(x + 1, y)) {
            x++;
        } else if (x > targetX && grid.isValidPosition(x - 1, y)) {
            x--;
        }
        if (y < targetY && grid.isValidPosition(x, y + 1)) {
            y++;
        } else if (y > targetY && grid.isValidPosition(x, y - 1)) {
            y--;
        }
    }

}
