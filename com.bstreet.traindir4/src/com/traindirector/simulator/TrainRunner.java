package com.traindirector.simulator;

import com.traindirector.commands.AssignCommand;
import com.traindirector.commands.ReverseCommand;
import com.traindirector.model.Direction;
import com.traindirector.model.Schedule;
import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.TextTrack;
import com.traindirector.model.Track;
import com.traindirector.model.TrackPath;
import com.traindirector.model.TrackStatus;
import com.traindirector.model.Train;
import com.traindirector.model.TrainStatus;
import com.traindirector.model.TrainStop;

public class TrainRunner {

	Simulator _simulator;

	public TrainRunner(Simulator simulator) {
		_simulator = simulator;
		/*
		CGEventDispatcher.getInstance().addListener(
				new CGEventListener(TimeSliceEvent.class) {
					@Override
					public void handle(CGEvent event, Object target) {
						if (target instanceof Simulator) {
							_simulator = (Simulator) target;
							if (_simulator == null) {
								return;
							}
							if(!_simulator.isRunning())
								return;
							if(_simulator._ignoreTimer)
								return;
							timeSlice();
						}
					}
				});
				*/
	}

	public void timeSlice() {
		int simulatedSpeed = _simulator.getSimulatedSpeed();
		
		for(int i = 0; i < simulatedSpeed; ++i) {
			timeStep();
		}
	}

	public void timeStep() {
		Signal signal;
		Schedule schedule = _simulator._schedule;
		Territory territory = _simulator._territory;

		for (Train train : schedule._trains) {
			switch (train._status) {
			case READY:

				// check time in
				if (train._entrance == null)
					continue;
				if(train._days != 0 && (_simulator._schedule._runDay & train._days) == 0)
					continue;
				if (train._timeIn < schedule._startTime)
					continue;
				// check random delay 3 minutes
				if (train._timeIn + train._inDelay > _simulator._simulatedTime)
					continue;
				if (train._waitFor != null) {
					// TODO: waitfor
				}

				// we can start the train
				Track text = territory.findTextTrack(train._entrance);
				text = territory.findStationNamed(train._entrance);
				if (text == null || text._wlink == null) {
					_simulator.alert(String.format("Train %s derailed: entry point %s not found",
							train._name, train._entrance));
					train.derailed();
					continue;
				}
				Track entryTrack = territory.findTrack(text._elink);
				if (entryTrack == null) {
					entryTrack = territory.findTrack(text._wlink);
					if (entryTrack == null) {
						_simulator.alert(String.format("Train %s derailed: entry point %s %s not linked to any track",
								train._name, train._entrance, text._position.toString()));
						train.derailed();
						continue;
					}
				}
				PathFinder finder = new PathFinder();
				Direction dir = territory.findEntryDirection(text, entryTrack);
				if (dir == null) {
					_simulator.alert(String.format("Train %s derailed: entry point %s %s not linked to horizontal or vertical track",
							train._name, train._entrance, text._position.toString()));
					train.derailed();
					continue;
				}
				TrackPath path = finder.find(entryTrack._position, dir);
				if (path == null) {
					_simulator.alert(String.format("Train %s derailed: cannot create path from entry point %s %s",
							train._name, train._entrance, text._position.toString()));
					train.derailed();
					continue;
				}
				train._path = path;
				if (!path.isFree()) {
					train._status = TrainStatus.DELAYED;
					continue;
				}
				startRunning(train);
				break;

			case ARRIVED:
				continue;

			case DELAYED:

				if (train._path == null) {
					_simulator.alert(String.format("Train %s derailed: path disappeared from entry point %s",
							train._name, train._entrance));
					train.derailed();
					break;
				}
				// check delay condition
				if (!train._path.isFree())
					break;
				startRunning(train);
				break;

			case DERAILED:
				continue;

			case RUNNING:

				// advance position
				advance(train);
				break;

			case STOPPED:

				// check etd
				if (train._timeDep > _simulator._simulatedTime)
					break;
//				startRunning(train);
				train._status = TrainStatus.RUNNING;
				train._outOf = train._position;
				findStopPoint(train);
				findSlowPoint(train);
				advance(train);
				break;

			case STARTING:
				
				if (--train._startDelay > 0)
					continue;
				train._startDelay = 0;
				// fall through

			case WAITING:

				// check waiting condition
				signal = findSignalAtEndOfPath(train._path);
				if (signal == null) {
					// TODO: set to stop - maybe signal was deleted during
					// editing
					train._status = TrainStatus.STOPPED;
					train._timeDep = _simulator._simulatedTime - 1;
					continue;
				}
				SignalAspect aspect = signal.getAspect();
				if (!aspect._action.equals(SignalAspect.PROCEED)) {
					continue;
				}
				train._status = TrainStatus.RUNNING;
				advance(train);
				break;

			default:
				break;

			}
		}
	}

