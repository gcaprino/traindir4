package com.traindirector.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.traindirector.uicomponents.LayoutCanvas;

public class EditToolsView extends ViewPart {

    public static String ID = "com.traindirector.views.editTools";

    private LayoutCanvas _layout;

    public EditToolsView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        _layout = new LayoutCanvas(parent);
        _layout.setEditorToolbox(true);
    }

    @Override
    public void setFocus() {
    }

}
