package com.traindirector.model;

import java.util.LinkedList;
import java.util.List;

import com.traindirector.commands.DoCommand;
import com.traindirector.scripts.ScriptFactory;
import com.traindirector.scripts.TDSScript;
import com.traindirector.simulator.Simulator;

public class Territory {

	public static final char PLATFORM_SEP = '@';

	public List<Track> _tracks;
	public List<Itinerary> _itineraries;
	public List<EntryExitPath> _paths;

	// private HashMap<String, SignalAspect> _signalAspects;

	public Territory() {
		_tracks = new LinkedList<Track>();
		_itineraries = new LinkedList<Itinerary>();
		_paths = new LinkedList<EntryExitPath>();
	}

	public void clear() {
		_tracks.clear();
		_itineraries.clear();
		_paths.clear();
	}

	public void add(Track trk) {
		_tracks.add(trk);
	}

	public Track get(int index) {
		return _tracks.get(index);
	}

	public Track findTrack(TDPosition pos) {
		if (pos == null)
			return null;
		for (Track track : _tracks) {
			if (track._position.sameAs(pos))
				return track;
		}
		return null;
	}

	public void linkSignals() {

		for (Track track : _tracks) {
			track._wsignal = null; // in case signal was relinked during editing
			track._esignal = null;
		}

		for (Track track : _tracks) {
			if (!(track instanceof Signal))
				continue;
			Signal signal = (Signal) track;
			Track linked = findTrack(signal._wlink);
			if (linked == null)
				continue;
			signal._controls = linked;
			if (signal._direction == TrackDirection.W_E
					|| signal._direction == TrackDirection.S_N)
				linked._esignal = signal;
			else
				linked._wsignal = signal;
		}
	}

	public Signal findSignalLinkedTo(Track track, Direction dir) {
		for (Track trk : _tracks) { // TODO: use signals list
			if (!(trk instanceof Signal)) {
				continue;
			}
			Signal signal = (Signal) trk;
			if (signal.linkedTo(track)
					&& signal.getDirectionFrom(signal._direction) == dir)
				return signal;
		}
		return null;
	}

	public Signal findSignalLinkedTo(TrackAndDirection td) {
		return findSignalLinkedTo(td._track, td._direction);
	}

	public TextTrack findTextLinkedTo(Track track, Direction dir) {
		for (Track trk : _tracks) { // TODO: use textTrack list
			if (!(trk instanceof TextTrack)) {
				continue;
			}
			TextTrack txt = (TextTrack) trk;
			if (dir == Direction.W && txt.linkedToEast(track))
				return txt;
			if (dir == Direction.E && txt.linkedToWest(track))
				return txt;
		}
		return null;
	}

	public Track findTrackLinkedTo(Track track, Direction dir) {
		switch(dir) {
		case E:
		case NE:
		case SE:
		case S:
			return findTrack(track._elink);
		case W:
		case NW:
		case SW:
		case N:
			return findTrack(track._wlink);
		}
		return null;
	}

	public TextTrack findTextTrack(String name) {
		for (Track track : _tracks) { // TODO: use textTrack list
			if (!(track instanceof TextTrack))
				continue;
			if (track._station != null && track._station.compareTo(name) == 0) {
				return (TextTrack) track;
			}
		}
		return null;
	}

	public Track findStation(String name) {
		for (Track track : _tracks) {
			if ((track instanceof TextTrack))
				continue;
			if (track._station != null && track._station.compareTo(name) == 0) {
				return track;
			}
		}
		return null;
	}

	// Entering from a text entry point
	// We need to infer the direction based on the
	// relative positions of the entry text and
	// the linked entry track
	public Direction findEntryDirection(TextTrack text, Track track) {
		TDPosition ptext = text._position;
		TDPosition ptrack = track._position;
		switch (track._direction) {
		case E_W:
		case W_E:
			return (ptext._x < ptrack._x) ? Direction.E : Direction.W;

		case NE_S:
			break;
		case NW_E:
			break;
		case NW_S:
			break;
		case NW_SE:
			break;
		case N_NE_S_SW:
			break;
		case N_NW_S_SE:
			break;
		case N_S_E:
			break;
		case N_S_W:
			break;
		case SE_N:
			break;
		case SW_E:
			break;
		case SW_N:
			break;
		case SW_NE:
			break;

		case N_S:
		case S_N:
		case TRK_N_S:
			return (ptext._y < ptrack._y) ? Direction.S : Direction.N;

		case W_NE:
			break;
		case W_SE:
			break;
		case XH_NW_SE:
			break;
		case XH_SW_NE:
			break;
		case X_PLUS:
			break;
		case X_X:
			break;
		case _FILLER:
			break;
		case signal_NORTH_FLEETED:
			break;
		case signal_SOUTH_FLEETED:
			break;
		default:
			break;
		}
		return null;
	}

