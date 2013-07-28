package com.traindirector.uicomponents;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
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
import com.traindirector.events.ResetEvent;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.TextTrack;
import com.traindirector.model.Track;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStop;
import com.traindirector.model.VLine;
import com.traindirector.simulator.Simulator;

public class TimeDistanceDiagram {

	private ScrolledComposite _scroller;
	private Canvas _canvas;
	private Simulator _simulator;
	private Color greenColor;
	private Color blackColor;
	private Color whiteColor;
	private Color orangeColor;
	private Color bgColor;

	private static int STATION_WIDTH = 100;
	private static int KM_WIDTH = 50;
	private static int HEADER_HEIGHT = 20;
	static	int	highkm;
	static	List<Track>	stations;

	public TimeDistanceDiagram(Composite parent) {
		stations = new ArrayList<Track>();

		_simulator = Simulator.INSTANCE;
		
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
									_canvas.redraw();
									_canvas.update();
								}
							});
						}
					}
				});
		CGEventDispatcher.getInstance().addListener(new CGEventListener(ResetEvent.class) {
			public void handle(CGEvent event, Object target) {
				if(target instanceof Simulator) {
					_simulator = (Simulator)target;
					if(_canvas == null || _canvas.isDisposed())
						return;
					_canvas.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							_canvas.redraw();
							_canvas.update();
						}
					});
				}
			}
		});
	}

	static	void	DrawTimeGrid(GC gc, int y) {
		int	h, m;
		int	x;
		String	buff = "";

		x = STATION_WIDTH + KM_WIDTH;
		for(h = 0; h < 24; ++h)
		    for(m = 0; m < 60; ++m) {
				if((m % 10) != 0) {
				    gc.drawLine(x + h * 240 + m * 4, y - 2, x + h * 240 + m * 4, y + 2);
				} else {
				    gc.drawLine(x + h * 240 + m * 4, 20, x + h * 240 + m * 4, 1000);
				}
		    }

		for(h = 0; h < 24; ++h) {
			buff = String.format("%2d:00", h);
		    gc.drawText(buff, x + h * 240, 10, false);
		}
	}

	int	km_to_y(int _km) {
		int	y;

		y = (int) (HEADER_HEIGHT + (double)_km / (double)highkm * 960);
		return y;
	}

	boolean islinkedtext(Track t) {
		if(t._elink._x != 0 && t._elink._y != 0)
		    return true;
		if(t._wlink._x != 0 && t._wlink._y != 0)
		    return true;
		return false;
	}


	public void updateCanvas(GC gc) {
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

	void	drawStations(GC gc)
	{
		int	y;
		String buff = "";

		highkm = 0;
		for(Track t : _simulator._territory._tracks) {
		    if(t instanceof TextTrack) {
				if(!islinkedtext(t))
				    continue;
				if(t._km > highkm)
				    highkm = t._km;
				continue;
		    }
		    if(!t._isStation || t._station == null || t._km == 0)
		    	continue;
		    if(t._km > highkm)
		    	highkm = t._km;
		}
		for(Track t : _simulator._territory._tracks) {
		    if(t instanceof TextTrack) {
				if(t._km == 0 || !islinkedtext(t))
				    continue;
		    } else if(!t._isStation || t._station == null || t._km == 0)
		    	continue;
		    stations.add(t);
		    y = km_to_y(t._km);
		    DrawTimeGrid(gc, y);

		    buff = String.format("%3d.%d %s", t._km / 1000, (t._km / 100) % 10, t._station);
		    int index = buff.indexOf(Territory.PLATFORM_SEP);
		    if(index > 0)
		    	buff = buff.substring(0, index);
		    gc.drawText(buff, 0, y, true);
		}
		if(stations.size() == 0) {
		    gc.drawText("Sorry, this feature is not available on this scenario.",10, 10, true);
		    gc.drawText("No station has distance information.", 10, 25, true);
		}
	}

	int	graphstation(String st) {
		int	i = 0;

		for(Track t : stations) {
		    if(st.equals(t._station))
		    	return i;
		    ++i;
		}
		return -1;
	}

	void	graph_xy(int _km, int tim, TDPosition pos)
	{
		pos._x = (int) (tim / 60 * 4 + STATION_WIDTH + KM_WIDTH);
		pos._y = km_to_y(_km);
	}

	void	time_to_time(GC gc, int x, int y, int nx, int ny, int type)
	{
		// TODO: int	colr = COL_TRAIN1 + type;
		Color color = _simulator._colorFactory.get(0,0,0);
		if(nx < x)	// ignore if arrival is next day
		    return;
		gc.setForeground(color);
		if(ny < y) {	// going from bottom of graph to top
		    gc.drawLine(x, y - 5, nx, ny + 5);
		    gc.drawLine(x, y, x, y - 5);
		    gc.drawLine(nx, ny + 5, nx, ny);
		} else {	// going from top of graph to bottom
		    gc.drawLine(x, y + 5, nx, ny - 5);
		    gc.drawLine(x, y, x, y + 5);
		    gc.drawLine(nx, ny - 5, nx, ny);
		}
	}

	boolean	samestation(String st, String arrdep)
	{
		String buff;
		int	i;

		for(i = 0; i < arrdep.length() && arrdep.charAt(i) != ' '; ++i);
		buff = arrdep.substring(0, i);
		return(Territory.sameStation(st, buff));
	}

	void	drawTrains(GC gc) {
		Track	trk;
		int	indx;
		int	x;

		for(Train t : _simulator._schedule._trains) {
		    x = -1;
		    trk = null;
		    TDPosition pos = new TDPosition();
		    TDPosition npos = new TDPosition();
		    if (t._entrance != null) {
    		    for(Track trk1 : _simulator._territory._tracks) {
    				if((trk1 instanceof TextTrack) && islinkedtext(trk1) && trk1._km > 0 && samestation(trk1._station, t._entrance)) {
    					trk = trk1;
    				    break;
    				}
    				if((trk1 instanceof Track) && trk1._isStation && trk1._station != null && samestation(trk1._station, t._entrance)) {
    					trk = trk1;
    					break;
    				}
    		    }
    		    if(trk != null)
    		    	if((indx = graphstation(trk._station)) >= 0)
    		    		graph_xy(stations.get(indx)._km, t._timeIn, pos);
		    }
	    	for(TrainStop ts : t._stops) {
				indx = graphstation(ts._station);
				if(indx < 0)
				    continue;
				Track station = stations.get(indx);
				if(x == -1) {
				    graph_xy(station._km, ts._departure, pos);
				    x = pos._x;
				    continue;
				}
				graph_xy(station._km, ts._arrival, npos);
				time_to_time(gc, pos._x, pos._y, npos._x, npos._y, t._type);
				graph_xy(station._km, ts._departure, pos);
				x = pos._x;
		    }
		    if(x != -1 && t._exit != null) {
		    	trk = null;
			    for(Track trk1 : _simulator._territory._tracks) {
					if((trk1 instanceof TextTrack) && islinkedtext(trk1) && trk1._km > 0 && samestation(trk1._station, t._exit)) {
						trk = trk1;
					    break;
					}
					if((trk1 instanceof Track) && trk1._isStation && samestation(trk1._station, t._exit)) {
						trk = trk1;
						break;
					}
			    }
				if(trk == null)
				    continue;
				indx = graphstation(trk._station);
				if(indx < 0)
				    continue;
				graph_xy(stations.get(indx)._km, t._timeOut, npos);
				time_to_time(gc, pos._x, pos._y, npos._x, npos._y, t._type);
		    }
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
			orangeColor = _simulator._colorFactory.get(255, 140, 0);
			bgColor = _simulator._colorFactory.getBackgroundColor();
		}

		synchronized (_simulator) {
			gc.setBackground(bgColor);
			gc.fillRectangle(0, 0, _canvas.getSize().x, _canvas.getSize().y);
			gc.setForeground(blackColor);
			
			drawStations(gc);
			drawTrains(gc);
		}
	}

	public void eraseGridCell(GC gc, TDPosition pos) {
		int x = pos._x * Simulator.HGRID;
		int y = pos._y * Simulator.VGRID;
		gc.setForeground(bgColor);
		gc.fillRectangle(x, y, Simulator.HGRID, Simulator.VGRID);
	}

	public void drawSegments(GC gc, TDPosition pos, VLine[] segs,
			TrackStatus status) {
		int x = pos._x * Simulator.HGRID;
		int y = pos._y * Simulator.VGRID;
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
			gc.drawLine(x + vl._x0, y + vl._y0, x + vl._x1, y + vl._y1);
		}
	}

	public void onMouseDown(MouseEvent e) {
	}

	public void onMouseMove(MouseEvent e) {
	}

	public void onMouseUp(MouseEvent e) {
	}

	public void onMouseDoubleClick(MouseEvent e) {
	}

	public void onKeyDown(KeyEvent e) {
	}

	public void onKeyUp(KeyEvent e) {
	}
}
