package com.traindirector.uicomponents;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

public class UIUtils {

	public static void restoreColumnSizes(final Table table, final String viewName) {
		IPreferenceStore store = PlatformUI.getPreferenceStore();
		String propertyName = "";
		for (int i = 0; i < table.getColumnCount(); ++i) {
			TableColumn col = table.getColumn(i);
			propertyName = viewName + "-" + col.getText();
			int val = store.getInt(propertyName);
			if (val > 0)
				col.setWidth(val);
			col.addControlListener(new ControlListener() {
				
				@Override
				public void controlResized(ControlEvent e) {
					saveColumnSizes(table, viewName);
				}
				
				@Override
				public void controlMoved(ControlEvent e) {
					
				}
			});
		}
	}

	public static void saveColumnSizes(Table table, String viewName) {
		IPreferenceStore store = PlatformUI.getPreferenceStore();
		String propertyName = "";
		for (int i = 0; i < table.getColumnCount(); ++i) {
			TableColumn col = table.getColumn(i);
			propertyName = viewName + "-" + col.getText();
			int value = col.getWidth();
			store.setValue(propertyName, value);
		}
	}


}
