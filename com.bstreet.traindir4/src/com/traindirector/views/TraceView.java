package com.traindirector.views;

import javax.xml.ws.Dispatch;

import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.traindirector.Application;

public class TraceView extends ViewPart {

	public static final String ID = "com.traindirector.views.trace";
	Text _text;
	Text _expr;
	
	public TraceView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout fill = new GridLayout();
		fill.numColumns = 1;
		parent.setLayout(fill);
		_expr = new Text(parent, SWT.BORDER);
		_expr.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		GridData ld = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		ld.minimumHeight = 30;
		_expr.setLayoutData(ld);
		_text = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		ld = new GridData(SWT.FILL, SWT.FILL, true, true);
		_text.setLayoutData(ld);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		Application.getSimulator().setTraceView(null);
		super.dispose();
	}

	public String getExpr() {
		final String[] result = new String[1];
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				if (_expr == null || _expr.isDisposed())
					result[0] = null;
				else
					result[0] = _expr.getText();
			}
		});
		return result[0];
	}

	public void addTrace(final String msg) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (_text == null || _text.isDisposed())
					return;
				_text.append(msg + "\n");
			}
		});
	}
}
