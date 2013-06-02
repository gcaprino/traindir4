package com.traindirector.simulator;

import java.util.List;

import com.traindirector.model.Direction;
import com.traindirector.model.Signal;
import com.traindirector.model.TDPosition;
import com.traindirector.model.TextTrack;
import com.traindirector.model.Track;
import com.traindirector.model.TrackPath;
import com.traindirector.model.TrackStatus;

public class PathFinder {
	
	String _error;
	
	public PathFinder() {
		
	}

	public TrackPath find(TDPosition pos, Direction dir) {
		TrackPath path = new TrackPath();
		TextTrack text = null;
		Track	track = Simulator.INSTANCE._territory.findTrack(pos);

		_error = null;
		if (track == null) {
			_error = "No track at " + pos.toString();
			return null;
		}
		path.add(track, dir);

		Direction newDir = dir;
		while(true) {
			Direction oldDir = newDir;
			newDir = track.walk(newDir);
			TDPosition newPos = newDir.offset(track._position);
			System.out.println(">> " + track.toString() + " [" + oldDir.toString() + " -> " + newDir.toString() + "] -> " + newPos.toString());
			Track	newTrack = Simulator.INSTANCE._territory.findTrack(newPos);
			if(newTrack == null) {
				text = Simulator.INSTANCE._territory.findTextLinkedTo(track, dir);
				if(text != null) {	// reached an entry/exit point
					break;
				}
				_error = "No track from " + track._position.toString() + " to " + newPos.toString();
				return null;
			}
			
			Signal sig = Simulator.INSTANCE._territory.findSignalLinkedTo(newTrack, newDir);
			if(sig != null)
				break;
			if(newTrack._length < 1)
				newTrack._length = 1;
			path.add(newTrack, newDir);
			track = newTrack;
		}

		int i = path.getTrackCount(0) - 1;
		track = path.getTrackAt(i);
		dir = path.getDirectionAt(i);
		newDir = dir.opposite();
		while(i > 0) {
			Direction oldDir = newDir;
			newDir = track.walk(newDir);
			TDPosition newPos = newDir.offset(track._position);
			System.out.println("<< " + track.toString() + " [" + oldDir.toString() + " -> " + newDir.toString() + "] -> " + newPos.toString());
			Track newTrack = path.getTrackAt(i - 1);
			if(!newPos.sameAs(newTrack._position)) {
				_error = "Cannot travel from " + track._position.toString() + " to " + newTrack._position.toString();
				return null;
			}
			track = newTrack;
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
