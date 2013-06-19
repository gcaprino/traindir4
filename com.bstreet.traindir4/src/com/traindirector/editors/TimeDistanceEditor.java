package com.traindirector.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.traindirector.uicomponents.TimeDistanceDiagram;

public class TimeDistanceEditor extends GraphicPage {

    protected TimeDistanceDiagram _graph;
    
    public TimeDistanceEditor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createPartControl(Composite parent) {
        _graph = new TimeDistanceDiagram(parent);
    }

    public static void openEditor(IWorkbenchWindow window) {
        LayoutEditorInput input = new LayoutEditorInput();
        input.setFileName("Time-Distance");
        try {
            IEditorPart editor = window.getActivePage().openEditor(input,
                    "com.traindirector.editor.timedistance");
            editor.setFocus();
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }
}
