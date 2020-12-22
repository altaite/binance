package altaite.binance.data;

import altaite.binance.analysis.CandleArray;
import altaite.binance.data.window.Window;
import altaite.binance.data.window.ExperimentParameters;

public class WindowsFactory {

	private Candles candles;
	private ExperimentParameters pars;

	public WindowsFactory(Candles candles, ExperimentParameters parameters) {
		this.candles = candles;
		this.pars = parameters;
	}

	public Windows createWindows() {
		Windows windows = new Windows();
		CandleArray a = candles.getCandleArray();
		for (int i = 0; i < a.size() - pars.getWindowN(); i += pars.getJump()) {
			Window w = createWindow(a, i);
			if (w != null) {
				windows.add(w);
			}
		}
		return windows;
	}

	public Window createWindow(CandleArray all, int index) {
		Candle[] a = new Candle[pars.getWindowN()];
		System.arraycopy(all.getArray(), index, a, 0, a.length);
		boolean valid = true;
		for (int i = 0; i < a.length - 1; i++) {
			long d = a[i + 1].getOpenTime() - a[i].getOpenTime();
			if (d != 60 * 1000) {
				valid = false;
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
