SRC_PATH = src
CLASS_PATH = ".;lib/jade.jar;src"

# Define your source files
SOURCES = $(SRC_PATH)/groundcontrol/GroundControl.java \
          $(SRC_PATH)/rover/RoverAgent.java \
		  $(SRC_PATH)/helidrone/HeliDrone.java 

# Java compiler
JAVAC = javac
JAVA = java

.PHONY: all compile run clean

all:  compile run

compile: $(SOURCES)
	$(JAVAC) -cp $(CLASS_PATH) $(SOURCES)

run:
	$(JAVA) -cp $(CLASS_PATH) jade.Boot -host localhost -port 1099 -agents "rover:rover.RoverAgent;groundcontrol:groundcontrol.GroundControl"

clean:
	rm -f $(SRC_PATH)/**/*.class


