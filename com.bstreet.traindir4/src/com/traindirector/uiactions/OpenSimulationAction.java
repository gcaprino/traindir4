package com.traindirector.uiactions;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.Workbench;

import com.traindirector.Activator;
import com.traindirector.Application;
import com.traindirector.ICommandIds;
import com.traindirector.SimulationPerspective;
import com.traindirector.commands.LoadCommand;
import com.traindirector.editors.LayoutPart;
import com.traindirector.editors.WebPage;
import com.traindirector.simulator.Simulator;


public class OpenSimulationAction extends Action {
	
	public OpenSimulationAction(String label) {
		setText(label);
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_OPEN);
		// Associate the action with a pre-defined command, to allow key
		// bindings.
		setActionDefinitionId(ICommandIds.CMD_OPEN);
		setImageDescriptor(Activator.getImageDescriptor("/icons/open16x16.png"));
	}
	
	FileDialog openDialog;
	
	public void run() {

		Shell shell = Display.getDefault().getActiveShell();
		if (openDialog == null) {
			openDialog = new FileDialog(shell);
			openDialog.setFilterExtensions(new String[] { "*.zip", "*.trk", "*.*" });
			openDialog.setFilterNames(new String[] {
					"Zipped Simulation (*.zip)", "Simulation (*.trk)", "All files (*.*)" });
		}
		String fname = openDialog.open();
		if (fname == null) {
			return;
		}
		openSimulation(fname);
	}
	
	public static void openSimulation(String fname) {
		// switch perspective
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		String simPerspective = SimulationPerspective.ID;
		try {
			workbench.showPerspective(simPerspective, window);
		} catch (WorkbenchException e) {
			System.out.println("Can not switch to perspective: " + simPerspective + "\n");
			e.printStackTrace();
		}
		LayoutPart.openEditor(window, fname);
		LoadCommand cmd = new LoadCommand(fname);
		Application.getSimulator().addCommand(cmd);
	}
}
