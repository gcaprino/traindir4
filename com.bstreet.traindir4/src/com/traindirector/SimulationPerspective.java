package com.traindirector;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class SimulationPerspective implements IPerspectiveFactory {

	/**
	 * The ID of the perspective as specified in the extension.
	 */
	public static final String ID = "com.bstreet.cg.traindirector.perspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);

		IFolderLayout folder = layout.createFolder("Status", IPageLayout.BOTTOM, 0.8f, editorArea);
		folder.addView("com.bstreet.cg.traindirector.views.ScheduleView");
		IFolderLayout folder1 = layout.createFolder("Infos", IPageLayout.RIGHT, 0.5f, "Status");
		folder1.addView("com.bstreet.cg.traindirector.views.AlertsView");
		IFolderLayout folder2 = layout.createFolder("stops", IPageLayout.RIGHT, 0.8f, editorArea);
		folder2.addView("com.traindirector.trainstops");
		
	}
}
