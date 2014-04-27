package com.traindirector.uicomponents;

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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import com.bstreet.cg.events.CGEvent;
import com.bstreet.cg.events.CGEventDispatcher;
import com.bstreet.cg.events.CGEventListener;
import com.traindirector.Application;
import com.traindirector.commands.ClickCommand;
import com.traindirector.commands.RunStopCommand;
import com.traindirector.events.ChangeZoomingEvent;
import com.traindirector.events.LoadEndEvent;
import com.traindirector.events.LoadStartEvent;
import com.traindirector.events.TimeSliceEvent;
import com.traindirector.events.ToggleCoordBarsEvent;
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
import com.traindirector.model.TrackDirection;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TriggerTrack;
import com.traindirector.model.VLine;
import com.traindirector.simulator.SignalFactory;
import com.traindirector.simulator.Simulator;

public class LayoutCanvas {

	ScrolledComposite _scroller;
	Canvas _canvas;
	Display _display;

	Simulator _simulator;

	Label _statusLine;
	
	volatile boolean _loading;

	long lastUpdate;
	Color greenColor;
	Color blackColor;
	Color whiteColor;
	Color orangeColor;
	Color blueColor;
	Color bgColor;

	private int _mouseDownX, _mouseDownY;
	private boolean _mouseDown;
	
	private int _xMultiplier = 1;
	private int _yMultiplier = 1;
	private int _currentTrackType;
	private int _currentTrackDirection;
	private int _currentMaxTrackDirection;

	private boolean _isEditorToolbox;
	protected boolean _coordBars;

	private static final int HCOORDBAR = 20;
	private static final int VCOORDBAR = 30;
	private int _xOffset = 0; // 0 or HCOORDBAR
	private int _yOffset = 0; // 0 or VCOORDBAR
	
	private LayoutEditor _editor;

	public LayoutCanvas(Composite parent, boolean isEditor) {
		
		_display = parent.getDisplay();
		_statusLine = Application._workbenchAdvisor._windowAdvisor._actionBarAdvisor._statusLine;
		_scroller = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		_canvas = new Canvas(_scroller, SWT.NO_BACKGROUND);
		_scroller.setContent(_canvas);
		if (!isEditor)
			_canvas.setSize(4000, 4000);
		else {
			_canvas.setSize(800, 400);
			setEditorToolbox(true);
		}

		registerUIEventHandlers();


		registerInternalEventHandlers();
	}



	public void updateCanvas(GC gc) {
		try {
			// draw into a buffer, then transfer it over to the canvas
			Image buffer = new Image(_display, _canvas.getBounds());
			GC gc2 = new GC(buffer);
			redrawCanvas(gc2);

			// transfer the image buffer onto this canvas (just
			// drawImage(buffer, w, h) didn't work for me, so I use this way, I
			// haven't investigated further)
			Rectangle b = _canvas.getBounds();
			gc.drawImage(buffer, 0, 0, b.width, b.height, 0, 0, b.width,
					b.height);

			// dispose used objects (important, or you'll run out of handles)
			buffer.dispose();
			gc2.dispose();
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		}
	}

	private void redrawCanvas(GC gc) {
		if (_simulator == null) {
			return;
		}
		if (greenColor == null) {
			greenColor = _simulator._colorFactory.get(0, 255, 0);
			blackColor = _simulator._colorFactory.get(0, 0, 0);
			whiteColor = _simulator._colorFactory.get(255, 255, 255);
			orangeColor = _simulator._colorFactory.get(255, 0, 0);
			blueColor = _simulator._colorFactory.get(0, 0, 255);
			bgColor = _simulator._colorFactory.getBackgroundColor();
		}

		if(_isEditorToolbox) {
		    drawToolbox(gc);
		    return;
		}
		long mostRecentUpdate = lastUpdate;

		synchronized (_simulator._territory) {
			gc.setBackground(bgColor);
			gc.fillRectangle(0, 0, _canvas.getSize().x, _canvas.getSize().y);

			mostRecentUpdate = drawLayout(gc);

			if(!Simulator.getEditing())
				drawTrains(gc);
			else
				drawLinks(gc);
		}
		lastUpdate = mostRecentUpdate;
	}

