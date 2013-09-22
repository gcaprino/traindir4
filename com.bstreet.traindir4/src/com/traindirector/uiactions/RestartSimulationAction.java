package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Activator;
import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.RestartCommand;

public class RestartSimulationAction extends Action {

	IWorkbenchWindow _window;
	
	public RestartSimulationAction(IWorkbenchWindow window, String label) {
		_window = window;
		setText(label);
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_RESTART);
		// Associate the action with a pre-defined command, to allow key
		// bindings.
		setActionDefinitionId(ICommandIds.CMD_RESTART);
		setImageDescriptor(Activator.getImageDescriptor("/icons/restart16x16black.png"));
	}

	public void run() {

		boolean result = MessageDialog.openConfirm(_window.getShell(), "Confirm",
				"Do you really want to restart the simulation?");

		if (result) {
			RestartCommand cmd = new RestartCommand();
			Application.getSimulator().addCommand(cmd);
		}
	}
}
