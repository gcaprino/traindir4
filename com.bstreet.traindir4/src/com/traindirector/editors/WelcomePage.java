package com.traindirector.editors;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import com.traindirector.uicomponents.HomeContent;
import com.traindirector.uicomponents.WebContent;

public class WelcomePage extends WebPage {
	
	public WelcomePage() {
		
	}
	
	public WebContent getContentProvider() {
		return new HomeContent();
	}
	
	public static void openEditor(IWorkbenchWindow window, String fname) {
		LayoutEditorInput input = new LayoutEditorInput();
		input.setFileName(fname);
		try {
			window.getActivePage().openEditor(input, "com.traindirector.editor.welcome");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
