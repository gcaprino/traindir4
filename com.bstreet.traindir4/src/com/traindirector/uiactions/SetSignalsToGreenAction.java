package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.SetSignalsToGreenCommand;

public class SetSignalsToGreenAction extends Action {

	public SetSignalsToGreenAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SET_SIGNALS_TO_GREEN);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_SET_SIGNALS_TO_GREEN);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
		SetSignalsToGreenCommand cmd = new SetSignalsToGreenCommand();
		Application.getSimulator().addCommand(cmd);
	}
}
