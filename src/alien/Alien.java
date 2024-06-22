package alien;

import java.util.Random;

import environment.Grid;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import gui.GridGUI;

public class Alien extends Agent {
    private Grid grid;
    private GridGUI gridGui;
    private Random random = new Random();
    private int x, y;
    private int targetX, targetY;
    boolean responseToTheProposal = false;

    protected void setup() {
        Object[] args = getArguments();
        grid = (Grid) args[0];
        gridGui = (GridGUI) args[1];
        setNewRandomtargets();
        moveToTargetBehaviour();
        sendProposalBehaviour();
        HandleProposalRejectionBehaviour();
    }

    private void setNewRandomtargets() {
        // set the target position randomly
        targetX = random.nextInt(grid.getWidth());
        targetY = random.nextInt(grid.getHeight());
    }
    //check if the alien reach the target
    private boolean isAtTarget() {
        return x == targetX && y == targetY;
    }

    private void moveToTargetBehaviour() {
        // ticker behaviour
        addBehaviour(new TickerBehaviour(this, 1000) {
            protected void onTick() {
                moveToTarget();
                gridGui.updateAlienPosition(x, y);
            }
        });
    }

    private void sendProposalBehaviour() {
        // waker behaviour
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                if (!responseToTheProposal) {
                    ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                    msg.addReceiver(new AID("rover", AID.ISLOCALNAME));
                    msg.setContent("Let's collaborate");
                    msg.setConversationId("Alien-Rover-Proposal");
                    send(msg);
                    System.out.println("AlienAgent sent a proposal to rover.");
                }
            }
        });
    }
        // Method to handle the rejection of proposals
        private void HandleProposalRejectionBehaviour() {
            addBehaviour(new CyclicBehaviour() {
                public void action() {
                    MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
                    ACLMessage msg = receive(mt);
                    if (msg != null) {
                        System.out.println("AlienAgent received proposal rejection: " + msg.getContent());
                        System.out.println("Nobody wants me! I'm going to a new target.");
                        setNewRandomtargets();
                        responseToTheProposal = true;
                        if (isAtTarget()) {
                            setNewRandomtargets();
                            moveToTargetBehaviour();
                        }
                    } else {
                        block();
                    }
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
