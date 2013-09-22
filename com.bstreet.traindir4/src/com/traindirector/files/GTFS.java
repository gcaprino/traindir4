package com.traindirector.files;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import com.traindirector.simulator.Simulator;

public class GTFS {

	String _dirName;
	private Simulator _simulator;
	
	public List<GTFS_Agency> _agencies;
	public List<GTFS_Stop> _stops;
	public List<GTFS_Route> _routes;
	public List<GTFS_Trip> _trips;
	public List<GTFS_Calendar> _calendar;
	public List<GTFS_CalendarDate> _calendarDates;
	public List<GTFS_FareAttribute> _fareAttributes;
	public List<GTFS_Shape> _shapes;
	public List<GTFS_FareRule> _fareRules;
	public List<GTFS_Frequency> _frequencies;
	public List<GTFS_Transfer> _transfers;
	public List<GTFS_StopTime> _stopTimes;
	//GTFS_FeedInfo _feedInfo;
	
	public GTFS(Simulator simulator, String dirName) {
		_simulator = simulator;
		_dirName = dirName;
	}
	
	public BufferedReader exists(String name) {
		String fname = _simulator._baseDir + "/" + _dirName + "/" + name + ".txt";
		BufferedReader rdr = _simulator.getReaderForFile(fname);
		return rdr;
	}
	
