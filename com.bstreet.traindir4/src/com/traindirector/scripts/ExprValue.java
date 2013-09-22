package com.traindirector.scripts;

import com.traindirector.model.Signal;
import com.traindirector.model.Track;
import com.traindirector.model.Train;

public class ExprValue {
	ExprValue(NodeOp op) {
		_op = op;
		_txt = null;
		_val = 0;
		_track = null;
		_signal = null;
		_train = null;
	};

	public NodeOp _op;
	public Track _track;
	public Signal _signal;
	public Train _train;
	public String _txt;
	public int _val;
}
