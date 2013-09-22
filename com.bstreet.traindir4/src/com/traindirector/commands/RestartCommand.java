package com.traindirector.commands;

import com.traindirector.simulator.SimulatorCommand;

public class RestartCommand extends SimulatorCommand {

	public void handle() {
		_simulator.restartSimulation();
	}
}
