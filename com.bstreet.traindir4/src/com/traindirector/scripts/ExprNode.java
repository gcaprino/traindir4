package com.traindirector.scripts;

public class ExprNode {

	public ExprNode(NodeOp op) {
		_op = op;
		_left = _right = null;
		_val = 0;
		_txt = null;
		_x = _y = 0;
	}

	NodeOp _op;
	ExprNode _left, _right;
	String _txt; // value for aspects compares
	int _val;
	int _x, _y; // coordinates of TrackRef, SwitchRef, SignalRef
}
