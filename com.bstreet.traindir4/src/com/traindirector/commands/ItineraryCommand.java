package com.traindirector.commands;

import com.traindirector.model.Itinerary;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.SimulatorCommand;

public class ItineraryCommand extends SimulatorCommand {

	String _name;
	public boolean _deselect;

	public ItineraryCommand(String name) {
		_name = name;
	}

	public void handle() {
		Simulator simulator = Simulator.INSTANCE;
		Itinerary itinerary = simulator._territory.findItinerary(_name);
		if (itinerary == null) {
			simulator.alert("Itinerary '" + _name + "' does not exist.");
			return;
		}
		if (_deselect)
			itinerary.deselect(false);
		else
			itinerary.select();
	}
}