	public void loadSignalAspects(ScriptFactory scriptFactory) {
		// TODO: to optimize signals with same aspects
		// _signalAspects = new HashMap<String, SignalAspect>();
		for (Track track : _tracks) {
			if (!(track instanceof Signal))
				continue;
			Signal signal = (Signal) track;
			if (signal._scriptFile == null || signal._scriptFile.isEmpty())
				continue;
			signal._script = scriptFactory.createInstance(signal._scriptFile);
			if (signal._script instanceof TDSScript) {
				TDSScript tdsScript = (TDSScript) signal._script;
				tdsScript.parse();
				// override default aspects with the one from the .tds file
				signal._aspects = tdsScript.getAspects();
			}
		}
		for (Track track : _tracks) {
			track.onInit();
		}
	}

	public void addPath(EntryExitPath path) {
		_paths.add(path);
	}

	public void add(Itinerary itin) {
		_itineraries.add(itin);
	}

	public Itinerary findItinerary(String name) {
		for (Itinerary itin : _itineraries) {
			if (itin._name.compareTo(name) == 0)
				return itin;
		}
		return null;
	}

	public void clearVisitedItineraries() {
		for (Itinerary itin : _itineraries)
			itin._visited = false;
	}

	public Switch findSwitch(TDPosition pos) {
		for (Track track : _tracks) {
			if (track instanceof Switch && track._position.sameAs(pos))
				return (Switch) track;
		}
		return null;
	}

	public Signal findSignalNamed(String name) {
		if (name.charAt(0) == '(') {
			TDPosition pos = new TDPosition(name);
			for (Track track : _tracks) {
				if ((track instanceof Signal) && track._position.sameAs(pos))
					return (Signal) track;
			}
			return null;
		}
		for (Track track : _tracks) {
			if ((track instanceof Signal) && track._station != null
					&& track._station.compareTo(name) == 0)
				return (Signal) track;
		}
		return null;
	}

	public void removeAllElements() {
		_tracks.clear();
		_itineraries.clear();
		_paths.clear();
	}

	public static boolean sameStation(String st1, String st2) {
		int i = 0;
		char ch = ' ';
		while (i < st1.length() && (ch = st1.charAt(i)) != PLATFORM_SEP
				&& i < st2.length() && ch == st2.charAt(i))
			++i;
		if (i >= st1.length() || ch == PLATFORM_SEP) { // end of first station name
			if (i >= st2.length() || st2.charAt(i) == PLATFORM_SEP) // and end of second station name
				return true;
		}
		return false;
	}

	public Track findStationNamed(String name) {
		int index = name.indexOf(PLATFORM_SEP);
		String station = name;
		if (index >= 0) {
			station = name.substring(0, index);
		}
		for (Track track : _tracks) {
			if (!track._isStation)
				continue;
			if (track instanceof TextTrack) {
				if (track._station.equals(station)
						&& (!track._wlink.isNull() || !track._elink.isNull())) {
					return track;
				}
			}
			String s1 = track._station;
			index = s1.indexOf(PLATFORM_SEP);
			if (index >= 0)
				s1 = s1.substring(0, index);
			if (station.equals(s1))
				return track;
		}
		return null;
	}

	public EntryExitPath findPath(String from, String to) {
		for (EntryExitPath path : _paths) {
			if (sameStation(path._from, from) && sameStation(path._to, to))
				return path;
		}
		return null;
	}

	public Signal findSignal(int x, int y) {
		Track track = findTrack(new TDPosition(x, y));
		if (track != null && track instanceof Signal)
			return (Signal) track;
		return null;
	}

	public TextTrack findTextTrack(TDPosition pos) {
		Track track = findTrack(pos);
		if (track != null && track instanceof TextTrack)
			return (TextTrack) track;
		return null;
	}

	public void removeTrains() {
		Simulator simulator = Simulator.INSTANCE;
		Territory territory = simulator._territory;
		for (Train train : simulator._schedule._trains) {
			train.reset();
		}
	}

	public static void checkPlatform(String s1, String s2) {
		int i1;
		for (i1 = 0; i1 < s1.length() && i1 < s2.length() && s1.charAt(i1) == s2.charAt(i1); ++i1);
		if (i1 >= s1.length()) {
			if (i1 >= s2.length() || s2.charAt(i1) == PLATFORM_SEP)
				return;
		} else if (i1 >= s2.length()) {
			if (s1.charAt(i1) == PLATFORM_SEP)
				return;
		}
		++Simulator.INSTANCE._performanceCounters.wrong_platform;
	}

