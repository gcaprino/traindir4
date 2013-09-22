package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.traindirector.Activator;
import com.traindirector.ICommandIds;
import com.traindirector.views.ScheduleView;

public class ShowScheduleViewAction extends Action {

	private final IWorkbenchWindow window;
	private int instanceNum = 0;

	public ShowScheduleViewAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SHOW_SCHEDULE);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_SHOW_SCHEDULE);
		setImageDescriptor(Activator.getImageDescriptor("/icons/schedule16x16black.png"));
	}

	public void run() {
		if(window != null) {	
			try {
				window.getActivePage().showView(ScheduleView.ID, Integer.toString(instanceNum++), IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:" + e.getMessage());
			}
		}
	}
}
