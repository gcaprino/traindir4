package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.SaveGameCommand;

public class SaveSimulationAction extends Action {
	
	public SaveSimulationAction(IWorkbenchWindow window, String label) {
        setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SAVE);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_SAVE);
//		setImageDescriptor(com.bstreet.cg.traindirector.Activator.getImageDescriptor("/icons/sample2.gif"));
	}
	
	static FileDialog saveDialog;
	
	public void run() {

		Shell shell = Display.getDefault().getActiveShell();
		if (saveDialog == null) {
			saveDialog = new FileDialog(shell);
			saveDialog.setFilterExtensions(new String[] { "*.sav", "*.*" });
			saveDialog.setFilterNames(new String[] { "Simulation State (*.sav)", "All files (*.*)" });
		}
		String fname = saveDialog.open();
		if (fname == null) {
			return;
		}
		SaveGameCommand cmd = new SaveGameCommand(fname);
		Application.getSimulator().addCommand(cmd);
	}

}
