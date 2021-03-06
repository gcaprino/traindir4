package com.traindirector.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.traindirector.files.XPMFile;
import com.traindirector.model.TDIcon;

public class IconFactory {

	List<TDIcon> _icons;
	public TDIcon _speedIcon;
	public TDIcon _cameraIcon;
	public TDIcon _itinIcon;
	public TDIcon _tracksIcon;
	public TDIcon _signalsIcon;
	public TDIcon _switchesIcon;
	public TDIcon _toolsIcon;
	public TDIcon _actionsIcon;
	
	public IconFactory() {
		_icons = new ArrayList<TDIcon>();
		initSignalIcons();
	}
	
	public void clear() {
		_icons.clear();		// forget icons defined by last simulation
		initSignalIcons();	// and restart with the default set of icons
	}
	
	public void initSignalIcons() {
		_icons.add(new TDIcon(":icons:sig_e_r1.xpm", sig_e_r1));
		_icons.add(new TDIcon(":icons:sig_w_r1.xpm", sig_w_r1));
		_icons.add(new TDIcon(":icons:sig_n_r1.xpm", sig_n_r1));
		_icons.add(new TDIcon(":icons:sig_s_r1.xpm", sig_s_r1));

		_icons.add(new TDIcon(":icons:sig_e_g1.xpm", sig_e_g1));
		_icons.add(new TDIcon(":icons:sig_w_g1.xpm", sig_w_g1));
		_icons.add(new TDIcon(":icons:sig_n_g1.xpm", sig_n_g1));
		_icons.add(new TDIcon(":icons:sig_s_g1.xpm", sig_s_g1));

		_icons.add(new TDIcon(":icons:sig_e_r2.xpm", sig_e_r2));
		_icons.add(new TDIcon(":icons:sig_w_r2.xpm", sig_w_r2));
		_icons.add(new TDIcon(":icons:sig_n_r2.xpm", sig_n_r2));
		_icons.add(new TDIcon(":icons:sig_s_r2.xpm", sig_s_r2));

		_icons.add(new TDIcon(":icons:sig_e_g2.xpm", sig_e_g2));
		_icons.add(new TDIcon(":icons:sig_w_g2.xpm", sig_w_g2));
		_icons.add(new TDIcon(":icons:sig_n_g2.xpm", sig_n_g2));
		_icons.add(new TDIcon(":icons:sig_s_g2.xpm", sig_s_g2));

		_icons.add(new TDIcon(":icons:sig_e_r2fleeted.xpm", sig_e_r2fleeted));
		_icons.add(new TDIcon(":icons:sig_w_r2fleeted.xpm", sig_w_r2fleeted));
		_icons.add(new TDIcon(":icons:sig_n_r2fleeted.xpm", sig_n_r2fleeted));
		_icons.add(new TDIcon(":icons:sig_s_r2fleeted.xpm", sig_s_r2fleeted));

		_icons.add(new TDIcon(":icons:sig_e_g2fleeted.xpm", sig_e_g2fleeted));
		_icons.add(new TDIcon(":icons:sig_w_g2fleeted.xpm", sig_w_g2fleeted));
		_icons.add(new TDIcon(":icons:sig_n_g2fleeted.xpm", sig_n_g2fleeted));
		_icons.add(new TDIcon(":icons:sig_s_g2fleeted.xpm", sig_s_g2fleeted));

		_icons.add(new TDIcon(":icons:etrain1.xpm", e_train_xpm));
		_icons.add(new TDIcon(":icons:wtrain1.xpm", w_train_xpm));
		_icons.add(new TDIcon(":icons:car1.xpm", car_xpm));
		_icons.add(new TDIcon(":icons:etrain2.xpm", e_train_xpm));
		_icons.add(new TDIcon(":icons:wtrain2.xpm", w_train_xpm));
		_icons.add(new TDIcon(":icons:car2.xpm", car_xpm));
		_icons.add(new TDIcon(":icons:etrain3.xpm", e_train_xpm));
		_icons.add(new TDIcon(":icons:wtrain3.xpm", w_train_xpm));
		_icons.add(new TDIcon(":icons:car3.xpm", car_xpm));
		_icons.add(new TDIcon(":icons:etrain4.xpm", e_train_xpm));
		_icons.add(new TDIcon(":icons:wtrain4.xpm", w_train_xpm));
		_icons.add(new TDIcon(":icons:car4.xpm", car_xpm));
		_icons.add(new TDIcon(":icons:etrain5.xpm", e_train_xpm));
		_icons.add(new TDIcon(":icons:wtrain5.xpm", w_train_xpm));
		_icons.add(new TDIcon(":icons:car5.xpm", car_xpm));
		_icons.add(new TDIcon(":icons:etrain6.xpm", e_train_xpm));
		_icons.add(new TDIcon(":icons:wtrain6.xpm", w_train_xpm));
		_icons.add(new TDIcon(":icons:car6.xpm", car_xpm));
		_icons.add(new TDIcon(":icons:etrain7.xpm", e_train_xpm));
		_icons.add(new TDIcon(":icons:wtrain7.xpm", w_train_xpm));
		_icons.add(new TDIcon(":icons:car7.xpm", car_xpm));
		_icons.add(new TDIcon(":icons:etrain8.xpm", e_train_xpm));
		_icons.add(new TDIcon(":icons:wtrain8.xpm", w_train_xpm));
		_icons.add(new TDIcon(":icons:car8.xpm", car_xpm));
		
		_icons.add(_itinIcon = new TDIcon(":icons:itinButton.xpm", itin_xpm));
		_icons.add(_cameraIcon = new TDIcon(":icons:camera.xpm", camera_xpm));

		_speedIcon = new TDIcon(":icons:speed.xpm", speed_xpm);
		
		_tracksIcon = new TDIcon(":icons:edit:tracks.xpm", tracks_xpm);
		_signalsIcon = new TDIcon(":icons:edit:signals.xpm", signals_xpm);
		_switchesIcon = new TDIcon(":icons:edit:switches.xpm", switches_xpm);
		_toolsIcon = new TDIcon(":icons:edit:tools.xpm", tools_xpm);
		_actionsIcon = new TDIcon(":icons:edit:actions.xpm", actions_xpm);
		
		_icons.add(new TDIcon(":icons:edit:moveStart.xpm", move_start_xpm));
		_icons.add(new TDIcon(":icons:edit:moveEnd.xpm", move_end_xpm));
		_icons.add(new TDIcon(":icons:edit:moveDest.xpm", move_dest_xpm));

		_icons.add(_tracksIcon);
		_icons.add(_signalsIcon);
		_icons.add(_switchesIcon);
		_icons.add(_toolsIcon);
		_icons.add(_actionsIcon);
	}
	

