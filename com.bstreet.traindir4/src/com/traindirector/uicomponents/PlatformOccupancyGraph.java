package com.traindirector.uicomponents;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
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

import com.bstreet.cg.events.CGEvent;
import com.bstreet.cg.events.CGEventDispatcher;
import com.bstreet.cg.events.CGEventListener;
import com.traindirector.events.LoadEndEvent;
import com.traindirector.events.LoadStartEvent;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.Track;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.simulator.Simulator;

public class PlatformOccupancyGraph {

	private static int STATION_WIDTH = 140;
	private static int KM_WIDTH = 5;
	private static int HEADER_HEIGHT = 20;
	private static int MAXWIDTH = (2 * 60 * 24 + STATION_WIDTH + KM_WIDTH);
	private static int Y_DIST = 20;
	private static int HEIGHT = 700;
	private Color greenColor;
	private Color blackColor;
	private Color whiteColor;
	private Color orangeColor;
	private Color bgColor;

	int	highkm;
	public class PlatformSegment {
		int	    _y;
		int	    _x0, _x1;
		Train	    _train;
		Train	    _parent;
		long	    _timein, _timeout;
	};

	List<PlatformSegment> _segments;


	List<Track> _stations;
	
	private Simulator _simulator;
	private ScrolledComposite _scroller;
	private Canvas _canvas;

