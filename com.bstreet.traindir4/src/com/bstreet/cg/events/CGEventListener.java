package com.bstreet.cg.events;

public class CGEventListener {

	Class<?> _type;
	
	public CGEventListener(Class<?> type) {
		_type = type;
	}

	public void handle(CGEvent event, Object target) {
	}

}