	public void doTriggers(Train train) {
		
		int rnd = Simulator._random.nextInt(100);
		if (train == null || train._position == null)
			return;
		for (Track track : _tracks) {
			if (!(track instanceof TriggerTrack) || track._station == null || track._station.isEmpty())
				continue;
			if (track._wlink == null)
				continue;
			Direction dir = null;
			switch (track._direction) {
			case E_W:
				dir = Direction.W;
				break;
			case W_E:
				dir = Direction.E;
				break;
			case N_S:
				dir = Direction.S;
				break;
			case S_N:
				dir = Direction.N;
				break;
			}
			if (dir == null)	// TODO: is this possible at all?
				continue;
			if (dir != train._direction)
				continue;
			if (!track._wlink.sameAs(train._position._position))
				continue;
			TriggerTrack trigger = (TriggerTrack) track;
			trigger.DoCrossed(train);
			
		    //
		    //	check to see if this trigger applies to a list of
		    //	specific trains.
		    //	The list starts with "{" and each train name is
		    //	separated from the next by a ',' character.
		    //	The list is terminated by "}".
		    //

			boolean found = false;
			int prob;
			StringBuilder sb = new StringBuilder();
			String s = trigger._station;
			int i = s.indexOf('{');
			if (i >= 0) {
				found = false;
				do {
					while((++i < s.length()) && s.charAt(i) == ' ');
					for (prob = 0; i < s.length() && s.charAt(i) != '}' && s.charAt(i) != ','; ++i)
						sb.append(s.charAt(i));
					if (train._name.equals(sb)) {
						found = true;
						break;
					}
				} while (i < s.length() && s.charAt(i) != '}');
				if (!found)
					continue;
			}
			
			prob = trigger._speed[train._type];
			if (prob == 0)
				prob = 100;

		    //
		    //	rnd < prob means:
		    //	    prob = 1, rnd almost never <
		    //	    prob = 99, rnd almost always <
		    //
			
			if (rnd >= prob)
				continue;
			
			sb = new StringBuilder();
			for (prob = 0; prob < s.length(); ++prob) {
				switch(s.charAt(prob)) {
				case '{':
					// skip conditional train sequence
					while(prob < s.length() && s.charAt(prob) != '}')
						++prob;
					if (prob < s.length())
						++prob;
					continue;
					
				case '@':
					sb.append(train._name);
					break;
					
				case ';':
					DoCommand dcmd = new DoCommand(sb.toString());
					dcmd.handle();
					while(prob < s.length() && s.charAt(prob) == ' ')
						++prob;
					continue;
				}
				sb.append(s.charAt(prob));
			}
			String cmd = sb.toString();
			if (cmd.length() > 0) {
				if (cmd.startsWith("script")) {
					cmd = cmd.substring(6).trim();
					if (cmd.isEmpty())
						continue;
					trigger.doScript(cmd, train);
				} else {
					DoCommand dcmd = new DoCommand(cmd);
					dcmd.handle();
				}
			}
		}
		/*

		Track	*trk;
		int	prob;
		int	i;
		int	rnd = rand() % 100;

		for(trk = layout; trk; trk = trk->next) {
		    prob = trk->speed[t->type];
		    if(!prob)
				prob = 100;

		    if(rnd < prob) {
				for(prob = rnd = 0; trk->station[prob]; ++prob) {
				    switch(trk->station[prob]) {
				    case '{':
						// skip conditional train sequence
						while(trk->station[prob] && trk->station[prob] != '}')
						    ++prob;
						if(trk->station[prob]) ++prob;
						continue;
	
				    case '@':
						wxStrcpy(buff + rnd, t->name);
						rnd += wxStrlen(t->name);
						continue;
	
				    case ';':
						buff[rnd] = 0;
						trainsim_cmd(buff);
						while(trk->station[++prob] == ' ');
						--prob;
						rnd = 0;
						continue;
				    }
				    buff[rnd++] = trk->station[prob];
				}
				buff[rnd] = 0;
				if(rnd) {
				    if(!wxStrncmp(buff, wxT("script"), 6)) {
						for(rnd = 6; buff[rnd] == ' ' || buff[rnd] == '\t'; ++rnd);
						if(!rnd)
						    continue;
						trk->RunScript(buff + rnd, t);
				    } else
						trainsim_cmd(buff);
				}
		    }
		}
		 */
	}

}
