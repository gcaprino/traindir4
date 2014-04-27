package com.traindirector;

/**
 * Interface defining the application's command IDs.
 * Key bindings can be defined for specific commands.
 * To associate an action with a command, use IAction.setActionDefinitionId(commandId).
 *
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public interface ICommandIds {

    public static final String CMD_OPEN = "com.bstreet.cg.traindirector.open";
    public static final String CMD_CLOSE = "com.bstreet.cg.traindirector.close";
    public static final String CMD_SAVE = "com.bstreet.cg.traindirector.save";
    
    public static final String CMD_EDIT = "com.bstreet.cg.traindirector.edit";
    public static final String CMD_SAVE_CHANGES = "com.bstreet.cg.traindirector.savechanges";
    public static final String CMD_PREFERENCES = "com.traindirector.preferences";
    public static final String CMD_RUN = "com.bstreet.cg.traindirector.run";
    public static final String CMD_STOP = "com.bstreet.cg.traindirector.stop";
    public static final String CMD_FAST = "com.bstreet.cg.traindirector.fast";
    public static final String CMD_SLOW = "com.bstreet.cg.traindirector.slow";
    public static final String CMD_RESTART = "com.bstreet.cg.traindirector.restart";
    public static final String CMD_ITINERARY = "com.traindirector.itinerary";
    public static final String CMD_ASSIGN = "com.traindirector.assign";

    public static final String CMD_SHOW_SCHEDULE = "com.bstreet.cg.traindirector.show.schedule";
    public static final String CMD_SHOW_ALERTS = "com.bstreet.cg.traindirector.show.alerts";
    public static final String CMD_SHOW_TRAIN_STOPS = "com.bstreet.cg.traindirector.show.trainStops";
    public static final String CMD_SHOW_EDIT_TOOLS = "com.bstreet.cg.traindirector.show.edit.tools";

	public static final String CMD_RESTORE = "com.traindirector.restore";
	public static final String CMD_COORD_BARS = "com.traindirector.coordbars";
	public static final String CMD_INFO = "com.traindirector.info";
	public static final String CMD_EDIT_ITINERARIES = "com.traindirector.edit.itineraries";
	public static final String CMD_LATE_GRAPH = "com.traindirector.lategraph";
	public static final String CMD_NEW = "com.traindirector.new";
	public static final String CMD_SET_SIGNALS_TO_GREEN = "com.traindirector.settogreen";
	public static final String CMD_SHOW_INFO_PAGE = "com.traindirector.infopage";
	public static final String CMD_SHOW_LAYOUT_PAGE = "com.traindirector.layoutpage";
	public static final String CMD_SHOW_PERFORMANCE = "com.traindirector.performance";
	public static final String CMD_SKIP_AHEAD = "com.traindirector.skipahead";
	public static final String CMD_STATION_SCHEDULE = "com.traindirector.stationschedule";
	public static final String CMD_STATIONS_LIST = "com.traindirector.stationslist";
	public static final String CMD_STATUS_BAR = "com.traindirector.statusbar";
	public static final String CMD_SWITCHBOARD = "com.traindirector.switchboard";
	public static final String CMD_TIME_DISTANCE_GRAPH = "com.traindirector.timedistance";
    public static final String CMD_PLATFORM_OCCUPANCY = "com.traindirector.platformoccupancy";
	public static final String CMD_ZOOM_IN = "com.traindirector.zoomin";
	public static final String CMD_ZOOM_OUT = "com.traindirector.zoomout";

	public static final String CMD_SHOW_WELCOME_PAGE = "com.traindirector.showwelcomepage";
    public static final String CMD_SHOW_MAP = "com.traindirector.showmap";

	public static final String CMD_HELP_LANGUAGE = "com.bstreet.cg.traindirector.help.language";
	public static final String CMD_HELP_UPDATE = "com.bstreet.cg.traindirector.help.update";
    
    // UNUSED
    public static final String CMD_OPEN_MESSAGE = "com.bstreet.cg.traindirector.openMessage";
	public static final String CMD_SHOW_TRACE = "com.traindirector.showtrace";
	public static final String CMD_SHOW_ASSETS = "com.traindirector.show.assets";
}
