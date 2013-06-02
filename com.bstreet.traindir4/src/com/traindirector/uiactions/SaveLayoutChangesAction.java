package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.ICommandIds;

public class SaveLayoutChangesAction extends Action {

	private final IWorkbenchWindow window;
	private int instanceNum = 0;
	
	public SaveLayoutChangesAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SAVE_CHANGES);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_SAVE_CHANGES);
//		setImageDescriptor(com.bstreet.cg.traindirector.Activator.getImageDescriptor("/icons/sample2.gif"));
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
