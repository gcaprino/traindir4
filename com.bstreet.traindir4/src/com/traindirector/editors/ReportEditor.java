package com.traindirector.editors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.traindirector.uicomponents.WebContent;

public class ReportEditor extends WebPage {

    public ReportEditor() {
        // TODO Auto-generated constructor stub
    }

    public WebContent getContentProvider() {
        return new ReportContent();
    }

    public static void openEditor(IWorkbenchWindow window, String fname) {
        LayoutEditorInput input = new LayoutEditorInput();
        input.setFileName(fname);
        try {
            IEditorPart part = window.getActivePage().openEditor(input, "com.traindirector.editor.report");
            ReportEditor report = (ReportEditor)part;
            //report.setType(fname);
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
