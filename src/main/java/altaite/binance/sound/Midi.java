package altaite.binance.sound;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class Midi {

	private static void playWarningSound() {
//        if (2 > 1) {
//            return;
//        }

		try {
//            int velocity = 127;    // max volume
			int velocity = 90;    // max volume
			int sound = 65;
			Synthesizer synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			MidiChannel channel = synthesizer.getChannels()[9];  // drums channel.
			for (int i = 0; i < 10; i++) {
				Thread.sleep(100);
				channel.noteOn(sound + i, velocity);
				Thread.sleep(100);
				channel.noteOff(sound + i);
			}
		} catch (MidiUnavailableException | InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	public static void main(String[] args) {
		playWarningSound();
	}
}
