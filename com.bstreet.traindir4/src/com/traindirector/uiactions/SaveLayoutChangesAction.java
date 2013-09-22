package com.traindirector.uiactions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.commands.SaveGameCommand;
import com.traindirector.commands.SaveLayoutCommand;

public class SaveLayoutChangesAction extends Action {

	private final IWorkbenchWindow window;
	private int instanceNum = 0;
	private FileDialog saveDialog;
	
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
		Shell shell = Display.getDefault().getActiveShell();
		if (saveDialog == null) {
			saveDialog = new FileDialog(shell, SWT.SAVE);
			saveDialog.setFilterExtensions(new String[] { "*.trk", "*.*" });
			saveDialog.setFilterNames(new String[] { "Simulation Layout (*.trk)", "All files (*.*)" });
		}
		String fname = saveDialog.open();
		if (fname == null) {
			return;
		}
		SaveLayoutCommand cmd = new SaveLayoutCommand(fname);
		Application.getSimulator().addCommand(cmd);
	}
}