	public void leaveTrack(Track track) {
		track.setStatus(TrackStatus.FREE);
		// track.onExitTrack(); // run scripts
	}

	public Track findEntryTrack(Train train) {
		return null;
	}

	/*
	 * max_approach_speed()
	 * 
	 * This computes the maximum speed allowed based on the distance of the
	 * train from the next slow or stop point. If the current train speed is
	 * greater than the computed speed, the train will be slowed down. If the
	 * current train speed is lower, it will be sped up.
	 */

	int maxApproachSpeed(Train trn, double _distanceToStop, int targetspeed) {
		/*
		 * It would be neat to have the ability to define the trains
		 * retardation,
		 */
		/*
		 * but then we have to mess with a lot of other stuff, for instance
		 * changing the definition of the Schdeule keywords and change to speed
		 * as cm/s or something like that. Next project!
		 */

		/* v*v - v0*v0 = 2*a*s => v = sqrt(2*a*s + v0*v0) */

		/* distance in meters, result in m/s, but we need it in km/h */
		/* deceleration is 0.6 m/s2, for now. */
		double s, v0;
		int v;

		s = (double) _distanceToStop;
		v0 = ((double) targetspeed) / 3.6;

		s = s * 2.0 * 0.6 + v0 * v0;
		if (s < 0) /* should be impossible, but... */
			return trn._speed;
		v = (int) (Math.sqrt(s) * 3.6);

		return v;
	}

	public int getLimitFromSignal(Track track, Direction dir) {
		if (track == null)
			return -1;
		if (!(track instanceof Signal)) {
			track = Simulator.INSTANCE._territory.findSignalLinkedTo(track, dir);
		}
		if (track != null && (track instanceof Signal)) {
			Signal signal = (Signal) track;
			SignalAspect aspect = signal.getAspect();
			return aspect.getSpeedLimit();
		}
		return -1;
	}

	public boolean stoppingAtSignal(Track track) {
		if (!(track instanceof Signal))
			return false;
		Signal signal = (Signal) track;
		return !signal.isClear();
	}

	public void computeSpeed(Train train, Direction dir) {
		int maxspeed = 10000, maxslowspeed, slowspeed;
		int speedincr = 1;

		/*
		 * This computes the speed by using max_approach_speed() to calculate
		 * the braking curve. Consider first the stopping distance and then
		 * distance to slowpoint
		 */

		if (train._stopPoint != null) {
			boolean doStop = true;
			// TODO: stoppoint may be beyond a station if train.length > 0
			if (!(train._stopPoint instanceof Signal)
					|| stoppingAtSignal(train._stopPoint)) {
				/*
				 * Check if we need to brake. Set target speed to 5 km/h, so we
				 * don't stop too early
				 */
				maxspeed = maxApproachSpeed(train, train._distanceToStop, 5);
			} else if ((maxspeed = getLimitFromSignal(train._stopPoint, dir)) > 0)
				maxspeed = maxApproachSpeed(train, train._distanceToStop, maxspeed);
			else
				doStop = false;
			if (doStop) {
				if (train._curmaxspeed < maxspeed)
					maxspeed = train._curmaxspeed;
				if (train._speed > maxspeed) {
					/*
					 * Instead of decelerating, we adjust immediately to target
					 * speed. Our train _must not_ speed
					 */
					train._speed = maxspeed;
				} else if (train._speed < maxspeed) {
					/*
					 * Accelerate, this shouldn't be a fixed number, but OK for
					 * now.
					 */
					train._speed += speedincr;
					speedincr = 0;
				}
			}
		}

		/* Is slowspeed lower? */

		if (train._slowPoint != null) {
			if ((slowspeed = train._slowPoint.getSpeedLimit(train._type)) == 0)
				slowspeed = train._slowPoint.getSpeedLimit(0);
			/* Check if we need to brake. */
			maxslowspeed = maxApproachSpeed(train, train._distanceToSlow, slowspeed);
			if (maxspeed == 10000)
				maxspeed = maxslowspeed;
			else {
				if (maxslowspeed < maxspeed)
					maxspeed = maxslowspeed;
			}
			if (train._curmaxspeed < maxspeed)
				maxspeed = train._curmaxspeed;
			if (train._speed > maxspeed) {
				/*
				 * Instead of decelerating, we adjust immediately to target
				 * speed. Our train _must not_ speed
				 */
				train._speed = maxspeed;
			} else if (train._speed < maxspeed) {
				train._speed += speedincr;
			}
			if (train._distanceToSlow <= 0) {
				if (train._position == train._slowPoint)
					train._slowPoint = null;
				/* This shouldn't really happen... */
				train._speed = slowspeed;
			}
		} else if (train._curmaxspeed != 0 && train._speed < train._curmaxspeed) {
			train._speed += speedincr;
		}

	}

