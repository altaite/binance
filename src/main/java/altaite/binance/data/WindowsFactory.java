package altaite.binance.data;

import altaite.binance.analysis.Candles;
import altaite.binance.data.window.Window;
import altaite.binance.data.window.ExperimentParameters;

public class WindowsFactory {

	private CandleUpdater candles;
	private ExperimentParameters params;

	public WindowsFactory(CandleUpdater candles, ExperimentParameters params) {
		this.candles = candles;
		this.params = params;
	}

	public Windows createWindows() {
		Windows windows = new Windows();
		Candles a = candles.getSortedArray();
		for (int i = 0; i < a.size() - params.minutes; i += params.getJump()) {
			Window w = createWindow(a, i);
			if (w != null) {
				windows.add(w);
			}
		}
		return windows;
	}

	public Window createWindow(Candles all, int index) {
		Candle[] a = new Candle[params.minutes];
		System.arraycopy(all.getArray(), index, a, 0, a.length);
		int totalHoles = 0;
		boolean valid = true;
		for (int i = 0; i < a.length - 1; i++) {
			long d = a[i + 1].getOpenTime() - a[i].getOpenTime();
			if (d != 60 * 1000) {
				valid = false;
				totalHoles++;
			}
		}
		for (int i = 0; i < a.length; i++) {
			if (!isValid(a[i])) {
				valid = false;
			}
		}
		//System.out.println("holes " + totalHoles);
		if (valid) {
			Window w = new Window(a);
			return w;
		} else {
			return null;
		}
	}

	private boolean isValid(Candle c) {
		return true;
	}
}
