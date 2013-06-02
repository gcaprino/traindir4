package com.traindirector.model;

public class TextTrack extends Track {

	public boolean linkedToWest(Track track) {
		if (_wlink != null && track._position.sameAs(_wlink))
			return true;
		return false;
	}

	public boolean linkedToEast(Track track) {
		if (_elink != null && track._position.sameAs(_elink))
			return true;
		return false;
	}
}