	public Signal findSignalAtEndOfPath(TrackPath path) {
		int nPathElements = path.getTrackCount(0);
		Track track = path.getLastTrack();
		PathFinder finder = new PathFinder();
		Direction dir = track.walk(path.getDirectionAt(nPathElements - 1));
		TDPosition nextPos = dir.offset(track._position);
		Track next = _simulator._territory.findTrack(nextPos);
		if (next == null)
			return null;
		Signal signal;
		signal = _simulator._territory.findSignalLinkedTo(next, dir);
		if (signal == null) {
			return null;
		}
		return signal;
	}

	public void speedLimit(Train train, Track track) {
		int speed;

		speed = track.getSpeedLimit(train._type);
		if (speed == 0)
			speed = track.getSpeedLimit(0);
		if (train._shunting && speed > 30)
			speed = 30;
		if (speed > 0) {
			train._curmaxspeed = speed;
			if (train._maxspeed > 0 && train._curmaxspeed > train._maxspeed)
				train._curmaxspeed = train._maxspeed;
			if (train._shunting)
				train._curmaxspeed = 30;
			train._speedlimit = train._curmaxspeed;
			if (train._curmaxspeed > 0 && train._speed > train._curmaxspeed)
				train._speed = train._curmaxspeed;
		}
	}

	public void findStopPoint(Train train) {
		int i;
		int distToStop = 0;
		train._stopPoint = null;
		train._distanceToStop = 0;
		for (i = 0; i < train._path.getTrackCount(0); ++i) {
			Track track = train._path.getTrackAt(i);
			distToStop += track._length;
			if (track != train._outOf && track._isStation && train.stopsAt(track)) {
				train._stopPoint = track;
				if (track._length > 0)
					distToStop -= track._length / 2; // stop in the middle of the track
				// TODO account for train length when stopping
				train._distanceToStop = distToStop;
				return;
			}
		}
		Signal signal = findSignalAtEndOfPath(train._path);
		if (signal == null)
			return;
		if (!signal.isClear()) {
			train._stopPoint = signal;
			train._distanceToStop = distToStop;
		}
	}

	public void findSlowPoint(Train train) {
		int i;
		int limit;
		int distToSlow = 0;
		int curMaxSpeed = train._curmaxspeed;
		train._slowPoint = null;
		train._distanceToSlow = 0;
		for (i = 0; i < train._path.getTrackCount(0); ++i) {
			Track track = train._path.getTrackAt(i);
			limit = track.getSpeedLimit(train._type);
			if (limit != 0 && limit < curMaxSpeed) {
				train._slowPoint = track;
				train._distanceToSlow = distToSlow;
				return;
			}
			distToSlow += track._length;
		}
		Signal signal = findSignalAtEndOfPath(train._path);
		if (signal == null)
			return;
		if (signal.isClear()) {
			SignalAspect aspect = signal.getAspect();
			limit = aspect.getSpeedLimit();
			if (limit != 0 && limit < curMaxSpeed) {
				train._slowPoint = signal;
				train._distanceToSlow = distToSlow;
			}
		}
	}

	/*
	 * Train is at a station.
	 * 
	 * We have to decide whether we have to stop
	 * at this station (because it's in our schedule
	 * or during shunting), and if so we have to
	 * compute the penalties for late arrivals,
	 * wrong platform and the estimated tiem of departure
	 */

