package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.ICommandIds;
import com.traindirector.editors.MapPage;

public class ShowMapAction extends Action {

    IWorkbenchWindow _window;
    
    public ShowMapAction(IWorkbenchWindow window, String label) {
        _window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_SHOW_MAP);
        // Associate the action with a pre-defined command, to allow key bindings.
        setActionDefinitionId(ICommandIds.CMD_SHOW_MAP);
        //setImageDescriptor(Activator.getImageDescriptor("/icons/faster16x16black.png"));
    }
    
    public void run() {
        MapPage.openEditor(_window, "");
    }
}
