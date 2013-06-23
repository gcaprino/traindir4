package com.traindirector.commands;

import com.traindirector.model.Train;
import com.traindirector.simulator.SimulatorCommand;

public class SplitCommand extends SimulatorCommand {

	private Train _train;
	private int _length;

	public SplitCommand(Train train, int length) {
		_train = train;
		_length = length;
	}
	
	public void handle() {
		// TODO: handle split command
	}
}