	public boolean trainAtStation(Train train, Track station) {
		if (!station._isStation)
			return false;
	
		TrainStop stop = null;
		if(train.stoppingAtStation(station))
			stop = train.findStop(station);
		if (train._shunting) {
			if (train._outOf == station)
				return false; // don't stop, since we departed from here
			if (train._oldStatus == TrainStatus.WAITING || train._oldStatus == TrainStatus.RUNNING)
				train._oldStatus = TrainStatus.STOPPED;
			train._status = train._oldStatus;
			train._stopping = null;
			train._speed = 0;
			train._shunting = false;
			train._outOf = null;
			if (stop != null)
				train._timeDep = stop._departure;
			else if(Territory.sameStation(station._station, train._entrance))
				train._timeDep = train._timeIn;
			else if (Territory.sameStation(station._station, train._exit) || train.isArrived())
				train.arrived();
			return true;
		}
		train._stopping = null;
		if (stop == null) {	// we are not at a stop
			if (//!assignOk ||  // TODO: what is this?
					!Territory.sameStation(station._station, train._exit))
				return false;
			
			// but we arrived at our destination
			
			Territory.checkPlatform(station._station, train._exit);
			train.arrived();
		} else {
			Territory.checkPlatform(station._station, stop._station);
			int arrTime = stop._arrival;
			if (arrTime < train._timeIn)
				arrTime += 24 * 60 * 60;
			int minLate = (_simulator._simulatedTime - arrTime) / 60;
			if (stop._minstop == 0) { // does not stop
				stop._delay = minLate;
				return false;
			}
			train.stopped();
			if (stop._stopped)	// we stopped here before!
				minLate = 0;
			stop._delay = minLate;
			stop._stopped = true;
			// sometimes we have multiple entries for the same station.
			// this should be fixed in the loader
			for (TrainStop stop1 : train._stops) {
				if (Territory.sameStation(stop1._station , stop._station))
					stop1._stopped = true;
			}
			if (minLate > 0) {
				stop._late = true;
				train._timeLate += minLate;
				_simulator._totalLate += minLate;
			}
		}
		train._speed = 0;
		if (stop == null)
			return true;
		if (stop._departure < stop._arrival) {
			stop._departure += 24 * 60 * 60;
		}
		train._timeDep = _simulator._simulatedTime + stop._minstop;
		if (train._timeDep < stop._departure)
			train._timeDep = stop._departure;
		Track track1 = train._position;
		if (track1 != null) {
			if (!track1._isStation && train._tail != null && train._tail._path != null) {
				track1 = train._tail._path.findStation(train._position);
			}
		}
		if (track1 != null && track1._isStation)
			track1.doStopped(train);
		return true;
	}

	public void stop(Train train) {
		train._status = TrainStatus.STOPPED;
		train._speed = 0;
	}

	public void tailAdvance(Train train, double traveled) {
		// TODO: to be implemented
	}

	public void startRunning(Train train) {
		train._status = TrainStatus.RUNNING;
		train._curmaxspeed = 60;
		if(train._maxspeed > 0 && train._maxspeed < train._curmaxspeed)
			train._curmaxspeed = train._maxspeed;
		train._speed = 60; // default entering speed: 60 km/h
		if(train._curmaxspeed < train._speed)
			train._speed = train._curmaxspeed;
		train._position = train.getHeadTrack();
		train._direction = train._path.getDirectionAt(0);
		train._trackDistance = 0;
		findStopPoint(train);
		findSlowPoint(train);
		// train._path.setBusy();
		for (int i = 0; i < train._path.getTrackCount(0); ++i) {
			Track tt = train._path.getTrackAt(i);
			tt.setStatus(TrackStatus.BUSY);
		}
		findStopPoint(train);
		findSlowPoint(train);
//		t->pathpos = 1;
		train.onEntry();
		if(train._position._isStation)
		    trainAtStation(train, train._position);
		//advance(train);
	}

