package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Activator;
import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.SpeedCommand;

public class CoordBarsAction extends Action {

	public CoordBarsAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_COORD_BARS);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_COORD_BARS);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
		//SpeedCommand cmd = new SpeedCommand(1);
		//Application.getSimulator().addCommand(cmd);
	}
}
