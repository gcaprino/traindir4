package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Activator;
import com.traindirector.ICommandIds;

public class EditAction extends Action {

	private final IWorkbenchWindow window;
	private int instanceNum = 0;
	
	public EditAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_EDIT);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_EDIT);
		setImageDescriptor(Activator.getImageDescriptor("/icons/edit16x16black.png"));
	}
	
	public void run() {
		if(window != null) {	
//			try {
//				window.getActivePage().showView(viewId, Integer.toString(instanceNum++), IWorkbenchPage.VIEW_ACTIVATE);
//			} catch (PartInitException e) {
//				MessageDialog.openError(window.getShell(), "Error", "Error opening view:" + e.getMessage());
//			}
		}
	}

}
