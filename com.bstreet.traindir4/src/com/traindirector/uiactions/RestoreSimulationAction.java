package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.RestoreCommand;
import com.traindirector.simulator.Simulator;

public class RestoreSimulationAction extends Action {

	public RestoreSimulationAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_RESTORE);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_RESTORE);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
		Simulator sim = Application.getSimulator();
		RestoreCommand cmd = new RestoreCommand(sim.getLastSaved());
		sim.addCommand(cmd);
	}
}
