package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.ICommandIds;
import com.traindirector.editors.ReportEditor;

public class StationsListAction extends Action {

	IWorkbenchWindow _window;
	
	public StationsListAction(IWorkbenchWindow window, String label) {
		_window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_STATIONS_LIST);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_STATIONS_LIST);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
        ReportEditor.openEditor(_window, "/stations/");
	}
}
