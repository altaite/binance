package altaite.binance.sound;

import java.util.Locale;
import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;

public class Voice {

	Synthesizer synthesizer;

	public Voice() {
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
			synthesizer
				= Central.createSynthesizer(
					new SynthesizerModeDesc(Locale.US));

			// Allocate synthesizer 
			synthesizer.allocate();

			// Resume Synthesizer 
			synthesizer.resume();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void speak(String msg) {
		// Speaks the given text 
		// until the queue is empty. 
		synthesizer.speakPlainText(
			msg, null);
	}

	public void cancel() throws Exception {
		synthesizer.waitEngineState(
			Synthesizer.QUEUE_EMPTY);

		// Deallocate the Synthesizer. 
		synthesizer.deallocate();
	}
}
