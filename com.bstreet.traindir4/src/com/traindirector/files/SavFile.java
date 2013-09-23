package com.traindirector.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.framework.adaptor.FilePath;

import com.traindirector.commands.LoadCommand;
import com.traindirector.model.Direction;
import com.traindirector.model.PerformanceCounters;
import com.traindirector.model.Schedule;
import com.traindirector.model.Signal;
import com.traindirector.model.Switch;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.Track;
import com.traindirector.model.TrackPath;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStatus;
import com.traindirector.model.TrainStop;
import com.traindirector.simulator.PathFinder;
import com.traindirector.simulator.Simulator;
import com.traindirector.simulator.TDDelay;

public class SavFile extends TextFile {

	Simulator _simulator;
	Territory _territory;
	String	trkFileName;
	
	static final String eol = "\n";

	public SavFile() {
		
	}

	public SavFile(Simulator simulator, Territory territory, String trkFileName) {
		super();
		this._simulator = simulator;
		this._territory = territory;
		this.trkFileName = trkFileName;
		setFileName(trkFileName, "sav");
	}

	public boolean load() {
		Schedule schedule = _simulator._schedule;
		String line;
		String[] cols;
		int[] values;
		BufferedReader in = null;

		_simulator.clearDelays();
		try {
			in = new BufferedReader(new FileReader(new File(_fileName)));
			
			line = in.readLine();
			if(line.startsWith("Layout:")) {
				return loadNewFormat(in, line);
			}
			// check for relative path
			File file = new File(line);
			if (!file.canRead()) {
				FilePath path = new FilePath(line);
				String[] segs = path.getSegments();
				file = new File(segs[segs.length - 1], "");
				if (!file.canRead()) {
					// TODO: show alert - file not found
					return false;
				}
			}
			String trkFileName = file.getPath();
			trkFileName = trkFileName.replace(".TRK", ".trk");
			trkFileName = trkFileName.replace(".ZIP", ".zip");
			LoadCommand loadCmd = new LoadCommand(trkFileName);
			loadCmd.handle();
			
			// 2nd line
			line = in.readLine();
			values = readIntArray(line, 10);
			_simulator._currentTimeMultiplier = values[0];
			schedule._startTime = values[1];
			_simulator._options._showSpeeds.set(values[2] != 0);
			_simulator._options._showBlocks.set(values[3] != 0);
			_simulator._options._beepOnAlert.set(values[4] != 0);
			_simulator._runPoints = values[5];
			_simulator._totalDelay = values[6];
			_simulator._totalLate = values[7];
			_simulator._timeMult = values[8];
			_simulator._simulatedTime = values[9];
			
			// reload the state of all switches
			
			while((line = in.readLine()) != null && line.length() > 0) {
				values = readIntArray(line, 3);
				Switch sw = _territory.findSwitch(new TDPosition(values[0], values[1]));
				if (sw == null)
					continue;
				sw._switched = values[2] != 0;
				if (sw._switched) {
					// TODO: change screen at sw
				}
			}
			
			// reload state of all signals
			
			while((line = in.readLine()) != null && line.length() > 0) {
				cols = line.split(",");
				int x = Integer.parseInt(cols[0]);
				int y = Integer.parseInt(cols[1]);
				Signal signal = _territory.findSignal(x, y);
				if (signal == null)
					continue;
				int status = Integer.parseInt(cols[2]);
				signal._status = status == 1 ? TrackStatus.BUSY : TrackStatus.FREE;
				signal.setFleeted(Integer.parseInt(cols[3]) != 0);
				if (cols.length > 4)
					signal.setAspectFromName(cols[4]);
				if (!signal.isApproach() && signal.isClear())
					signal.unlock();
				// TODO: change screen at signal
			}
			
			// reload state of all trains
			
			while((line = in.readLine()) != null && line.length() > 0) {
				if(line.charAt(0) == '.')
					break;
				Train train = schedule.findTrainNamed(line);
				if (train == null) {
					/* the train could not be found in the schedule.
					 * Warn the user, and ignore all lines up to the
					 * next empty line.
					 */
					while((line = in.readLine()) != null && line.length() > 0)
						if (line.charAt(0) == '.')
							break;
					continue;
				}
				
				// 2nd line
				
				line = in.readLine().trim();
				cols = line.split(",");
				train._status = TrainStatus.fromInteger(Integer.parseInt(cols[0]));
				train._direction = Direction.fromInteger(Integer.parseInt(cols[1]));
				if (cols.length > 2)
					train._exited = cols[3];
				
				// 3rd line
				
				line = in.readLine().trim();
				values = readIntArray(line, 10);
				train._timeExited = values[0];
				train._wrongDest = values[1] != 0;
				train._speed = values[2];
				train._maxspeed = values[3];
				train._curmaxspeed = values[4];
				train._trackpos = values[5];
				train._timeLate = values[6];
				train._timeDelay = values[7];
				train._timeRed = values[8];
				if (train._entryDelay != null) {// TODO: create new _entryDelay
					train._entryDelay._nSeconds = values[9];
				}
				
				// 4th line
				
				line = in.readLine().trim();
				values = readIntArray(line, 5);
				train._timeDep = values[0];
				// train._pathpos = values[1];	// backward compatibility
				train._pathtravelled = values[2];
				train._distanceToStop = values[3];
				if (values.length > 4)
					train._shunting = values[4] != 0;
				
				// 5th line
				
				line = in.readLine().trim();
				values = readIntArray(line, 6);
				TDPosition pos = new TDPosition(values[0], values[1]);
				train._stopPoint = _territory.findTrack(pos);
				if (train._stopPoint == null)
					train._stopPoint = _territory.findSwitch(pos);
				train._distanceToSlow = values[2];
				pos = new TDPosition(values[3], values[4]);
				train._slowPoint = _territory.findTrack(pos);
				if (train._slowPoint == null)
					train._slowPoint = _territory.findSwitch(pos);
				if (values.length > 5)
					train._needFindStop = values[5] != 0;
				
				// 6th line
				
				line = in.readLine().trim();
				cols = line.split(",");
				for (int i = 0; i < cols.length; i += 2) {
					int x = Integer.parseInt(cols[i]);
					int y = Integer.parseInt(cols[i + 1]);
					if (x == 0 || y == 0)
						continue;
					if (train._fleet == null)
						train._fleet = new ArrayList<Track>();
					train._fleet.add(_territory.findSignal(x, y));
				}
				
				// 7th line
				boolean notOnTrack = false;
				line = in.readLine().trim();
				values = readIntArray(line, 5);
				pos = new TDPosition(values[0], values[1]);
				Track track = _territory.findTrack(pos);
				if (track == null)
					track = _territory.findSwitch(pos);
				if (track == null) {
					switch (train._status) {
					case READY:
					case ARRIVED:
					case DERAILED:
					case DELAYED:
						break;
					case WAITING:
					case STOPPED:
					case RUNNING:
						notOnTrack = true;
					}
				}
				train._position = track;
				train._waitTime = values[2];
				train._flags = values[3];
				train._arrived = values[4];
				
				// 8th line
				line = in.readLine().trim();
				cols = line.split(",");
				train._oldStatus = TrainStatus.fromInteger(Integer.parseInt(cols[0]));
				if (cols.length > 1)
					train._outOf = _territory.findStation(cols[1]);

				// 9th line - stations we stopped at
				
				while((line = in.readLine()) != null) {
					line = line.trim();
					if (line.startsWith(":startDelay ")) {
						train._startDelay = Integer.parseInt(line.substring(12));
						continue;
					}
					if (line.length() < 1 || line.charAt(0) == '.')
						break;
					cols = line.split(",");
					if (cols.length < 2)
						continue;
					TrainStop foundStop = null;
					for (TrainStop stop : train._stops) {
						if (stop._station.equals(cols[0])) {
							foundStop = stop;
							break;
						}
					}
					if (foundStop == null)
						continue;
					foundStop._stopped = Integer.parseInt(cols[1]) != 0;
					foundStop._delay = Integer.parseInt(cols[2]);
					if (cols.length > 3) {
						if (foundStop._depDelay != null) {
							foundStop._depDelay._nSeconds = Integer.parseInt(cols[3]);
						}
					}
				}
			
				if (line == null)
					break;
				
				if (line.charAt(0) == '.') {	// tail info is present
					line = in.readLine().trim();
					if (line.length() < 1 || line.charAt(0) == '.')
						break;
					Train tail = train._tail;
					if (tail == null) {	// maybe Length: was removed in the .sch file
						train._tail = tail = new Train(null);
						// tail._ecarpix = tail._wcarpix = -1;	// TODO
					}
					if (!line.isEmpty())
						train._stopping = _territory.findStationNamed(line);
					
					line = in.readLine().trim();
					while(line.length() > 0 && line.charAt(0) == '=') {
						if (line.startsWith("=length")) {
							train._length = Integer.parseInt(line.substring(7).trim());
						} else if(line.startsWith("=icons")) {
							cols = line.substring(6).trim().split(",");
							//train._ecarpix = Integer.parseInt(cols[0]);	// TODO
							//train._wcarpix = Integer.parseInt(cols[1]);	// TODO
						}
						line = in.readLine().trim();
					}
					
					if (line.length() > 0 && line.charAt(0) == '!') {	// list of fleeted signals
						cols = line.substring(1).split(",");
						for (int i = 0; i < cols.length; i += 2) {
							int x = Integer.parseInt(cols[i]);
							int y = Integer.parseInt(cols[i + 1]);
							if (x != 0 && y != 0) {
								if (tail._fleet == null)
									tail._fleet = new ArrayList<Track>();
								tail._fleet.add(_territory.findSignal(x, y));
							}
						}
						line = in.readLine().trim();
					}
					
					cols = line.split(",");
					// tail._pathpos = Integer.parseInt(cols[0]); // backward compatibility
					tail._trackpos = Integer.parseInt(cols[1]);
					tail._tailEntry = Integer.parseInt(cols[2]);
					tail._tailExit = Integer.parseInt(cols[3]);
					for (int i = 4; i < cols.length; ++i) {
						int x = Integer.parseInt(cols[i++]);
						int y = Integer.parseInt(cols[i++]);
						if (tail._path == null)
							tail._path = new TrackPath();
						pos = new TDPosition(x, y);
						track = _territory.findTrack(pos);
						if (track == null) {
							track = _territory.findSwitch(pos);
							if (track == null)
								track = _territory.findTextTrack(pos);
						}
						if (track == null) {	// maybe layout changed?
							tail._path = null;	// disable length of this train
							train._tail = null;
							train._length = 0;
							break;
						}
						Direction dir = Direction.fromInteger(Integer.parseInt(cols[i]));
						tail._path.add(track, dir);
					}
					
					if (train._status == TrainStatus.DELAYED && tail != null && tail._path != null)
						tail._path = null;
				} // end tail info

				if (notOnTrack) {
					// alert ("train is not on track!");
					// train._status = TrainStatus.DERAILED;
				}

			} // end while trains


			// other attribute types
			
			while((line = in.readLine()) != null && !line.isEmpty() && line.charAt(0) == '(') {
				if (line.startsWith("(white tracks")) {
					while((line = in.readLine()) != null && !line.isEmpty() && line.charAt(0) != ')') {
						values = readIntArray(line,  2);
						TDPosition pos = new TDPosition(values[0], values[1]);
						Track track = _territory.findTrack(pos);
						if (track == null)
							track = _territory.findSwitch(pos);
						if (track != null) {
							track._status = TrackStatus.BUSYSHUNTING;
							// TODO: change screen coords (track)
						}
					}
					continue;
				}
				if (line.startsWith("(stranded")) {
					while((line = in.readLine()) != null && !line.isEmpty() && line.charAt(0) != ')') {
						Train train = new Train(null);
						schedule._stranded.add(train);
						cols = line.split(",");
						train._type = Integer.parseInt(cols[0]);
						int x = Integer.parseInt(cols[1]);
						int y = Integer.parseInt(cols[2]);
						TDPosition pos = new TDPosition(x, y);
						Track track = _territory.findTrack(pos);
						if (track == null)
							track = _territory.findSwitch(pos);
						train._position = track;
						train._flags = Train.STRANDED;
						train._status = TrainStatus.ARRIVED;
						train._direction = Direction.fromInteger(Integer.parseInt(cols[3]));
						//train._ecarpix = Integer.parseInt(cols[4]);	// TODO
						//train._wcarpix = Integer.parseInt(cols[5]);	// TODO
						train._maxspeed = Integer.parseInt(cols[6]);
						train._curmaxspeed = Integer.parseInt(cols[7]);
						train._length = Integer.parseInt(cols[8]);
						if (train._length > 0) {
							int tailLength, l, f;
							int pathLength = 0;
							
							tailLength = Integer.parseInt(cols[9]);
							if (tailLength > 0) {
								line = in.readLine();
								if (line == null)
									break;
								cols = line.trim().split(",");
								train._tail = new Train(null);
								train._tail._path = new TrackPath();
								for (l = 0; l < tailLength * 3; l += 3) {
									x = Integer.parseInt(cols[l]);
									y = Integer.parseInt(cols[l + 1]);
									pos = new TDPosition(x, y);
									track = _territory.findTrack(pos);
									if (track == null)
										track = _territory.findSwitch(pos);
									if (track == null)
										break;
									track._status = TrackStatus.OCCUPIED;
									Direction dir = Direction.fromInteger(Integer.parseInt(cols[l + 2]));
									train._tail._path.add(track, dir);
									pathLength += track._length;
								}
								train._tail._length = pathLength;
								train._tail._position = train._tail._path.getTrackAt(0);
							}
							// train._position = train._path.getTrackAt(0);
						}
					} // end while stranded lines
					continue;
				}
				if (line.startsWith("(late minutes")) {
					int x = 0;
					while((line = in.readLine()) != null && !line.isEmpty() && line.charAt(0) != ')') {
						cols = line.split(",");
						for (int i = 0; i < cols.length; ++i) {
							int m = Integer.parseInt(cols[i]);
							_simulator._lateData[x % (24 * 60)] = m;
							++x;
						}
					}
					continue;
				}
			}

			if (line != null) {
				values = readIntArray(line, 5);
				_simulator._runDay = values[0];
				_simulator._options._terseStatus.set(values[1] != 0);
				_simulator._options._statusOnTop.set(values[2] != 0);
				_simulator._options._showSeconds.set(values[3] != 0);
				_simulator._options._traditionalSignals.set(values[4] != 0);
			}
			
			line = in.readLine();
			if (line != null) {
				values = readIntArray(line, 2);
				_simulator._options._autoLink.set(values[0] != 0);
				_simulator._options._showGrid.set(values[1] != 0);
			}
			
			line = in.readLine();
			if (line != null) {
				values = readIntArray(line, 12);
				PerformanceCounters cnt = _simulator._performanceCounters;
				cnt.wrong_dest = values[0];
				cnt.late_trains = values[1];
				cnt.thrown_switch = values[2];
				cnt.cleared_signal = values[3];
				cnt.denied = values[4];
				cnt.turned_train = values[5];
				cnt.waiting_train = values[6];
				cnt.wrong_platform = values[7];
				cnt.ntrains_late = values[8];
				cnt.ntrains_wrong = values[9];
				cnt.nmissed_stops = values[10];
				cnt.wrong_assign = values[11];
			}
			
			_simulator._options._hardCounters.set(readIntegerLine(in) != 0);
			_simulator._options._showCanceled = readIntegerLine(in) != 0;
			_simulator._options._showLinks.set(readIntegerLine(in) != 0);
			_simulator._options._beepOnEnter.set(readIntegerLine(in) != 0);
			_simulator._options._showCoords = readIntegerLine(in) != 0;
			_simulator._options._showIcons.set(readIntegerLine(in) != 0);
			_simulator._options._showTooltip.set(readIntegerLine(in) != 0);
			_simulator._options._showScripts.set(readIntegerLine(in) != 0);
			_simulator._options._randomDelays.set(readIntegerLine(in) != 0);
			_simulator._options._linkToLeft.set(readIntegerLine(in) != 0);
			_simulator._options._playSynchronously.set(readIntegerLine(in) != 0);
			

			/* needs to be here, after we reloaded the list
			 * of stranded rolling stock, because it might
			 * reduce the path a shunting train can travel.
			 */
			
			
			// first need to position trains
			
			for (Train train : schedule._trains) {
				if (train._shunting || train.isMerging())
					continue;
				if (train._position != null) {
					PathFinder finder = new PathFinder();
					train._path = finder.find(train._position._position, train._direction);
					if (train._path != null && train._path.getTrackCount(0) > 1) {
						for (Track track : train._path._tracks) {
							if (track._status != TrackStatus.BUSYSHUNTING)
								track._status = TrackStatus.BUSY;
						}
					}
				}
				positionTail(train);
			}
			
			// then need to position shunting material
			// because we need to know the position
			// of material we are merging to
			
			for (Train train : schedule._trains) {
				if (!train._shunting && !train.isMerging())
					continue;
				if (train._position == null)
					continue;
			
				int i;
				Train trn;
				
				PathFinder finder = new PathFinder();
				train._path = finder.find(train._position._position , train._direction);
				if (train._path != null) {
					// 0 is the position of the train
					for(i = 1; i < train._path.getTrackCount(0); ++i) {
						Track track = train._path.getTrackAt(i);
						if ((trn = schedule.findTrainAt(track._position)) == null) {
							if((trn = schedule.findTailAt(track._position)) == null) {
								if ((trn = schedule.findStrandedAt(track._position)) == null) {
									trn = schedule.findStrandedTailAt(track._position);
								}
							}
						}
						if (trn != null) {
							train._merging = trn;
							train._flags |= Train.MERGING;
							train._path.truncate(i);
							trn._flags |= Train.WAITINGMERGE;
							break;
						}
						// if FREE the path was clear when we saved the game
						if (track._status == TrackStatus.FREE)
							track._status = TrackStatus.BUSY;
						// else the path must have been colored white by (white tracks) above
						if (track._status != TrackStatus.BUSYSHUNTING && track._status != TrackStatus.BUSY)
							break;// impossible (should be caught by findStranded above)
					}
				}
				Train tail;
				
				if ((tail = train._tail) != null && tail._path != null) {
					tail._position = null;
					for (i = 0; i < tail._path.getTrackCount(0); ++i) {
						Track track = tail._path.getTrackAt(i);
						if (track == train._position)
							break;
						track._status = TrackStatus.OCCUPIED;
					}
					if (tail._path.getTrackCount(0) > 0)
						tail._position = tail._path.getTrackAt(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * 
		ZipFile zFile = null;
		ZipEntry zEntry;
		 *         ZipFile zFile = null;
        StringBuilder sb = new StringBuilder();
        try {
            zFile = new ZipFile(archive);
            for (Enumeration<?> e = zFile.getEntries(); e.hasMoreElements();) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                String name = ze.getName();
                // build a Tar-like representation of the file entry
                sb.append("-r--r--r-- root/root  ");
                sb.append(ze.getSize());
                sb.append(" 2011-11-11 00:00:00 ");
                sb.append(name);
                sb.append("\n");
            }

		*/
		return false;
	}

	private void positionTail(Train train) {
		Train tail;
		
		if ((tail = train._tail) != null && tail._path != null) {
			tail._position = null;
//			if (train.isArrived()) {
//				tail._path = null;
//			}
//			else {
			tail._path.setStatus(TrackStatus.OCCUPIED, 0);
			if (train._path != null) {
				train._path.setStatus(TrackStatus.BUSY, 0);
			}
//			if (tail._pathpos >= 0 && tail._pathpos < tail._path._size
			if (tail._path.getTrackCount(0) > 0)
				tail._position = tail._path.getTrackAt(0);
//			else
//		    tail->pathpos = 0;
///?		if(notOnTrack)
///?		    tr->status = tr->curspeed ? train_RUNNING : train_STOPPED;
///?		notOnTrack = 0;
//	        }

			// sometimes the saved tail path does not include all
			// track elements that the train's path has. This causes
			// some tracks to be left colored red because the tail "skips"
			// them when it is advanced. The following loop tries to correct
			// this situation, even though the real problem is clearly
			// somewhere else (the tail's path should always contain all
			// elements of the head's path).
			
			if (train._path != null) { // if we are now exiting...
				for (int i = 0; i < train._path.getTrackCount(0); ++i) {
					Track trk = train._path.getTrackAt(i);
					if (tail._path.find(trk) < 0)
						tail._path.add(trk, train._path.getDirectionAt(i));
				}
			}
		}
	}

	public boolean save() {
		Schedule schedule = _simulator._schedule;
		
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(new File(_fileName)));
			if(saveNewFormat(out))
				return true;
			// TODO: remove saving in old format
			out.append(trkFileName + eol);
			out.append(String.format("%d,%ld,%d,%d,%d,%d,%d,%d,%d,%ld\n",
					_simulator._currentTimeMultiplier,	// cur_time_mult
					schedule._startTime,	// start_time
					_simulator._options._showSpeeds._intValue,	// show_speeds
					_simulator._options._showBlocks._intValue,	// show_blocks
					_simulator._options._beepOnAlert._intValue,// beep_on_alert
					_simulator._runPoints,// run_points
					_simulator._totalDelay,// total_delay
					_simulator._totalLate,// total_late
					_simulator._timeMult,// time_mult
					_simulator._simulatedTime));// current_time
	
			//
			// Save the state of every switch
			//
			// Actually we only need to save the positions
			// of switches that are currently thrown, since
			// all other switches will be reset in the main position
			// when the layout is loaded from disk
			//
			
			for (Track track : _territory.getTracks()) {
				if (!(track instanceof Switch))
					continue;
				Switch sw = (Switch) track;
				if (!sw.isThrown())
					continue;
				out.append(String.format("%d,%d,1\n", sw._position._x, sw._position._y));
			}
			out.append('\n');
			
			// Save the state of every signal
			
			for (Track track : _territory.getTracks()) {
				if (!(track instanceof Signal))
					continue;
				Signal signal = (Signal) track;
				if (!signal.isClear() && !signal.isFleeted())	// signal is in the default state,
					continue;									// so no need to save it
				out.append(String.format("%d,%d,%d,%d",
						signal._position._x, signal._position._y,
						signal.isClear() ? 1 : 0, signal.isFleeted() ? 1 : 0));
				if (signal._intermediate) {
					out.append("/" + signal._nReservations);
				}
				if (signal._currentAspect != null) {
					out.append(',');
					out.append(signal._currentAspect._name);	// Format(wxT(",%s"), sig->_currentState))
				}
				out.append('\n');
			}
			out.append('\n');
			
	
			// Save the position of every train
		
			for (Train train : schedule._trains) {
				if (train._status == TrainStatus.READY &&
						(train._entryDelay == null || train._entryDelay._nSeconds == 0))
					continue; // train in initial state - no need to save its data
				
				// 1st line
				out.append(String.format("%s\n", train._name));
				
				// 2nd line
				out.append(String.format("  %d,%d,%s\n", train._status.ordinal(), train._direction.ordinal(),
					train._exited != null ? train._exited : ""));
				
				// 3rd line
				out.append(String.format("  %d,%d,%d,%d,%d,%d,%d,%d,%d", train._timeExited,
						train._wrongDest ? 1 : 0, train._speed, train._maxspeed,
						train._curmaxspeed, train._trackpos, train._timeLate,
						train._timeDelay, train._timeRed));
				if (train._entryDelay != null)
					out.append(String.format(",%d", train._entryDelay._nSeconds));
				out.append('\n');
				
				// 4th line
				out.append(String.format("  %ld,%d,%ld,%ld,%d\n", train._timeDep, 0, //tr->pathpos,
					train._pathtravelled, train._distanceToStop, train._shunting ? 1 : 0));
				if (train._stopPoint == null)
					out.append("  0,0,0,");
				else
					out.append(String.format("  %d,%d,%d,",  train._stopPoint._position._x,
							train._stopping._position._y, train._distanceToSlow));
				if (train._slowPoint == null)
					out.append("0,0");
				else
					out.append(String.format("%d,%d",  train._slowPoint._position._x,
							train._slowPoint._position._y));
				out.append(String.format(",%d\n", train._needFindStop ? 1 : 0));
				
				// 5th line
				if (train._fleet != null && !train._fleet.isEmpty()) {
					String sep = "  ";
					for (Track track : train._fleet) {
						out.append(String.format("%s%d,%d", sep, track._position._x, track._position._y));
						sep = ",";
					}
				} else
					out.append("  0,0");
				out.append('\n');
				
				// 6th line
				if (train._position != null)
					out.append(String.format("  %d,%d", train._position._position._x, train._position._position._y));
				else
					out.append("  0,0");
				out.append(String.format(",%d,%d,%d\n", train._waitTime, train._flags, train._arrived));
				
				// 7th line
				out.append(String.format("  %d,%s\n", train._oldStatus.ordinal(),
						train._outOf != null ? train._outOf._station : ""));
				
				// from 8th line
				
				if (train._startDelay > 0) {
					out.append(String.format(":startDelay %d\n", train._startDelay));
				}

				// Save status of each stop
				
				for (TrainStop stop : train._stops) { 
					if (stop._depDelay == null)
						out.append(String.format("    %s,%d,%d\n", stop._station, stop._stopped ? 1 : 0, stop._delay));
					else
						out.append(String.format("    %s,%d,%d,%d\n", stop._station, stop._stopped ? 1 : 0,
								stop._delay, stop._depDelay._nSeconds));
				}
				
				// Tail information
				
				Train tail = train._tail;
				if (tail != null && tail._path != null) {
					out.append(".\n");		// marks beginning of tail path
					out.append(String.format("  %s\n", train._stopping != null ? train._stopping._station : ""));
					if (train._length > 0) {
						// save the length. This may be different than
						// the length specified in the sch file because
						// it may have been changed by a split/merge operation.
						
						out.append(String.format("=length %d\n", train._length));
						// out.append(String.format("=icons %d %d\n", train._ecarpix, train._wcarpix)); // TODO: how do we encode this?
					}
					if (tail._fleet != null && !tail._fleet.isEmpty()) {
						String sep = "!";
						for (Track track : tail._fleet) {
							out.append(String.format("%s%d,%d", sep, track._position._x, track._position._y));
							sep = ",";
						}
						out.append('\n');
					}
					out.append(String.format("  %d,%d,%d,%d", tail._position == null ? -1 : 0,
							tail._trackpos, tail._tailEntry, tail._tailExit));
					int i;
					for (i = 0; i < tail._path.getTrackCount(0); ++i) {
						Track trk = tail._path.getTrackAt(i);
						Direction dir = tail._path.getDirectionAt(i);	// TODO: how to maintain backward compatibility?
						out.append(String.format(",%d,%d,%d", trk._position._x, trk._position._y, dir.ordinal()));
					}
				}
				out.append('\n');
			}
			out.append(".\n");	// end of trains information
			
			// save white tracks (to allow merging trains)
	
			boolean found = false;
			for (Track track : _territory.getTracks()) {
				if (track instanceof Track || track instanceof Switch) {
					if (track._status == TrackStatus.BUSYSHUNTING) {
						found = true;
						break;
					}
				}
			}
	
			if (found) {
				out.append("(white tracks\n");
				for (Track track : _territory.getTracks()) {
					if (track instanceof Track || track instanceof Switch) {
						if (track._status == TrackStatus.BUSYSHUNTING) {
							out.append(String.format("%d,%d\n", track._position._x, track._position._y));
						}
					}
				}
				out.append(")\n");
			}
			
			// Save the position of every stranded train
			
			for (Train train : schedule._stranded) {
				out.append(String.format("(stranded\n%d,%d,%d,%d,%d,%d,%d,%d,%d",
						train._type, train._position._position._x, train._position._position._y,
						train._direction, 0, 0, //  TODO: train._ecarpix, train._wcarpix,
						train._maxspeed, train._curmaxspeed,
						train._length));
				if (train._length > 0) {
					if (train._tail != null && train._tail._path != null) {
						String sep = "";
						int i;
						out.append(String.format(",%d\n", train._tail._path.getTrackCount(0)));
						for (i = 0; i < train._tail._path._tracks.size(); ++i) {
							Track track = train._tail._path.getTrackAt(i);
							Direction dir = train._tail._path.getDirectionAt(i);// TODO: how to do this?
							out.append(String.format("%s%d,%d,%d", sep, track._position._x, track._position._y, dir.ordinal()));
							sep = ",";
						}
						out.append('\n');
					} else
						out.append(",0\n");
				} else
					out.append('\n');
				out.append(")\n");
			}
			
			// late minutes statistics
			
			int m = 0;
			out.append("(late minutes\n");
			for (int i = 0; i < 24 * 60; ++i) {
				out.append(String.format(" %d", _simulator._lateData[i]));
				if (++m == 15) { // 15 values per line
					out.append('\n');
					m = 0;
				}
			}
			out.append(")\n");
			
			// other statistics and options
			
			out.append(String.format("%d,%d,%d,%d,%d\n",
					_simulator._runDay, _simulator._options._terseStatus, _simulator._options._statusOnTop,
					_simulator._options._showSeconds, _simulator._options._traditionalSignals));
			
			out.append(String.format("%d,%d\n", _simulator._options._autoLink, _simulator._options._showGrid));
	
			PerformanceCounters perf_tot = _simulator._performanceCounters;
			out.append(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
			    perf_tot.wrong_dest, perf_tot.late_trains, perf_tot.thrown_switch,
			    perf_tot.cleared_signal, perf_tot.denied, perf_tot.turned_train,
			    perf_tot.waiting_train, perf_tot.wrong_platform,
			    perf_tot.ntrains_late, perf_tot.ntrains_wrong,
			    perf_tot.nmissed_stops, perf_tot.wrong_assign));
			
			out.append(String.format("%d\n", _simulator._options._hardCounters.isSet() ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._showCanceled ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._showLinks.isSet() ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._beepOnEnter.isSet() ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._showCoords ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._showIcons.isSet() ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._showTooltip.isSet() ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._showScripts.isSet() ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._randomDelays.isSet() ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._linkToLeft.isSet() ? 1 : 0));
			out.append(String.format("%d\n", _simulator._options._playSynchronously.isSet() ? 1 : 0));
	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	protected int[] readIntArray(String s, int n) {
		String[] a = s.split(",");
		int[] values = new int[n];
		for (int i = 0; i < n; ++i) {
			values[i] = Integer.parseInt(a[i]);
		}
		return values;
	}
	
	protected int readIntegerLine(BufferedReader in) throws IOException {
		String line = in.readLine();
		if (line == null)
			return 0;
		line = line.trim();
		return Integer.parseInt(line);
	}

	String key, value;
	int ivalue;
	boolean bvalue;
	TDPosition posValue = new TDPosition();
	
	private boolean getKV(String in) {
		String[] split = in.split(":");
		key = split[0].trim();
		if(split.length < 2) {
			value = "";
			ivalue = 0;
		} else {
			value = split[1];
			try {
				ivalue = Integer.parseInt(value);
				bvalue = ivalue != 0;
				return true;
			} catch (Exception e) {
				ivalue = 0;
				bvalue = false;
			}
		}
		return false;
	}
	
	private TDPosition getPosValue() {
		posValue._x = 0;
		posValue._y = 0;
		String[] split = value.split(",");
		if(split.length == 2) {
			try {
				posValue._x = Integer.parseInt(split[0]);
				posValue._y = Integer.parseInt(split[1]);
			} catch (Exception e) {
				// do nothing, will return 0,0
			}
		}
		return posValue;
	}

	public boolean loadNewFormat(BufferedReader in, String line) throws IOException {
		Schedule schedule = _simulator._schedule;
		getKV(line);
		// check for relative path
		File file = new File(value);
		if (!file.canRead()) {
			FilePath path = new FilePath(line);
			String[] segs = path.getSegments();
			file = new File(segs[segs.length - 1], "");
			if (!file.canRead()) {
				// TODO: show alert - file not found
				return false;
			}
		}
		String trkFileName = file.getPath();
		trkFileName = trkFileName.replace(".TRK", ".trk");
		trkFileName = trkFileName.replace(".ZIP", ".zip");
		LoadCommand loadCmd = new LoadCommand(trkFileName);
		loadCmd.handle();

		Switch sw = null;
		Signal signal = null;

		while((line = in.readLine()) != null) {
			if(line.length() == 0)
				break;
			if(!getKV(line))
				continue;
			if(key.equals("CurrentTimeMultiplier")) {
				_simulator._currentTimeMultiplier = ivalue;
				continue;
			}
			if(key.equals("StartTime")) {
				schedule._startTime = ivalue;
				continue;
			}
			if(key.equals("ShowSpeeds")) {
				_simulator._options._showSpeeds.set(bvalue);
				continue;
			}
			if(key.equals("ShowBlocks")) {
				_simulator._options._showBlocks.set(bvalue);
				continue;
			}
			if(key.equals("BeepOnAlert")) {
				_simulator._options._beepOnAlert.set(bvalue);
				continue;
			}
			if(key.equals("RunPoints")) {
				_simulator._runPoints = ivalue;
				continue;
			}
			if(key.equals("TotalDelay")) {
				_simulator._totalDelay = ivalue;
				continue;
			}
			if(key.equals("TotalLate")) {
				_simulator._totalLate = ivalue;
				continue;
			}
			if(key.equals("TimeMultiplier")) {
				_simulator._timeMult= ivalue;
				continue;
			}
			if(key.equals("SimulatedTime")) {
				_simulator._simulatedTime = ivalue;
				continue;
			}

			if(key.equals("Switch")) {
				getPosValue();
				sw = _simulator._territory.findSwitch(posValue);
				if(sw != null)
					sw._switched = true;
				continue;
			}
		}
		
		// reload the state of every signal

		while((line = in.readLine()) != null) {
			if(line.length() == 0)
				break;
			getKV(line);
			if(key.equals("Signal")) {
				getPosValue();
				signal = _territory.findSignal(posValue._x, posValue._y);
				// TODO: report error if signal not found
				continue;
			}
			if(signal == null)
				continue;
			if(key.equals("Clear")) {
				signal._status = ivalue == 1 ? TrackStatus.BUSY : TrackStatus.FREE;
				continue;
			}
			if(key.equals("Fleeted")) {
				signal.setFleeted(bvalue);
				continue;
			}
			if(key.equals("Aspect")) {
				signal.setAspectFromName(value);
				continue;
			}
		}

		// post-process signals

		List<Signal> signals = _territory.getAllSignals();
		for(Signal sig : signals) {
			if (!sig.isApproach() && sig.isClear())
				sig.unlock();
		}


		// reload state of all trains

		Train train = null;
		TrainStop stop = null;
		boolean notOnTrack = false;
		while((line = in.readLine()) != null) {
			if(line.length() == 0)	// separator between trains
				continue;
			if(line.charAt(0) == '.')
				break;
			getKV(line);
			if(key.equals("Train")) {
				train = schedule.findTrainNamed(value);
				stop = null;
				// TODO: report error if train not found
				continue;
			}

			if(train == null)
				continue;

			if(key.equals("Status")) {
				train._status = TrainStatus.fromInteger(ivalue);
				continue;
			}
			if(key.equals("Direction")) {
				train._direction = Direction.fromInteger(ivalue);
				continue;
			}
			if(key.equals("Exited")) {
				train._exited = value;
				continue;
			}
			if(key.equals("TimeExited")) {
				train._timeExited = ivalue;
				continue;
			}
			if(key.equals("WrongDest")) {
				train._wrongDest = ivalue != 0;
				continue;
			}
			if(key.equals("Speed")) {
				train._speed = ivalue;
				continue;
			}
			if(key.equals("MaxSpeed")) {
				train._maxspeed = ivalue;
				continue;
			}
			if(key.equals("CurMaxSpeed")) {
				train._curmaxspeed = ivalue;
				continue;
			}
			if(key.equals("TrackPos")) {
				train._trackpos = ivalue;
				continue;
			}
			if(key.equals("TimeLate")) {
				train._timeLate = ivalue;
				continue;
			}
			if(key.equals("TimeDelay")) {
				train._timeDelay = ivalue;
				continue;
			}
			if(key.equals("TimeRed")) {
				train._timeRed = ivalue;
				continue;
			}
			if(key.equals("EntryDelay")) {
				train._entryDelay = new TDDelay();
				train._entryDelay._nSeconds = ivalue;
				continue;
			}
			if(key.equals("TimeDep")) {
				train._timeDep = ivalue;
				continue;
			}
			if(key.equals("PathTraveled")) {
				train._pathtravelled= ivalue;
				continue;
			}
			if(key.equals("Shunting")) {
				train._shunting = bvalue;
				continue;
			}
			if(key.equals("StopPoint")) {
				getPosValue();
				train._stopPoint = _territory.findTrack(posValue);
				continue;
			}
			if(key.equals("DistanceToStop")) {
				train._distanceToStop = ivalue;
				continue;
			}
			if(key.equals("SlowPoint")) {
				getPosValue();
				train._slowPoint = _territory.findTrack(posValue);
				continue;
			}
			if(key.equals("DistanceToSlow")) {
				train._distanceToSlow = ivalue;
				continue;
			}
			if(key.equals("NeedFindStop")) {
				train._needFindStop = bvalue;
				continue;
			}
			if(key.equals("FleetSignal")) {
				getPosValue();
				signal = _territory.findSignal(posValue._x, posValue._y);
				if(signal == null)
					continue;
				if(train._fleet == null)
					train._fleet = new ArrayList<Track>();
				train._fleet.add(signal);
				continue;
			}

			if(key.equals("Position")) {
				getPosValue();
				Track track = _territory.findTrack(posValue);
				if(track == null)
					track = _territory.findSwitch(posValue);
				if(track != null)
					train._position = track;
				else {
					switch (train._status) {
					case READY:
					case ARRIVED:
					case DERAILED:
					case DELAYED:
						break;
					case WAITING:
					case STOPPED:
					case RUNNING:
						notOnTrack = true;
					}
				}
				continue;
			}
			if(key.equals("WaitTime")) {
				train._waitTime = ivalue;
				continue;
			}
			if(key.equals("Flags")) {
				train._flags = ivalue;
				continue;
			}
			if(key.equals("Arrived")) {
				train._arrived = ivalue;
				continue;
			}
			if(key.equals("OldStatus")) {
				train._oldstatus = TrainStatus.fromInteger(ivalue);
				continue;
			}
			if(key.equals("OutOf")) {
				train._outOf = _territory.findStation(value);
				continue;
			}
			if(key.equals("Stop")) {
				stop = train.findStop(value);
				continue;
			}
			if(stop != null) {
				if(key.equals("Stopped")) {
					stop._stopped = bvalue;
					continue;
				}
				if(key.equals("Delay")) {
					stop._delay = ivalue;
					continue;
				}
				if(key.equals("DepDelay")) {
					stop._depDelay = new TDDelay();
					stop._depDelay._nSeconds = ivalue;
					continue;
				}
			}
			if(key.equals("StoppingAt")) {
				train._stopping = _territory.findStation(value);
				continue;
			}
			if(key.equals("Length")) {
				// get the train's length. This may be different than
				// the length specified in the sch file because
				// it may have been changed by a split/merge operation.
				train._length = ivalue;
				continue;
			}
			/* TODO
			 
			 if(key.equals("Icons")) {
			 }
			 
			 */

			// Tail information

			if(key.equals("Tail")) {
				if(train._tail == null) // maybe Length: was removed in the .sch file
					train._tail = new Train(null);
				continue;
			}
			Train tail = train._tail;
			if(tail != null) {
				if(key.equals("TailFleet")) {
					getPosValue();
					signal = _territory.findSignal(posValue._x, posValue._y);
					if(signal == null) 
						continue;
					if(tail._fleet == null)
						tail._fleet = new ArrayList<Track>();
					tail._fleet.add(signal);
					continue;
				}
				if(key.equals("TailTrackPos")) {
					tail._trackpos = ivalue;
					continue;
				}
				if(key.equals("TailEntry")) {
					tail._tailEntry = ivalue;
					continue;
				}
				if(key.equals("TailExit")) {
					tail._tailExit = ivalue;
					continue;
				}
				if(key.equals("TailTrack")) {
					getPosValue();
					Track track = _territory.findTrack(posValue);
					if (track == null) {
						track = _territory.findSwitch(posValue);
						if (track == null)
							track = _territory.findTextTrack(posValue);
					}
					if(track == null)
						continue;
					if (tail._path == null)
						tail._path = new TrackPath();
					Direction dir = Direction.E;
					line = in.readLine();
					if(line == null)
						break;
					getKV(line);
					if(key.equals("TailDir"))
						dir = Direction.fromInteger(ivalue);
					tail._path.add(track, dir);
					continue;
				}
			}
			
			if (train._status == TrainStatus.DELAYED && tail != null && tail._path != null)
				tail._path = null;
			if (notOnTrack) {
				// alert ("train is not on track!");
				// train._status = TrainStatus.DERAILED;
			}
		} // end while trains

		// other attribute types

		PerformanceCounters perf_tot = _simulator._performanceCounters;
		while((line = in.readLine()) != null && !line.isEmpty()) {
			getKV(line);
			if(key.equals("WhiteTrack")) {
				getPosValue();
				Track track = _territory.findTrack(posValue);
				if(track == null)
					track = _territory.findSwitch(posValue);
				if(track == null)
					continue;
				track._status = TrackStatus.BUSYSHUNTING;
				continue;
			}

			if(key.equals("Stranded")) {
				train = new Train(null);
				train._flags = Train.STRANDED;
				train._status = TrainStatus.ARRIVED;
				_simulator._schedule._stranded.add(train);
				int pathLength = 0;
				while((line = in.readLine()) != null && line.length() > 0) {
					getKV(line);
					if(key.equals("Type")) {
						train._type = ivalue;
						continue;
					}
					if(key.equals("Position")) {
						getPosValue();
						train._position = _territory.findTrack(posValue);
						if(train._position == null)
							train._position = _territory.findSwitch(posValue);
						continue;
					}
					if(key.equals("Direction")) {
						train._direction = Direction.fromInteger(ivalue);
						continue;
					}
					if(key.equals("MaxSpeed")) {
						train._maxspeed = ivalue;
						continue;
					}
					if(key.equals("CurMaxSpeed")) {
						train._curmaxspeed = ivalue;
						continue;
					}
					if(key.equals("Length")) {
						train._length = ivalue;
						continue;
					}
					if(key.equals("TailLength")) {
						train._tail = new Train(null);
						train._tail._path = new TrackPath();
						continue;
					}
					if(train._tail == null)
						continue;
					if(key.equals("TailPos")) {
						getPosValue();
						Track track = _territory.findTrack(posValue);
						if(track == null)
							_territory.findSwitch(posValue);
						line = in.readLine();
						if(line == null)
							break;
						Direction dir = Direction.E;
						getKV(line);
						if(key.equals("TailDirection"))
							dir = Direction.fromInteger(ivalue);
						if(track != null) {
							train._tail._path.add(track, dir);
							track._status = TrackStatus.OCCUPIED;
							train._tail._length += track._length;
						}
						continue;
					}
				}
				if(train._tail != null && train._tail._path != null)
					train._tail._position = train._tail._path.getTrackAt(0);
				continue;
			}

			if(key.equals("LateMinutes")) {
				int x = 0;
				while((line = in.readLine()) != null && !line.isEmpty()) {
					String[] cols = line.split(",");
					for (int i = 0; i < cols.length; ++i) {
						int m = Integer.parseInt(cols[i]);
						_simulator._lateData[x % (24 * 60)] = m;
						++x;
					}
				}
				continue;
			}
			if(key.equals("RunDay")) {
				_simulator._runDay = ivalue;
				continue;
			}
			if(key.equals("TerseStatus")) {
				_simulator._options._terseStatus.set(bvalue);
				continue;
			}
			if(key.equals("StatusOnTop")) {
				_simulator._options._statusOnTop.set(bvalue);
				continue;
			}
			if(key.equals("ShowSeconds")) {
				_simulator._options._showSeconds.set(bvalue);
				continue;
			}
			if(key.equals("TraditionalSignals")) {
				_simulator._options._traditionalSignals.set(bvalue);
				continue;
			}
			if(key.equals("AutoLink")) {
				_simulator._options._autoLink.set(bvalue);
				continue;
			}
			if(key.equals("ShowGrid")) {
				_simulator._options._showGrid.set(bvalue);
				continue;
			}
			if(key.equals("WrongDest")) {
				perf_tot.wrong_dest = ivalue;
				continue;
			}
			if(key.equals("LateTrains")) {
				perf_tot.late_trains = ivalue;
				continue;
			}
			if(key.equals("ThrownSwitches")) {
				perf_tot.thrown_switch = ivalue;
				continue;
			}
			if(key.equals("ClearedSignals")) {
				perf_tot.cleared_signal = ivalue;
				continue;
			}
			if(key.equals("Denied")) {
				perf_tot.denied = ivalue;
				continue;
			}
			if(key.equals("TurnedTrains")) {
				perf_tot.turned_train = ivalue;
				continue;
			}
			if(key.equals("WaitingTrains")) {
				perf_tot.waiting_train = ivalue;
				continue;
			}
			if(key.equals("WrongPlatform")) {
				perf_tot.wrong_platform = ivalue;
				continue;
			}
			if(key.equals("NTrainsLate")) {
				perf_tot.ntrains_late = ivalue;
				continue;
			}
			if(key.equals("NTrainsWrong")) {
				perf_tot.ntrains_wrong = ivalue;
				continue;
			}
			if(key.equals("NMissedStops")) {
				perf_tot.nmissed_stops = ivalue;
				continue;
			}
			if(key.equals("NWrongAssign")) {
				perf_tot.wrong_assign = ivalue;
				continue;
			}
			if(key.equals("HardCounters")) {
				_simulator._options._hardCounters.set(bvalue);
				continue;
			}
			if(key.equals("ShowCanceled")) {
				_simulator._options._showCanceled = bvalue;
				continue;
			}
			if(key.equals("ShowLinks")) {
				_simulator._options._showLinks.set(bvalue);
				continue;
			}
			if(key.equals("BeepOnEnter")) {
				_simulator._options._beepOnEnter.set(bvalue);
				continue;
			}
			if(key.equals("ShowCoords")) {
				_simulator._options._showCoords = bvalue;
				continue;
			}
			if(key.equals("ShowIcons")) {
				_simulator._options._showIcons.set(bvalue);
				continue;
			}
			if(key.equals("ShowToolTip")) {
				_simulator._options._showTooltip.set(bvalue);
				continue;
			}
			if(key.equals("ShowScripts")) {
				_simulator._options._hardCounters.set(bvalue);
				continue;
			}
			if(key.equals("RandomDelay")) {
				_simulator._options._randomDelays.set(bvalue);
				continue;
			}
			if(key.equals("LinkToLeft")) {
				_simulator._options._linkToLeft.set(bvalue);
				continue;
			}
			if(key.equals("PlaySynchronously")) {
				_simulator._options._playSynchronously.set(bvalue);
				continue;
			}
		}

		/* needs to be here, after we reloaded the list
		 * of stranded rolling stock, because it might
		 * reduce the path a shunting train can travel.
		 */
		
		// first need to position trains
		
		for (Train train1 : schedule._trains) {
			train = train1;
			if (train._shunting || train.isMerging())
				continue;
			if (train._position != null) {
				PathFinder finder = new PathFinder();
				train._path = finder.find(train._position._position, train._direction);
				if (train._path != null && train._path.getTrackCount(0) > 1) {
					for (Track track : train._path._tracks) {
						if (track._status != TrackStatus.BUSYSHUNTING)
							track._status = TrackStatus.BUSY;
					}
				}
			}
			positionTail(train);
		}
		
		// then need to position shunting material
		// because we need to know the position
		// of material we are merging to
		
		for (Train train1 : schedule._trains) {
			train = train1;
			if (!train._shunting && !train.isMerging())
				continue;
			if (train._position == null)
				continue;
		
			int i;
			Train trn;
			
			PathFinder finder = new PathFinder();
			train._path = finder.find(train._position._position , train._direction);
			if (train._path != null) {
				// 0 is the position of the train
				for(i = 1; i < train._path.getTrackCount(0); ++i) {
					Track track = train._path.getTrackAt(i);
					if ((trn = schedule.findTrainAt(track._position)) == null) {
						if((trn = schedule.findTailAt(track._position)) == null) {
							if ((trn = schedule.findStrandedAt(track._position)) == null) {
								trn = schedule.findStrandedTailAt(track._position);
							}
						}
					}
					if (trn != null) {
						train._merging = trn;
						train._flags |= Train.MERGING;
						train._path.truncate(i);
						trn._flags |= Train.WAITINGMERGE;
						break;
					}
					// if FREE the path was clear when we saved the game
					if (track._status == TrackStatus.FREE)
						track._status = TrackStatus.BUSY;
					// else the path must have been colored white by (white tracks) above
					if (track._status != TrackStatus.BUSYSHUNTING && track._status != TrackStatus.BUSY)
						break;// impossible (should be caught by findStranded above)
				}
			}
			Train tail;
			
			if ((tail = train._tail) != null && tail._path != null) {
				tail._position = null;
				for (i = 0; i < tail._path.getTrackCount(0); ++i) {
					Track track = tail._path.getTrackAt(i);
					if (track == train._position)
						break;
					track._status = TrackStatus.OCCUPIED;
				}
				if (tail._path.getTrackCount(0) > 0)
					tail._position = tail._path.getTrackAt(0);
			}
		}
		
		return true;
	}

	public boolean saveNewFormat(BufferedWriter out) throws IOException {
		Schedule schedule = _simulator._schedule;
		out.append("Layout: " + trkFileName + eol);
		out.append(String.format("CurrentTimeMultiplier:%d\nStartTime:%ld\nShowSpeeds:%d\nShowBlocks:%d\nBeenOnAlert:%d\nRunPoints:%d\nTotalDelay:%d\nTotalLate:%d\nTimeMultiplier:%d\nSimulatedTime:%ld\n",
				_simulator._currentTimeMultiplier,	// cur_time_mult
				schedule._startTime,	// start_time
				_simulator._options._showSpeeds._intValue,	// show_speeds
				_simulator._options._showBlocks._intValue,	// show_blocks
				_simulator._options._beepOnAlert._intValue,// beep_on_alert
				_simulator._runPoints,// run_points
				_simulator._totalDelay,// total_delay
				_simulator._totalLate,// total_late
				_simulator._timeMult,// time_mult
				_simulator._simulatedTime));// current_time

		//
		// Save the state of every switch
		//
		// Actually we only need to save the positions
		// of switches that are currently thrown, since
		// all other switches will be reset in the main position
		// when the layout is loaded from disk
		//

		for (Track track : _territory.getTracks()) {
			if (!(track instanceof Switch))
				continue;
			Switch sw = (Switch) track;
			if (!sw.isThrown())
				continue;
			out.append(String.format("Switch:%d,%d\n", sw._position._x, sw._position._y));
		}
		out.append(eol);
		
		// Save the state of every signal
		
		List<Signal> signals = _territory.getAllSignals();
		for (Signal signal : signals) {
			if (!signal.isClear() && !signal.isFleeted())	// signal is in the default state,
				continue;									// so no need to save it
			out.append(String.format("Signal:%d,%d\n  Clear:%d\n  Fleeted:%d\n",
					signal._position._x, signal._position._y,
					signal.isClear() ? 1 : 0, signal.isFleeted() ? 1 : 0));
			if (signal._currentAspect != null) {
				out.append("Aspect:" + signal._currentAspect._name + eol);	// Format(wxT(",%s"), sig->_currentState))
			}
		}
		out.append(eol);

		// Save the position of every train
	
		for (Train train : schedule._trains) {
			if (train._status == TrainStatus.READY &&
					(train._entryDelay == null || train._entryDelay._nSeconds == 0))
				continue; // train in initial state - no need to save its data
			
			out.append(String.format("Train:%s\n", train._name));
			out.append(String.format("  Status:%d\n  Direction:%d\n  Exited:%s\n", train._status.ordinal(), train._direction.ordinal(),
				train._exited != null ? train._exited : ""));
			out.append(String.format("  TimeExited:%d\n  WrondDest:%d\n  Speed:%d\n  MaxSpeed:%d\n  CurMaxSpeed:%d\n  TrackPos:%d\n  TimeLate:%d\n  TimeDelay:%d\n  TimeRed:%d\n",
					train._timeExited, train._wrongDest ? 1 : 0, train._speed, train._maxspeed,
					train._curmaxspeed, train._trackpos, train._timeLate,
					train._timeDelay, train._timeRed));
			if (train._entryDelay != null)
				out.append(String.format("  EntryDelay:%d\n", train._entryDelay._nSeconds));
			
			out.append(String.format("  TimeDep:%ld\n  PathPos:%d\n  PathTraveled:%ld\n  DistanceToStop:%ld\n  Shunting:%d\n",
					train._timeDep, 0, //tr->pathpos,
					train._pathtravelled, train._distanceToStop, train._shunting ? 1 : 0));
			if (train._stopPoint != null)
				out.append(String.format("  StopPoint:%d,%d\n  DistanceToStop:%d\n",
						train._stopPoint._position._x, train._stopping._position._y, train._distanceToStop));
			if (train._slowPoint != null)
				out.append(String.format("  SlowPoint:%d,%d\n  DistanceToSlow:%d\n",
						train._slowPoint._position._x, train._slowPoint._position._y, train._distanceToSlow));
			out.append(String.format("  NeedFIndStop:%d\n", train._needFindStop ? 1 : 0));
			
			if (train._fleet != null) {
				for (Track track : train._fleet) {
					out.append(String.format("  FleetSignal:%d,%d\n", track._position._x, track._position._y));
				}
			}
			
			if (train._position != null)
				out.append(String.format("  Position:%d,%d\n", train._position._position._x, train._position._position._y));
			out.append(String.format("  WaitTime:%d\n  Flags:%d\n  Arrived:%d\n", train._waitTime, train._flags, train._arrived));

			out.append(String.format("  OldStatus:%d\n  OutOf:%s\n", train._oldStatus.ordinal(),
					train._outOf != null ? train._outOf._station : ""));
			
			// Save status of each stop
			
			for (TrainStop stop : train._stops) {
					out.append(String.format("    Stop:%s\n    Stopped:%d\n    Delay:%d\n", stop._station, stop._stopped ? 1 : 0, stop._delay));
				if (stop._depDelay != null)
					out.append(String.format("    DepDelay:%d\n", stop._depDelay._nSeconds));
				out.append(eol);
			}
			if(train._stopping != null)
				out.append(String.format("  StoppingAt:%s\n", train._stopping._station));
			if (train._startDelay != 0) {
				out.append(String.format("  StartDelay:%d\n", train._startDelay));
			}
			if (train._length > 0) {
				// save the length. This may be different than
				// the length specified in the sch file because
				// it may have been changed by a split/merge operation.
				out.append(String.format("  Length:%d\n", train._length));
			}

			// Tail information

			Train tail = train._tail;
			if (tail != null && tail._path != null) {
				if (tail._fleet != null && !tail._fleet.isEmpty()) {
					for (Track track : tail._fleet) {
						out.append(String.format("    TailFleet:%d,%d\n", track._position._x, track._position._y));
					}
					out.append(eol);
				}
				out.append(String.format("    TailPos:%d\n    TailTrackPos:%d\n    TailEntry:%d\n    TailExit:%d",
						tail._position == null ? -1 : 0, tail._trackpos, tail._tailEntry, tail._tailExit));
				int i;
				for (i = 0; i < tail._path.getTrackCount(0); ++i) {
					Track trk = tail._path.getTrackAt(i);
					Direction dir = tail._path.getDirectionAt(i);	// TODO: how to maintain backward compatibility?
					out.append(String.format("    TailTrack:%d,%d\n    TailDir:%d\n", trk._position._x, trk._position._y, dir.ordinal()));
				}
			}
			out.append(eol);
		}
		out.append(".\n");	// end of trains information
		
		// save white tracks (to allow merging trains)

		boolean found = false;
		for (Track track : _territory.getTracks()) {
			if (track instanceof Track || track instanceof Switch) {
				if (track._status == TrackStatus.BUSYSHUNTING) {
					found = true;
					break;
				}
			}
		}

		if (found) {
			for (Track track : _territory.getTracks()) {
				if (track instanceof Track || track instanceof Switch) {
					if (track._status == TrackStatus.BUSYSHUNTING) {
						out.append(String.format("WhiteTrack:%d,%d\n", track._position._x, track._position._y));
					}
				}
			}
		}

		// Save the position of every stranded train

		for (Train train : schedule._stranded) {
			out.append(String.format("Stranded:\n  Type:%d\n  Position:%d,%d\n  Direction:%d\n  MaxSpeed:%d\n  CurMaxSpeed:%d\n  Length:%d\n",
					train._type, train._position._position._x, train._position._position._y,
					train._direction, //  TODO: train._ecarpix, train._wcarpix,
					train._maxspeed, train._curmaxspeed,
					train._length));
			if (train._length > 0) {
				if (train._tail != null && train._tail._path != null) {
					String sep = "";
					int i;
					out.append(String.format("  TailLength:%d\n", train._tail._path.getTrackCount(0)));
					for (i = 0; i < train._tail._path._tracks.size(); ++i) {
						Track track = train._tail._path.getTrackAt(i);
						Direction dir = train._tail._path.getDirectionAt(i);// TODO: how to do this?
						out.append(String.format("    TailPos:%d,%d\n    TailDirection:%d", track._position._x, track._position._y, dir.ordinal()));
					}
					out.append(eol);
				}
			}
		}

		// late minutes statistics

		int m = 0;
		out.append("LateMinutes\n");
		for (int i = 0; i < 24 * 60; ++i) {
			out.append(String.format(" %d", _simulator._lateData[i]));
			if (++m == 15) { // 15 values per line
				out.append('\n');
				m = 0;
			}
		}
		
		// other statistics and options
		
		out.append(String.format("RunDay:%d\nTerseStatus:%d\nStatusOnTop:%d\nShowSeconds:%d\nTraditionalSignals:%d\n",
				_simulator._runDay, _simulator._options._terseStatus, _simulator._options._statusOnTop,
				_simulator._options._showSeconds, _simulator._options._traditionalSignals));
		
		out.append(String.format("AutoLink:%d\nShowGrid:%d\n", _simulator._options._autoLink, _simulator._options._showGrid));

		PerformanceCounters perf_tot = _simulator._performanceCounters;
		out.append(String.format("WrongDest:%d\nLateTrains:%d\nThrownSwitches:%d\nClearedSignals:%d\nDenied:%d\nTurnedTrains:%d\nWaitingTrains:%d\nWrongPlatform:%d\nNTrainsLate:%d\nNTrainsWrong:%d\nNMissedStops:%d\nNWrongAssign:%d\n",
		    perf_tot.wrong_dest, perf_tot.late_trains, perf_tot.thrown_switch,
		    perf_tot.cleared_signal, perf_tot.denied, perf_tot.turned_train,
		    perf_tot.waiting_train, perf_tot.wrong_platform,
		    perf_tot.ntrains_late, perf_tot.ntrains_wrong,
		    perf_tot.nmissed_stops, perf_tot.wrong_assign));

		out.append(String.format("HardCounters:%d\n", _simulator._options._hardCounters.isSet() ? 1 : 0));
		out.append(String.format("ShowCanceled:%d\n", _simulator._options._showCanceled ? 1 : 0));
		out.append(String.format("ShowLinks:%d\n", _simulator._options._showLinks.isSet() ? 1 : 0));
		out.append(String.format("BeepOnEnter:%d\n", _simulator._options._beepOnEnter.isSet() ? 1 : 0));
		out.append(String.format("ShowCoords:%d\n", _simulator._options._showCoords ? 1 : 0));
		out.append(String.format("ShowIcons:%d\n", _simulator._options._showIcons.isSet() ? 1 : 0));
		out.append(String.format("ShowToolTip:%d\n", _simulator._options._showTooltip.isSet() ? 1 : 0));
		out.append(String.format("ShowScripts:%d\n", _simulator._options._showScripts.isSet() ? 1 : 0));
		out.append(String.format("RandomDelays:%d\n", _simulator._options._randomDelays.isSet() ? 1 : 0));
		out.append(String.format("LinkToLeft:%d\n", _simulator._options._linkToLeft.isSet() ? 1 : 0));
		out.append(String.format("PlaySynchronously:%d\n", _simulator._options._playSynchronously.isSet() ? 1 : 0));

		return true;
	}
}
