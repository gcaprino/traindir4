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

    public static final String CMD_HELP_LANGUAGE = "com.bstreet.cg.traindirector.help.language";
    
    // UNUSED
    public static final String CMD_OPEN_MESSAGE = "com.bstreet.cg.traindirector.openMessage";
    
    
}
