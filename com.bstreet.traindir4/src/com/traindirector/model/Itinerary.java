package com.traindirector.model;

import java.util.ArrayList;
import java.util.List;

import com.traindirector.simulator.Simulator;

public class Itinerary {

	public String _name;
	public String _signame;
	public String _endsig;
	public String _nextitin;
	public boolean _visited;
	public List<ItinerarySwitch> _switches = new ArrayList<ItinerarySwitch>();
	public String _iconSelected, _iconDeselected;	// TODO
	Territory territory;
	
	public class ItinerarySwitch {
		public TDPosition _position;
		public boolean _thrown;
		public boolean _oldThrown;
	}
	
	public void addSwitch(int x, int y, boolean thrown) {
		ItinerarySwitch sw = new ItinerarySwitch();
		sw._position = new TDPosition(x, y);
		sw._thrown = thrown;
		_switches.add(sw);
	}

	public boolean canSelect() {
		return false;
	}
	
	public boolean isSelected() {
		return false;
	}

	public boolean select() {
		 territory = Simulator.INSTANCE._territory;
		 if (!checkItinerary()) {
			 return false;
		 }
		 toggleItinerary();
		 if (greenItinerary()) {
			 return true;
		 }
		 
		 // error - restore switches statuses
		 
		 for (ItinerarySwitch isw : _switches) {
			 Switch sw = territory.findSwitch(isw._position);
			 if (sw == null)
				 continue;
			 if (sw._switched == isw._oldThrown)
				 continue;
			 sw._switched = isw._oldThrown;
			 sw.setUpdated();
			 if (sw._wlink != null && (sw._wlink._x + sw._wlink._y) != 0) {
				 Switch linkedSwitch = territory.findSwitch(sw._wlink);
				 if (linkedSwitch != null) {
					 linkedSwitch._switched = isw._oldThrown;
					 linkedSwitch.setUpdated();
				 }
			 }
		 }
		 return false;
	}
	
	public boolean deselect(boolean checkOnly) {
		return false;
	}
	
	public boolean turnSwitches() {
		return false;
	}
	
	public boolean restoreSwitches() {
		return false;
	}
	
	public void onInit() {
		
	}
	
	public void onClick() {
		
	}
	
	public void onCanceled() {
		
	}
	

	private void clearVisited() {
		territory.clearVisitedItineraries();
	}
	private boolean checkItinerary() {
		String nextItin;
		clearVisited();
		Itinerary it = this;
		do {
			if (it._visited)		// prevent infinite loop
				return false;
			
			// check that we can throw all the switches in the itinerary
			
			for (ItinerarySwitch isw : it._switches) {
				Switch sw = territory.findSwitch(isw._position);
				if (sw == null || sw._status != TrackStatus.FREE)  {
					return false;
				}
				if (isw._thrown != sw._switched && sw._wlink != null && (sw._wlink._x + sw._wlink._y) != 0) {
					Switch linkedSwitch = territory.findSwitch(sw._wlink);
					if (linkedSwitch != null && linkedSwitch._status != TrackStatus.FREE)
						return false;
				}
			}
		
			if (it._nextitin == null || it._nextitin.isEmpty())
				return true;
			it = territory.findItinerary(it._nextitin);
		} while(it != null);
		return false;
	}
	
	private void toggleItinerary() {
		Itinerary it = this;
		do {
		 	for (ItinerarySwitch isw : it._switches) {
		 		Switch sw = territory.findSwitch(isw._position);
				isw._oldThrown = sw._switched;
		 		if (isw._thrown != sw._switched) {
		 			sw._switched = isw._thrown;
		 			sw.setUpdated();
		 			if (sw._wlink != null && (sw._wlink._x + sw._wlink._y) != 0) {
						Switch linkedSwitch = territory.findSwitch(sw._wlink);
						if (linkedSwitch != null) {
							linkedSwitch._switched = isw._thrown;
							linkedSwitch.setUpdated();
						}
		 			}
		 		}
		 	}
		 	if (it._nextitin == null || it._nextitin.isEmpty())
		 		return;
		 	it = territory.findItinerary(it._nextitin);
		} while(it != null);
	}

	private boolean greenItinerary() {
		Itinerary it = this;
		List<Signal> blocks = new ArrayList<Signal>();
		do {
			Signal sig = territory.findSignalNamed(it._signame);
			if (sig == null)
				return false;
			SignalAspect aspect = sig.getAspect();
			if (aspect._action.compareTo(SignalAspect.PROCEED) == 0)
				return false;
			blocks.add(sig);
			if (it._nextitin == null || it._nextitin.isEmpty())
				break;
			it = territory.findItinerary(it._nextitin);
		} while (it != null);
		
		// all signals are red, try to turn them green
		
		int i;
		Signal sig = null;
		for (i = 0; i < blocks.size(); ++i) {
			sig = blocks.get(i);
			if (!sig.toggle())
				break;			// line block is busy
		}
		if (i >= blocks.size())	// success
			return true;
		while (--i >= 0) {		// undo signal toggling
			sig = blocks.get(i);
			sig.toggle();
		}
		return false;
	}
	
}
