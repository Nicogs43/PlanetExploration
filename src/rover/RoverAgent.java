package rover;

import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import environment.Grid;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RoverAgent extends Agent {
    /**
     * 
     */
    private static final long serialVersionUID = -6241375886823075380L;
    private int x, y, targetX, targetY;
    private boolean isMovingToTarget = false;
    private Grid grid;

    protected void setup() {
        grid = new Grid(10, 10);
        x = new Random().nextInt(grid.getWidth());
        y = new Random().nextInt(grid.getHeight());

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println("Hello! Rover-agent " + getAID().getName() + " is ready at position (" + x + "," + y + ").");
                    String content = msg.getContent();
                    if (content.contains(",")) { // Assuming coordinates are sent in "x,y" format
                        String[] parts = content.split(",");
                        targetX = Integer.parseInt(parts[0].trim());
                        targetY = Integer.parseInt(parts[1].trim());
                        isMovingToTarget = true;
                        System.out.println("Moving to target position (" + targetX + "," + targetY + ")");
                    }
                } else {
                    block();
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 1000) { // Update every second
            protected void onTick() {
                if (isMovingToTarget) {
                    moveToTarget();
                    System.out.println(getAID().getName() + " is at (" + x + "," + y + ")");
                    if (x == targetX && y == targetY) {
                        isMovingToTarget = false;
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(new AID("GroundControl", AID.ISLOCALNAME));
                        msg.setConversationId("Rover-Target-Reach");
                        msg.setContent("Rover has reached the target position at (" + x + "," + y + ")");
                        send(msg);
                        System.out.println(
                                "Here rover to Ground control: I reached the target position. I start to dig.");

                        addBehaviour(new WakerBehaviour(myAgent, 200) {
                            protected void onWake() {
                                //send a message that the rover is finished digging
                                ACLMessage digFinishedMsg = new ACLMessage(ACLMessage.INFORM);
                                digFinishedMsg.addReceiver(new AID("GroundControl", AID.ISLOCALNAME));
                                digFinishedMsg.setConversationId("Rover-Dig-Finished");
                                digFinishedMsg.setContent("Rover has finished digging at (" + x + "," + y + ")");
                                send(digFinishedMsg);
                                System.out.println("Here rover to Ground control: I finished digging.");


                                addBehaviour(new WakerBehaviour(myAgent, 3000) {
                                    protected void onWake() {
                                        Random random = new Random();
                                        String[] findings = { "stone", "water", "unknown manufacture" };
                                        int index = random.nextInt(findings.length);
                                        String foundItem = findings[index];
                                        System.out.println("Analysis complete, found: " + foundItem);

                                        // Notify ground control of analysis result
                                        ACLMessage analysisMsg = new ACLMessage(ACLMessage.INFORM);
                                        analysisMsg.addReceiver(new AID("GroundControl", AID.ISLOCALNAME));
                                        analysisMsg.setConversationId("Rover-Analysis-Result");
                                        analysisMsg.setContent(foundItem);
                                        send(analysisMsg);

                                        addBehaviour(new WakerBehaviour(myAgent, 5000) {
                                            protected void onWake() {
                                                notifyGroundControlDigFinished();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        // i want simulate the time to dig and after few seconds i will notify the
                        // ground control
                    }
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

    private void notifyGroundControlDigFinished() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("groundcontrol", AID.ISLOCALNAME));
        msg.setConversationId("Rover-work-Finished");
        msg.setContent("Finish to work at (" + x + "," + y + ")");
        send(msg);
        System.out.println("Here Rover to Ground Control: I'm ready to receive new target coordinates.");

    }

    protected void takeDown() {
        System.out.println("Rover-agent " + getAID().getName() + " is shutting down.");

    }

}
