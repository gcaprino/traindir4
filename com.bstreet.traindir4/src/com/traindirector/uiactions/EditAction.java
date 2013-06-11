package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.traindirector.Activator;
import com.traindirector.ICommandIds;
import com.traindirector.SimulationPerspective;
import com.traindirector.simulator.Simulator;

public class EditAction extends Action {

    private final IWorkbenchWindow window;
    private int instanceNum = 0;

    public EditAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_EDIT);
        // Associate the action with a pre-defined command, to allow key
        // bindings.
        setActionDefinitionId(ICommandIds.CMD_EDIT);
        setImageDescriptor(Activator.getImageDescriptor("/icons/edit16x16black.png"));
    }

    public void run() {
        String perspectiveId = "com.traindirector.edit.layoutPerspective";
        if(Simulator.getEditing()) {
            perspectiveId = SimulationPerspective.ID;
            Simulator.INSTANCE.setEditing(false);
        } else {
            Simulator.INSTANCE.setEditing(true);
        }
        try {
            PlatformUI.getWorkbench().showPerspective(perspectiveId, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        } catch (WorkbenchException e) {
            e.printStackTrace();
        }
    }

}
