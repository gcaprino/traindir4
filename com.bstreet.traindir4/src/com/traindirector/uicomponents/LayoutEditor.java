package com.traindirector.uicomponents;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import com.traindirector.Application;
import com.traindirector.dialogs.PropertyDialog;
import com.traindirector.dialogs.TextOption;
import com.traindirector.dialogs.TriggerPropertiesDialog;
import com.traindirector.files.TrkFile;
import com.traindirector.model.ImageTrack;
import com.traindirector.model.Itinerary;
import com.traindirector.model.Itinerary.ItinerarySwitch;
import com.traindirector.model.ItineraryButton;
import com.traindirector.model.PlatformTrack;
import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;
import com.traindirector.model.Switch;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.TextTrack;
import com.traindirector.model.Track;
import com.traindirector.model.TrackDirection;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.TriggerTrack;
import com.traindirector.options.BooleanOption;
import com.traindirector.options.FileOption;
import com.traindirector.options.Option;
import com.traindirector.simulator.Simulator;

public class LayoutEditor {

	List<Option> _options;
	static TriggerPropertiesDialog _triggerPropertiesDialog;
	Simulator _simulator;
	LayoutCanvas _canvas;
	TDPosition _pos;
	TDPosition _linkStart;
	private String _currentMacroName;
	private TDPosition _moveStart;
	private TDPosition _moveEnd;

	public LayoutEditor(Simulator sim, LayoutCanvas canvas) {
		_simulator = sim;
		_canvas = canvas;
	}

