package com.traindirector.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import com.traindirector.files.Option;
import com.traindirector.model.Itinerary;

public class ItineraryProperties {

	List<Option> _options;
	Itinerary _itinerary;
	
	public ItineraryProperties(Itinerary itinerary) {
		_itinerary = itinerary;
	}
	
	public boolean open() {
    	_options = new ArrayList<Option>();
    	Option o = new TextOption("name", "Name:");
    	o._value = _itinerary._name;
    	_options.add(o); // 0
    	o = new TextOption("sig1", "From signal:");
    	o._value = _itinerary._signame;
    	_options.add(o); // 1
    	o = new TextOption("sig2", "Next itinerary:");
    	o._value = _itinerary._nextitin;
    	_options.add(o); // 2

    	final int[] result = new int[1];
    	final PropertyDialog dialog = new PropertyDialog(null, _options);
    	Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = dialog.open();
			}
		});
    	if(result[0] == PropertyDialog.CANCEL)
    		return false;

    	_itinerary._name = _options.get(0)._value;
    	_itinerary._nextitin = _options.get(2)._value;
    	return true;
	}
}
