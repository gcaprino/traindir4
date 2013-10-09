package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;

import com.bstreet.cg.events.CGEventDispatcher;
import com.traindirector.ICommandIds;
import com.traindirector.events.ToggleCoordBarsEvent;

public class CoordBarsAction extends Action {

	public CoordBarsAction(IWorkbenchWindow window, String label) {
		super(label, IAction.AS_CHECK_BOX);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_COORD_BARS);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_COORD_BARS);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/coordBars16x16black.png"));
	}
	
	public void run() {
		this.setChecked(!isChecked());
		ToggleCoordBarsEvent event = new ToggleCoordBarsEvent();
		CGEventDispatcher.INSTANCE.postEvent(event);
	}
}