	public void advance(Train train) {

		Train tail = null;

		if (train._shunting && train._curmaxspeed > Train.MAX_SHUNTING_SPEED) {
			train._curmaxspeed = Train.MAX_SHUNTING_SPEED;
		}
		double traveled = train._speed / 3.6; // meters traveled in 1 second
		Track position = train._position;
		if (position == null) {
			tail = train._tail;
			if (tail == null)
				return;

			// head has exited the territory

			computeSpeed(train, tail._path.getDirectionAt(0));

			// has tail entered the territory?
			// (maybe the train is longer than the distance
			// between the entry and exit point)

			if (tail._entryDistanceToGo > 0) {
				tail._entryDistanceToGo -= traveled;
				if (tail._entryDistanceToGo <= 0) {
					// tail enters the territory
					tail._trackDistance = -tail._entryDistanceToGo;
					tail._entryDistanceToGo = 0;
				}
			} else
				tail._trackDistance += traveled;
			tailAdvance(train, traveled);
			if (tail._path == null) {
				// TODO: alert - train has no tail path!
				train.derailed();
				return;
			}
			if (tail._path.getTrackCount(0) > 0)
				return;
			// exited!
			fetchPath(train);
			return;
		}

		while (true) {
// agn
			speedLimit(train, position);
			
//			computeSpeed(train, train._path.getDirectionAt(0));
			
			position._flags &= ~Track.THROWN;
			if (position._length < 1)
				position._length = 1;
			if (position._length < 2 && train.isSet(Train.NEEDFINDSTOP)) {
				findStopPoint(train);
				train.clearFlag(Train.NEEDFINDSTOP);
			}
			int trackDistance = train._trackDistance;
			if (trackDistance + traveled < position._length) {
				trackDistance += traveled;
				_simulator._runPoints += _simulator._timeMult * _simulator._runPointBase + 1;
				computeSpeed(train, train._path.getDirectionAt(0));
				if (train._stopping != null && traveled >= train._distanceToStop) {
					tailAdvance(train, traveled);
					trainAtStation(train, train._stopping);
					return;
				}
				tail = train._tail;
			
				train._pathtravelled += traveled;
				train._distanceToStop -= traveled;
				if (train._distanceToStop < 0) train._distanceToStop = 0;
				train._distanceToSlow -= traveled;
				if (train._distanceToSlow < 0) train._distanceToSlow = 0;
				if (trackDistance < position._length) {
					train._trackDistance = trackDistance;	// we are still in the same track
					if (train._length == 0 || tail == null)	// no length info for this train
						return;
					if (tail._entryDistanceToGo > 0) {
						tail._entryDistanceToGo -= traveled;
						if (tail._entryDistanceToGo > 0)
							return;	// tail is still outside the field
						// tail enters the field
						tail._trackDistance = -tail._entryDistanceToGo;
						tail._entryDistanceToGo = 0;
					} else
						tail._trackDistance += traveled;
					tailAdvance(train, traveled);
					return;
				}
				
				train._trackDistance = trackDistance - position._length; // meters already traveled in the next track
				if (tail != null) {
					if (tail._entryDistanceToGo > 0) {
						tail._entryDistanceToGo -= traveled;
						if (tail._entryDistanceToGo > 0)
							return;	// tail is still outside the field
						// tail enters the field
						tail._trackDistance = -tail._entryDistanceToGo;
						tail._entryDistanceToGo = 0;
					} else
						tail._trackDistance += traveled;
				}
				traveled = 0;
			}

			// train has traveled the full length
			// of the current track element.
			// Advance to the next track element in the path,
			// or get a new path
			
			if (train._path == null || train._path.getTrackCount(0) == 1) {
				if (train._stopping != null) {
					// don't advance to another path if we wanted to stop at a station
					// (maybe we are advancing after the station and we reached the exit signal)
					trainAtStation(train, train._stopping);
					return;
				}
				if (train._shunting && train._merging != null) {
					merge(train);
					return;
				}
				
				train._pathtravelled = train._trackDistance;

				switch(fetchPath(train)) {
				case 0:
					if (train._tail != null) {
						tailAdvance(train, traveled);
					}
					return;
					
				case -1:
					train.derailed();
					return;
				}
				traveled = train._trackDistance;
				train._trackDistance = 0;
				train._pathtravelled = 0;
				position = train._position;
				continue; // goto agn
			}
			
			// advance to next track in this path
			
			tailAdvance(train, traveled);
			if (train._stopping != null) {
				if (train._distanceToStop <= 0 || position == train._stopPoint) {
					trainAtStation(train, position);
					return;
				}
			}
			if (traveled != 0) {
				// we didn't update the position because we traveled
				// a number of meter higher than current track's length.
				// Adjust the stop and slow points here by the track's length
				// so that we don't "go long" on the expected stop point
				traveled -= position._length - train._trackDistance;
				train._distanceToSlow -= position._length - train._trackDistance;
				train._distanceToStop -= position._length - train._trackDistance;
			}
			
			leaveTrack(position);
			train._path.advance(1);
			position = train.getHeadTrack();
			train._position = position;
			train._trackDistance = 0;
			train._direction = train._path.getDirectionAt(0);
			position.setStatus(TrackStatus.OCCUPIED);
			_simulator._territory.doTriggers(train);
			position.DoEnter(train);
			if (train._slowPoint != null && position == train._slowPoint) {
				speedLimit(train, position);
				findSlowPoint(train);
			}
			if (position._isStation && train.stoppingAtStation(position)) {
				if (train._length == 0) {
					if (!trainAtStation(train, position)) {
						continue;
					}
					return;
				}
				// decide where to stop so that as much of the train
				// as possible is at the station
				
				train._stopping = position;
				return;
			}
			if (train._status != TrainStatus.RUNNING)
				return;
		
			/*
			if (train._distanceToStop > 0) {
				train._distanceToStop -= traveled;
				if (train._distanceToStop <= 0) {
					train._distanceToStop = 0;
					train._distanceToSlow = 0;
					if (train._stopping != null && train._trackDistanceToGo <= 0) {
						tailAdvance(train, traveled);
						train._trackDistanceToGo = 0;
						trainAtStation(train, train._stopping);
						return;
					}
				}
				if (train._distanceToSlow > 0) {
					train._distanceToSlow -= traveled;
					if (train._distanceToSlow <= 0) {
						trackDistanceToGo = train._distanceToSlow;
						findSlowPoint(train);
						train._trackDistanceToGo += trackDistanceToGo; // actually a subtract,
						// since trackDistanceToGo is < 0
					}
				}
			}
			if (traveled < train._trackDistanceToGo) {
				train._trackDistanceToGo -= traveled;
				// still in the same track
				if (tail != null) {
					if (tail._entryDistanceToGo > 0) {
						tail._entryDistanceToGo -= traveled;
						if (tail._entryDistanceToGo < 0) {
							// tail is entering territory
							tail._position = tail.getHeadTrack();
							tail._trackDistanceToGo = 0;
							tail._entryDistanceToGo = 0;
							tailAdvance(train, traveled);
						}
					}
					tailAdvance(train, traveled);
				}
				return;
			}
			traveled -= train._trackDistanceToGo; // travel remaining length of
													// this track
			train._trackDistanceToGo = 0;

			// train has traveled one track element
			// advance train's head

			if (train._path.getTrackCount(0) > 1) {
				leaveTrack(position);
				train._position = train._path.advance(1);
				position = train._position;
				if (position instanceof TextTrack) {	// entry/exit
					if (train._tail != null) {
						// TODO: advance tail
					}
					train.arrived();
					return;
				}
				train._trackDistanceToGo = position._length;
				if (train._stopping == position && train._length == 0) {
					trainAtStation(train, position);
					return;
				}
				train._stopping = position;
				return;
			}

			// we reached the end of our path

			// are we exiting?

			// so we need to compute another path and enter it

			// get signal's aspect

			PathFinder finder = new PathFinder();
			Direction dir = position.walk(train._path.getDirectionAt(0));
			TDPosition nextPos = dir.offset(position._position);
			TrackPath path = finder.find(nextPos, dir);
			if (path == null) {
				leaveTrack(position);
				// TODO: Derailed
				break;
			}
			Track position1 = path.getTrackAt(0);
			Signal signal;
			signal = _simulator._territory.findSignalLinkedTo(position1, path.getDirectionAt(0));
			if (signal == null) {
				leaveTrack(position);
				// TODO: Derailed
				// impossible
				break;
			}
			SignalAspect aspect = signal.getAspect();
			if (aspect._action == SignalAspect.STOP) {
				trainAtStation(train, position);
				break;
			}
			signal.setAspectFromName(SignalAspect.RED);
			leaveTrack(position);
			train._path = path;
			train._position = position = path.getTrackAt(0);
			train._trackDistanceToGo += train._position._length;
			findStopPoint(train);
			findSlowPoint(train);
			*/
		} // end while true
	}

