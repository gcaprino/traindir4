package com.traindirector.uiactions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Activator;
import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.AssignCommand;
import com.traindirector.commands.ItineraryCommand;
import com.traindirector.dialogs.AssignDialog;
import com.traindirector.model.Itinerary;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.SimulatorCommand;

public class AssignAction extends Action {

	IWorkbenchWindow _window;
	
	public AssignAction(IWorkbenchWindow window, String label) {
		_window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_ASSIGN);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_ASSIGN);
		setImageDescriptor(Activator.getImageDescriptor("/icons/train16x16black.png"));
	}
	
	public void run() {
		// TODO: remove assign action
	}

}
