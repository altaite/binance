package altaite.binance.features;

import altaite.binance.data.Candle;
import altaite.binance.data.window.Window;
import altaite.learn.Instance;
import altaite.analysis.Sample;
import altaite.binance.data.window.ExperimentParameters;
import org.jtransforms.fft.DoubleFFT_1D;

public class SimpleFeaturizer implements Featurizer {

	private ExperimentParameters pars;

	public SimpleFeaturizer(ExperimentParameters pars) {
		this.pars = pars;
	}

	@Override
	public Instance createInstance(Window window) {
		boolean windowWithTarget = window.size() > pars.getFeatureN();
		Instance instance = new Instance(windowWithTarget);
		setFeatures(window, instance);
		if (windowWithTarget) {
			setTarget(window, instance);
		}
		return instance;
	}

	private void setFeatures(Window window, Instance instance) {
		// derive distribution chars from single candle
		// then i can have same for multiple candles, just better?
		// do some statistics on multiple candles?

		Candle[] candles = getFeatureCandles(window);
		double buyPrice = getBuyPrice(window);
		Relative r = new Relative(buyPrice);
		int k = 0;
		for (Candle c : candles) {
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

			instance.addNumeric(c.getNumberOfTrades());
			instance.addNumeric(c.getVolume());
			instance.addNumeric(c.getQuoteAssetVolume());
			instance.addNumeric(c.getTakerBuyBaseAssetVolume());
		}

		/*	double[] hl = extract(candles, c -> (c.getLow() + c.getHigh()) / 2);
		while (hl.length > 0) {
			hl = averagePairsFromEnd(hl);
			for (int i = 0; i < hl.length; i++) {
				instance.addNumeric(r.r(hl[i]));
			}
		}
		 */
		double[] high = new double[candles.length];
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
		addFourier(close, instance);

	}

	private double getBuyPrice(Window w) {
		Candle[] a = getFeatureCandles(w);
		return a[a.length - 1].getClose();
	}

	private void addFourier(double[] inOut, Instance instance) {
		DoubleFFT_1D fft = new DoubleFFT_1D(inOut.length);
		fft.realForward(inOut);
		for (int i = 0; i < inOut.length; i++) {
			instance.addNumeric(inOut[i]);
		}
	}

	/*public double[] averagePairsFromEnd(double[] a) {
		double[] b = new double[a.length / 2];
		for (int i = 0; i < b.length; i++) {
			double a1 = a[a.length - 1 - 2 * i];
			double a2 = a[a.length - 2 - 2 * i];
			b[i] = (a1 + a2) / 2;
		}
		return b;
	}
	 */
	private void setTarget(Window window, Instance instance) {
		double relativeGain = computeTargetWithFee(window);
		instance.addNumeric(relativeGain);
	}

	public double computeTargetWithFee(Window window) {
		Candle[] candles = getTargetCandles(window);
		//double sell;
		// sell = new Sample(ca.flatten(c -> c.getHigh())).max();
		// sell = new Sample(ca.flatten(c -> c.getHigh())).max();
		for (int i = 0; i < candles.length - 1; i++) {
			Candle a = candles[i];
			Candle b = candles[i + 1];
			if (b.getCloseTime() <= a.getCloseTime()) {
				throw new RuntimeException();
			}
		}
		//Candle l = candles[candles.length - 1];
		//sell = l.getClose();

		//double sell = new Sample(ca.flatten(c -> c.getHigh())).average();
		/*sell = getMaxPrice(candles, r);

		double relativeGain = r.r(sell);
		if (!buying) {
			relativeGain *= -1;
		}*/
		//double relativeGain = transform(getSellGain(candles, r));
		double absoluteGain = getSellPrice(window);
		Relative r = new Relative(getBuyPrice(window));
		return r.r(absoluteGain) - 0.0015;

	}

	/*private void computeAmplitude(Candle[] candles, double previousVolume, Instance instance) {
		for (Candle c : candles) {

		}
		instance.addNumeric(getVolumeChange(candles, previousVolume));
	}

	private void computeRelativeVolumeChange(Candle[] candles, double previousVolume, Instance instance) {
		instance.addNumeric(getVolumeChange(candles, previousVolume));
	}

	private double getVolumeChange(Candle[] candles, double oldVolume) {
		double sum = 0;
		for (int i = 0; i < candles.length; i++) {
			sum += candles[i].getVolume();
		}
		System.out.println("? " + candles.length + " " + sum + " " + oldVolume);
		return (sum / candles.length - oldVolume) / oldVolume;
	}*/

 /*Doubles limitValue = new Doubles();
	Doubles stopValue = new Doubles();
	Doubles averageValue = new Doubles();*/
	private double getMaxPrice(Candle[] candles, Relative r) {
		Sample s = new Sample(candles, c -> c.getClose());
		return s.max();
	}

	@Override
	public double getSellPrice(Window window) {
		double limit = 10;
		double stop = -0.015;
		double movingStop = stop;
		Candle[] candles = getTargetCandles(window);
		/*if (true) { // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			Relative relative = new Relative(getBuyPrice(window));
			// if profit, start selling proportionally to its size?
			// dump windows predicted high, set stop and limit randomly for each combination of time/10, profit and prediction percentile
			for (int i = 0; i < candles.length; i++) {
				Candle c = candles[i];
				double gain = relative.r(c.getClose());
				if (gain > limit) {
					//return limit;
				}
				if (gain < movingStop) {
					return c.getClose();
					//return movingStop;
				}
				if (gain + stop > movingStop) {
					movingStop = gain + stop;
				}
			}
		}*/
		int total = 0;
		double avg = 0;
		double max = Double.NEGATIVE_INFINITY;
		//for (int i = 0; i < candles.length; i++) {
		for (int i = candles.length / 2; i < candles.length; i++) {
			Candle c = candles[i];
			/*if (c.getHigh() > max) {
				max = c.getHigh();
			}*/
			avg += (c.getClose() + c.getHigh() + c.getLow()) / 3;
			total++;
		}
		//return max;

		return avg / total;
	}

	public Candle[] getFeatureCandles(Window window) {
		Candle[] candles = window.getCandles();
		Candle[] a = new Candle[pars.getFeatureN()];
		System.arraycopy(candles, 0, a, 0, pars.getFeatureN());

		return a;
	}

	public Candle[] getTargetCandles(Window window) {
		Candle[] candles = window.getCandles();
		Candle[] b = new Candle[pars.getTargetN()];
		System.arraycopy(candles, pars.getFeatureN(), b, 0, pars.getTargetN());
		return b;
	}

}

class Relative {

	private double base;

	public Relative(double base) {
		this.base = base;
	}

	public double r(double value) {
		return (value - base) / base;
	}
}