	private void merge(Train train) {
		Train	t2;
		boolean	doDelete = false;
		int	i;

		//Vector_dump(trn, _name);
		train._status = train._oldstatus;
		train._shunting = false;
		train._speed = 0;
		t2 = train._merging;
		train._position._status = TrackStatus.FREE;
		leaveTrack(train._position);
		t2.clearFlag(Train.WAITINGMERGE);
		train._position = null;
		if(t2.isStranded()) {
			train._length += t2._length;
		    if(t2._length > 0) {
				if(train._direction != t2._direction) {
					ReverseCommand reverse = new ReverseCommand(t2);
					reverse.handle();
				}
	//			Vector_dump(trn, "loco");
	//			Vector_dump(t2, "materiale");
				// incoming train always attaches to the tail of previous train.
				AssignCommand assign = new AssignCommand(train, t2);
				assign.handle();
	//			Vector_dump(trn, "dopo assign");
				train._merging = null;
				Train tail = train._tail;
				// t2 is deleted here, so nothing else to do
				if(tail == null || tail._path == null) {
				    if(train._position == null || !train._position._isStation || train._position == train._outOf)
				    	return;
				    if(Territory.sameStation(train._position._station, train._exit) || train.isArrived()) {
						// in case we were shunted to our destination
				    	train.arrived();
				    }
				    return;
				}
				for(i = 0 /*tail._pathpos*/; i < tail._path.getTrackCount(0); ++i) {
				    Track trk = tail._path.getTrackAt(i);
				    if(trk._isStation && Territory.sameStation(trk._station, train._exit) && trk != train._outOf) {
				    	train.arrived();
				    }
				    if(trk == train._position)
				    	break;
				    trk.setStatus(TrackStatus.OCCUPIED);
				}
				return;
		    }
		    train._position = t2._position;
		    _simulator._schedule.removeStranded(t2);
		    
			PathFinder finder = new PathFinder();
			TrackPath path = finder.find(train._position._position, train._direction);
			train._path = path;
		    train._path.setStatus(TrackStatus.BUSY, 1);
//		    _pathpos = 0;
		    findStopPoint(train);
		    findSlowPoint(train);
//		    _pathpos = 1;
	    	trainAtStation(train, train._position);
		    train._merging = null;
		    return;
		}
		train._merging = null;
		t2._length += train._length;

		if(t2.isArrived()) {
		    // we want to keep train, and remove t2
		    train._path = t2._path;
		    t2._path = null;
		    if(t2._length > 0) {
		    	train._length = t2._length;
			if(t2._tail != null) {
			    if(train._tail != null && train._tail._path != null) {
			    	train._tail._path.append(t2._tail._path);
					t2._tail._path = null;
			    } else {
			    	train._tail = t2._tail;
			    }
			    // TODO: train._ecarpix = t2._ecarpix;
			    // TODO: train._wcarpix = t2._wcarpix;
			}
		    }
		    train._position = t2._position;
		    t2._position = null;
		    t2._tail = null;
		    return;
		}
		// else we want to keep t2 and remove trn
		if(train._length > 0) {
		    if(t2._tail == null)
		    	t2._tail = train._tail;
		    else {
				// the append is to trn instead of t2
				// so that the tail's path of the merging tain
				// will appear before the path of the stationary train
		    	train._tail._path.append(t2._tail._path);
				t2._tail._path = train._tail._path;
				t2._tail._position = train._tail._position;
				train._tail._path = null;
		    }
		    train._tail = null;
		}
	}

