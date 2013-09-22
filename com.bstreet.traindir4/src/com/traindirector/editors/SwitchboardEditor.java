package com.traindirector.editors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import com.traindirector.uicomponents.SwitchboardContent;
import com.traindirector.uicomponents.WebContent;

public class SwitchboardEditor extends WebPage {
	
	public SwitchboardEditor() {
	}
	
	public WebContent getContentProvider() {
		return new SwitchboardContent();
	}
	
	public static void openEditor(IWorkbenchWindow window, String fname) {
		LayoutEditorInput input = new LayoutEditorInput();
		input.setFileName(fname);
		try {
			IEditorPart part = window.getActivePage().openEditor(input, "com.traindirector.editor.switchboard");
			//WebPage wpage = (WebPage) part;
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
