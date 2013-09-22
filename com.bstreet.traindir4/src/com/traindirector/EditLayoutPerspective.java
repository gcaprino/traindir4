package com.traindirector;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.traindirector.views.EditToolsView;

public class EditLayoutPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);

		IFolderLayout folder = layout.createFolder("Tools", IPageLayout.BOTTOM, 0.8f, editorArea);
		folder.addView(EditToolsView.ID);
		//IFolderLayout folder1 = layout.createFolder("Infos", IPageLayout.RIGHT, 0.5f, "Status");
		//folder1.addView("com.bstreet.cg.traindirector.views.AlertsView");

	}

}