	public void handle(boolean isRightClick) {
	    Track track;
	    Switch sw;
	    Signal signal;
	    int dx, dy;
	    Territory territory = _simulator._territory;
	    
	    if(isRightClick) {
	        
	        // Open properties dialog based on object type
	        track = territory.findTrack(_pos);
	        if(track == null)
	        	return;
	        
	        if(track instanceof Switch) {
	        	return; // switches have no properties
	        } else if(track instanceof Signal) {
	        	editSignalProperties((Signal)track);
	        } else if(track instanceof TriggerTrack) {
	        	editTriggerProperties((TriggerTrack)track);
	        } else if(track instanceof ItineraryButton) {
	        	editItineraryButton((ItineraryButton)track);
	        } else {
	        	editTrackProperties(track);
	        }
	    } else {
	        int type = _simulator.getEditorTrackType();
	        int dir = _simulator.getEditorTrackDirection();
	        track = territory.findTrack(_pos);
	        if(track != null) {
	            territory.remove(track);
	        }
	        switch(type) {
	        case 0:    // delete
	            break;
	        case 1:    // tracks
	            track = new Track(_pos);
	            track._direction = TrackDirection.parse(dir);
	            territory.add(track);
	            break;
	        case 2:    // switches
                sw = new Switch(_pos);
                sw._direction = TrackDirection.parse(dir);
                territory.add(sw);
	            break;
	        case 3:    // signals
                signal = _simulator._signalFactory.newInstance(dir < 4 ? 1 : 2);
                signal._position = _pos;
                signal._direction = TrackDirection.parse(dir);
                signal.setAspectFromName(SignalAspect.RED);
                signal._status = TrackStatus.FREE; // RED
                if(dir >= 4)
                    signal._fleeted = true;
                territory.add(signal);
                if(!_simulator._options._autoLink.isSet())
                    break;
                dx = 0;
                dy = 0;
                switch(dir) {
                case 0:
                case 4:
                    dx = 0;
                    dy = -1;
                    break;
                case 1:
                case 5:
                    dx = 0;
                    dy = 1;
                    break;
                case 2:
                case 6:
                    dx = 1;
                    dy = 0;
                    break;
                case 3:
                case 7:
                    dx = -1;
                    dy = 0;
                    break;
                }
                if(_simulator._options._linkToLeft.isSet()) {
                    dx *= -1;
                    dy *= -1;
                }
                track = territory.findTrack(new TDPosition(_pos._x + dx, _pos._y * dy));
                if(track == null)
                    break;
                signal.linkedTo(track);
	            break;
	        case 4:    // items
	        	switch (dir) {
	        	case 0:
	        	case 1:
	        		track = new TextTrack();
	        		track._position = _pos;
	        		track._station = "Abc";
	        		track._direction = TrackDirection.parse(dir); // needed for .trk compatibility
	        		territory.add(track);
	        		break;
	        		
	        	case 2:
	        	case 3:
	        		track = new ItineraryButton();
	        		track._position = _pos;
	        		track._station = "A";
	        		track._direction = TrackDirection.parse(dir - 2); // needed for .trk compatibility
	        		territory.add(track);
	        		break;
	        		
	        	case 4:
	        		track = new ImageTrack();
	        		track._position = _pos;
	        		track._direction = TrackDirection.parse(0);
	        		track._station = null;
	        		territory.add(track);
	        		break;
	        		
	        	case 5:
	        		track = new PlatformTrack();
	        		track._position = _pos;
	        		track._direction = TrackDirection.parse(0);
	        		track._station = null;
	        		territory.add(track);
	        		break;
	        		
	        	}
	            break;
	            
	        case 5:    // actions
	        	
	        	switch (dir) {
	        	case 0: // Link...
	        		track = territory.findTrack(_pos);
	        		if (track == null)
	        			break;
	        		_linkStart = _pos;
	        		_simulator.setEditorTool(5, 1);
	        		break;

	        	case 1: // ...to...
	        		Track track1 = territory.findTrack(_pos);
	        		track = territory.findTrack(_linkStart);
	        		if (track != null && track1 != null) {
	        			if (track instanceof TriggerTrack && !(track1 instanceof Track))
	        				return;
	        			if (track instanceof ImageTrack && !(track1 instanceof Switch) && !(track1 instanceof Signal))
	        				return;
	        			linkTracks(track, track1);
	        		}
	        		_simulator.setEditorTool(5, 0);
	        		break;
	        		
	        	case 2:
	        		if (!macroSelect()) {
	        			break;
	        		}
	        		_simulator.setEditorTool(5, 1);
	        		break;

	        	case 3:
	        		macroPlace(_pos._x, _pos._y);
	        		break;

	        	case 4:
	        	case 5:
	        	case 6:
	        	case 7:
	        		track = new TriggerTrack();
	        		track._position = _pos;
	        		track._direction = TrackDirection.parse(dir - 4);
	        		track._station = null;
	        		territory.add(track);
	                if(!_simulator._options._autoLink.isSet())
	                    break;
	                dx = 0;
	                dy = 0;
	                switch(dir) {
	                case 4:
	                    dx = 0;
	                    dy = -1;
	                    break;
	                case 5:
	                    dx = 0;
	                    dy = 1;
	                    break;
	                case 6:
	                    dx = 1;
	                    dy = 0;
	                    break;
	                case 7:
	                    dx = -1;
	                    dy = 0;
	                    break;
	                }
	                if(_simulator._options._linkToLeft.isSet()) {
	                    dx *= -1;
	                    dy *= -1;
	                }
	                track1 = territory.findTrack(new TDPosition(_pos._x + dx, _pos._y * dy));
	                if(track1 == null)
	                    break;
	                linkTracks(track, track1);
	                break;

	        	case 8:
	        		_moveStart = _pos;
	        		_simulator.setEditorTool(5, 9);
	        		break;
	        		
	        	case 9:
	        		if (_moveStart == null)
	        			break;
	        		_moveEnd = _pos;
	        		_simulator.setEditorTool(5, 10);
	        		break;
	        		
	        	case 10:
	        		if (_moveStart == null || _moveEnd == null)
	        			break;
	        		moveLayout(_pos);
	        		break;
	        	}
	            break;
	        }
	    }
    }

	private void moveLayout(TDPosition pos) {
		// avoid overlaps by moving the original tracks
		// to an area where there cannot be any other track
		moveAllElements(_moveStart._x + 1000, _moveStart._y + 1000);
		// move back from the temporary area to the
		// final destination area.
		_moveStart._x += 1000;
		_moveStart._y += 1000;
		_moveEnd._x += 1000;
		_moveEnd._y += 1000;
		moveAllElements(pos._x, pos._y);
		_moveStart._x -= 1000;
		_moveStart._y -= 1000;
		_moveEnd._x -= 1000;
		_moveEnd._y -= 1000;
	}

