package com.traindirector.scripts;

public enum NodeOp {
		Equal,			// bool if left == right OR assignment
		NotEqual,		// bool if left != right
		Less,			// bool if left < right
		Greater,		// bool if left > right
		LessEqual,		// bool if left <= right
		GreaterEqual,	// bool if left >= right
		And,			// bool if left && right
		Or,				// bool if left || right
		Dot,			// result is left.value
		TrackRef,		// Track(x, y)   OR  Track(value)
		SwitchRef,		// Switch(x, y)  OR  Switch(value)
		SignalRef,		// Signal(x, y)  OR  Signal(value)
		NextSignalRef,	// Signal,
		NextApproachRef,// Signal,
		LinkedRef,		// Image to Switch/Signal
		TrainRef,
		Addr,			// Ref + Dot
		Random,			// return 0..100
		Time,			// current time, in decimal hhmm
		None,
		Bool,
		Number,
		String;

}
