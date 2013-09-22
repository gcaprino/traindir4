package com.traindirector.files;

public class GTFS_Calendar {

	public String _serviceId;
	public boolean[] _days = new boolean[7];
	public String _startDate;
	public String _endDate;
	public int getMask() {
        int days = 0;
        int m = 1;
        int i;
        for(i = 0; i < 7; ++i) {
            if(_days[i])
                days |= m;
            m <<= 1;
        }
        return days;
	}
}
