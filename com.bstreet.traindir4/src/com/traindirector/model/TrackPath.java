package com.traindirector.model;

import java.util.ArrayList;
import java.util.List;

public class TrackPath {

	public List<Track> _tracks;
	public List<Direction> _dirs;

	public TrackPath() {
		_tracks = new ArrayList<Track>();
		_dirs = new ArrayList<Direction>();
	}

	public void add(Track track, Direction dir) {
		_tracks.add(track);
		_dirs.add(dir);
	}

	public void clear() {
		_tracks.clear();
		_dirs.clear();
	}

	public Track getTrackAt(int i) {
		return _tracks.get(i);
	}

	public Direction getDirectionAt(int i) {
		return _dirs.get(i);
	}
	
	public Track getFirstTrack() {
		if (_tracks.size() > 0)
			return _tracks.get(0);
		return null;
	}

	public Track getLastTrack() {
		if (_tracks.size() > 0)
			return _tracks.get(_tracks.size() - 1);
		return null;
	}

	public boolean isFree() {
		for (Track track : _tracks) {
			if (track._status != TrackStatus.FREE)
				return false;
		}
		return true;
	}

	public boolean isEmpty() {
		return _tracks.size() == 0;
	}
	
	public int getTrackCount(int offset) {
		return _tracks.size() - offset;
	}

	public int getDistance(int offset) {
		int distance = 0;
		for (int i = offset; i < _tracks.size(); ++i)
			distance += _tracks.get(i)._length;
		return distance;
	}

	public Track advance(int nElements) {
		Track head = _tracks.get(0);
		while (nElements > 0) {
			_tracks.remove(0);
			_dirs.remove(0);
			--nElements;
		}
		return head;
	}

	public void setStatus(TrackStatus status, int index) {
		while(index < _tracks.size()) {
			Track track = _tracks.get(index);
			track._status = status;
			++index;
		}
	}

	public int find(Track trk) {
		for (int i = 0; i < _tracks.size(); ++i)
			if (trk == _tracks.get(i))
				return i;
		return -1;
	}

	public void truncate(int i) {
		List<Track> newTracks = new ArrayList<Track>();
		List<Direction> newDirs = new ArrayList<Direction>();
		for(int x = 0; x < i; ++x) {
			newTracks.add(_tracks.get(x));
			newDirs.add(_dirs.get(x));
		}
		_tracks.clear();
		_dirs.clear();
		_tracks = newTracks;
		_dirs = newDirs;
	}

	public void append(TrackPath newPath) {
		int i, j;
		for (i = 0; i < newPath._tracks.size(); ++i) {
			Track newTrack = newPath._tracks.get(i);
			j = find(newTrack);
			if (j >= 0)
				continue; // already in our path
			_tracks.add(newTrack);
			_dirs.add(newPath._dirs.get(i));
		}
	}

	public Track findStation(Track position) {
		int i;
		Track track;
		for (i = _tracks.size(); --i >= 0; ) {
			track = _tracks.get(i);
			if (track == position)
				break;
		}
		while(i >= 0) {
		    track = _tracks.get(i);
		    if (track._isStation)
		    	return track;
		    --i;
		}
		return null;
	}

	public void insert(TrackPath newPath) {
		for(int i = 0; i < newPath._tracks.size(); ++i) {
			_tracks.add(0, newPath.getTrackAt(i));
			_dirs.add(0, newPath.getDirectionAt(i));
		}
	}

	public void reverse() {
		Track track;
		Direction dir;
		int i, j;
		j = _tracks.size() - 1;
		i = 0;
		while(i < j) {
			track = _tracks.get(i);
			_tracks.set(i, _tracks.get(j));
			_tracks.set(j, track);
			dir = _dirs.get(i);
			_dirs.set(i, _dirs.get(j));
			_dirs.set(j, dir);
			++i;
			--j;
		}
	}

	public void remove(Track track) {
		int index = _tracks.indexOf(track);
		if(index < 0)
			return; // impossible
		_tracks.remove(index);
		_dirs.remove(index);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("nItems: " + _tracks.size() + " ");
		int i;
		for (i = 0; i < _tracks.size(); ++i) {
			sb.append(" ");
			sb.append(_tracks.get(i)._position.toString());
		}
		return sb.toString();
	}
}
