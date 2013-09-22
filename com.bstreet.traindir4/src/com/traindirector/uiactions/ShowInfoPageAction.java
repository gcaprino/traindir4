package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.editors.InfoPage;
import com.traindirector.simulator.Simulator;

public class ShowInfoPageAction extends Action {

	IWorkbenchWindow _window;
	
	public ShowInfoPageAction(IWorkbenchWindow window, String label) {
		_window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SHOW_INFO_PAGE);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_SHOW_INFO_PAGE);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}

	public void run() {
		Simulator sim = Application.getSimulator();
    	InfoPage.openEditor(_window, sim._baseFileName + ".htm");
		//SpeedCommand cmd = new SpeedCommand(1);
		//Application.getSimulator().addCommand(cmd);
	}
}
