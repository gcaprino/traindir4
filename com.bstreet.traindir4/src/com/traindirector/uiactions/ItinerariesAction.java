package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.traindirector.ICommandIds;
import com.traindirector.SimulationPerspective;
import com.traindirector.simulator.Simulator;

public class ItinerariesAction extends Action {

	public ItinerariesAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_EDIT_ITINERARIES);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_EDIT_ITINERARIES);
		//setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
	}
	
	public void run() {
        String perspectiveId = "com.traindirector.edit.itineraryPerspective";
        if(Simulator.getEditing()) {
            perspectiveId = SimulationPerspective.ID;
            Simulator.INSTANCE.setEditingItineraries(false);
        } else {
            Simulator.INSTANCE.setEditingItineraries(true);
        }
        try {
            PlatformUI.getWorkbench().showPerspective(perspectiveId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        } catch (WorkbenchException e) {
            e.printStackTrace();
        }
	}
}
