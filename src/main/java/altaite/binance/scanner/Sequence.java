package altaite.binance.scanner;

import altaite.binance.data.Candle;

public class Sequence {

	private Candle[] candles;

	public Sequence(Candle[] candles) {
		this.candles = candles;
	}

	public Sequence chipEnd(int n) {
		Candle[] a = new Candle[candles.length - n];
		Candle[] b = new Candle[n];
		for (int i = 0; i < a.length; i++) {
			a[i] = candles[i];
		}
		for (int i = 0; i < b.length; i++) {
			b[i] = candles[a.length + i];
		}
		this.candles = a;
		return new Sequence(b);
	}

	public Candle[] getCandles() {
		return candles;
	}
}
