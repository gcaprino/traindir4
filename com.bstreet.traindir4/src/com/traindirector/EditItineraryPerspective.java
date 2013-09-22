package com.traindirector;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.traindirector.views.ItinerariesView;

public class EditItineraryPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);

		IFolderLayout folder = layout.createFolder("Itineraries", IPageLayout.BOTTOM, 0.8f, editorArea);
		folder.addView(ItinerariesView.ID);
		//IFolderLayout folder1 = layout.createFolder("Infos", IPageLayout.RIGHT, 0.5f, "Status");
		//folder1.addView("com.bstreet.cg.traindirector.views.AlertsView");

	}
}

