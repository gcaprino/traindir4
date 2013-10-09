package com.traindirector.editors;

import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.bstreet.cg.events.CGEvent;
import com.bstreet.cg.events.CGEventDispatcher;
import com.bstreet.cg.events.CGEventListener;
import com.traindirector.Application;
import com.traindirector.commands.ClickCommand;
import com.traindirector.events.LoadEndEvent;
import com.traindirector.events.LoadStartEvent;
import com.traindirector.events.TimeSliceEvent;
import com.traindirector.model.Direction;
import com.traindirector.model.ImageTrack;
import com.traindirector.model.ItineraryButton;
import com.traindirector.model.PlatformTrack;
import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;
import com.traindirector.model.Switch;
import com.traindirector.model.TDIcon;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.TextTrack;
import com.traindirector.model.Track;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TriggerTrack;
import com.traindirector.model.VLine;
import com.traindirector.simulator.Simulator;
import com.traindirector.uicomponents.LayoutCanvas;
import com.traindirector.uicomponents.XPM;

public class LayoutPart extends EditorPart {

	private LayoutCanvas _layout;

	public LayoutPart() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		this.setSite(site);
		this.setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		_layout = new LayoutCanvas(parent, false);
	}

	@Override
	public void setFocus() {
		
	}

	public static void openEditor(IWorkbenchWindow window, String fname) {
		LayoutEditorInput input = new LayoutEditorInput();
		input.setFileName(fname);
		try {
			IEditorPart editor = window.getActivePage().openEditor(input,
					"com.bstreet.cg.traindirector.layout");
			editor.setFocus();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
