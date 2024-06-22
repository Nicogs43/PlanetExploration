package rover;

import java.awt.Color;
import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import environment.Grid;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import gui.GridGUI;

public class RoverAgent extends Agent {
    private static final long serialVersionUID = -6241375886823075380L;
    private int x, y, targetX, targetY;
    private boolean isMovingToTarget = false;
    public Grid grid;
    private String droneName = "";
    private boolean isDroneLaunched = false;

    private AgentController droneAgent;
    public GridGUI gridGUI;

    protected void setup() {
        // grid = new Grid(10, 10);
        // gridGUI = new GridGUI(grid);
        // take the grid from the main container
        System.out.println("Rover-agent " + getAID().getName() + " is ready.");
        Object[] args = getArguments();
        if (args == null || args.length < 2) {
            System.out.println("Invalid arguments. Please provide the grid and gridGUI objects.");
        }
        grid = (Grid) args[0];
        gridGUI = (GridGUI) args[1];
        x = 0;
        y = 0;
        handleTargetCoordinatesMessage();
        handleLaunchHelidrone();
        updateDroneInGridGUI();
        receiveDroneLandingMessage();
        mainRoverLogic();
        handleHelidroneRequest();
        handleAlienProposal();
        addBehaviour(new ReceivedShutdown());
    }

