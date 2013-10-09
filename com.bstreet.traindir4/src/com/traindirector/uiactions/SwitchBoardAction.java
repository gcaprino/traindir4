package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.ICommandIds;
import com.traindirector.editors.SwitchboardEditor;

public class SwitchBoardAction extends Action {

	IWorkbenchWindow _window;
	
	public SwitchBoardAction(IWorkbenchWindow window, String label) {
		_window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SWITCHBOARD);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_SWITCHBOARD);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
		SwitchboardEditor.openEditor(_window, "Switchboard");
	}
}
