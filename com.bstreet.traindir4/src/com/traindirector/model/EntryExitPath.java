package com.traindirector.model;

import com.traindirector.model.Track;

public class EntryExitPath {
	public String _from;
	public String _to;
	public String _enter;
	public int[] _times;
	
	public EntryExitPath() {
		_times = new int[Track.NSPEEDS];
	}
}