	boolean	isInside(TDPosition upleft, TDPosition downright, int x, int y) {
		if(x >= upleft._x && x <= downright._x && y >= upleft._y && y <= downright._y)
			return true;
		return false;
	}

//	Move all track elements in the rectangle
//	comprised by (move_start,move_end) to
//	the coordinarte x,y (upper-left corner)

	private void moveAllElements(int x, int y) {

		TDPosition start = new TDPosition();
		TDPosition end = new TDPosition();
		int dx, dy;
		Track t1;
		
		if(_moveEnd._x < _moveStart._x) {
		    start._x = _moveEnd._x;
		    end._x = _moveStart._x;
		} else {
		    start._x = _moveStart._x;
		    end._x = _moveEnd._x;
		}
		if(_moveEnd._x < _moveStart._y) {
		    start._y = _moveEnd._y;
		    end._y = _moveStart._y;
		} else {
		    start._y = _moveStart._y;
		    end._y = _moveEnd._y;
		}
		dx = x - start._x;
		dy = y - start._y;
		for (Track t : _simulator._territory.getTracks()) {
		    x = t._position._x;
		    y = t._position._y;
		    if(isInside(start, end, x, y)) {
		    	t1 = _simulator._territory.findTrack(t._position._x + dx, t._position._y + dy);
		    	if (t1 != null)
		    		_simulator._territory.remove(t1);
				t._position._x += dx;
				t._position._y += dy;
		    }
		    if(t._elink != null && !t._elink.isNull() &&
		    		isInside(start, end, t._elink._x, t._elink._y)) {
				t._elink._x += dx;
				t._elink._y += dy;
		    }
		    if(t._wlink != null && !t._wlink.isNull() &&
		    		isInside(start, end, t._wlink._x, t._wlink._y)) {
				t._wlink._x += dx;
				t._wlink._y += dy;
			}
		}

		//  I hope this is right :)

		int	    n;

		for (Itinerary it : _simulator._territory._itineraries) {
		    for(n = 0; n < it._switches.size(); ++n) {
		    	ItinerarySwitch sw = it._switches.get(n);
				if(isInside(start, end, sw._position._x, sw._position._y)) {
				    sw._position._x += dx;
				    sw._position._y += dy;
				}
		    }
		}
	}

	private boolean macroSelect() {
    	Track t;
    	Itinerary nextItin, itinList;
    	
    	String name = MacroFileDialog.get();
    	if (name == null)
    		return false;
    	_currentMacroName = name;
    	return true;
    }

    private void relocateItinerary(Itinerary it, int xbase, int ybase) {
    	for (int i = it._switches.size(); --i >= 0; ) {
    		ItinerarySwitch sw = it._switches.get(i);
    		sw._position._x += xbase;
    		sw._position._y += ybase;
    	}
    }
    
    private boolean macroPlace(int xbase, int ybase) {
    	
    	if (_currentMacroName == null || _currentMacroName.isEmpty())
    		return false;
		BufferedReader rdr = _simulator.getReaderForFile(_currentMacroName);
		if (rdr == null) {
			return false;
		}
		Territory macro = new Territory();
		TrkFile trkFile = new TrkFile(_simulator, macro, _currentMacroName, rdr);
		trkFile.loadTracks(rdr);
		_simulator._territory.updateReferences();
		_simulator._territory.loadSignalAspects(_simulator._scriptFactory);

		for (Track track : macro.getTracks()) {
			track._position._x += xbase;
			track._position._y += ybase;
			Track old = _simulator._territory.findTrack(track._position);
			if (old != null) {
				_simulator._territory.remove(old);
			}
			if (track._wlink != null && !track._wlink.isNull()) {
				track._wlink._x += xbase;
				track._wlink._y += ybase;
			}
			if (track._elink != null && !track._elink.isNull()) {
				track._elink._x += xbase;
				track._elink._y += ybase;
			}
			_simulator._territory.add(track);
		}
		
		/* Link in the itineraries from the macro.  Delete duplicates  */
		for (int i = macro._itineraries.size(); --i >= 0; ) {
			Itinerary it = macro._itineraries.get(i);
			Itinerary old = _simulator._territory.findItinerary(it._name);
			if (old != null) {
				_simulator._territory._itineraries.remove(old);
			}
			relocateItinerary(it, xbase, ybase);
			_simulator._territory._itineraries.add(it);
		}
    	return true;
    }

