# Planet Exploration Multi-Agent System

Welcome to the Planet Exploration Multi-Agent System repository. This project, for Unige's Multi-agent system course, exploits JADE (Java Agent DEvelopment Framework) to simulate the exploration of a hypothetical Mars-like planet. The system consists of four types of agents: Drone, Rover, Ground Control and Alien.

## Project Structure 📂

- **src/**: Contains the source code for the agents and their behaviors.
  - **DroneAgent.java**: Defines the Drone agent's behaviors and communication protocols.
  - **RoverAgent.java**: Defines the Rover agent's behaviors and communication protocols.
  - **GroundControlAgent.java**: Defines the Ground Control agent's behaviors and communication protocols.
  - **AlienAgent.java**: Defines the Alien agent's behaviors and communication protocols.
- **lib/**: Contains the JADE library and its dependencies.
- **images/**: Images of icons.
- **bin/**: the compiled output files will be generated in the `bin` folder by default.
- **makefile**: make file to compile and run the project.
- **README.md**: Project overview and setup instructions.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Agents Overview

### Drone Agent
The Drone agent is responsible for aerial surveillance and mapping of the planetary surface. It communicates with rover to relay information and receives commands for specific tasks.

### Rover Agent
The Rover agent performs ground-level exploration, collecting soil samples, and conducting in-situ analysis. It interacts with the Drone and Ground Control to navigate and complete its mission objectives.

### Ground Control Agent
The Ground Control agent acts as the central coordinator for the mission. Entry point for the user.

### Alien Agent
The Alien agent represents indigenous entities on the planet. It interacts with the Rover and Drone agents, introducing dynamic elements and challenges to the exploration mission.

## Getting Started 🚀

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Apache Maven
- JADE library (included in the `lib/` directory)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/Nicogs43/PlanetExploration.git
   cd PlanetExploration

2. Compile and run
    ```bash
    make all