	public boolean load() {
		_agencies = new ArrayList<GTFS_Agency>();
		_stops = new ArrayList<GTFS_Stop>();
		_routes = new ArrayList<GTFS_Route>();
		_trips = new ArrayList<GTFS_Trip>();
		_calendar = new ArrayList<GTFS_Calendar>();
		_calendarDates = new ArrayList<GTFS_CalendarDate>();
		_fareAttributes = new ArrayList<GTFS_FareAttribute>();
		_shapes = new ArrayList<GTFS_Shape>();
		_fareRules = new ArrayList<GTFS_FareRule>();
		_frequencies = new ArrayList<GTFS_Frequency>();
		_transfers = new ArrayList<GTFS_Transfer>();
		_stopTimes = new ArrayList<GTFS_StopTime>();
		
        BufferedReader rdr;
        if((rdr = exists("agency")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int agencyId       = csv.findColumn(("agency_id"));
                int agencyName     = csv.findColumn(("agency_name"));
                int agencyUrl      = csv.findColumn(("agency_url"));
                int agencyTimeZone = csv.findColumn(("agency_timezone"));
                int agencyLang     = csv.findColumn(("agency_lang"));
                int agencyPhone    = csv.findColumn(("agency_phone"));
                int agencyFareUrl  = csv.findColumn(("agency_fare_url"));
                while(csv.readLine()) {
                    GTFS_Agency a = new GTFS_Agency();
                    a._agencyId = csv.getValue(agencyId);
                    a._agencyName = csv.getValue(agencyName);
                    a._agencyUrl = csv.getValue(agencyUrl);
                    a._agencyTimeZone = csv.getValue(agencyTimeZone);
                    a._agencyLang = csv.getValue(agencyLang);
                    a._agencyPhone = csv.getValue(agencyPhone);
                    a._agencyFareUrl = csv.getValue(agencyFareUrl);
                    _agencies.add(a);
                }
            }
        }

        if((rdr = exists("stops")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int stopId = csv.findColumn(("stop_id"));
                int stopCode = csv.findColumn(("stop_code"));
                int stopName = csv.findColumn(("stop_name"));
                int stopDesc = csv.findColumn(("stop_desc"));
                int stopLat = csv.findColumn(("stop_lat"));
                int stopLon = csv.findColumn(("stop_lon"));
                int zoneId = csv.findColumn(("zone_id"));
                int stopUrl = csv.findColumn(("stop_url"));
                int locationType = csv.findColumn(("location_type"));
                int parentStation = csv.findColumn(("parent_station"));
                int stopTimeZone = csv.findColumn(("stop_timezone"));
                int wheelchairBoarding = csv.findColumn(("wheelchair_boarding"));
                while(csv.readLine()) {
                    GTFS_Stop s = new GTFS_Stop();
                    s._stopId = csv.getValue(stopId);
                    s._stopCode = csv.getValue(stopCode);
                    s._stopName = csv.getValue(stopName);
                    s._stopDesc = csv.getValue(stopDesc);
                    s._stopLat = csv.getValue(stopLat);
                    s._stopLon = csv.getValue(stopLon);
                    s._zoneId = csv.getValue(zoneId);
                    s._stopUrl = csv.getValue(stopUrl);
                    s._locationType = csv.getValue(locationType);
                    s._parentStation = csv.getValue(parentStation);
                    s._stopTimeZone = csv.getValue(stopTimeZone);
                    s._wheelchairBoarding = csv.getValue(wheelchairBoarding);
                    _stops.add(s);
                }
            }
        } else
            return false;

        if((rdr = exists("routes")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int routeId = csv.findColumn(("route_id"));
                int agencyId = csv.findColumn(("agency_id"));
                int routeShortName = csv.findColumn(("route_short_name"));
                int routeLongName = csv.findColumn(("route_long_name"));
                int routeDesc = csv.findColumn(("route_desc"));
                int routeType = csv.findColumn(("route_type"));
                int routeUrl = csv.findColumn(("route_url"));
                int routeColor = csv.findColumn(("route_color"));
                int routeTextColor = csv.findColumn(("route_text_color"));
                while(csv.readLine()) {
                    GTFS_Route r = new GTFS_Route();
                    r._routeId = csv.getValue(routeId);
                    r._agencyId = csv.getValue(agencyId);
                    r._routeShortName = csv.getValue(routeShortName);
                    r._routeLongName = csv.getValue(routeLongName);
                    r._routeDesc = csv.getValue(routeDesc);
                    r._routeDesc = csv.getValue(routeType);
                    r._routeUrl = csv.getValue(routeUrl);
                    r._routeColor = csv.getValueHex(routeColor);
                    r._routeTextColor = csv.getValueHex(routeTextColor);
                    _routes.add(r);
                }
            }
        }


        if((rdr = exists("trips")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int routeId = csv.findColumn(("route_id"));
                int serviceId = csv.findColumn(("service_id"));
                int tripId = csv.findColumn(("trip_id"));
                int tripHeadSign = csv.findColumn(("trip_head_sign"));
                int tripShortName = csv.findColumn(("trip_short_name"));
                int directionId = csv.findColumn(("direction_id"));
                int blockId = csv.findColumn(("block_id"));
                int shapeId = csv.findColumn(("shape_id"));
                while(csv.readLine()) {
                    GTFS_Trip t = new GTFS_Trip();

                    t._routeId = csv.getValue(routeId);
                    t._serviceId = csv.getValue(serviceId);
                    t._tripId = csv.getValue(tripId);
                    t._tripHeadsign = csv.getValue(tripHeadSign);
                    t._tripShortName = csv.getValue(tripShortName);
                    t._directionId = csv.getValue(directionId);
                    t._blockId = csv.getValue(blockId);
                    t._shapeId = csv.getValue(shapeId);
                    _trips.add(t);
                }
            }
        }


        if((rdr = exists("stop_times")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int tripId = csv.findColumn(("trip_id"));
                int arrivalTime = csv.findColumn(("arrival_time"));
                int departureTime = csv.findColumn(("departure_time"));
                int stopId = csv.findColumn(("stop_id"));
                int stopSequence = csv.findColumn(("stop_sequence"));
                int stopHeadsign = csv.findColumn(("stop_headsign"));
                int pickupType = csv.findColumn(("pickup_type"));
                int dropoffType = csv.findColumn(("dropoff_type"));
                int shapeDistTraveled = csv.findColumn(("shape_dist_traveled"));
                while(csv.readLine()) {
                    GTFS_StopTime t = new GTFS_StopTime();

                    t._tripId = csv.getValue(tripId);
                    t._arrivalTime = csv.getValue(arrivalTime);
                    t._departureTime = csv.getValue(departureTime);
                    t._stopId = csv.getValue(stopId);
                    t._stopSequence = csv.getValue(stopSequence);
                    t._stopHeadsign = csv.getValue(stopHeadsign);
                    t._pickupType = csv.getValue(pickupType);
                    t._dropoffType = csv.getValue(dropoffType);
                    t._shapeDistTraveled = csv.getValue(shapeDistTraveled);
                    _stopTimes.add(t);
                }
            }
        }


        if((rdr = exists("calendar")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int serviceId = csv.findColumn(("service_id"));
                int mon = csv.findColumn(("monday"));
                int tue = csv.findColumn(("tuesday"));
                int wed = csv.findColumn(("wednesday"));
                int thu = csv.findColumn(("thursday"));
                int fri = csv.findColumn(("friday"));
                int sat = csv.findColumn(("saturday"));
                int sun = csv.findColumn(("sunday"));
                int startDate = csv.findColumn(("start_date"));
                int endDate = csv.findColumn(("end_date"));
                while(csv.readLine()) {
                    GTFS_Calendar c = new GTFS_Calendar();

                    c._serviceId = csv.getValue(serviceId);
                    c._days[0] = csv.getValueAsBool(mon); 
                    c._days[1] = csv.getValueAsBool(tue); 
                    c._days[2] = csv.getValueAsBool(wed); 
                    c._days[3] = csv.getValueAsBool(thu); 
                    c._days[4] = csv.getValueAsBool(fri); 
                    c._days[5] = csv.getValueAsBool(sat); 
                    c._days[6] = csv.getValueAsBool(sun); 
                    c._startDate = csv.getValue(startDate);
                    c._endDate = csv.getValue(endDate);
                    _calendar.add(c);
                }
            }
        }

        if((rdr = exists("calendar_dates")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int serviceId = csv.findColumn(("service_id"));
                int date = csv.findColumn(("date"));
                int exceptionType = csv.findColumn(("exception_type"));
                while(csv.readLine()) {
                    GTFS_CalendarDate c = new GTFS_CalendarDate();

                    c._serviceId = csv.getValue(serviceId);
                    c._date = csv.getValue(date);
                    c._exceptionType = csv.getValue(exceptionType);
                    _calendarDates.add(c);
                }
            }
        }

        if((rdr = exists("fare_attributes")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int fareId = csv.findColumn(("fare_id"));
                int price = csv.findColumn(("price"));
                int currencyType = csv.findColumn(("currency_type"));
                int paymentMethod = csv.findColumn(("payment_method"));
                int transfers = csv.findColumn(("transfers"));
                int transferDuration = csv.findColumn(("transfer_duration"));
                while(csv.readLine()) {
                    GTFS_FareAttribute f = new GTFS_FareAttribute();

                    f._fareId = csv.getValue(fareId);
                    f._price = csv.getValue(price);
                    f._currencyType = csv.getValue(currencyType);
                    f._paymentMethod = csv.getValue(paymentMethod);
                    f._transfers = csv.getValue(transfers);
                    f._transferDuration = csv.getValue(transferDuration);
                    _fareAttributes.add(f);
                }
            }
        }


        if((rdr = exists("fare_rules")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int fareId = csv.findColumn(("fare_id"));
                int routeId = csv.findColumn(("route_id"));
                int originId = csv.findColumn(("origin_id"));
                int destinationId = csv.findColumn(("destination_id"));
                int containsId = csv.findColumn(("contains_id"));
                while(csv.readLine()) {
                    GTFS_FareRule f = new GTFS_FareRule();

                    f._fareId = csv.getValue(fareId);
                    f._routeId = csv.getValue(routeId);
                    f._originId = csv.getValue(originId);
                    f._destinationId = csv.getValue(destinationId);
                    f._containsId = csv.getValue(containsId);
                    _fareRules.add(f);
                }
            }
        }

        if((rdr = exists("shapes")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int shapeId = csv.findColumn(("shape_id"));
                int shapePtLat = csv.findColumn(("shape_pt_lat"));
                int shapePtLon = csv.findColumn(("shape_pt_lon"));
                int shapePtSequence = csv.findColumn(("shape_pt_sequence"));
                int shapeDistTraveled = csv.findColumn(("shape_dist_traveled"));
                while(csv.readLine()) {
                    GTFS_Shape s = new GTFS_Shape();

                    s._shapeId = csv.getValue(shapeId);
                    s._shapePtLat = csv.getValue(shapePtLat);
                    s._shapePtLon = csv.getValue(shapePtLon);
                    s._shapePtSequence = csv.getValue(shapePtSequence);
                    s._shapeDistTraveled = csv.getValue(shapeDistTraveled);
                    _shapes.add(s);
                }
            }
        }


        if((rdr = exists("frequencies")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int tripId = csv.findColumn(("trip_id"));
                int startTime = csv.findColumn(("start_time"));
                int endTime = csv.findColumn(("end_time"));
                int headwaySecs = csv.findColumn(("headways_secs"));
                int exactTimes = csv.findColumn(("exact_times"));
                while(csv.readLine()) {
                    GTFS_Frequency f = new GTFS_Frequency();

                    f._tripId = csv.getValue(tripId);
                    f._startTime = csv.getValue(startTime);
                    f._endTime = csv.getValue(endTime);
                    f._headwaySecs = csv.getValue(headwaySecs);
                    f._exactTimes = csv.getValue(exactTimes);
                    _frequencies.add(f);
                }
            }
        }


        if((rdr = exists("transfers")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int fromStopId = csv.findColumn(("from_stop_id"));
                int toStopId = csv.findColumn(("to_stop_id"));
                int transferType = csv.findColumn(("transfer_type"));
                int minTransferTime = csv.findColumn(("min_transfer_time"));
                while(csv.readLine()) {
                    GTFS_Transfer t = new GTFS_Transfer();

                    t._fromStopId = csv.getValue(fromStopId);
                    t._toStopId = csv.getValue(toStopId);
                    t._transferType = csv.getValue(transferType);
                    t._minTransferTime = csv.getValue(minTransferTime);
                    _transfers.add(t);
                }
            }
        }

        /*
         * TODO
        if((rdr = exists("feed_info")) != null) {
        	CSVFile csv = new CSVFile(rdr);
            if(csv.readColumns()) {
                int feedPublisherName = csv.findColumn(("feed_publisher_name"));
                int feedPublisherUrl = csv.findColumn(("feed_publisher_url"));
                int feedLang = csv.findColumn(("feed_lang"));
                int feedStartDate = csv.findColumn(("feed_start_date"));
                int feedEndDate = csv.findColumn(("feed_end_date"));
                int feedVersion = csv.findColumn(("feed_version"));
                if(csv.readLine()) {
                    csv.getValue(_feedInfo._feedPublisherName, feedPublisherName);
                    csv.getValue(_feedInfo._feedPublisherUrl, feedPublisherUrl);
                    csv.getValue(_feedInfo._feedLang, feedLang);
                    csv.getValue(_feedInfo._feedStartDate, feedStartDate);
                    csv.getValue(_feedInfo._feedEndDate, feedEndDate);
                    csv.getValue(_feedInfo._feedVersion, feedVersion);
                }
            }
        }
         */

        return true;

	}

	String[] _ourRoutes;
	
	public void setOurRoutes(String ourRoutes) {
		_ourRoutes = ourRoutes.split(",");
	}

	public boolean ignoreRoute(String routeId) {
        if(_ourRoutes == null || _ourRoutes.length == 0)
            return false;       // no route list specified, allow all
        for(String rt : _ourRoutes) 
        	if(rt.equals(routeId))
        		return true;
        return false;        // not found in our list, so ignore it
    }

	public GTFS_Calendar findCalendarByService(String serviceId) {
        for(int c = 0; c < _calendar.size(); ++c) {
            GTFS_Calendar calEntry = _calendar.get(c);
            if(calEntry._serviceId.equals(serviceId)) {
                return calEntry;
            }
        }
		return null;
	}

	public GTFS_Route findRouteById(String routeId) {

        int r;
        GTFS_Route route;

        for(r = 0; r < _routes.size(); ++r) {
            route = _routes.get(r);
            if(route._routeId.equals(routeId))
                return route;
        }
		return null;
	}

}
