package helidrone;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import environment.Grid;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import gui.HeliDroneGUI;

public class HeliDrone extends Agent {
    private boolean inFlight = false;
    public Grid grid;
    public HeliDroneGUI heliDroneGUI;
    private int[] lastKnownCoordinates = new int[2];

    protected void setup() {
        grid = new Grid(10, 10);
        heliDroneGUI = new HeliDroneGUI();

        // create a simple behaviour that print out that the drone is created and ready
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                heliDroneGUI.printMessage("Hello! HeliDrone: " + getAID().getName() + " is ready.");
                takeOff();
                moveAround();
                //land();
            }

        });
    };

    // helicopter drone goes around on the grid for 1,5 after that request the new
    // coordinates from the rover agent from rover agent and then goes to the new
    // coordinates
    public void moveAround() {
        Random rand = new Random();
        Set<String> visitedPositions = new HashSet<>();

        // Create a ParallelBehaviour to run both the TickerBehaviour and WakerBehaviour
        ParallelBehaviour parallelBehaviour = new ParallelBehaviour(ParallelBehaviour.WHEN_ANY);

        // TickerBehaviour to move the drone at regular intervals
        TickerBehaviour ticker = new TickerBehaviour(this, 400) { // 300 ms interval
            protected void onTick() {
                int newX, newY;
                do {
                    newX = lastKnownCoordinates[0] + rand.nextInt(5) - 2;
                    newY = lastKnownCoordinates[1] + rand.nextInt(5) - 2;
                    // Ensure x and y are within the grid boundaries
                    newX = Math.max(0, Math.min(newX, grid.getWidth() - 1));
                    newY = Math.max(0, Math.min(newY, grid.getHeight() - 1));
                } while (visitedPositions.contains(newX + "," + newY));
    
                visitedPositions.add(newX + "," + newY);
    
                heliDroneGUI.printMessage("HeliDrone is moving to position (" + newX + "," + newY + ")");
                lastKnownCoordinates[0] = newX;
                lastKnownCoordinates[1] = newY;
                requestGridGUIUpdate(lastKnownCoordinates[0], lastKnownCoordinates[1]);
            }
        };

        // WakerBehaviour to stop the movement after 1.5 seconds
        WakerBehaviour waker = new WakerBehaviour(this, 2000) {
            protected void onWake() {
                heliDroneGUI.printMessage("HeliDrone has stopped moving.");
                parallelBehaviour.removeSubBehaviour(ticker);
                requestNewCoordinates();
                acceptNewCoordinates();
                land();
            }
        };
        // Add both behaviours to the ParallelBehaviour
        parallelBehaviour.addSubBehaviour(ticker);
        parallelBehaviour.addSubBehaviour(waker);

        // Add the ParallelBehaviour to the agent
        addBehaviour(parallelBehaviour);
    }
    public void requestGridGUIUpdate(int x , int y) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("rover", AID.ISLOCALNAME));
        msg.setContent(x + "," + y);
        msg.setConversationId("Drone-Grid-Update");
        send(msg);
        //heliDroneGUI.printMessage("HeliDrone: Requesting Grid Update." + x + "," + y);
    }
    // send the request for the new coordinates to the rover agent
    public void requestNewCoordinates() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("rover", AID.ISLOCALNAME));
        msg.setContent("New Coordinates");
        msg.setConversationId("Drone-Rover-Coordinates");
        send(msg);
        heliDroneGUI.printMessage("HeliDrone: Requesting new coordinates from Rover.");
    }

    // accept the new coordinates from the rover agent
    public void acceptNewCoordinates() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("Rover-Drone-Coordinates"));
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    String content = msg.getContent();

                    // process the message and store the last known coordinates of the rover agent
                    if (content.contains(",")) {
                        String[] parts = content.split(",");
                        lastKnownCoordinates[0] = Integer.parseInt(parts[0].trim());
                        lastKnownCoordinates[1] = Integer.parseInt(parts[1].trim());
                        heliDroneGUI.printMessage(
                                "Received new coordinates from Rover: (" + lastKnownCoordinates[0] + ","
                                        + lastKnownCoordinates[1] + ")");
                    } else {
                        System.out.println("Invalid coordinates received.");
                    }
                } else {
                    block();
                }
            }
        });
    }

    public void land() {
        addBehaviour(new WakerBehaviour(this, 2000) {
            protected void onWake() {
                heliDroneGUI.printMessage("HeliDrone is landing at position (" + lastKnownCoordinates[0] + ","
                        + lastKnownCoordinates[1] + ")");
                droneShutDown shutdown = new droneShutDown();
                addBehaviour(shutdown);
                //send an message to the rover agent that the drone has landed and so delete the icon in GridGUI
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("rover", AID.ISLOCALNAME));
                msg.setContent("Drone has landed");
                msg.setConversationId("Drone-Landed");
                send(msg);
            }
        });
    }

    public void takeOff() {
        if (!inFlight) {
            heliDroneGUI.printMessage("HeliDrone is taking off.");
            requestNewCoordinates();
            acceptNewCoordinates();
            inFlight = true;
        } else {
            heliDroneGUI.printMessage("HeliDrone is already in flight.");

        }
    }

    private class droneShutDown extends OneShotBehaviour {
        public void action() {
            doDelete();
        }
    }

    public void takeDown() {
        System.out.println("HeliDrone is shutting down.");
        // heliDroneGUI.dispose();

    }

}