	private int fetchPath(Train train) {
		Track position = train._position;
		if (position == null || (position instanceof TextTrack && !position._isStation)) {
			trainIsExiting(train, position);
			return 0;
		}
		
		// find the start of next block
		
		Direction dir = position.walk(train._path.getDirectionAt(0));
		TDPosition nextPos = dir.offset(position._position);
		Track nextTrack = _simulator._territory.findTrack(nextPos);
		if (nextTrack == null)
			_simulator._territory.findSwitch(nextPos);
		if (nextTrack == null) {
			// alert cannot go from position to nextTrack
			_simulator.alert(String.format("Train %s: cannot go from %s to %s - no track",
					train._name, position._position.toString(), nextPos.toString()));
			train.derailed();
			return 0;
		}
		
		// check if we can cross the signal that protects the next block
		
		Signal signal;
		signal = _simulator._territory.findSignalLinkedTo(nextTrack, dir);
		if (signal == null) {
			_simulator.alert(String.format("Train %s: no signal linked to track %s", train._name, nextTrack._position.toString()));
			train.derailed();
			return 0;
		}
		boolean doStopTrain = false;	// TODO remove and simplify
		boolean doProceed = false;
		PathFinder finder = new PathFinder();

		// check if we are shunting and entering a block
		// that is already occupied
		
		if (nextTrack._status == TrackStatus.BUSYSHUNTING && train._shunting) {

			Track wtrack;

			// if so, create a path limited to where the next train is
			
			train._direction = dir;
			train._path = finder.find(nextPos, dir);
			for (int x = 0; x < train._path.getTrackCount(0); ++x) {
				wtrack = train._path.getTrackAt(x);
				if (wtrack == null)
					continue; // impossile
				if (wtrack._status == TrackStatus.BUSYSHUNTING)
					continue;
				train._merging = _simulator._schedule.findTrainAt(wtrack._position);
				if (train._merging == null) {
					train._merging = _simulator._schedule.findTailAt(wtrack._position);
				}
				if (train._merging == null) {
					train._merging = _simulator._schedule.findStrandedAt(wtrack._position);
				}
				if (train._merging == null) {
					train._merging = _simulator._schedule.findStrandedTailAt(wtrack._position);
				}
				if (train._merging != null && (train._merging._status == TrainStatus.STOPPED ||
						train._merging._status == TrainStatus.WAITING ||
						train._merging._status == TrainStatus.ARRIVED)) {
					train._path.truncate(x); // limit to where the next train is
					train.setFlag(Train.MERGING); // lock both arriving...
					train._merging.setFlag(Train.WAITINGMERGE); // ...and stationary trains
					break;
				}
				// train is not there anymore, so it's moving
				train._merging = null;
				wtrack = train.getHeadTrack();
				signal = _simulator._territory.findSignalLinkedTo(wtrack, dir);
				signal.doUnclear();
				train._path.setStatus(TrackStatus.FREE, 0);
				train._path = null;
				doStopTrain = true;
				break;
			}
			if (!doStopTrain) {
				signal.doCross(train);
				doProceed = true;
			}
		} else {

			// we are not shunting, but the signal is opened for shunting.
			// We force the train to stop and the user to send it forward
			// with an explicit shunt command (otherwise a reload will
			// incorrectly fetch a path beyond the signal and color it green)
			
			if (nextTrack._status == TrackStatus.BUSYSHUNTING || !signal.isClear())
				doStopTrain = true;
		}
		if (doStopTrain) {
// stop_train:
			train._speed = 0;
			if (!train.isWaiting()) {
				// alert Train is waiting at position
				if (signal._station != null)
					_simulator.alert(String.format("Train %s: waiting at signal %s %s",
							train._name, signal._station, train._position.toString()));
				else
					_simulator.alert(String.format("Train %s: waiting at signal %s",
							train._name, train._position.toString()));
				if (!signal._nopenalty)
					++_simulator._performanceCounters.waiting_train;
			}
			train.doWaiting(signal);
			if (train._trackDistance > train._position._length)
				train._trackDistance = 0;
			// TODO: set train status changed
			return 0;
		}
		
		if (!doProceed) {
			SignalAspect aspect = signal.getAspect();
			int i = aspect.getSpeedLimit();
			if (i != 0)
				train._curmaxspeed = i;
			signal.doCross(train);
			leaveTrack(train._position);
			train._path = finder.find(nextTrack._position, dir);
			if (train._path == null) {
				// TODO: alert no path
				return -1;
			}
		}
// proceed:
		if (train._tail != null) {
			// make tail-s path partially overlap the head's path
			train._tail._path.append(train._path);
		}
		train._direction = train._path.getDirectionAt(0);
		train._position = train._path.getTrackAt(0);
		train._position.setStatus(TrackStatus.OCCUPIED);
		train._trackDistance = 0;
		findStopPoint(train);
		findSlowPoint(train);
		return 1;
	}

	private void trainIsExiting(Train train, Track position) {
		train._position = null;
		
		// check that we are exiting by the same station
		// as specified in our schedule
		
		if (position != null && position._station != null && !Territory.sameStation(position._station, train._exit)) {
			Simulator.INSTANCE._performanceCounters.wrong_dest++;
			train._wrongDest = true;
			train._exited = position._station;
		}
		
		// if train has a tail, see if the tail is still traveling in the layout
		
		if (train._tail != null) {
			if (train._tail._path != null &&
					train._tail._path.getTrackCount(0) > 0) {
				if (position != null) {
					train._tail._trackDistance = train._length - train._trackDistance; 
				}
				return;
			}
			
			// tail has traveled all tracks, remove if from the layout
			
			train._tail._position = null;
			train._tail._path = null;
			train._tail = null;
		}
		position._status = TrackStatus.FREE;
		train.arrived();
		// TODO: bstreet_train_arrived(train)
		train._speed = 0;
		train.onExit();
	}

}
