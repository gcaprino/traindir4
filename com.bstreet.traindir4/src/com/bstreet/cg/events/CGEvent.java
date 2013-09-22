package com.bstreet.cg.events;

public class CGEvent {
	Class<?> _type;
	protected Object _target;
	
	public CGEvent(Class<?> type, Object target) {
		_type = type;
		_target = target;
	}
	
}
