package com.bstreet.cg.events;

import java.util.LinkedList;
import java.util.List;

public class CGEventDispatcher implements Runnable {

	public static CGEventDispatcher INSTANCE = new CGEventDispatcher();
	public static List<CGEvent> _eventQueue;
	public static List<CGEventListener> _eventListeners;
	public static Thread _thread;

	private CGEventDispatcher() {
		INSTANCE = this;
		_eventQueue = new LinkedList<CGEvent>();
		_eventListeners = new LinkedList<CGEventListener>();
		_thread = new Thread(this);
		_thread.start();
	}

	public static CGEventDispatcher getInstance() {
		return INSTANCE;
	}

	public void postEvent(CGEvent event) {
		synchronized(_eventQueue) {
			_eventQueue.add(event);
		}
	}
	
	public void addListener(CGEventListener l) {
		synchronized(_eventListeners) {
			// TODO: use a map to keep list of listeners for l._type
			_eventListeners.add(l);
		}
	}
	
	public void removeListener(CGEventListener l) {
		synchronized(_eventListeners) {
			// TODO: use a map to keep list of listeners for l._type
			_eventListeners.remove(l);
		}
	}

	@Override
	public void run() {
		while(true) {
			CGEvent[] events;
			synchronized(_eventQueue) {
				events = new CGEvent[_eventQueue.size()];
				for(int i = 0; i < _eventQueue.size(); ++i) {
					events[i] = _eventQueue.get(i);
				}
				_eventQueue.clear();
			}
			for(CGEvent event : events) {
				CGEventListener[] listeners;
				synchronized(_eventListeners) {
					listeners = new CGEventListener[_eventListeners.size()];
					for(int j = 0; j < _eventListeners.size(); ++j) {
						listeners[j] = _eventListeners.get(j);
					}
				}
				for(CGEventListener l : listeners) {
					// TODO: use a map to get list of listeners for event._type
					if(l._type == event._type)
						l.handle(event, event._target);
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

}
