package com.traindirector.simulator;

import java.util.ArrayList;
import java.util.List;

import com.traindirector.model.Signal;
import com.traindirector.model.SignalAspect;

public class SignalFactory {

	List<Signal> _signalTemplates;
	List<SignalAspect> _aspects;

	Signal sig1, sig2;

	public SignalFactory() {

		_signalTemplates = new ArrayList<Signal>();
		_aspects = new ArrayList<SignalAspect>();

		// Create standard signal types

		SignalAspect aspectRed1 = new SignalAspect();
		aspectRed1._name = "red";
		aspectRed1._iconE = new String[] { ":icons:sig_e_r1.xpm" };
		aspectRed1._iconW = new String[] { ":icons:sig_w_r1.xpm" };
		aspectRed1._iconN = new String[] { ":icons:sig_n_r1.xpm" };
		aspectRed1._iconS = new String[] { ":icons:sig_s_r1.xpm" };
		aspectRed1._action = SignalAspect.STOP;

		SignalAspect aspectGreen1 = new SignalAspect();
		aspectGreen1._name = "green";
		aspectGreen1._iconE = new String[] { ":icons:sig_e_g1.xpm" };
		aspectGreen1._iconW = new String[] { ":icons:sig_w_g1.xpm" };
		aspectGreen1._iconN = new String[] { ":icons:sig_n_g1.xpm" };
		aspectGreen1._iconS = new String[] { ":icons:sig_s_g1.xpm" };
		aspectGreen1._action = SignalAspect.PROCEED;

		SignalAspect aspectRed2 = new SignalAspect();
		aspectRed2._name = "red";
		aspectRed2._iconE = new String[] { ":icons:sig_e_r2.xpm" };
		aspectRed2._iconW = new String[] { ":icons:sig_w_r2.xpm" };
		aspectRed2._iconN = new String[] { ":icons:sig_n_r2.xpm" };
		aspectRed2._iconS = new String[] { ":icons:sig_s_r2.xpm" };
		aspectRed2._action = SignalAspect.STOP;

		SignalAspect aspectGreen2 = new SignalAspect();
		aspectGreen2._name = "green";
		aspectGreen2._iconE = new String[] { ":icons:sig_e_g2.xpm" };
		aspectGreen2._iconW = new String[] { ":icons:sig_w_g2.xpm" };
		aspectGreen2._iconN = new String[] { ":icons:sig_n_g2.xpm" };
		aspectGreen2._iconS = new String[] { ":icons:sig_s_g2.xpm" };
		aspectGreen2._action = SignalAspect.PROCEED;

		SignalAspect aspectRed2Fleeted = new SignalAspect();
		aspectRed2Fleeted._name = "redFleeted";
		aspectRed2Fleeted._iconE = new String[] { ":icons:sig_e_r2fleeted.xpm" };
		aspectRed2Fleeted._iconW = new String[] { ":icons:sig_w_r2fleeted.xpm" };
		aspectRed2Fleeted._iconN = new String[] { ":icons:sig_n_r2fleeted.xpm" };
		aspectRed2Fleeted._iconS = new String[] { ":icons:sig_s_r2fleeted.xpm" };
		aspectRed2Fleeted._action = SignalAspect.STOP;

		SignalAspect aspectGreen2Fleeted = new SignalAspect();
		aspectGreen2Fleeted._name = "greenFleeted";
		aspectGreen2Fleeted._iconE = new String[] { ":icons:sig_e_g2fleeted.xpm" };
		aspectGreen2Fleeted._iconW = new String[] { ":icons:sig_w_g2fleeted.xpm" };
		aspectGreen2Fleeted._iconN = new String[] { ":icons:sig_n_g2fleeted.xpm" };
		aspectGreen2Fleeted._iconS = new String[] { ":icons:sig_s_g2fleeted.xpm" };
		aspectGreen2Fleeted._action = SignalAspect.PROCEED;

		sig1 = new Signal();
		sig1.addAspect(aspectRed1);
		sig1.addAspect(aspectGreen1);

		sig2 = new Signal();
		sig2.addAspect(aspectRed2);
		sig2.addAspect(aspectGreen2);
		sig2.addAspect(aspectRed2Fleeted);
		sig2.addAspect(aspectGreen2Fleeted);

		_signalTemplates.add(sig1);
		_signalTemplates.add(sig2);
	}

	public Signal newInstance(int nLights) {
		Signal s = new Signal();
		Signal src = sig1;
		if (nLights == 2)
			src = sig2;
		s._aspects = src._aspects;
		s._currentAspect = src.findAspect("red");
		return s;
	}
}
