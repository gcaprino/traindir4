package com.traindirector.uicomponents;

import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.traindirector.simulator.Simulator;

public class SoundPlayer {

	public SoundPlayer() {
		
	}

	public void play(String fname) {
		try {
			Clip clip = AudioSystem.getClip();
			InputStream stream = Simulator.INSTANCE._fileManager.getByteStreamFor(fname);
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(stream);
			clip.open(inputStream);
			clip.start();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
