package com.traindirector;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.traindirector.editors.WebPage;

public class WelcomePerspective implements IPerspectiveFactory {

	public static final String ID = "com.traindirector.welcomePerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
	}

}