	private void linkTracks(Track t, Track t1) {
    	if (t instanceof Switch) {
    		if (!(t1 instanceof Switch)) {
    			_simulator.alert("Only like tracks can be linked.");
    			return;
    		}
    		t._wlink = t1._position;
    		t1._wlink = t._position;
    		return;
    	}
    	if (t instanceof Signal) {
    		if (!(t1 instanceof Track)) {
    			_simulator.alert("Signals can only be linked to a track.");
    			return;
    		}
    		t._wlink = t1._position;
    		((Signal) t)._controls = t1;
    		return;
    	}
    	if (t instanceof TriggerTrack) {
    		if (!(t1 instanceof Track)) {
    			_simulator.alert("Triggers can only be linked to a track.");
    			return;
    		}
    		t._wlink = t1._position;
    		((TriggerTrack) t)._controls = t1;
    		return;
    	}
    	if (t instanceof ImageTrack) {
    		t._wlink = t1._position;
    		((ImageTrack) t)._controls = t1; // t1 could be a signal or a switch
    		return;
    	}

    	if (t instanceof TextTrack) {
    		if (!(t1 instanceof Track)) {
    			_simulator.alert("Entry/Exit points can only be linked to a track.");
    			return;
    		}
    		if (t1._position._x < t._position._x) {
    			t._wlink = t1._position;
    		} else {
    			t._elink = t1._position;
    		}
    		return;
    	}

    	if (!(t1 instanceof Track)) {
		    _simulator.alert("Only like tracks can be linked.");
		    return;
		}
    	if (t1._direction != TrackDirection.W_E && t1._direction != TrackDirection.N_S) {
		    _simulator.alert("Only horizontal or vertical tracks can be linked automatically.\nTo link other track types, use the track properties dialog.");
		    return;
		}
    	if (t._direction == TrackDirection.N_S) {
    		if (_simulator._territory.findTrack(t._position._x, t._position._y + 1) == null) {
    			t._elink = t1._position;
    		} else {
    			t._wlink = t1._position;
    		}
    		if (_simulator._territory.findTrack(t1._position._x, t1._position._y + 1) == null) {
    			t1._elink = t._position;
    		} else {
    			t1._wlink = t._position;
    		}
    		if (_simulator._territory.findTrack(t._position._x + 1, t._position._y) == null &&
    				_simulator._territory.findTrack(t._position._x + 1, t._position._y) == null) {
    			t._elink = t1._position;
    		} else {
    			t._wlink = t1._position;
    		}
    		if (_simulator._territory.findTrack(t1._position._x - 1, t1._position._y) == null &&
    				_simulator._territory.findTrack(t1._position._x - 1, t1._position._y) == null) {
    			t1._wlink = t._position;
    		} else {
    			t1._elink = t._position;
    		}
    	}
	}

