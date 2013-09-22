package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.NewCommand;

public class NewAction extends Action {

	public NewAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_NEW);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_NEW);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
		NewCommand cmd = new NewCommand();
		Application.getSimulator().addCommand(cmd);
	}
}