    private void handleAlienProposal() {
        // add behaviour to receive the proposal from the alien
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                        MessageTemplate.MatchConversationId("Alien-Rover-Proposal"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    gridGUI.printMessageColored("Someone or something try to communicate with me. So scary! " + msg.getContent(), Color.RED);
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    reply.setConversationId("Rover-Alien-Rej-Proposal");
                    reply.setContent("Who are you? I'm busy.");
                    send(reply);
                    gridGUI.printMessageColored("Proposal rejected", Color.RED);
                } else {
                    block();
                }
            }
        });
    }

    private void handleHelidroneRequest() {
        // Add the behaviour to respond to the helidrone if it request the coordinates
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchConversationId("Drone-Rover-Coordinates"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    SendCoordToHeliDrone();
                } else {
                    block();
                }
            }
        });
    }

    private void mainRoverLogic() {
        addBehaviour(new TickerBehaviour(this, 1000) { // Update every second
            protected void onTick() {
                if (isMovingToTarget) {
                    // Rover launches the drone if the target is far away
                    if (isDroneLaunched == false && distanceToTarget() >= 8) {
                        gridGUI.printMessageColored(
                                "The target is far away from the rover. Launching the drone to watch from above.",
                                Color.RED);
                        launchDrone();
                        isDroneLaunched = true;
                    }
                    moveToTarget();
                    gridGUI.updateRoverPosition(x, y);
                    if (x == targetX && y == targetY) {
                        isMovingToTarget = false;
                        isDroneLaunched = false;
                        x = targetX;
                        y = targetY;
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(new AID("GroundControl", AID.ISLOCALNAME));
                        msg.setConversationId("Rover-Target-Reach");
                        msg.setContent("Rover has reached the target position at (" + x + "," + y + ")");
                        send(msg);
                        gridGUI.printMessageColored(
                                "Here rover to Ground control: I reached the target position. I start to dig.",
                                Color.RED);
                        if (isMovingToTarget == false) {
                            // send a message to the helidrone to move to the new coordinates
                            addBehaviour(new WakerBehaviour(myAgent, 2000) {
                                protected void onWake() {
                                    // send a message that the rover is finished digging
                                    ACLMessage digFinishedMsg = new ACLMessage(ACLMessage.INFORM);
                                    digFinishedMsg.addReceiver(new AID("GroundControl", AID.ISLOCALNAME));
                                    digFinishedMsg.setConversationId("Rover-Dig-Finished");
                                    digFinishedMsg.setContent("Rover has finished digging at (" + x + "," + y + ")");
                                    send(digFinishedMsg);
                                    gridGUI.printMessageColored("Here rover to Ground control: I finished digging.",
                                            Color.RED);
                                    addBehaviour(new WakerBehaviour(myAgent, 3000) {
                                        protected void onWake() {
                                            Random random = new Random();
                                            String[] findings = { "stone", "water", "unknown manufacture" };
                                            int index = random.nextInt(findings.length);
                                            String foundItem = findings[index];
                                            gridGUI.printMessageColored("Analysis complete, found: " + foundItem,
                                                    Color.RED);

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
                    }
                }
            }
        });
    }

    private void receiveDroneLandingMessage() {
        //beahviour to receive the message from the ground control that the drone has landed
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("Drone-Landed"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    if (msg.getContent().equals("Drone has landed")) {
                        gridGUI.printMessageColored("Drone has landed.", Color.RED);
                        gridGUI.clearDronePosition();
                    }
                } else {
                    block();
                }
            }

        });
    }

    private void updateDroneInGridGUI() {
        // TODO: add the behaviour to update the drone in GRIDGUI
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                        MessageTemplate.MatchConversationId("Drone-Grid-Update"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    String content = msg.getContent();
                    // System.out.println("Received new coordinates from Rover: " + content);
                    if (content.contains(",")) {
                        String[] parts = content.split(",");
                        int droneX = Integer.parseInt(parts[0].trim());
                        int droneY = Integer.parseInt(parts[1].trim());
                        // gridGUI.printMessageColored("Drone is at position (" + droneX + "," + droneY
                        // + ")", Color.RED);
                        // System.out.println("Drone is at position (" + droneX + "," + droneY + ")");
                        gridGUI.updateDronePosition(droneX, droneY);
                    } else {
                        System.out.println("Invalid coordinates received.");
                    }
                } else {
                    block();
                }
            }

        });
    }

    private void handleLaunchHelidrone() {
        // add a cyclic behaviour to receive the message if launch the helidrone
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("GroundControl-Launch-Helidrone"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    String content = msg.getContent();
                    if (content.contains("launch-helidrone")) {
                        if (!isDroneLaunched) {
                            launchDrone();
                            isDroneLaunched = true;
                        } else {
                            gridGUI.printMessageColored("The drone is already launched.", Color.RED);
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void handleTargetCoordinatesMessage() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("GroundControl-Target-Coordinates"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    gridGUI.printMessageColored(
                            "Rover-agent " + getAID().getName() + " is ready at position (" + x + "," + y + ").",
                            Color.RED);
                    String content = msg.getContent();
                    if (content.contains(",")) {
                        String[] parts = content.split(",");
                        targetX = Integer.parseInt(parts[0].trim());
                        targetY = Integer.parseInt(parts[1].trim());
                        isMovingToTarget = true;
                        gridGUI.printMessageColored("Moving to target position (" + targetX + "," + targetY + ")",
                                Color.RED);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private int distanceToTarget() {
        // Manhattan distance
        return Math.abs(targetX - x) + Math.abs(targetY - y);
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

    private void launchDrone() {
        ContainerController container = getContainerController();
        try {
            droneName = "HeliDrone" + System.currentTimeMillis(); // unique name
            droneAgent = container.createNewAgent(droneName,
                    "helidrone.HeliDrone", new Object[]{this.grid});
            droneAgent.start();
            gridGUI.printMessageColored("Drone agent launched: " + droneName, Color.RED);
        } catch (ControllerException e) {
            e.printStackTrace();
            gridGUI.printMessageColored("Failed to launch the drone agent.", Color.RED);
        }
    }

    private void SendCoordToHeliDrone() {
        // send a message with the coordinates to the helidrone
        ACLMessage msgDrone = new ACLMessage(ACLMessage.INFORM);
        msgDrone.addReceiver(new AID(droneName, AID.ISLOCALNAME));
        msgDrone.setConversationId("Rover-Drone-Coordinates");
        msgDrone.setContent(x + "," + y);
        send(msgDrone);
        gridGUI.printMessageColored("Here rover to HeliDrone: I'm at position (" + x + "," + y + ").", Color.RED);
    }

    private void notifyGroundControlDigFinished() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("groundcontrol", AID.ISLOCALNAME));
        msg.setConversationId("Rover-work-Finished");
        msg.setContent("Finish to work at (" + x + "," + y + ")");
        send(msg);
        gridGUI.printMessageColored("Here Rover to Ground Control: I'm ready to receive new target coordinates.",
                Color.RED);

    }

    protected void takeDown() {
        System.out.println("Rover-agent " + getAID().getName() + " is shutting down.");
        GridGUI gridGUI = new GridGUI(grid);
        gridGUI.printMessageColored("Rover-agent " + getAID().getName() + " is shutting down.", Color.RED);
        gridGUI.dispose();
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