	public PlatformOccupancyGraph(Composite parent) {
		_simulator = Simulator.INSTANCE;
		
		_stations = new ArrayList<Track>();
		_segments = new ArrayList<PlatformSegment>();

		_scroller = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		_canvas = new Canvas(_scroller, SWT.NO_BACKGROUND);
		_scroller.setContent(_canvas);
		_canvas.setSize(4000, 4000);

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
		CGEventDispatcher.getInstance().addListener(
				new CGEventListener(LoadStartEvent.class) {
					@Override
					public void handle(CGEvent event, Object target) {
						// TODO: clear canvas
					}
				});
		CGEventDispatcher.getInstance().addListener(
				new CGEventListener(LoadEndEvent.class) {
					private Simulator _simulator;

					@Override
					public void handle(CGEvent event, Object target) {
						if (_canvas == null || _canvas.isDisposed()) {
							_canvas = null;
							return;
						}
						if (target instanceof Simulator) {
							_simulator = (Simulator) target;
							_canvas.getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									// _canvas.redraw();
									_canvas.update();
								}
							});
						}
					}
				});

	}
	void	DrawTimeGrid(GC gc, int y) {
		int	h, m;
		int	x;
		String buff = "";

		x = STATION_WIDTH + KM_WIDTH;
		gc.drawLine(x, HEIGHT, x + 23 * 120 + 59 * 2, HEIGHT);
		for(h = 0; h < 24; ++h)
		    for(m = 0; m < 60; ++m) {
				if((m % 10) != 0) {
	//			    g->DrawLine(
	//				x + h * 120 + m * 2, y - 2,
	//				x + h * 120 + m * 2, y + 2,
	//				0);
				} else {
				    gc.drawLine(
					x + h * 120 + m * 2, 20,
					x + h * 120 + m * 2, HEIGHT);
	//				m != 0 ? 6 : 0);
				}
		    }

		for(h = 0; h < 24; ++h) {
			buff = String.format("%2d:00", h);
		    gc.drawText(buff, x + h * 120, 10);
		}
	}

	void	drawStations(GC gc) {
		Track	t;
		int	y = HEADER_HEIGHT;

		for(Track track : _simulator._territory._tracks) {
		    if(!track._isStation || track._station == null || track._station.isEmpty())
		    	continue;
		    _stations.add(track);
		    DrawTimeGrid(gc, y);
		    gc.drawText(track._station, 0, y - 10);
		    gc.drawLine(STATION_WIDTH + KM_WIDTH, y, STATION_WIDTH + KM_WIDTH + 24 * 120, y);
		    y += Y_DIST;
		}
		if(_stations.size() == 0) {
		    gc.drawText("Sorry, this feature is not available on this scenario.", 10, 10);
		    gc.drawText("No station was found on the layout.", 10, 25);
		}
	}

	int	graphstation(String st)	{
		int	i;

		for(i = 0; i < _stations.size(); ++i)
		    if(st.equals(_stations.get(i)._station))
		    	return i;
		return -1;
	}

	void	graph_xy(int km, int tim, TDPosition pos) {
		pos._x = tim / 60 * 2 + STATION_WIDTH + KM_WIDTH;
		pos._y = (km + 1) * Y_DIST;
	}

	void	time_to_time(GC gc, int x, int y, int nx, int ny, int type)	{
		//int	color = fieldcolors[COL_TRAIN1 + type];
		Color color = _simulator._colorFactory.get("black");

		if(nx < x)	// ignore if arrival is next day
		    return;
		gc.setForeground(color);
		gc.drawLine(x, y - 1, nx, y - 1);
		gc.drawLine(x, y, nx, y);
		gc.drawLine(x, y + 1, nx, y + 1);
	}

	boolean samestation(String st, String arrdep) {
		return st.equals(arrdep);
	}

	void	addSegment(Train trn, int x0, int x1, int y, int timein, int timeout, Train parent)	{
		PlatformSegment segment = new PlatformSegment();

		segment._train = trn;
		segment._x0 = x0;
		segment._x1 = x1;
		segment._y = y;
		segment._timein = timein;
		segment._timeout = timeout;
		segment._parent = parent;
		_segments.add(segment);
	}

	void	drawTrains(GC gc) {
		Track	trk;
		int	indx;
		int	x, y;
		int	nx, ny;

		for(Train t : _simulator._schedule._trains) {
		    if(t._days != 0 && _simulator._runDay != 0 && (t._days & _simulator._runDay) == 0)
		    	continue;
		    x = y = -1;
		    trk = null;
		    for(Track track1 : _simulator._territory._tracks) {
				if(track1 instanceof Track && track1._isStation &&
							samestation(track1._station, t._entrance)) {
					trk = track1;
				    break;
				}
		    }
		    TDPosition pos = new TDPosition();
		    TDPosition npos = new TDPosition();
		    if(trk != null && (indx = graphstation(trk._station)) >= 0) {
				graph_xy(indx, t._timeIn, pos);
				if(t._waitFor != null) {
				    Train parent = _simulator._schedule.findTrainNamed(t._waitFor);
				    if(parent != null) {
						graph_xy(indx, parent._timeOut, npos);
						time_to_time(gc, pos._x, pos._y, npos._x, npos._y, t._type);
						addSegment(t, pos._x, npos._x, pos._y, parent._timeOut, t._timeIn, parent);
				    }
				}
		    }
		    for(TrainStop ts : t._stops) {
				indx = graphstation(ts._station);
				if(indx < 0)
				    continue;
				graph_xy(indx, ts._arrival, npos);
				graph_xy(indx, ts._departure, pos);
				time_to_time(gc, npos._x, npos._y, pos._x, pos._y, t._type);
				addSegment(t, npos._x, pos._x, pos._y, ts._arrival, ts._departure, null);
		    }
		    if(t._stock != null) {
		    	trk = null;
				for(Track trk1 : _simulator._territory._tracks) {
				    if(trk instanceof Track && trk1._isStation &&
							    samestation(trk1._station, t._exit)) {
				    	trk = trk1;
				    	break;
				    }
				}
				if(trk  != null && (indx = graphstation(trk._station)) >= 0) {
				    Train child = _simulator._schedule.findTrainNamed(t._stock);
				    if(child != null) {
						graph_xy(indx, t._timeOut, pos);
						graph_xy(indx, child._timeIn, npos);
						time_to_time(gc, pos._x, pos._y, npos._x, npos._y, t._type);
						addSegment(child, pos._x, npos._x, pos._y, t._timeOut, child._timeIn, t);
				    }
				}
		    }
		}
	}

	protected void updateCanvas(GC gc) {
		try {
			// draw into a buffer, then transfer it over to the canvas
			Image buffer = new Image(Display.getDefault(), _canvas.getBounds());
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
	
	protected void redrawCanvas(GC gc) {
		if (_simulator == null) {
			return;
		}
		if (greenColor == null) {
			greenColor = _simulator._colorFactory.get(0, 255, 0);
			blackColor = _simulator._colorFactory.get(0, 0, 0);
			whiteColor = _simulator._colorFactory.get(255, 255, 255);
			orangeColor = _simulator._colorFactory.get(255, 140, 0);
			bgColor = _simulator._colorFactory.getBackgroundColor();
		}

		synchronized (_simulator) {
			Territory territory = _simulator._territory;

			gc.setBackground(bgColor);
			gc.fillRectangle(0, 0, _canvas.getSize().x, _canvas.getSize().y);
			gc.setForeground(blackColor);
			
			_segments.clear();
			drawStations(gc);
			DrawTimeGrid(gc, 0);
			drawTrains(gc);
		}
	}

	protected void onMouseMove(MouseEvent e) {
	}
	
	/*

extern	Track	*layout;
extern	int	is_windows;




PlatformGraphView::PlatformGraphView(wxWindow *parent)
	: wxScrolledWindow(parent, wxID_ANY, wxPoint(0, 0), wxSize(XMAX * 2 + STATION_WIDTH + KM_WIDTH, YMAX))
{
	SetScrollbars(1, 1, XMAX * 2 + STATION_WIDTH + KM_WIDTH, YMAX);
	grid	*g = new grid(this, XMAX * 2 + STATION_WIDTH + KM_WIDTH, YMAX);
	platform_graph_grid = g;
	m_tooltip = 0;
	wxToolTip::SetDelay(1000);
	wxToolTip::Enable(true);
	g->Clear();
}

void	PlatformGraphView::Refresh()
{
	grid	*g = platform_graph_grid;

	wxScrolledWindow::Refresh();
}

void	PlatformGraphView::OnPaint(wxPaintEvent& event)
{
	if(platform_graph_grid)
	    platform_graph_grid->Paint(this);
}

wxPoint PlatformGraphView::GetEventPosition(wxPoint& pt)
{
	double	xScale, yScale;
	wxPoint	pos(pt);
	CalcUnscrolledPosition(pos.x, pos.y, &pos.x, &pos.y);
	field_grid->m_dc->GetUserScale(&xScale, &yScale);
	pos.x /= xScale;
	pos.y /= yScale;
	return pos;
}

void PlatformGraphView::OnMouseMove(wxMouseEvent& event)
{
	wxPoint pos = event.GetPosition();
	pos = GetEventPosition(pos);

	Coord	coord(pos.x, pos.y);

	wxChar	oldTooltip[sizeof(tooltipString)/sizeof(tooltipString[0])];
	wxStrcpy(oldTooltip, tooltipString);

//	pointer_at(coord);

	PlatformSegment *segment;

	for(segment = segments; segment; segment = segment->next) {
	    if(pos.x >= segment->x0 && pos.x < segment->x1 &&
		pos.y >= segment->y - 2 && pos.y < segment->y + 2) {
		if(segment->parent) {
		    wxSnprintf(tooltipString, sizeof(tooltipString)/sizeof(tooltipString[0]),
			L("Train %s              \nArrives %s\n"),
			segment->parent->name, format_time(segment->timein));
		    wxSprintf(tooltipString + wxStrlen(tooltipString),
			L("Departs %s\nas train %s"), format_time(segment->timeout), segment->train->name);
		} else {
		    wxSnprintf(tooltipString, sizeof(tooltipString)/sizeof(tooltipString[0]),
			L("Train %s              \nArrives %s\n"),
			segment->train->name, format_time(segment->timein));
		    wxSprintf(tooltipString + wxStrlen(tooltipString),
			L("Departs %s\n"), format_time(segment->timeout));
		}
		break;
	    }
	}
	if(!segment) {
	    SetToolTip(0);
//	    if(m_tooltip)
//		delete m_tooltip;
	    m_tooltip = 0;
	    tooltipString[0] = 0;
	} else if(wxStrcmp(oldTooltip, tooltipString)) {
#ifdef WIN32
	    wxToolTip *newTip = new wxToolTip(tooltipString);
	    SetToolTip(newTip);
//	    if(m_tooltip)
//		delete m_tooltip;
	    m_tooltip = newTip;
#else
//	    canvasHelp.AddHelp(this, tooltipString);
//	    canvasHelp.ShowHelp(this);
//	    canvasHelp.RemoveHelp(this);
#endif
	}
	event.Skip();
}


	 */
}