	private void editTrackProperties(Track track) {
    	_options = new ArrayList<Option>();
    	Option o = new TextOption("length", "Track length (m) :");
    	o._value = "" + track._length;
    	_options.add(o); // 0
    	o = new TextOption("station", "Station name :");
    	o._value = track._station;
    	_options.add(o); // 1
    	o = new TextOption("km", "km :");
    	o._value = "" + track._km;
    	_options.add(o); // 2
    	o = new TextOption("speeds", "Speed(s) :");
    	if(track._speed == null)
    		track._speed = new int[Track.NSPEEDS];
    	o._value = track.speedsToString();
    	_options.add(o); // 3
    	o = new TextOption("eastlink", "Linked to east :");
    	if(track._elink != null)
    		o._value = "" + track._elink._x + "," + track._elink._y;
    	_options.add(o); // 4
    	o = new TextOption("westlink", "Linked to west :");
    	if(track._wlink != null)
    		o._value = "" + track._wlink._x + "," + track._wlink._y;
    	_options.add(o); // 5
    	
    	o = new BooleanOption("hidden", "Hidden");
    	o._intValue = track._invisible ? 1 : 0;
    	_options.add(o); // 6
    	o = new BooleanOption("highsignal", "Don't stop if shunting");
    	o._intValue = (track._flags & Track.DONTSTOPSHUNTERS) != 0 ? 1 : 0;
    	_options.add(o); // 7

    	// TODO: add script button
    	
    	final int[] result = new int[1];
    	final PropertyDialog dialog = new PropertyDialog(null, _options);
    	Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = dialog.open();
			}
		});
    	if(result[0] == PropertyDialog.CANCEL)
    		return;
    	
    	track._length = Integer.parseInt(_options.get(0)._value);
    	track._station = _options.get(1)._value;
    	track._km = Integer.parseInt(_options.get(2)._value);	// TODO: parse km.meters
    	String[] spds = _options.get(3)._value.split("/");
    	for(int i = 0; i < track._speed.length; ++i) {
    		if(i >= spds.length)
    			track._speed[i] = 0;
    		else
    			track._speed[i] = Integer.parseInt(spds[i]);
    	}
		track._elink = new TDPosition(_options.get(4)._value);
		track._wlink = new TDPosition(_options.get(5)._value);
		track._invisible = _options.get(6)._intValue != 0;
		track._flags &= ~Track.DONTSTOPSHUNTERS;
		if(_options.get(7)._intValue != 0)
			track._flags |= Track.DONTSTOPSHUNTERS;
	}

	private void editItineraryButton(ItineraryButton track) {
	}

	private void editTriggerProperties(TriggerTrack track) {
		if (_triggerPropertiesDialog == null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					_triggerPropertiesDialog = new TriggerPropertiesDialog(Application._display.getActiveShell());
				}
			});
		}
		_triggerPropertiesDialog.open(track);
	}

	private void editSignalProperties(Signal signal) {
    	_options = new ArrayList<Option>();
    	String name = signal._station;
    	if((name == null || name.isEmpty()) && signal._position != null)
    		name = signal._position.toString();
    	addTextOption("Signal name :", name);	// 0
    	name = "";
    	if(signal._wlink != null) {
    		name = "" + signal._wlink._x + "," + signal._wlink._y;
    	}
    	addTextOption("Linked to track at :", name); // 1
    	
    	name = "";
    	if(signal._blockedBy != null) {
    		name = "" + signal._blockedBy._x + "," + signal._blockedBy._y;
    	}
    	addTextOption("Blocked by :", name); // 2
    	
    	addBooleanOption("Signal is always red", signal._fixedred); // 3
    	addBooleanOption("Signal has square frame", signal._signalx); // 4
    	addBooleanOption("No penalty for train stopping at this signal", signal._nopenalty); // 5
    	addBooleanOption("No penalty for un-necessary clicks", signal._noClickPenalty); // 6
    	addBooleanOption("Hidden", signal._invisible); // 7
    	addBooleanOption("Intermediate", signal._intermediate); // 8
    	FileOption fo = new FileOption("script", "Script file :"); // 9
    	if(signal._script != null)
    		fo._value = signal._scriptFile;
    	_options.add(fo);

    	final int[] result = new int[1];
    	final PropertyDialog dialog = new PropertyDialog(null, _options);
    	Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				result[0] = dialog.open();
			}
		});
    	if(result[0] == PropertyDialog.CANCEL)
    		return;
    	
    	signal._station = _options.get(0)._value;
    	// TODO: read coords for linked and blocked
    	signal._fixedred = _options.get(3)._intValue != 0;
    	signal._signalx = _options.get(4)._intValue != 0;
    	signal._nopenalty = _options.get(5)._intValue != 0;
    	signal._noClickPenalty = _options.get(6)._intValue != 0;
    	signal._invisible = _options.get(7)._intValue != 0;
    	signal._intermediate = _options.get(8)._intValue != 0;
	}

	private void addTextOption(String descr, String value) {
    	TextOption o = new TextOption("", descr);
    	o._value = value;
    	_options.add(o); // 0
	}
	
	private void addBooleanOption(String descr, boolean value) {
    	BooleanOption o = new BooleanOption("", descr);
    	o._intValue = value ? 1 : 0;
    	_options.add(o);
	}
	
}
