package altaite.binance.sound;

import java.util.Locale;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;

public class Sound {

	public static boolean muted = false;
	public static float SAMPLE_RATE = 8000f;
	private static double volume = 0.1;

	public static void low() {
		for (int i = 0; i < 2; i++) {
			tone(500, 400, volume);
			tone(1000, 600, volume);
		}
	}

	public static void high() {
		for (int i = 0; i < 2; i++) {
			tone(1000, 400, volume);
			tone(500, 600, volume);
		}
	}

	public static void mute() {
		muted = true;
	}

	public static void setVolume(double d) {
		volume = d;
	}

	public static void tone(int hz, int msecs)
		throws LineUnavailableException {
		tone(hz, msecs, 1.0);
	}

	public static void tone(int hz, int msecs, double vol) {
		if (muted) {
			return;
		}
		try {
			byte[] buf = new byte[1];
			AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
			SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
			sdl.open(af);
			sdl.start();
			for (int i = 0; i < msecs * 8; i++) {
				double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
				buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
				sdl.write(buf, 0, 1);
			}
			sdl.drain();
			sdl.stop();
			sdl.close();
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		}
	}

	public static synchronized void speak(String msg) {

		try {
			// Set property as Kevin Dictionary 
			System.setProperty(
				"freetts.voices",
				"com.sun.speech.freetts.en.us"
				+ ".cmu_us_kal.KevinVoiceDirectory");

			// Register Engine 
			Central.registerEngineCentral(
				"com.sun.speech.freetts"
				+ ".jsapi.FreeTTSEngineCentral");

			// Create a Synthesizer 
			Synthesizer synthesizer
				= Central.createSynthesizer(
					new SynthesizerModeDesc(Locale.US));

			// Allocate synthesizer 
			synthesizer.allocate();

			// Resume Synthesizer 
			synthesizer.resume();

			// Speaks the given text 
			// until the queue is empty. 
			synthesizer.speakPlainText(
				msg, null);


			synthesizer.waitEngineState(
				Synthesizer.QUEUE_EMPTY);

			// Deallocate the Synthesizer. 
			synthesizer.deallocate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		
		Sound.speak("aa");
		Sound.speak("aa");
		Sound.speak("aa");
	}
	
}
