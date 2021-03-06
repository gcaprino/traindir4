package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Activator;
import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.SkipAheadCommand;
import com.traindirector.commands.SpeedCommand;

public class SkipAheadAction extends Action {

	public SkipAheadAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SKIP_AHEAD);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_SKIP_AHEAD);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
		SkipAheadCommand cmd = new SkipAheadCommand();
		Application.getSimulator().addCommand(cmd);
	}
}
