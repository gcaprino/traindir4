package com.traindirector.editors;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.traindirector.Application;
import com.traindirector.simulator.Simulator;
import com.traindirector.uicomponents.WebContent;

public class MapPage extends WebPage {

	MapContent _content;
	
	public MapPage() {
		super();
	}

    public WebContent getContentProvider() {
        _content = new MapContent();
        return _content;
    }

    public static void openEditor(IWorkbenchWindow window, String fname) {
    	if (window == null) {
    		window = Application._workbenchAdvisor._windowAdvisor._actionBarAdvisor._window;
    	}
        LayoutEditorInput input = new LayoutEditorInput();
        input.setFileName(fname);
        try {
            IEditorPart part = window.getActivePage().openEditor(input, "com.traindirector.editor.map");
            MapPage report = (MapPage)part;
            final Browser browser = report._browser;
            browser.addControlListener(new ControlListener() {
            	 
                public void controlResized(ControlEvent e) {
                    browser.execute("document.getElementById('map_canvas').style.width= "+ (browser.getSize().x - 20) + ";");
                    browser.execute("document.getElementById('map_canvas').style.height= "+ (browser.getSize().y - 20) + ";");
                }
       
                public void controlMoved(ControlEvent e) {
                }
            });
            Simulator sim = Application.getSimulator();
            // TODO: look for "locale" + baseFileName first
            report._content.setName(sim._baseFileName + ".htm");
            report.showContent(fname);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

}
