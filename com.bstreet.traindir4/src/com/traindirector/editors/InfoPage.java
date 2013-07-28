package com.traindirector.editors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.traindirector.Application;
import com.traindirector.simulator.Simulator;
import com.traindirector.uicomponents.WebContent;

public class InfoPage extends WebPage {

	InfoContent _content;
	
	public InfoPage() {
		super();
	}

    public WebContent getContentProvider() {
        _content = new InfoContent();
        return _content;
    }

    public static void openEditor(IWorkbenchWindow window, String fname) {
    	if (window == null) {
    		window = Application._workbenchAdvisor._windowAdvisor._actionBarAdvisor._window;
    	}
        LayoutEditorInput input = new LayoutEditorInput();
        input.setFileName(fname);
        try {
            IEditorPart part = window.getActivePage().openEditor(input, "com.traindirector.editor.info");
            InfoPage report = (InfoPage)part;
            Simulator sim = Application.getSimulator();
            // TODO: look for "locale" + baseFileName first
            report._content.setName(sim._baseFileName + ".htm");
            report.showContent(fname);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }
}
