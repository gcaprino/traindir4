package com.traindirector;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.traindirector.views.AssetsView;
import com.traindirector.views.EditToolsView;

public class EditLayoutPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);

		IFolderLayout folder = layout.createFolder("Tools", IPageLayout.BOTTOM, 0.8f, editorArea);
		folder.addView(EditToolsView.ID);
		IFolderLayout assets = layout.createFolder("Assets", IPageLayout.RIGHT, 0.8f, editorArea);
		assets.addView(AssetsView.ID);

	}

}