	public TDIcon get(String iconFileName) {
		if(iconFileName == null)
			return null;
		for(TDIcon icon : _icons) {
			if(icon._xpmFile.compareTo(iconFileName) == 0) {
				return icon;
			}
		}
		BufferedReader rdr = Simulator.INSTANCE._fileManager.getReaderForFile(iconFileName);
		if (rdr == null)
			return null;
		TDIcon icon = null;
		XPMFile xpmFile = new XPMFile(iconFileName);
		if (xpmFile.load(rdr)) {
			icon = new TDIcon(iconFileName);
			icon._xpmBytes = xpmFile.getLines();
			_icons.add(icon);
		}
		try {
			rdr.close();
		} catch (IOException e) {
		}
		return icon;
	}
	
	static String sig_e_r1[] = {
		"9 7 3 1",
		"   c None",
		".  c #000000",
		"G  c #FF0000",
		"         ",
		"         ",
		".    ... ",
		".   .GGG.",
		".....GGG.",
		".   .GGG.",
		".    ... "};
	static String sig_e_g1[] = {
		"9 7 3 1",
		"   c None",
		".  c #000000",
		"G  c #00FF00",
		"         ",
		"         ",
		".    ... ",
		".   .GGG.",
		".....GGG.",
		".   .GGG.",
		".    ... "};
	static String sig_w_r1[] = {
		"9 7 3 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"         ",
		"         ",
		" ...    .",
		".GGG.   .",
		".GGG.....",
		".GGG.   .",
		" ...    ."
		};
	static String sig_w_g1[] = {
		"9 7 3 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"         ",
		"         ",
		" ...    .",
		".GGG.   .",
		".GGG.....",
		".GGG.   .",
		" ...    ."
		};

