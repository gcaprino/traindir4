package com.traindirector.uicomponents;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class MacroFileDialog {

	private static FileDialog openDialog;

	public static String get() {
		
		// TODO: implement a more sophisticated dialog to show the layout of each macro file

		Shell shell = Display.getDefault().getActiveShell();
		if (openDialog == null) {
			openDialog = new FileDialog(shell);
			openDialog.setFilterExtensions(new String[] { "*.trk", "*.*" });
			openDialog.setFilterNames(new String[] { "Layout File (*.trk)", "All files (*.*)" });
		}
		return openDialog.open();
	}
}
