package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.views.TraceView;

public class ShowTraceViewAction extends Action {

	private final IWorkbenchWindow window;
	private int instanceNum = 0;

	public ShowTraceViewAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SHOW_TRACE);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_SHOW_TRACE);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/schedule16x16black.png"));
	}

	public void run() {
		if(window != null) {	
			try {
				TraceView view = (TraceView)window.getActivePage().showView(TraceView.ID, Integer.toString(instanceNum++), IWorkbenchPage.VIEW_ACTIVATE);
				Application.getSimulator().setTraceView(view);
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:" + e.getMessage());
			}
		}
	}
}
