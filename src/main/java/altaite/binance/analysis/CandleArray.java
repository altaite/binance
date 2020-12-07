package altaite.binance.analysis;

import altaite.binance.data.Candle;
import java.util.function.Function;

public class CandleArray {

	private Candle[] candles;

	public CandleArray(Candle[] candles) {
		this.candles = candles;
	}

	public double[] flatten(Function<Candle, Double> f) {
		double[] a = new double[candles.length];
		for (int i = 0; i < a.length; i++) {
			a[i] = f.apply(candles[i]);
		}
		return a;
	}

	public int size() {
		return candles.length;
	}

	public Candle[] getArray() {
		return candles;
	}
}
