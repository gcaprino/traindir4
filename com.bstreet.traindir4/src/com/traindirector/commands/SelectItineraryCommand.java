package com.traindirector.commands;

import com.traindirector.model.Itinerary;
import com.traindirector.model.Track;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.SimulatorCommand;

public class SelectItineraryCommand extends SimulatorCommand {

	Track	_track;

	public SelectItineraryCommand(Track track) {
		_track = track;
	}
	
	public void handle() {
		String name = _track._station;
		if (name.charAt(0) == '@') {
			_track.onClicked();
			return;
		}
		int index = name.lastIndexOf('@');
		if (index > 0)
			name = name.substring(0, index);
		Itinerary itin = Simulator.INSTANCE._territory.findItinerary(name);
		if (itin != null) {
			itin.select();
		}
		
	}
}
