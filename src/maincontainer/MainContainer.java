package maincontainer;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import environment.Grid;
import gui.GridGUI;

public class MainContainer {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        AgentContainer mainContainer = rt.createMainContainer(p);

        try {
            Grid grid = new Grid(10, 10);
            GridGUI gridGui = new GridGUI(grid);

            AgentController roverAgent = mainContainer.createNewAgent("rover", "rover.RoverAgent", new Object[] {grid, gridGui});
            AgentController GroundControlAgent = mainContainer.createNewAgent("groundcontrol", "groundcontrol.GroundControl", null);
            AgentController alienAgent = mainContainer.createNewAgent("alien", "alien.Alien", new Object[] {grid, gridGui});
            roverAgent.start();
            GroundControlAgent.start();
            alienAgent.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }
}


