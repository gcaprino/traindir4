package com.traindirector.files;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.traindirector.model.ImageTrack;
import com.traindirector.model.Itinerary;
import com.traindirector.model.ItineraryButton;
import com.traindirector.model.PlatformTrack;
import com.traindirector.model.Signal;
import com.traindirector.model.Switch;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.TerritoryInfo;
import com.traindirector.model.TextTrack;
import com.traindirector.model.Track;
import com.traindirector.model.TrackDirection;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.TriggerTrack;
import com.traindirector.scripts.Script;
import com.traindirector.simulator.Simulator;

public class TrkFile {

	Simulator _simulator;
	Territory _territory;
	String _fname;
	
	public TrkFile(Simulator simulator, Territory territory, String fname) {
		_simulator = simulator;
		_territory = territory;
		_fname = fname;
	}

	public void load() {
		BufferedReader input = null;
		String line;
		int	i;
		int x, y;
		try {
			input = new BufferedReader(new FileReader(_fname));
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
					// TODO
					continue;
				}
				if(line.startsWith("(switchboard ")) {
					// TODO
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
					trk._wlink = new TDPosition(Integer.parseInt(elements[5]),
												Integer.parseInt(elements[6]));
					trk._elink = new TDPosition(Integer.parseInt(elements[7]),
												Integer.parseInt(elements[8]));
					if(elements.length > 9 && elements[9].charAt(0) == '>') {
						trk.parseMilepost(elements[9].substring(1));
					}
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
					if(i < elements.length && elements[i].equalsIgnoreCase("noname"))
						trk._station = elements[i];
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if (input != null) {
				try {
					input.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	

}

