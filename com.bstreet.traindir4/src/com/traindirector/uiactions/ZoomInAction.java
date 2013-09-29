package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.bstreet.cg.events.CGEventDispatcher;
import com.traindirector.ICommandIds;
import com.traindirector.events.ChangeZoomingEvent;

public class ZoomInAction extends Action {

	public ZoomInAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_ZOOM_IN);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_ZOOM_IN);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/zoomin16x16black.png"));
	}
	
	public void run() {
		ChangeZoomingEvent event = new ChangeZoomingEvent(1);
		CGEventDispatcher.getInstance().postEvent(event);
	}
}
