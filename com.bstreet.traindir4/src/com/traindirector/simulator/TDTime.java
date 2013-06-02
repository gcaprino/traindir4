package com.traindirector.simulator;

public class TDTime {
	
	public static String toString(int tim) {
		StringBuilder sb = new StringBuilder();
		sb.append((tim / 3600));
		sb.append(':');
		int h = (tim / 60) % 60;
		if(h < 10)
			sb.append('0');
		sb.append(h);
		sb.append(':');
		h = tim % 60;
		if(h < 10)
			sb.append('0');
		sb.append(h);
		return sb.toString();
	}

}
