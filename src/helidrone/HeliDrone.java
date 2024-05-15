package helidrone;

import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import environment.Grid;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import environment.Grid;
import rover.RoverAgent;

public class HeliDrone extends Agent {
    private boolean inFlight = false;
    public RoverAgent roverAgent;
    private Grid grid;
    private int[] lastKnownCoordinates = new int[2];

    protected void setup() {
        grid = new Grid(10, 10);
        // create a simple behaviour that print out that the drone is created and ready
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                System.out.println("Hello! HeliDrone: " + getAID().getName() + " is ready.");
                takeOff();
                moveAround();
                land();
            }

        });
    };

    // helicopter drone goes around on the grid for 1,5 after that request the new
    // coordinates from the rover agent from rover
    // agent and then goes to the new coordinates
    public void moveAround() {
        Random rand = new Random();

        // wait for 1.5 seconds
        addBehaviour(new WakerBehaviour(this, 1000) {
            protected void onWake() {
                int newX = lastKnownCoordinates[0] + rand.nextInt(5) - 2;
                int newY = lastKnownCoordinates[1] + rand.nextInt(5) - 2;
                // Ensure x and y are within the grid boundaries
                newX = Math.max(0, Math.min(newX, grid.getWidth() - 1));
                newY = Math.max(0, Math.min(newY, grid.getHeight() - 1));
                // request the new coordinates from the rover agent
                System.out.println("HeliDrone is moving to position (" + newX + "," + newY + ")");
                requestNewCoordinates();
                acceptNewCoordinates();
            }
        });
    }

    // send the request for the new coordinates to the rover agent
    public void requestNewCoordinates() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("rover", AID.ISLOCALNAME));
        msg.setContent("New Coordinates");
        msg.setConversationId("Drone-Rover-Coordinates");
        send(msg);
        System.out.println("Requesting new coordinates from Rover.");
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
                        System.out.println("Received new coordinates from Rover: (" + lastKnownCoordinates[0] + ","
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
        addBehaviour(new WakerBehaviour(this, 1500) {
            protected void onWake() {
                System.out.println("HeliDrone is landing at position (" + lastKnownCoordinates[0] + ","
                        + lastKnownCoordinates[1] + ")");
                droneShutDown shutdown = new droneShutDown();
                addBehaviour(shutdown);
            }
        });
    }

    public void takeOff() {
        if (!inFlight) {
            System.out.println("HeliDrone is taking off.");
            inFlight = true;
        } else {
            System.out.println("HeliDrone is already in flight.");
        }
    }

    private class droneShutDown extends OneShotBehaviour {
        public void action() {
            doDelete();
        }
    }

    public void takeDown() {
        System.out.println("HeliDrone is shutting down.");

    }

}