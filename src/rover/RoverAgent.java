package rover;

import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import environment.Grid;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

public class RoverAgent extends Agent {
    /**
     * 
     */
    private static final long serialVersionUID = -6241375886823075380L;
    private int x, y, targetX, targetY;
    private boolean isMovingToTarget = false;
    public Grid grid;
    private String droneName = "";

    protected void setup() {
        grid = new Grid(10, 10);
        x = 0;
        y = 0;

        //TODO: make the rover can launch the helidrone itself 
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("GroundControl-Target-Coordinates"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println("Hello! Rover-agent " + getAID().getName() + " is ready at position (" + x + ","
                            + y + ").");
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
        // add a cyclic behaviour to receive th message if launch the helidrone
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("GroundControl-Launch-Helidrone"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    String content = msg.getContent();
                    if (content.contains("launch-helidrone")) {
                        ContainerController container = getContainerController();
                        try {
                            droneName = "HeliDrone" + System.currentTimeMillis(); // unique name
                            AgentController droneAgent = container.createNewAgent(droneName,
                                    "helidrone.HeliDrone", null);
                            droneAgent.start();
                            System.out.println("Drone agent launched: " + droneName);
                        } catch (ControllerException e) {
                            e.printStackTrace();
                            System.out.println("Failed to launch the drone agent.");
                        }
                        SendCoordToHeliDrone();
                        
                    }
                } else {
                    block();
                }
            }

            private void SendCoordToHeliDrone() {
                //send a message with the coordinates to the helidrone
                ACLMessage msgDrone = new ACLMessage(ACLMessage.INFORM);
                msgDrone.addReceiver(new AID(droneName, AID.ISLOCALNAME));
                msgDrone.setConversationId("Rover-Drone-Coordinates");
                msgDrone.setContent(x + "," + y);
                send(msgDrone);
                System.out.println("Here rover to HeliDrone: I'm at position (" + x + "," + y + ").");
            }

        });

        addBehaviour(new TickerBehaviour(this, 1000) { // Update every second
            protected void onTick() {
                if (isMovingToTarget) {
                    moveToTarget();
                    System.out.println(getAID().getName() + " is at (" + x + "," + y + ")");
                    if (x == targetX && y == targetY) {
                        isMovingToTarget = false;
                        x = targetX;
                        y = targetY;
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(new AID("GroundControl", AID.ISLOCALNAME));
                        msg.setConversationId("Rover-Target-Reach");
                        msg.setContent("Rover has reached the target position at (" + x + "," + y + ")");
                        send(msg);
                        System.out.println(
                                "Here rover to Ground control: I reached the target position. I start to dig.");
                        if(isMovingToTarget == false){
                            //TODO: send a message to the helidrone to move to the new coordinates

                        
                        addBehaviour(new WakerBehaviour(myAgent, 2000) {
                            protected void onWake() {
                                // send a message that the rover is finished digging
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
                    }
                        // i want simulate the time to dig and after few seconds i will notify the
                        // ground control
                    }
                }
            }
        });
        //Add the behaviour to respond to the helidrone if it request the corrdinates
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchConversationId("Drone-Rover-Coordinates"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    // send the coordinates to the helidrone
                    ACLMessage msgDrone = new ACLMessage(ACLMessage.INFORM);
                    msgDrone.addReceiver(new AID("HeliDrone", AID.ISLOCALNAME));
                    msgDrone.setConversationId("Rover-Drone-Coordinates");
                    msgDrone.setContent(x + "," + y);
                    send(msgDrone);
                    System.out.println("Here rover to HeliDrone: I'm at position (" + x + "," + y + ").");
                } else {
                    block();
                }
            }
        });
        addBehaviour(new ReceivedShutdown());
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

    private class ReceivedShutdown extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchConversationId("GroundControl-Shutdown"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                myAgent.doDelete();
            } else {
                block();
            }
        }
    }

}
