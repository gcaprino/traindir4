package com.traindirector.uicomponents;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.traindirector.model.Itinerary;

public class ItinerariesTable {

	public Table _table;
	
	public ItinerariesTable() {
		
	}

	public void create(Composite parent) {
		
		// The text fields will grow with the size of the parent
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;

		_table = new Table(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		_table.setLayoutData(gridData);
		TableColumn col = new TableColumn(_table, SWT.NONE);
		col.setText("Name");
		col.setWidth(140);
		
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Start Signal");
		col.setWidth(140);
		
		col = new TableColumn(_table, SWT.NONE);
		col.setText("End Signal");
		col.setWidth(140);
		
		col = new TableColumn(_table, SWT.NONE);
		col.setText("Next Itinerary");
		col.setWidth(140);
		
		_table.setHeaderVisible(true);
	}

	public int getSelectionCount() {
		return _table.getSelectionCount();
	}

	public TableItem[] getSelection() {
		return _table.getSelection();
	}

	public void fill(List<Itinerary> itineraries) {
		_table.removeAll();
		for (Itinerary i : itineraries) {
			TableItem item = new TableItem(_table, SWT.NONE);
			item.setText(i._name);
			item.setText(1, i._signame);
			item.setText(2, i._endsig);
			item.setText(3, i._nextitin == null ? "" : i._nextitin);
			item.setData("itin", i);
		}
		if(_table.getItemCount() != 0)
			_table.setSelection(0);
	}

}
