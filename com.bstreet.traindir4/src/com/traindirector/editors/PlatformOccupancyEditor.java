package com.traindirector.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.traindirector.uicomponents.PlatformOccupancyGraph;

public class PlatformOccupancyEditor extends GraphicPage {

    protected PlatformOccupancyGraph graph;
    
    public PlatformOccupancyEditor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void createPartControl(Composite parent) {
        graph = new PlatformOccupancyGraph(parent);
    }

    public static void openEditor(IWorkbenchWindow window) {
        LayoutEditorInput input = new LayoutEditorInput();
        input.setFileName("Platform Occupancy");
        try {
            IEditorPart editor = window.getActivePage().openEditor(input,
                    "com.traindirector.editor.platforms");
            editor.setFocus();
        } catch (PartInitException e) {
            e.printStackTrace();
        }
    }

}
