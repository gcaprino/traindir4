package com.traindirector.uiactions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Activator;
import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.ItineraryCommand;
import com.traindirector.dialogs.ItineraryDialog;
import com.traindirector.model.Itinerary;
import com.traindirector.simulator.Simulator;

public class ItineraryAction extends Action {

	IWorkbenchWindow _window;
	
	public ItineraryAction(IWorkbenchWindow window, String label) {
		_window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_ITINERARY);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_ITINERARY);
		setImageDescriptor(Activator.getImageDescriptor("/icons/switch16x16black.png"));
		setImageDescriptor(Activator.getImageDescriptor("/icons/direction30x30black.png"));
	}
	
	public void run() {
		String itinerary = null;
		
		ItineraryDialog dialog = new ItineraryDialog(_window.getShell());
		Simulator simulator = Application.getSimulator();
		List<Itinerary> itineraries = simulator._territory._itineraries;
		dialog.create();
		dialog.fillTable(itineraries);
		int result = dialog.open();
		if (result == ItineraryDialog.CANCEL)
			return;
		itinerary = dialog._selectedItinerary;
		ItineraryCommand icmd = new ItineraryCommand(itinerary);
		icmd._deselect = result == ItineraryDialog.CLEAR;
		Application.getSimulator().addCommand(icmd);
	}

}
