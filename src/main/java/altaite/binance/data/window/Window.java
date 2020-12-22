package altaite.binance.data.window;

import altaite.binance.data.Candle;

public class Window implements Comparable<Window> {

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

	public int size() {
		return candles.length;
	}

	public Candle get(int i) {
		return candles[i];
	}

	public double getHigh() {
		double high = Double.NEGATIVE_INFINITY;
		for (Candle c : candles) {
			if (c.getHigh() > high) {
				high = c.getHigh();
			}
		}
		return high;
	}

	public double getLow() {
		double low = Double.POSITIVE_INFINITY;
		for (Candle c : candles) {
			if (c.getLow() < low) {
				low = c.getLow();
			}
		}
		return low;
	}

	@Override
	public int compareTo(Window other) {
		return Long.compare(this.getStart(), other.getStart());
	}
}
