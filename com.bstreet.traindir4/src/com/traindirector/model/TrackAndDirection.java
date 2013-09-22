package com.traindirector.model;

public class TrackAndDirection {

	public Track	_track;
	public Direction _direction;

	public String toString() {
		return _track.toString() + "->" + _direction.toString();
	}
}
