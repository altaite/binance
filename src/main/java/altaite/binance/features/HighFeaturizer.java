package altaite.binance.features;

import altaite.analysis.Sample;
import altaite.binance.data.Candle;
import altaite.binance.data.window.Window;
import altaite.learn.MyInstance;
import altaite.binance.data.window.ExperimentParameters;
import org.jtransforms.fft.DoubleFFT_1D;

public class HighFeaturizer {

	private ExperimentParameters pars;

	public HighFeaturizer(ExperimentParameters pars) {
		this.pars = pars;
	}

	public MyInstance createInstance(Window window) {
		boolean windowWithTarget = window.size() > pars.getFeatureN();
		MyInstance instance = new MyInstance(windowWithTarget);
		setFeatures(window, instance);
		if (windowWithTarget) {
			setTarget(window, instance);
		}
		return instance;
	}

	private void setFeatures(Window window, MyInstance instance) {
		// derive distribution chars from single candle
		// then i can have same for multiple candles, just better?
		// do some statistics on multiple candles?

		Candle[] candles = getFeatureCandles(window);
		Candle[] a = getFirst(candles, candles.length / 2);
		Candle[] b = getLast(candles, candles.length - candles.length / 2);

		double buyPrice = getBuyPrice(window);
		Relative r = new Relative(buyPrice);

		double volumeBase = new Sample(b, c -> c.getVolume()).average();

		int k = 0;
		for (Candle c : b) {
			if (k < candles.length / 2) {
				//continue;
			}
			k++;
			// TODO moving average
			double hiLo = r.r((c.getHigh() + c.getLow()) / 2);
			double close = r.r(c.getClose());
			double var = (c.getHigh() - c.getLow()) / buyPrice;
			instance.addNumeric(hiLo);
			instance.addNumeric(close);
			instance.addNumeric(var);
			// need those relative:

			//instance.addNumeric(c.getNumberOfTrades());
			instance.addNumeric(c.getVolume() / volumeBase);
			//instance.addNumeric(c.getQuoteAssetVolume());
			//instance.addNumeric(c.getTakerBuyBaseAssetVolume());
		}

		addMovingAverage(candles, 9, instance);

		/*	double[] hl = extract(candles, c -> (c.getLow() + c.getHigh()) / 2);
		while (hl.length > 0) {
			hl = averagePairsFromEnd(hl);
			for (int i = 0; i < hl.length; i++) {
				instance.addNumeric(r.r(hl[i]));
			}
		}
		 */
 /*double[] high = new double[candles.length];
		double[] low = new double[candles.length];
		double[] close = new double[candles.length];
		for (int i = 0; i < candles.length; i++) {
			Candle c = candles[i];
			high[i] = r.r(c.getHigh());
			low[i] = r.r(c.getLow());
			close[i] = r.r(c.getClose());
		}
		addFourier(high, instance);
		addFourier(low, instance);
		addFourier(close, instance);*/
	}

	private Candle[] getFirst(Candle[] a, int n) {
		Candle[] b = new Candle[n];
		for (int i = 0; i < b.length; i++) {
			b[i] = a[i];
		}
		return b;
	}

	private Candle[] getLast(Candle[] a, int n) {
		Candle[] b = new Candle[n];
		for (int i = 0; i < b.length; i++) {
			b[i] = a[a.length - n + i];
		}
		return b;
	}

	private void addMovingAverage(Candle[] candles, int n, MyInstance instance) {
		Double last = null;
		for (int i = 0; i < candles.length - n; i++) {
			double sum = 0;
			for (int k = 0; k <= n; k++) {
				Candle c = candles[i + k];
				sum += (c.getLow() + c.getHigh()) / 2;
			}
			double movingAverage = sum / n;
			instance.addNumeric(movingAverage);
			if (last != null) {
				instance.addNumeric(movingAverage - last);
			}
			last = movingAverage;

		}
	}

	private double getBuyPrice(Window w) {
		Candle[] a = getFeatureCandles(w);
		return a[a.length - 1].getClose();
	}

	private void addFourier(double[] inOut, MyInstance instance) {
		DoubleFFT_1D fft = new DoubleFFT_1D(inOut.length);
		fft.realForward(inOut);
		for (int i = 0; i < inOut.length; i++) {
			instance.addNumeric(inOut[i]);
		}
	}

	private void setTarget(Window window, MyInstance instance) {
		instance.addNominal(isHighRise(window) ? 1 : 0);
	}

	public boolean isHighRise(Window window) {
		return isHighRise(window, null);
	}

	public boolean isHighRise(Window window, double[] lowHi) {
		Candle[] candles = getTargetCandles(window);

		for (int i = 0; i < candles.length - 1; i++) {
			Candle a = candles[i];
			Candle b = candles[i + 1];
			if (b.getCloseTime() <= a.getCloseTime()) {
				throw new RuntimeException();
			}
		}

		double buy = candles[0].getOpen();
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < candles.length; i++) {
			double relative = (candles[i].getHigh() - buy) / buy;
			if (relative > max) {
				max = relative;
			}
			if (relative < min) {
				min = relative;
			}
		}

		double high = 5 * (2 * 0.00075);
		boolean isHigh = max > high;
		boolean isLow = min < -max / 2;
		if (lowHi != null) {
			lowHi = new double[2];
			lowHi[0] = min;
			lowHi[1] = max;
		}
		return isHigh & !isLow;
	}

	private Candle[] getFeatureCandles(Window window) {
		Candle[] candles = window.getCandles();
		Candle[] a = new Candle[pars.getFeatureN()];
		System.arraycopy(candles, 0, a, 0, pars.getFeatureN());

		return a;
	}

	private Candle[] getTargetCandles(Window window) {
		Candle[] candles = window.getCandles();
		Candle[] b = new Candle[pars.getTargetN()];
		System.arraycopy(candles, pars.getFeatureN(), b, 0, pars.getTargetN());
		return b;
	}

}
