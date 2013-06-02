package com.traindirector.files;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.traindirector.model.TDIcon;
import com.traindirector.model.Track;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStatus;
import com.traindirector.model.TrainStop;
import com.traindirector.scripts.Script;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDDelay;

public class SchFile extends TextFile {

	public Simulator _simulator;

	private int _time;

	private TDIcon[] westIcon;
	private TDIcon[] eastIcon;
	private TDIcon[] westCarIcon;
	private TDIcon[] eastCarIcon;

	public static char DELAY_CHAR = '!';
	
	public SchFile(Simulator simulator) {
		_simulator = simulator;
		westIcon = new TDIcon[Track.NSPEEDS];
		eastIcon = new TDIcon[Track.NSPEEDS];
		westCarIcon = new TDIcon[Track.NSPEEDS];
		eastCarIcon = new TDIcon[Track.NSPEEDS];
	}

	public void readFile(String fname) {
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(fname));
			readFile(input);
			input.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readFile(BufferedReader input) {
		String	line;
		TrainStop lastStop = null;
		int currentType = 0;
		try {
			Train	train = null;
			while((line = input.readLine()) != null) {
				int i;
				char ch;
				if(line.length() > 0 && line.charAt(0) == '.') {
					train = null;
					continue;
				}
				line = line.replace("\t", " ");
				i = skipBlanks(line, 0);
				if(i < 0 || line.charAt(i) == '#')
					continue;
				if(line.startsWith("Include:", i)) {
					i = skipBlanks(line, i + 9);
					if(i < 0)
						continue;
					readFile(line.substring(i));
					continue;
				}
				if(line.startsWith("Routes:", i)) {
					// TODO: GTFS
					continue;
				}
				if(line.startsWith("GTFS:", i)) {
					// TODO: GTFS
					continue;
				}
				if(line.startsWith("Cancel:", i)) {
					i = skipBlanks(line, i + 7);
					if(i < 0)
						continue;
					cancelTrain(line.substring(i));
					continue;
					
				}
				if(line.startsWith("Today:", i)) {
					i = skipBlanks(line, 6);
					if(i < 0)
						continue;
					_simulator._runDay = parseSafeInteger(line, i);
					if(_simulator._runDay > 0)
						_simulator._runDay = 1 << (_simulator._runDay - 1);
					else
						_simulator._runDay = 0;
					continue;
				}
				if(line.startsWith("Start:", i)) {
					i = skipBlanks(line, i + 6);
					if (i < 0)
						continue;
					_simulator._schedule._startTime = parseTime(line, i);
					_simulator._simulatedTime = _simulator._schedule._startTime;
					continue;
				}
				if(line.startsWith("Train:", i)) {
					i = skipBlanks(line, 6);
					train = new Train(line.substring(i));
					train._status = TrainStatus.READY;
					train._type = currentType;
					_simulator._schedule.add(train);
					continue;
				}
				if(train == null) {
					if(!line.startsWith("Type:", i))
						continue;
					i = skipBlanks(line, 5);
					i = scanInteger(line, i);
					if(--_intValue >= Track.NSPEEDS || _intValue < 0)
						_intValue = 0;
					currentType = _intValue;
					if(i < 0) continue;
					i = scanString(line, i);
					if(i < 0) continue;
					westIcon[currentType] = createIcon(_stringValue, currentType, "wtrain");
					i = scanString(line, i);
					if(i < 0) continue;
					eastIcon[currentType] = createIcon(_stringValue, currentType, "etrain");
					i = scanString(line, i);
					if(i < 0) continue;
					westCarIcon[currentType] = createIcon(_stringValue, currentType, "car");
					eastCarIcon[currentType] = westCarIcon[currentType];
					i = scanString(line, i);
					if(i < 0) continue;
					if(!_stringValue.isEmpty())
						eastCarIcon[currentType] = createIcon(_stringValue, currentType, "car");
					continue;
				}
				if(line.startsWith("Wait:", i)) {
					i = skipBlanks(line, i + 5);
					if(i < 0)
						continue;
					i = scanString(line, i);
					train._waitFor = _stringValue;
					_intValue = 0;
					if(i > 0) 
						i = scanInteger(line, i);
					train._waitTime = _intValue > 0 ? _intValue : 60;
					continue;
				}
				if(line.startsWith("When:", i)) {
					i = skipBlanks(line, i + 5);
					if(i < 0)
						continue;
					train._days = 0;
					while(i < line.length()) {
						ch = line.charAt(i++);
						if(ch >= '0' && ch <= '9')
							train._days |= 1 << (ch - '1');
					}
					continue;
				}
				if(line.startsWith("Speed:", i)) {
					i = skipBlanks(line, 6);
					if (i < 0)
						continue;
					train._maxspeed = parseSafeInteger(line, i);
					continue;
				}
				if(line.startsWith("Type:", i)) {
					i = skipBlanks(line, 5);
					i = scanInteger(line, i);
					if(--_intValue >= Track.NSPEEDS || _intValue < 0)
						_intValue = 0;
					train._type = _intValue;
					train._westIcon = westIcon[train._type];
					train._eastIcon = eastIcon[train._type];
					train._westCarIcon = westCarIcon[train._type];
					train._eastCarIcon = eastCarIcon[train._type];
					if(i < 0) continue;
					i = scanString(line, i);
					if(!_stringValue.isEmpty())
						train._westIcon = createIcon(_stringValue, train._type, "wtrain");
					if(i < 0) continue;
					i = scanString(line, i);
					if(!_stringValue.isEmpty())
						train._eastIcon = createIcon(_stringValue, train._type, "etrain");
					continue;
				}
				if(line.startsWith("Stock:", i)) {
					i = skipBlanks(line, 6);
					if(i < 0)
						continue;
					train._stock = line.substring(i);
					continue;
				}
				if(line.startsWith("Length:", i)) {
					i = skipBlanks(line, 7);
					if(i < 0)
						continue;
					i = scanInteger(line, i);
					train._entryLength = train._length;
					train._length = _intValue;
					if(_intValue <= 0)
						continue;
					if(i < 0) continue;
					i = scanString(line, i);
					if(!_stringValue.isEmpty()) {
						train._eastCarIcon = createIcon(_stringValue, train._type, "car");
						train._westCarIcon = train._eastCarIcon;
					}
					i = scanString(line, i);
					if(!_stringValue.isEmpty())
						train._westCarIcon = createIcon(_stringValue, train._type, "car");
					continue;
				}
				if(line.startsWith("Enter:", i)) {
					i = parseTime(line, i + 6);
					train._timeIn = _time;
					if(i >= line.length() && line.charAt(i) == DELAY_CHAR) {
						train._entryDelay = new TDDelay();
						i = parseDelay(line, i + 1, train._entryDelay);
					}
					i = skipBlanks(line, i);
					if(i < line.length() && line.charAt(i) == ',')
						++i;
					i = skipBlanks(line, i);
					train._entrance = line.substring(i);
					continue;
				}
				if(line.startsWith("Notes:", i)) {
					i = skipBlanks(line, 6);
					if(i < 0)
						continue;
					train._notes.add(line.substring(i));
					continue;
				}
				if(line.startsWith("Script:", i)) {
					StringBuilder sb = new StringBuilder();
					while((line = input.readLine()) != null) {
						if(line.isEmpty() || line.charAt(0) == '#')
							continue;
						if(line.startsWith("EndScript"))
							break;
						sb.append(line);
						sb.append('\n');
					}
					train._script = new Script(null);
					train._script._body = sb.toString();
					continue;
				}
				
				// else it's a stop

				int arrival, departure;
				TrainStop stop = new TrainStop();
				stop._minstop = 30;
				ch = line.charAt(i);
				if(ch == '-') {	// doesn't stop
					i = skipBlanks(line,  i + 1);
					stop._minstop = 0;
					arrival = 0;
				} else if(ch == '+') {
					arrival = train._timeIn;
					i = scanInteger(line, i + 1);
					if(lastStop != null)
						arrival = lastStop._departure;
					arrival += _intValue;
				} else {
					i = parseTime(line, i);
					arrival = _time;
				}
				if(i < 0)
					continue;
				if(line.charAt(i) == '+') {
					i = scanInteger(line, i + i);
					stop._minstop = _intValue;
				}
				stop._arrival = arrival;
				if(i < 0)
					continue;
				if(line.charAt(i) == ',')
					++i;
				i = skipBlanks(line, i);
				if(i < 0)
					continue;
				ch = line.charAt(i);
				if(ch == '-') {		// this is the exit point
					if(train._exit != null)
						continue;
					i = skipBlanks(line, i + 1);
					if(i < 0)
						continue;
					if(line.charAt(i) == ',')
						++i;
					i = skipBlanks(line, i);
					if(i < 0)
						continue;
					train._exit = line.substring(i);
					train._timeOut = arrival;
					continue;
				}
				if(ch == '+') {
					departure = 0;					// TODO: check if we should use departure = arrival
					i = scanInteger(line, i + 1);
					stop._departure = _intValue;
					if(stop._departure < stop._minstop)
						stop._departure = stop._minstop;
					if(stop._minstop == 0) {
						if(lastStop == null)
							departure = train._timeIn;
						else
							departure = lastStop._departure;
					}
					stop._departure += departure;
				} else {
					i = parseTime(line, i);
					stop._departure = _time;
				}
				if(stop._minstop == 0) {
					stop._arrival = stop._departure;
				} else {
					stop._arrival = arrival;
					if(stop._departure == stop._arrival) {
						stop._departure = stop._arrival + stop._minstop;
					} else if(stop._minstop > stop._departure - stop._arrival)
						stop._minstop = stop._departure - stop._arrival;
				}
				if(i >= line.length())
					continue;
				ch = line.charAt(i);
				if(ch == DELAY_CHAR) {
					stop._depDelay = new TDDelay();
					i = parseDelay(line, i, stop._depDelay);
					if(i >= line.length())
						continue;
					ch = line.charAt(i);
				}
				if(ch == ',')
					++i;
				i = skipBlanks(line, i);
				if(i >= line.length())
					continue;
				stop._station = line.substring(i);
				train._stops.add(stop);
				lastStop = stop;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// if schedule file didn't specify train icons, use the defaults
		for(Train train : _simulator._schedule._trains) {
			if(train._westIcon == null)
				train._westIcon = _simulator._iconFactory.get(":icons:wtrain" + (train._type + 1) + ".xpm");
			if(train._eastIcon == null)
				train._eastIcon = _simulator._iconFactory.get(":icons:etrain" + (train._type + 1) + ".xpm");
			if(train._length > 0) {
				if(train._westCarIcon == null)
					train._westCarIcon = _simulator._iconFactory.get(":icons:car" + (train._type + 1) + ".xpm");
				if(train._eastCarIcon == null)
					train._eastCarIcon = _simulator._iconFactory.get(":icons:car" + (train._type + 1) + ".xpm");
			}
		}
	}
	
	private TDIcon createIcon(String iconFile, int type, String defaultName) {
		TDIcon icon = null;
		
		if(!iconFile.isEmpty()) {
			icon = _simulator._iconFactory.get(iconFile);
			if(icon == null) {
				// TODO: alert train._name invalid icon westIconFile
			}
		}
		return icon;
	}

	private void cancelTrain(String substring) {
		// TODO GTFS
	}

	public int parseTime(String input, int offset) {
		char ch = ' ';
		int hh = 0, mm = 0, ss = 0;
		while(offset < input.length()) {
			ch = input.charAt(offset);
			if(ch != ' ')
				break;
			++offset;
		}
		while(offset < input.length()) {
			ch = input.charAt(offset);
			if(ch >= '0' && ch <= '9')
				hh = hh * 10 + (ch - '0');
			else
				break;
			++offset;
		}
		if(ch == ':')
			++offset;
		while(offset < input.length()) {
			ch = input.charAt(offset);
			if(ch >= '0' && ch <= '9')
				mm = mm * 10 + (ch - '0');
			else
				break;
			++offset;
		}
		if(ch == ':')
			++offset;
		while(offset < input.length()) {
			ch = input.charAt(offset);
			if(ch >= '0' && ch <= '9')
				ss = ss * 10 + (ch - '0');
			else
				break;
			++offset;
		}
		_time = hh * 3600 + mm * 60 + ss;
		return offset;
	}
	
	public int parseDelay(String line, int offset, TDDelay delay) {
		delay._nDelays = 0;
		do {
			offset = scanInteger(line, offset);
			if(offset >= line.length())
				return offset;
			int secs = _intValue;
			char ch = line.charAt(offset);
			if(ch == '/')
				++offset;
			offset = scanInteger(line, offset);
			int prob = _intValue;
			if(delay._nDelays < TDDelay.MAX_DELAY) {
				delay._prob[delay._nDelays] = prob;
				delay._seconds[delay._nDelays] = secs;
				++delay._nDelays;
			}
		} while(offset < line.length() && line.charAt(offset++) == ',');
		return skipBlanks(line, offset);
	}

	public int parseSafeInteger(String line, int offset) {
		try {
			return Integer.parseInt(line.substring(offset));
		} catch (Exception e) {
			return 0;
		}
	}
}
