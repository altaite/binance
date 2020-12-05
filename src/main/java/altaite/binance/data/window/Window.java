package altaite.binance.data.window;

import altaite.binance.data.Candle;

public class Window {

	Candle[] candles;

	public Window(Candle[] candles) {
		this.candles = candles;
	}

	public Candle[] getCandles() {
		return candles;
	}

	public long getStart() {
		return candles[0].getOpenTime();
	}

	public long getEnd() {
		return candles[candles.length - 1].getCloseTime();
	}

	public void invertCandles() {
		for (Candle c : candles) {
			c.invert();
		}
	}
}
