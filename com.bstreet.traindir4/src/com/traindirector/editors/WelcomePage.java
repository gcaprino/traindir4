package com.traindirector.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

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
			IEditorPart part = window.getActivePage().openEditor(input, "com.traindirector.editor.welcome");
			WebPage wpage = (WebPage) part;
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