	// Draw all static elements of the layout (tracks, signals, platforms, etc.)

	protected long drawLayout(GC gc) {
		long mostRecentUpdate = lastUpdate;
		Territory territory = _simulator._territory;
		for (Track trk : territory.getTracks()) {
			try {
				gc.setForeground(blackColor);

				// if(trk._updateTime < lastUpdate)
				// continue;
				if (trk._updateTime > mostRecentUpdate)
					mostRecentUpdate = trk._updateTime;

				if (trk instanceof ImageTrack) {
					drawImage(gc, trk._position, trk._station);

				} else if (trk instanceof ItineraryButton) {
					drawImage(gc, trk._position, ":icons:itinButton.xpm");
					drawText(gc, trk._position, trk._station);
				} else if (trk instanceof PlatformTrack) {
					VLine[] segs = trk.getSegments();
					if(segs != null)
						drawSegments(gc, trk._position, segs, TrackStatus.FREE);
				} else if (trk instanceof Signal) {
					Signal sig = (Signal) trk;
					SignalAspect aspect = sig.getAspect();
					if (aspect == null) {
						continue;
					}
					String iconFileName = aspect.getXpmStrings(sig._direction);
					drawImage(gc, sig._position, iconFileName);

				} else if (trk instanceof Switch) {
					Switch sw = (Switch) trk;
					VLine[] segs = sw.getSegments();
					if (segs != null)
						drawSegments(gc, sw._position, segs, sw._status);
					segs = sw.getBlockSegments();
					if (segs != null)
						drawSegments(gc, sw._position, segs, TrackStatus.FREE);

				} else if (trk instanceof TextTrack) {
					
					// TODO: small and large text
					drawText(gc, trk._position, trk._station);
					
				} else if (trk instanceof TriggerTrack) {
					VLine[] segs = trk.getSegments();
					if (segs != null)
						drawSegments(gc, trk._position, segs, trk._status);

				} else if (trk instanceof Track) {
					VLine[] segs = trk.getSegments();
					if (segs != null)
						drawSegments(gc, trk._position, segs, trk._status);
				}
				
				drawCoordBars(gc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mostRecentUpdate;
	}


	
	protected void drawCoordBars(GC gc) {
		if (!_coordBars)
			return;
		Rectangle rect = _canvas.getBounds();
		int offset = VCOORDBAR;
		for (int y = 0; offset < rect.height; ++y, offset += _yMultiplier * Simulator.VGRID) {
			String txt = "" + (y + 1);
			gc.drawText(txt, 0, offset);
		}
	}
	
	protected void drawTrains(GC gc) {
		TDIcon icon;
		
		for (Train train : _simulator._schedule._trains) {
			switch (train._status) {
			case ARRIVED:
			case RUNNING:
			case STOPPED:
			case WAITING:
			case STARTING:
				if (train._position != null) {
					if(train._direction == Direction.W)
						drawImage(gc, train._position._position, train._westIcon);
					else
						drawImage(gc, train._position._position, train._eastIcon);
				}
				if(train._tail != null && train._tail._position != null) {
					icon = train._westCarIcon;
					if(train._tail._direction == Direction.W)
						drawImage(gc, train._tail._position._position, icon == null ? train._eastCarIcon : icon);
					else
						drawImage(gc, train._tail._position._position, train._eastCarIcon == null ? icon : train._eastCarIcon);
				}

			default:
				break;
			}
		}
		for (Train train : _simulator._schedule._stranded) {
			if (train._position != null) {
				icon = train._westCarIcon;
				if(train._direction == Direction.W)
					drawImage(gc, train._position._position, icon == null ? train._eastCarIcon : icon);
				else
					drawImage(gc, train._position._position, train._eastCarIcon == null ? icon : train._eastCarIcon);
			}
			if(train._tail != null && train._tail._position != null) {
				icon = train._westCarIcon;
				if(train._tail._direction == Direction.W)
					drawImage(gc, train._tail._position._position, icon == null ? train._eastCarIcon : icon);
				else
					drawImage(gc, train._tail._position._position, train._eastCarIcon == null ? icon : train._eastCarIcon);
			}
		}
	}
	
	
	protected void drawLinks(GC gc) {
		Territory territory = _simulator._territory;
		gc.setForeground(blueColor);
		TDPosition pos;
		for (Track trk : territory.getTracks()) {
			try {
				pos = null;
				if(trk._elink == null || (trk._elink._x | trk._elink._y) == 0)
					pos = trk._wlink;
				else if(trk._elink != null)
					pos = trk._elink;
				if(pos == null || (pos._x | pos._y) == 0)
					continue;

				int xMult = Simulator.HGRID * _xMultiplier;
				int yMult = Simulator.VGRID * _yMultiplier;
				int x1 = pos._x * xMult;
				int y1 = pos._y * yMult;
				int x0 = trk._position._x * xMult;
				int y0 = trk._position._y * yMult;
				gc.drawLine(x0 + xMult / 2, y0 + yMult / 2 + yMult / 2, x1, y1 + yMult / 2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void drawText(GC gc, TDPosition position, String text) {
		gc.drawText(text,
				_xOffset + (position._x + 1) * Simulator.HGRID * _xMultiplier,
				_yOffset + position._y * Simulator.VGRID * _yMultiplier);
	}

    private void drawImage(GC gc, TDPosition position, String iconFileName) {
		TDIcon icon = _simulator._iconFactory.get(iconFileName);
		if (icon == null) {
			icon = _simulator._iconFactory.get(":icons:camera.xpm");
		}
		drawImage(gc, position, icon);
	}

	private void drawImage(GC gc, TDPosition position, TDIcon icon) {
		if (icon != null) {
			if (icon._bytes == null || !(icon._bytes instanceof Image)) {
				String[] xpm = icon._xpmBytes;
				if (xpm != null) {
					icon._bytes = drawXpm(position, xpm);
				}
			}
			if (icon._bytes != null)
				drawImage(gc, position, (Image) icon._bytes);
		}
	}

	public void eraseGridCell(GC gc, TDPosition pos) {
		int x = _xOffset + pos._x * Simulator.HGRID * _xMultiplier;
		int y = _yOffset + pos._y * Simulator.VGRID * _yMultiplier;
		gc.setForeground(bgColor);
		gc.fillRectangle(x, y, Simulator.HGRID, Simulator.VGRID);
	}

	public void drawSegments(GC gc, TDPosition pos, VLine[] segs,
			TrackStatus status) {
		int x = _xOffset + pos._x * Simulator.HGRID * _xMultiplier;
		int y = _yOffset + pos._y * Simulator.VGRID * _yMultiplier;
		// TODO: use options for track colors
		if (status == TrackStatus.BUSY)
			gc.setForeground(greenColor);
		else if (status == TrackStatus.OCCUPIED)
			gc.setForeground(orangeColor);
		else if (status == TrackStatus.BUSYSHUNTING)
			gc.setForeground(whiteColor);
		else
			gc.setForeground(blackColor);
		for (VLine vl : segs) {
			gc.drawLine(x + vl._x0 * _xMultiplier, y + vl._y0 * _yMultiplier, x + vl._x1 * _xMultiplier, y + vl._y1 * _yMultiplier);
		}
	}

	public void drawImage(GC gc, TDPosition pos, Image image) {
		int x = _xOffset + pos._x * Simulator.HGRID * _xMultiplier;
		int y = _yOffset + pos._y * Simulator.VGRID * _yMultiplier;
		gc.drawImage(image, x, y);
	}

	public Image drawXpm(TDPosition pos, String[] xpm) {
		try {
			XPM xpmImage = new XPM(xpm);
			Image img = new Image(_display, xpmImage.data);
			return img;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// convert canvas to layout coords
	public TDPosition toLayoutCoord(int x, int y) {
		TDPosition pos = new TDPosition(
				(x - _xOffset) / Simulator.HGRID / _xMultiplier,
				(y - _yOffset) / Simulator.VGRID / _yMultiplier);
		return pos;
	}

	public void onMouseDown(MouseEvent e) {
		if (_loading)
			return;
		_mouseDownX = e.x;
		_mouseDownY = e.y;
		_mouseDown = true;
	}

	public void onMouseMove(MouseEvent e) {
		if (_simulator == null || _simulator._territory == null || _isEditorToolbox)
			return;
		// TODO: if editing we could draw a line
		// if not editing, we could select an area
		synchronized(_simulator) {
			TDPosition pos = toLayoutCoord(e.x, e.y);
			StringBuilder statusLine = new StringBuilder();
			Track track = _simulator._territory.findTrack(pos);
			Train train = null;
			if (_simulator._schedule != null)
				train = _simulator._schedule.findTrainAt(pos);
			if (train != null) {
				statusLine.append(train.toString());
			} else if (track != null) {
				statusLine.append(track.toString());
			} else
				statusLine.append(pos.toString());
			_statusLine.setText(statusLine.toString());
		}
	}

	public void onMouseUp(MouseEvent e) {
		if (_loading)
			return;
		if (_simulator == null)
			return;
		TDPosition pos = toLayoutCoord(e.x, e.y);
		if(!_isEditorToolbox) {
			if (Simulator.getEditing()) {
				if (_editor == null) {
					// this is here because _simulator may be null in our constructor
					_editor = new LayoutEditor(_simulator, this);
				}
				_editor._pos = pos;
				_editor.handle(e.button != 1, (e.stateMask & SWT.CONTROL) != 0);
				return;
			}
        	ClickCommand cmd = new ClickCommand(pos);
        	cmd.setLeftClick(e.button == 1);
        	cmd.setShiftKey((e.stateMask & SWT.SHIFT) != 0);
        	cmd.setCtrlKey((e.stateMask & SWT.CONTROL) != 0);
        	cmd.setAltKey((e.stateMask & SWT.ALT) != 0);
        	_simulator.addCommand(cmd);
        	return;
		}
		if(pos._y == 0) {
		    if(pos._x >= 0 && pos._x < 6) {
		        _currentTrackType = pos._x;
		        // redraw second row, since we changed the tools' type
                _canvas.update();
		    }
		    return;
		}
		if(pos._x >= 0 && pos._x < _currentMaxTrackDirection) {
		    _currentTrackDirection = pos._x;
		    _simulator.setEditorTool(_currentTrackType, _currentTrackDirection);
		}
	}

	public void onMouseDoubleClick(MouseEvent e) {
		if (_loading)
			return;

	}

	public void onKeyDown(KeyEvent e) {
		if (_loading)
			return;

	}

	public void onKeyUp(KeyEvent e) {
		if (_loading)
			return;
		if (e.character == ' ' || e.character == 0x13) {
			RunStopCommand cmd = new RunStopCommand();
			_simulator.addCommand(cmd);
		}
	}

    public void setEditorToolbox(boolean b) {
        _isEditorToolbox = b;
        _xMultiplier = 4;
        _yMultiplier = 4;
        _xOffset = 0;
        _yOffset = 0;
    }

    private void drawToolbox(GC gc) {
    	Track trk;
        // First row
        //String[] firstRow = { "Del", "Tracks", "Switches", "Signals", "Items", "Actions" };
    	String[] firstRow = { ":icons:edit:tracks.xpm", ":icons:edit:switches.xpm", ":icons:edit:signals.xpm",
    			":icons:edit:tools.xpm", ":icons:edit:actions.xpm" };
        TDPosition pos = new TDPosition(0, 0);

        
        gc.setBackground(bgColor);
        gc.fillRectangle(0, 0, _canvas.getSize().x, _canvas.getSize().y);
        gc.setBackground(greenColor);
        gc.fillRectangle(_currentTrackDirection * Simulator.HGRID * _xMultiplier, Simulator.VGRID * _yMultiplier,
                Simulator.HGRID * _xMultiplier - 3, Simulator.VGRID * _yMultiplier - 3);
        gc.setBackground(whiteColor);
        for(int x = 0; x < firstRow.length; ++x) {
//            gc.drawText(firstRow[x], x * Simulator.HGRID * _xMultiplier, 0);
        	pos._x = x + 1;
        	drawImage(gc, pos, firstRow[x]);
        }
        pos._x = 0;
        drawText(gc, pos, "Del");
        ++pos._y;
        VLine[] segs;
		switch(_currentTrackType) {
        case 0: // Delete
            break;
        case 1: // Tracks
            _currentMaxTrackDirection = trackDirections.length;
            for(int x = 0; x < _currentMaxTrackDirection; ++x) {
                pos._x = x;
                segs = Track.getSegments(trackDirections[x]);
                if (segs != null)
                    drawSegments(gc, pos, segs, TrackStatus.FREE);
            }
            break;
        case 2: // Switches
            Switch sw = new Switch();
            _currentMaxTrackDirection = 24;
            for(int x = 0; x < _currentMaxTrackDirection; ++x) {
                pos._x = x;
                segs = sw.getSegments(x);
                if (segs != null)
                    drawSegments(gc, pos, segs, TrackStatus.FREE);
                segs = sw.getBlockSegments();
                if (segs != null)
                    drawSegments(gc, pos, segs, TrackStatus.FREE);
            }
            break;
        case 3: // Signals
            _currentMaxTrackDirection = 8;
            SignalFactory factory = Simulator.INSTANCE._signalFactory;
            Signal signal = factory.newInstance(1);
            for(int x = 0; x < 4; ++x) {
                pos._x = x;
                SignalAspect aspect = signal.getAspect();
                String iconFileName = aspect.getXpmStrings(signalsDirections[x]);
                drawImage(gc, pos, iconFileName);
            }
            signal = factory.newInstance(2);
            for(int x = 0; x < 4; ++x) {
                pos._x = 4 + x;
                SignalAspect aspect = signal.getAspect();
                String iconFileName = aspect.getXpmStrings(signalsDirections[x]);
                drawImage(gc, pos, iconFileName);
            }
            break;
        case 4: // Items
            _currentMaxTrackDirection = 6;
            drawText(gc, pos, "Abc");
        	++pos._x;
        	drawText(gc, pos, "Abc"); // TODO: small text
        	++pos._x;
			drawImage(gc, pos, ":icons:itinButton.xpm");
			drawText(gc, pos, "A");
			++pos._x;
			drawImage(gc, pos, ":icons:itinButton.xpm");
			drawText(gc, pos, "A");
			++pos._x;
			drawImage(gc, pos, _simulator._iconFactory.get(":icons:camera.xpm"));
			++pos._x;
			trk = new PlatformTrack();
			trk._direction = TrackDirection.E_W;
			segs = trk.getSegments();
			drawSegments(gc, pos, segs, TrackStatus.FREE);
			break;

        case 5: // Actions
            _currentMaxTrackDirection = 11;
            drawText(gc, pos, "Link...");
        	++pos._x;
        	drawText(gc, pos, "...to...");
        	++pos._x;
        	drawText(gc, pos, "Macro");
        	++pos._x;
        	drawText(gc, pos, "Place");
        	++pos._x;
        	trk = new TriggerTrack();
        	trk._direction = TrackDirection.E_W;
			drawSegments(gc, pos, trk.getSegments(), trk._status);
			++pos._x;
        	trk._direction = TrackDirection.W_E;
			drawSegments(gc, pos, trk.getSegments(), trk._status);
			++pos._x;
        	trk._direction = TrackDirection.N_S;
			drawSegments(gc, pos, trk.getSegments(), trk._status);
			++pos._x;
        	trk._direction = TrackDirection.S_N;
			drawSegments(gc, pos, trk.getSegments(), trk._status);
			++pos._x;
			drawImage(gc, pos, _simulator._iconFactory.get(":icons:edit:moveStart.xpm"));
			++pos._x;
			drawImage(gc, pos, _simulator._iconFactory.get(":icons:edit:moveEnd.xpm"));
			++pos._x;
			drawImage(gc, pos, _simulator._iconFactory.get(":icons:edit:moveDest.xpm"));
            break;
        }
        
    }

    

	protected void changeZooming(int factor) {
		if (_isEditorToolbox)	// TODO: how about mini-canvases for dialogs?
			return;
		if (_xMultiplier == 1 && factor > 0) {
			_xMultiplier = 2;
			_yMultiplier = 2;
			
		} else if (_xMultiplier == 2 && factor < 0) {
			_xMultiplier = 1;
			_yMultiplier = 1;
		}
	}


	protected void registerUIEventHandlers() {
		_canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				updateCanvas(e.gc);
			}
		});
		_canvas.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				onMouseMove(e);
			}
		});
		_canvas.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				onMouseUp(e);
			}

			@Override
			public void mouseDown(MouseEvent e) {
				onMouseDown(e);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				onMouseDoubleClick(e);
			}
		});
		_canvas.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				onKeyUp(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				onKeyDown(e);
			}
		});
	}


	protected void registerInternalEventHandlers() {
		CGEventDispatcher.getInstance().addListener(
				new CGEventListener(LoadStartEvent.class) {
					@Override
					public void handle(CGEvent event, Object target) {
						_loading = true;
					}
				});
		CGEventDispatcher.getInstance().addListener(
				new CGEventListener(LoadEndEvent.class) {
					@Override
					public void handle(CGEvent event, Object target) {
						if (_canvas == null || _canvas.isDisposed()) {
							_canvas = null;
							return;
						}
						if (target instanceof Simulator) {
							_loading = false;
							_simulator = (Simulator) target;
							_display.syncExec(new Runnable() {
								@Override
								public void run() {
									// _canvas.redraw();
									_canvas.update();
								}
							});
						}
					}
				});
		CGEventDispatcher.getInstance().addListener(
				new CGEventListener(TimeSliceEvent.class) {
					@Override
					public void handle(CGEvent event, Object target) {
						if (_canvas == null || _canvas.isDisposed()) {
							_canvas = null;
							return;
						}
						if (target instanceof Simulator) {
							_simulator = (Simulator) target;
							if (_simulator == null) {
								return;
							}
							// TODO: synchronized _territory
							_display.syncExec(new Runnable() {
								@Override
								public void run() {
									if(_canvas.isDisposed())
										return;
									_canvas.redraw();
									_canvas.update();
								}
							});
						}
					}
				});
		CGEventDispatcher.getInstance().addListener(
				new CGEventListener(ChangeZoomingEvent.class) {
					@Override
					public void handle(CGEvent event, Object target) {
						if (!(event instanceof ChangeZoomingEvent))
							return;	// impossible
						ChangeZoomingEvent zooming = (ChangeZoomingEvent) event;
						int factor = zooming.getFactor();
						changeZooming(factor);
					}
				});
		CGEventDispatcher.getInstance().addListener(
				new CGEventListener(ToggleCoordBarsEvent.class) {
					@Override
					public void handle(CGEvent event, Object target) {
						if (_isEditorToolbox)
							return;
						if (_coordBars) {
							_coordBars = false;
							_xOffset = 0;
							_yOffset = 0;
						} else {
							_coordBars = true;
							_xOffset = HCOORDBAR;
							_yOffset = VCOORDBAR;
						}
					}
				});

	}

    TrackDirection[] trackDirections = {
             TrackDirection.E_W,
             TrackDirection.NW_SE,
             TrackDirection.SW_NE,
             TrackDirection.W_NE,
             TrackDirection.W_SE,
             TrackDirection.NW_E,
             TrackDirection.SW_E,
             TrackDirection.TRK_N_S,
             TrackDirection.SW_N,
             TrackDirection.NW_S,
             TrackDirection.SE_N,
             TrackDirection.NE_S,
             TrackDirection.X_X,
             TrackDirection.X_PLUS,
             TrackDirection.XH_SW_NE,
             TrackDirection.XH_NW_SE,
             TrackDirection.N_NE_S_SW,
             TrackDirection.N_NW_S_SE
    };

    TrackDirection[] signalsDirections = {
            TrackDirection.E_W,
            TrackDirection.W_E,
            TrackDirection.S_N,
            TrackDirection.N_S,
    };
}
