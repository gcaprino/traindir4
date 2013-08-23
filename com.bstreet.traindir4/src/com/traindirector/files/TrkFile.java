package com.traindirector.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import com.traindirector.model.ImageTrack;
import com.traindirector.model.Itinerary;
import com.traindirector.model.ItineraryButton;
import com.traindirector.model.PlatformTrack;
import com.traindirector.model.Signal;
import com.traindirector.model.Switch;
import com.traindirector.model.Switchboard;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.TerritoryInfo;
import com.traindirector.model.TextTrack;
import com.traindirector.model.Track;
import com.traindirector.model.TrackDirection;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TriggerTrack;
import com.traindirector.model.Itinerary.ItinerarySwitch;
import com.traindirector.scripts.Script;
import com.traindirector.simulator.Simulator;

public class TrkFile {

	Simulator _simulator;
	Territory _territory;
	String _fname;
	BufferedReader _reader;
	private BufferedWriter writer;
	
	public TrkFile(Simulator simulator, Territory territory, String fname, BufferedReader rdr) {
		_simulator = simulator;
		_territory = territory;
		_fname = fname;
		_reader = rdr;
	}

	public void load() {
		try {
			_reader = new BufferedReader(new FileReader(_fname));
			loadTracks(_reader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadTracks(BufferedReader input) {
		String line;
		int	i;
		int x, y;
		try {
			while((line = input.readLine()) != null) {
				Track trk = null;
				if(line.startsWith("(script ")) {
					TDPosition pos = new TDPosition();
					pos.fromString(line, 7);
					trk = _territory.findTrack(pos);
					if (trk == null)		// impossible
						continue;
					StringBuilder sb = new StringBuilder();
					while((line = input.readLine()) != null) {
						if (line.length() > 0 && line.charAt(0) == ')')
							break;
						sb.append(line);
						sb.append('\n');
					}
					trk._script = new Script(null);
					trk._script._body = sb.toString();
					trk._script.parse();
					continue;
				}
				if(line.startsWith("(attributes ")) {
					while((line = input.readLine()) != null && !line.equals(")")) {
						if (trk == null)
							continue;
						if(line.equals("hidden")) {
							trk._invisible = true;
							continue;
						}
						if (line.startsWith("icons:")) {	// ITIN and IMAGE
							// TODO
							continue;
						}
						if (line.startsWith("locked")) {
							if (!(trk instanceof Signal))	// impossible
								continue;
							Signal sig = (Signal)trk;
							sig._blockedBy = new TDPosition();
							sig._blockedBy.fromString(line.substring(6).trim(), 0);
							continue;
						}
						if (line.startsWith("intermediate")) {
							if (!(trk instanceof Signal))	// impossible
								continue;
							Signal sig = (Signal)trk;
							sig._intermediate = Integer.parseInt(line.substring(12).trim()) != 0;
							sig._nReservations = 0;
							continue;
						}
						if (line.startsWith("dontstopshunters")) {
							trk._flags |= Track.DONTSTOPSHUNTERS;
							continue;
						}
					}
					continue;
				}
				if(line.startsWith("(switchboard ")) {
					String name = line.substring(13);
					if(name.endsWith(")"))
						name = name.substring(0, name.length() - 1);
					name = name.trim();
					createSwitchBoard(name);
					continue;
				}
				Itinerary itin = null;
				String[] elements = line.split(",");
				if(elements.length < 4) 
					continue;
				x = Integer.parseInt(elements[1]);
				y = Integer.parseInt(elements[2]);
				int dirval = Integer.parseInt(elements[3]);
				TrackDirection dir = TrackDirection.parse(dirval);
				Signal sig;
				switch(elements[0].charAt(0)) {
				case '0':		// TRACK
					trk = new Track(x, y);
					_territory.add(trk);
					trk._direction = dir;
					trk._isStation = Integer.parseInt(elements[4]) != 0;
					trk._length =  Integer.parseInt(elements[5]);
					trk._wlink = new TDPosition(Integer.parseInt(elements[6]),
												Integer.parseInt(elements[7]));
					trk._elink = new TDPosition(Integer.parseInt(elements[8]),
												Integer.parseInt(elements[9]));
					i = 10;
					if(elements.length <= 10)
						break;
					if(elements[i].charAt(0) == '@') {
						trk.readSpeeds(elements[i].substring(1));
						++i;
					}
					if(elements.length <= i || elements[i].equalsIgnoreCase("noname"))
						break;
					if(elements[i].charAt(0) == '>') {
						trk.parseMilepost(elements[i].substring(1));
						++i;
					}
					if(elements.length <= i)
						break;
					trk._station = elements[i];
					break;
					
				case '1':		// SWITCH
					trk = new Switch(x, y);
					_territory.add(trk);
					trk._direction = dir;
					trk._length = 1;
					trk._wlink = new TDPosition(Integer.parseInt(elements[4]),
												Integer.parseInt(elements[5]));
					break;
					
				case '2':		// SIGNAL
					sig = _simulator._signalFactory.newInstance(((dirval & 2)) != 0 ? 2 : 1);
					_territory.add(sig);
					sig._position = new TDPosition(x, y);
					sig._status = TrackStatus.FREE;	// RED
					if((dirval & 2) != 0) {
						sig._fleeted = true;
						dirval &= ~2;
					}
					if((dirval & 0x100) != 0) {
						sig._fixedred = true;
					}
					if((dirval & 0x200) != 0) {
						sig._nopenalty = true;
					}
					if((dirval & 0x400) != 0) {
						sig._signalx = true;
					}
					if((dirval & 0x800) != 0) {
						sig._noClickPenalty = true;
					}
					dirval &= ~0xF00;
					sig._direction = TrackDirection.parse(dirval);
					switch(dirval) {
					case 0:	sig._direction = TrackDirection.E_W;
							break;
					case 1: sig._direction = TrackDirection.W_E;
							break;
					}
					sig._wlink = new TDPosition(Integer.parseInt(elements[4]),
												Integer.parseInt(elements[5]));
					i = 6;
					if(elements.length > 6 && elements[6].charAt(0) == '@') {
						sig._scriptFile = elements[6].substring(1);// name of script file
						++i;
					}
					if(elements.length > i)
						sig._station = elements[i];	// name of itinerary
					break;
					
				case '3':		// PLATFORM
					trk = new PlatformTrack();
					_territory.add(trk);
					trk._position = new TDPosition(x, y);
					if(dirval == 0)
						trk._direction = TrackDirection.W_E;
					else
						trk._direction = TrackDirection.N_S;
					break;
					
				case '4':		// TEXT
					trk = new TextTrack();
					_territory.add(trk);
					trk._position = new TDPosition(x, y);
					trk._station = elements[4];
					if(elements.length > 9 && elements[9].charAt(0) == '>') {
						trk.parseMilepost(elements[9].substring(1));
					} else {
						int indx = elements[8].indexOf('>');
						if(indx > 0) {
							trk.parseMilepost(elements[8].substring(indx + 1));
							elements[8] = elements[8].substring(0, indx);
						}
					}
					trk._wlink = new TDPosition(Integer.parseInt(elements[5]),
												Integer.parseInt(elements[6]));
					trk._elink = new TDPosition(Integer.parseInt(elements[7]),
												Integer.parseInt(elements[8]));
					break;

				case '5':		// IMAGE
					trk = new ImageTrack();
					_territory.add(trk);
					trk._position = new TDPosition(x, y);
					if (elements.length > 4)
						trk._station = elements[4];
					else
						trk._station = "?";
					break;
					
				case '6':		// INFO
					trk = new TerritoryInfo();
					_territory.add(trk);
					trk._station = elements[4];
					break;

				case '7':		// ITINERARY
					itin = new Itinerary();
					_territory.add(itin);
					itin._name = elements[4];
					i = 5;
					{
						int nparens = 0;
						int xx = 0;
						String signame = "";
						while(true) {
							for(xx = 0; xx < elements[i].length(); ++xx) {
								if(elements[i].charAt(xx) == '(')
									++nparens;
								else if(elements[i].charAt(xx) == ')' && nparens > 0)
									--nparens;
								signame += elements[i].charAt(xx);
							}
							if(nparens == 0 || i + 1 >= elements.length) {
								break;
							}
							++i;
							signame += ',';
						}
						if(nparens == 0) {
							++i;
						}
						itin._signame = signame;
					}
					{
						int nparens = 0;
						int xx = 0;
						String signame = "";
						while(true) {
							for(xx = 0; xx < elements[i].length(); ++xx) {
								if(elements[i].charAt(xx) == '(')
									++nparens;
								else if(elements[i].charAt(xx) == ')' && nparens > 0)
									--nparens;
								signame += elements[i].charAt(xx);
							}
							if(nparens == 0 || i + 1 >= elements.length) {
								break;
							}
							++i;
							signame += ',';
						}
						if(nparens == 0) {
							++i;
						}
						itin._endsig = signame;
					}
					if(i < elements.length && elements[i].charAt(0) == '@') {
						itin._nextitin = elements[i].substring(1);
						++i;
					}
					// store positions and state of switches along the itinerary
					while(i < elements.length) {
						x = Integer.parseInt(elements[i]); ++i;
						y = Integer.parseInt(elements[i]); ++i;
						dirval = Integer.parseInt(elements[i]); ++i;
						itin.addSwitch(x, y, dirval != 0);
					}
					break;
					
				case '8':		// ITINERARY PLACEMENT
					trk = new ItineraryButton();
					_territory.add(trk);
					trk._position = new TDPosition(x, y);
					trk._station = elements[4];
					break;
					
				case '9':		// TRIGGER
					trk = new TriggerTrack();
					trk._direction = dir;
					_territory.add(trk);
					trk._position = new TDPosition(x, y);
					trk._wlink = new TDPosition(Integer.parseInt(elements[4]),
												Integer.parseInt(elements[5]));
					trk._elink = new TDPosition(Integer.parseInt(elements[6]),
												Integer.parseInt(elements[7]));
					i = 8;
					if(i < elements.length) {
						trk.readSpeeds(elements[i]);
						++i;
					}
					if(i < elements.length && !elements[i].equalsIgnoreCase("noname"))
						trk._station = elements[i];
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void createSwitchBoard(String name) {
		SwbFile swbFile = new SwbFile(_simulator);
		swbFile.readFile(name);
		/*
		BufferedReader swbReader = null;
		try {
			swbReader = new BufferedReader(new FileReader(name));	// TODO: use FileManager
			swb.load(swbReader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (swbReader != null)
				try {
					swbReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		*/
	}

	public void save() {
		try {
			writer = new BufferedWriter(new FileWriter(_fname));
			saveTracks(writer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
	}
/*

int	save_layout(const wxChar *name, Track *layout)
{
	wxFFile file;
	Track	*t;
	TextList *tl;
	Itinerary *it;
	int	i;
	int	ch;


	SaveSwitchBoards(file);

	file.Close();
	layout_modified = 0;
	return 1;
}


 */

	private void saveTracks(BufferedWriter file) throws IOException {
		int	i;

		for (Track t : _territory._tracks) {
			if (t instanceof Switch) {
				file.write(String.format("1,%d,%d,%d,", t._position._x, t._position._y, t._direction.ordinal()));
				file.write(String.format("%d,%d\n", t._wlink._x, t._wlink._y));
			} else if (t instanceof Signal) {
				Signal signal = (Signal)t;
				file.write(String.format("2,%d,%d,%d,", signal._position._x, signal._position._y,
						signal._direction.ordinal() +		// TODO: compatibility
						(signal._fleeted ? 1 : 0) * 2 +
						((signal._fixedred ? 1 : 0) << 8) +
						((signal._nopenalty ? 1 : 0) << 9) +
						((signal._signalx ? 1 : 0) << 10) +
						((signal._noClickPenalty ? 1 : 0) << 11)));
				file.write(String.format("%d,%d", signal._wlink._x, signal._wlink._y));
				// TODO
				/*
				if(signal._script != null) {
				    for(int i = wxStrlen(signal._stateProgram); i >= 0; --i)
						if(signal._stateProgram[i] == '/' || signal._stateProgram[i] == '\\')
						    break;
				    file.write(String.format(",@%s", signal._stateProgram + i + 1));
				}
				*/
				if(signal._station != null && !signal._station.isEmpty())	// for itineraries
				    file.write(String.format(",%s", signal._station));
				file.write("\n");
			} else if (t instanceof PlatformTrack) {
				file.write(String.format("3,%d,%d,%d,", t._position._x, t._position._y, t._direction.ordinal()));
			} else if (t instanceof TextTrack) {
				file.write(String.format("4,%d,%d,%d,%s,", t._position._x, t._position._y, t._direction.ordinal(), t._station));
				file.write(String.format("%d,%d,%d,%d", t._wlink._x, t._wlink._y, t._elink._x, t._elink._y));
				if(t._km != 0)
				    file.write(String.format(">%d.%d", t._km / 1000, t._km % 1000));
				file.write("\n");
			} else if (t instanceof ImageTrack) {
				if(t._station == null)
				    t._station = "";
				for(i = t._station.length(); i >= 0; --i)
				    if(t._station.charAt(i) == '/' || t._station.charAt(i) == '\\')
				    	break;
				file.write(String.format("5,%d,%d,0,%s\n", t._position._x, t._position._y, t._station.substring(i + 1)));
				
			} else if (t instanceof TriggerTrack) {
				file.write(String.format("9,%d,%d,%d,", t._position._x, t._position._y, t._direction.ordinal()));
				file.write(String.format("%d,%d,%d,%d", t._wlink._x, t._wlink._y, t._elink._x, t._elink._y));
				int ch = ',';
				for(i = 0; i < t._speed.length; ++i) {
				    file.write(String.format("%c%d", ch, t._speed[i]));
				    ch = '/';
				}
				file.write(String.format(",%s\n", t._station));
				
			} else if (t instanceof TerritoryInfo) {
			    file.write(String.format("6,0,0,0,%s\n", t._station));
			} else if (t instanceof ItineraryButton) {
				file.write(String.format("8,%d,%d,%d,%s\n", t._position._x, t._position._y, t._direction.ordinal(), t._station));
				
			} else { // generic Track
				file.write(String.format("0,%d,%d,%d,", t._position._x, t._position._y, t._direction.ordinal()));
				file.write(String.format("%d,%d,", t._isStation ? 1 : 0, t._length));
				file.write(String.format("%d,%d,%d,%d,", t._wlink._x, t._wlink._y, t._elink._x, t._elink._y));
				if(t._speed != null && t._speed[0] != 0) {
				    int ch = '@';

				    for(i = 0; i < t._speed.length; ++i) {
						file.write(String.format("%c%d", ch, t._speed[i]));
						ch = '/';
				    }
				    file.write(',');
				}
				if(t._km != 0)
				    file.write(String.format(">%d.%d,", t._km / 1000, t._km % 1000));
				if(t._isStation && t._station != null)
				    file.write(String.format("%s\n", t._station));
				else
				    file.write(String.format("noname\n"));
			}
		}
		
		for (Itinerary it : _territory._itineraries) {
		    file.write(String.format("7,0,0,0,%s,%s,%s,", it._name, it._signame, it._endsig));
		    if(it._nextitin != null && !it._nextitin.isEmpty())
		    	file.write(String.format("@%s,", it._nextitin));
		    for (ItinerarySwitch sw : it._switches)
				file.write(String.format("%d,%d,%d,", sw._position._x, sw._position._y, sw._thrown ? 1 : 0));
		    file.write(String.format("\n"));
		}
		
		for (Track track : _territory._tracks) {
			if (track instanceof ItineraryButton || track instanceof ImageTrack) {
				// TODO
				/*
				if(track._flashingIcons[0]) {
					file.write(String.format("(attributes %d,%d\nicons:", track._position._x, track._position._y));
					for(int x = 0; ;) {
						file.write(track._flashingIcons[x]);
						++x;
						if(x >= MAX_FLASHING_ICONS || !track._flashingIcons[x])
							break;
						file.write(",");
					}
					file.write("\n)\n");
				}
				 */
			} else if (track instanceof Signal) {
				Signal t = (Signal) track;
                if(t._blockedBy != null) {
                    file.write(String.format("(attributes %d,%d\nlocked %s\n)\n", t._position._x, t._position._y, t._blockedBy));
                }
                if((t._flags & Train.DONTSTOPSHUNTERS) != 0) {
                    file.write(String.format("(attributes %d,%d\ndontstopshunters\n)\n", t._position._x, t._position._y));
                }
                if(t._intermediate) {
                    file.write(String.format("(attributes %d,%d\nintermediate %d\n)\n",
                        t._position._x, t._position._y, t._intermediate ? 1 : 0));
                }
			}
			if(!(track instanceof Signal) && track._script != null) {
			    file.write(String.format("(script %d,%d\n%s)\n", track._position._x, track._position._y, track._script._body));
			}
			if (track._invisible) {
				file.write(String.format("(attributes %d,%d\nhidden\n)\n", track._position._x, track._position._y));				
			}
		}
		// TODO: _simulator._switchboards.save(file);
	}
	
}

