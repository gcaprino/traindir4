package com.traindirector.model;

import com.traindirector.commands.SelectItineraryCommand;
import com.traindirector.simulator.Simulator;

public class ItineraryButton extends Track {
	public void onClick() {
		SelectItineraryCommand cmd = new SelectItineraryCommand(this);
		Simulator.INSTANCE.addCommand(cmd);
	}
}
