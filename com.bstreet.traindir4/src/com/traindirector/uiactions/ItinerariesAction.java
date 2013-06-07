package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.ICommandIds;

public class ItinerariesAction extends Action {

	public ItinerariesAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_EDIT_ITINERARIES);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_EDIT_ITINERARIES);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
		//SpeedCommand cmd = new SpeedCommand(1);
		//Application.getSimulator().addCommand(cmd);
	}
}
