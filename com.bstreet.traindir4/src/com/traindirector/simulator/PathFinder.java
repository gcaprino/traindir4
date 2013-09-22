package com.traindirector.simulator;

import java.util.List;

import com.traindirector.model.Direction;
import com.traindirector.model.Signal;
import com.traindirector.model.TDPosition;
import com.traindirector.model.Territory;
import com.traindirector.model.TextTrack;
import com.traindirector.model.Track;
import com.traindirector.model.TrackAndDirection;
import com.traindirector.model.TrackPath;
import com.traindirector.model.TrackStatus;

public class PathFinder {
	
	String _error;
	
	public PathFinder() {
		
	}

	public TrackPath find(TDPosition pos, Direction dir) {
		TrackPath path = new TrackPath();
		TextTrack text = null;
		TDPosition newPos = null;
		Territory territory = Simulator.INSTANCE._territory;
		Track	track = territory.findTrack(pos);

		_error = null;
		if (track == null) {
			_error = "No track at " + pos.toString();
			return null;
		}
		path.add(track, dir);

		Direction newDir = dir;
		TrackAndDirection td = new TrackAndDirection();
		while(true) {
			if (!track.getNextTrack(newDir, td)) {
				text = territory.findTextLinkedTo(track, newDir);
				if(text != null) {	// reached an entry/exit point
					break;
				}
//				newTrack = territory.findTrackLinkedTo(track, newDir);
				newDir = track.walk(newDir);
				newPos = newDir.offset(track._position);
				_error = "No track from " + track._position.toString() + " to " + newPos.toString();
				//System.out.println(_error);
				return null;
			}
			newDir = td._direction;
			
			Signal sig = Simulator.INSTANCE._territory.findSignalLinkedTo(td);
			if(sig != null && !sig.isApproach())
				break;
			if(td._track._length < 1)
				td._track._length = 1;
			path.add(td._track, td._direction);
			track = td._track;
			newDir = td._direction;
		}

		int i = path.getTrackCount(0) - 1;
		track = path.getTrackAt(i);
		dir = path.getDirectionAt(i);
		newDir = dir.opposite();
		while(i > 0) {
			if(!track.getNextTrack(newDir, td)) {
				newDir = track.walk(newDir);
				newPos = newDir.offset(track._position);
				_error = "Cannot travel from " + track._position.toString() + " to " + newPos.toString();
				System.out.println(_error);
				return null;
			}
			//System.out.println("<< " + track.toString() + " [" + oldDir.toString() + " -> " + newDir.toString() + "] -> " + newPos.toString());
			Track backTrack = path.getTrackAt(i - 1);
			if(td._track != backTrack) {
				_error = "Cannot travel back from " + td._track._position.toString() + " to " + backTrack._position.toString();
				//System.out.println(_error);
				return null;
			}
			track = td._track;
			newDir = td._direction;
			--i;
		}
		if (text != null)
			path.add(text, newDir);
		return path;
	}
	
	public boolean isFree(TrackPath path) {
		for (Track track : path._tracks) {
			if (track._status != TrackStatus.FREE)
				return false;
		}
		return true;
	}

	public void setAs(TrackPath path, TrackStatus status) {
		for(Track track : path._tracks) {
			if(track._status != status) {
				track.setUpdated(Simulator.INSTANCE._updateCounter++);
				if (status == TrackStatus.FREE)
					track.onSetFree();
				else
					track.onSetBusy();
			}
			track._status = status;
		}
	}
}
