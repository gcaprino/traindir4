package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.traindirector.ICommandIds;

public class PreferencesAction  extends Action {

	IWorkbenchWindow window;
	
	public PreferencesAction(IWorkbenchWindow window, String label) {
		this.window = window;
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_PREFERENCES);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_PREFERENCES);
//		setImageDescriptor(com.bstreet.cg.traindirector.Activator.getImageDescriptor("/icons/sample2.gif"));
	}

	public void run() {
		PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(
				window.getShell(), "Preferences", null, null);
		if (pref != null)
			pref.open();
	}
}