	static String sig_n_r1[] = {
		"7 9 3 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"       ",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  ",
		"   .   ",
		"   .   ",
		" ..... "};
	static String sig_n_g1[] = {
		"7 9 3 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"       ",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  ",
		"   .   ",
		"   .   ",
		" ..... "};
	static String sig_s_r1[] = {
		"7 9 3 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"       ",
		" ..... ",
		"   .   ",
		"   .   ",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  "};
	static String sig_s_g1[] = {
		"7 9 3 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"       ",
		" ..... ",
		"   .   ",
		"   .   ",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  "};

	static String sig_e_r2[] = {
		"13 7 4 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"X      c #000000",
		"             ",
		"             ",
		".   ...  ... ",
		".  .XXX..GGG.",
		"....XXX..GGG.",
		".  .XXX..GGG.",
		".   ...  ... "};

	static String sig_e_g2[] = {
		"13 7 4 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"X      c #000000",
		"             ",
		"             ",
		".   ...  ... ",
		".  .XXX..GGG.",
		"....XXX..GGG.",
		".  .XXX..GGG.",
		".   ...  ... "};

	static String sig_e_g2fleeted[] = {
		"13 7 4 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"X      c #00FF00",
		"             ",
		"             ",
		".   ...  ... ",
		".  .XXX..GGG.",
		"....XXX..GGG.",
		".  .XXX..GGG.",
		".   ...  ... "};
	static String sig_e_r2fleeted[] = {
		"13 7 4 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"X      c #FFFF00",
		"             ",
		"             ",
		".   ...  ... ",
		".  .XXX..GGG.",
		"....XXX..GGG.",
		".  .XXX..GGG.",
		".   ...  ... "};

	static String sig_w_r2[] = {
		"13 7 4 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"X      c #000000",
		"             ",
		"             ",
		" ...  ...   .",
		".GGG..XXX.  .",
		".GGG..XXX....",
		".GGG..XXX.  .",
		" ...  ...   ."};
	static String sig_w_g2[] = {
		"13 7 4 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"X      c #000000",
		"             ",
		"             ",
		" ...  ...   .",
		".GGG..XXX.  .",
		".GGG..XXX....",
		".GGG..XXX.  .",
		" ...  ...   ."};
	static String sig_w_g2fleeted[] = {
		"13 7 4 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"X      c #00FF00",
		"             ",
		"             ",
		" ...  ...   .",
		".GGG..XXX.  .",
		".GGG..XXX....",
		".GGG..XXX.  .",
		" ...  ...   ."};
	static String sig_w_r2fleeted[] = {
		"13 7 4 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"X      c #FFFF00",
		"             ",
		"             ",
		" ...  ...   .",
		".GGG..XXX.  .",
		".GGG..XXX....",
		".GGG..XXX.  .",
		" ...  ...   ."};

	static String sig_n_r2[] = {
		"7 13 4 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"X      c #000000",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  ",
		"  ...  ",
		" ..... ",
		" ..... ",
		" ..... ",
		"  ...  ",
		"   .   ",
		"   .   ",
		" ..... "};
	static String sig_n_g2[] = {
		"7 13 4 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"X      c #000000",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  ",
		"  ...  ",
		" ..... ",
		" ..... ",
		" ..... ",
		"  ...  ",
		"   .   ",
		"   .   ",
		" ..... "};
	static String sig_n_g2fleeted[] = {
		"7 13 4 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"X      c #00FF00",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  ",
		"  ...  ",
		" .XXX. ",
		" .XXX. ",
		" .XXX. ",
		"  ...  ",
		"   .   ",
		"   .   ",
		" ..... "};

	static String sig_n_r2fleeted[] = {
		"7 13 4 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"X      c #FFFF00",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  ",
		"  ...  ",
		" .XXX. ",
		" .XXX. ",
		" .XXX. ",
		"  ...  ",
		"   .   ",
		"   .   ",
		" ..... "};
	static String sig_s_r2[] = {
		"7 13 4 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"X      c #000000",
		" ..... ",
		"   .   ",
		"   .   ",
		"  ...  ",
		" .XXX. ",
		" .XXX. ",
		" .XXX. ",
		"  ...  ",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  "};
	static String sig_s_g2[] = {
		"7 13 4 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"X      c #000000",
		" ..... ",
		"   .   ",
		"   .   ",
		"  ...  ",
		" .XXX. ",
		" .XXX. ",
		" .XXX. ",
		"  ...  ",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  "};
	static String sig_s_g2fleeted[] = {
		"7 13 4 1",
		"       c None",
		".      c #000000",
		"G      c #00FF00",
		"X      c #00FF00",
		" ..... ",
		"   .   ",
		"   .   ",
		"  ...  ",
		" .XXX. ",
		" .XXX. ",
		" .XXX. ",
		"  ...  ",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  "};
	static String sig_s_r2fleeted[] = {
		"7 13 4 1",
		"       c None",
		".      c #000000",
		"G      c #FF0000",
		"X      c #FFFF00",
		" ..... ",
		"   .   ",
		"   .   ",
		"  ...  ",
		" .XXX. ",
		" .XXX. ",
		" .XXX. ",
		"  ...  ",
		"  ...  ",
		" .GGG. ",
		" .GGG. ",
		" .GGG. ",
		"  ...  "};
	
	static String e_train_xpm[] = {
		"13 10 3 1",
		"       c None",
		".      c #000000",
		"X      c #000FFF",
		"             ",
		"...........  ",
		".XXXXXXXXX.. ",
		".X..X..X..X..",
		".XXXXXXXXXXX.",
		".XXXXXXXXXXX.",
		".............",
		"  ...   ...  ",
		"             ",
		"             "};

		static String w_train_xpm[] = {
		"13 10 3 1",
		"       c None",
		".      c #000000",
		"X      c #000FFF",
		"             ",
		"  ...........",
		" ..XXXXXXXXX.",
		"..X.X..X..XX.",
		".XXXXXXXXXXX.",
		".XXXXXXXXXXX.",
		".............",
		"  ...   ...  ",
		"             ",
		"             "};

		static	String car_xpm[] = {	/* same for both e and w */
		"13 10 3 1",
		"       c None",
		".      c #000000",
		"X      c #000FFF",
		"             ",
		"............ ",
		".XXXXXXXXXX. ",
		".X..X..X..X. ",
		".XXXXXXXXXX. ",
		"XXXXXXXXXXXX ",
		"............ ",
		" ...    ...  ",
		"             ",
		"             "};

		static	String speed_xpm[] = {
		"8 3 2 1",
		"       c None",
		".      c #000000",
		"  ....  ",
		" ..  .. ",
		"  ....  "};

		static	String camera_xpm[] = {
		"13 10 2 1",
		"       c None",
		".      c #000000",
		"             ",
		"   ..        ",
		" ........... ",
		" . ..      . ",
		" .   ...   . ",
		" .   . .   . ",
		" .   ...   . ",
		" .         . ",
		" ........... ",
		"             "};

		static	String itin_xpm[] = {
		"8 9 4 1",
		"       c None",
		".      c #808080",
		"X      c #C0c0c0",
		"#      c #000000",
		"        ",
		"  ....  ",
		" ...... ",
		"..XXXX..",
		".XXXXXX.",
		"..XXXX..",
		"#......#",
		" #....# ",
		"  ####  "
		};

		public static String[] tracks_xpm = {
			"26 26 2 1",
			"  c #000000",
			"! c #C0C0C0",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!             !!!!!!!!!!!!",
			"!             !!!!!!!!!!!!",
			"!             !!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!          !!!",
			"!!!!!!!!!!!!           !!!",
			"!!!!!!!!!!!            !!!",
			"!!!!!!!!!!   !!!!!!!!!!!!!",
			"!!!!!!!!!   !!!!!!!!!!!!!!",
			"!!!!!!!!   !!!!!!!!!!!!!!!",
			"!!!!!!!   !!!!!!!!!!!!!!!!",
			"!!!!!!!  !!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!"};

		public static String[] switches_xpm = {
			"26 26 2 1",
			"  c #000000",
			"! c #C0C0C0",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!              !!!!!!!!!!!",
			"!              !!!!!!!!!!!",
			"!              !!!!!!!!!!!",
			"!!!!!!    !!!!!!!!!!!!!!!!",
			"!!!!!    !!!!!!!!!!!!!!!!!",
			"!!!!    !!!!!!   !!!!!!!!!",
			"!!!    !!!!!!!   !!!!!!!!!",
			"!!    !!!!!!!!   !!!!!!!!!",
			"!    !!!!!!!!!   !!!!!!!!!",
			"!   !!!!!!!!!!    !!!!!!!!",
			"!  !!!!!!!!!!!     !!!!!!!",
			"!!!!!!!!!!!!!!      !!!!!!",
			"!!!!!!!!!!!!!!       !!!!!",
			"!!!!!!!!!!!!!!   !    !!!!",
			"!!!!!!!!!!!!!!   !!   !!!!",
			"!!!!!!!!!!!!!!   !!!  !!!!",
			"!!!!!!!!!!!!!!   !!!! !!!!",
			"!!!!!!!!!!!!!!   !!!!!!!!!",
			"!!!!!!!!!!!!!!   !!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!"};

		public static String[] signals_xpm = {
			"26 26 3 1",
			"  c #000000",
			"! c #C0C0C0",
			"# c #FF0000",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!! !!!!!   !!!!!!!!!!!!!!!",
			"!! !!!! ### !!!!!!!!!!!!!!",
			"!!      ### !!!!!!!!!!!!!!",
			"!!      ### !!!!!!!!!!!!!!",
			"!! !!!! ### !!!!!!!!!!!!!!",
			"!! !!!!!   !!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!    !!!!!!!!",
			"!!!!!!!!!!!!! #### !!!!!!!",
			"!!!!!!!!!!!!! #### !!!!!!!",
			"!!!!!!!!!!!!! #### !!!!!!!",
			"!!!!!!!!!!!!!!    !!!!!!!!",
			"!!!!!!!!!!!!!!!  !!!!!!!!!",
			"!!!!!!!!!!!!!!!  !!!!!!!!!",
			"!!!!!!!!!!!!!!!  !!!!!!!!!",
			"!!!!!!!!!!!!!!!  !!!!!!!!!",
			"!!!!!!!!!!!!!      !!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!",
			"!!!!!!!!!!!!!!!!!!!!!!!!!!"};

		public static String[] tools_xpm = {
			"26 26 3 1",
			"  c #000000",
			"! c #008080",
			"# c #C0C0C0",
			"##########################",
			"##########################",
			"###  #####################",
			"##         ###############",
			"## ####### ###############",
			"## ##   ## ###############",
			"## ## # ## ###############",
			"## ##   ## ###############",
			"## ####### ###############",
			"##         ###############",
			"##########################",
			"##########################",
			"##############          ##",
			"############## !!!!!!!! ##",
			"############## !!!!!!!! ##",
			"############## !!!!!!!! ##",
			"############## !!!!!!!! ##",
			"##############          ##",
			"##########################",
			"##########################",
			"##########################",
			"##########################",
			"##########################",
			"##########################",
			"##########################",
			"##########################"};

		public static String[] actions_xpm = {
			"26 26 2 1",
			"  c #A0A0A0",
			"! c #0000FF",
			"                          ",
			"                          ",
			"                          ",
			"                          ",
			"            !!            ",
			"           !!!!           ",
			"          !!!!!           ",
			"          !!!!!           ",
			"         !!!!!            ",
			"         !!!!             ",
			"        !!!!              ",
			"        !!!               ",
			"       !!!                ",
			"       !!                 ",
			"      !                   ",
			"                          ",
			"   !!                     ",
			"  !!!!                    ",
			"  !!!!                    ",
			"   !!                     ",
			"                          ",
			"                          ",
			"                          ",
			"                          ",
			"                          ",
			"                          "};

		public static String[] move_start_xpm = {
		"26 26 2 1",
		"  c #A0A0A0",
		"! c #000000",
		"                          ",
		"                          ",
		"                          ",
		"                          ",
		"  !!!!!!!!!!!!!!!!!!!!!!  ",
		"  !                    !  ",
		"  ! !!!!               !  ",
		"  ! !!                 !  ",
		"  ! ! !                !  ",
		"  ! !  !               !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !!!!!!!!!!!!!!!!!!!!!!  ",
		"                          ",
		"                          ",
		"                          ",
		"                          "};

		public static String[] move_end_xpm = {
		"26 26 2 1",
		"  c #A0A0A0",
		"! c #000000",
		"                          ",
		"                          ",
		"                          ",
		"                          ",
		"  !!!!!!!!!!!!!!!!!!!!!!  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !                    !  ",
		"  !               !  ! !  ",
		"  !                ! ! !  ",
		"  !                 !! !  ",
		"  !               !!!! !  ",
		"  !                    !  ",
		"  !!!!!!!!!!!!!!!!!!!!!!  ",
		"                          ",
		"                          ",
		"                          ",
		"                          "};

		public static String[] move_dest_xpm = {
		"26 26 3 1",
		"  c #A0A0A0",
		"! c #000000",
		"# c #0000FF",
		"                          ",
		"                          ",
		"                          ",
		"  !! !! !! !! !! !!!      ",
		"  !                !      ",
		"    ! !                   ",
		"  !  !!            !      ",
		"  ! !!!#################  ",
		"  !    #           !   #  ",
		"       #               #  ",
		"  !    #           !   #  ",
		"  !    #           !   #  ",
		"       #               #  ",
		"  !    #           !   #  ",
		"  !    #           !   #  ",
		"  !    #           !   #  ",
		"  !! !!#!! !! !! !!!   #  ",
		"       #               #  ",
		"       #               #  ",
		"       #               #  ",
		"       #################  ",
		"                          ",
		"                          ",
		"                          ",
		"                          ",
		"                          "};


}
