package altaite.binance.features;

import altaite.analysis.Doubles;
import altaite.binance.data.Candle;
import altaite.binance.data.window.Window;
import altaite.learn.Instance;
import altaite.analysis.Sample;
import altaite.binance.data.window.ExperimentParameters;
import java.util.function.Function;
import org.jtransforms.fft.DoubleFFT_1D;

// TODO
// very simple few features, no great resolution, averages and trends
// connect to ML, maybe do weka for simplicity
// backtest - buy/sell model with simple thresholds
// test on multiple markets, whole learning -> backtest workflow
// ML API
// ModelFactory(Dataset) -> Model (just predictions, double[] -> double for sipmlicity)
// split data, produce results, train on all too
// BackTest - split data, Trader.action(double[], model)

/*
price model -> optimize how much to buy and sell given: prediction, current diff

train buy and sell separatelly?

first sell: buy at random point, price prediction, current diff -> how much to sell / sell or not
then buy: compute when i would sell, this gain is target feature -> same

simple linear model, regression, svm, nn, rf
possibly enrich with features from the price model
OR
nn for price, connect to train and sell, after some iterations unlock upper nn nondes


 */
public class SimpleFeaturizer implements Featurizer {

	private ExperimentParameters pars;
	private boolean buying = true;

	public SimpleFeaturizer(ExperimentParameters pars) {
		this.pars = pars;
	}

	@Override
	public Instance createInstance(Window window) {
		Candle[] candles = window.getCandles();
		int al = pars.getFeatureN();
		int bl = candles.length - al;
		assert candles.length == al + bl : candles.length + " " + al + " " + bl;
		assert pars.getWindowN() == al + bl;
		Candle[] a = new Candle[al];
		Candle[] b = new Candle[bl];
		System.arraycopy(candles, 0, a, 0, al);
		System.arraycopy(candles, al, b, 0, bl);
		double lastPrice = a[a.length - 1].getOpen();

		Instance instance = new Instance(b.length > 0);
		computeFeatures(a, lastPrice, instance);
		if (b.length > 0) {
			computeRelativePriceChange(b, lastPrice, buying, instance);
			/*double previousVolume = 0;
			for (Candle c : a) {
				previousVolume += c.getVolume();
			}
			previousVolume /= a.length;
			computeRelativeVolumeChange(b, previousVolume, instance);*/
		}
		return instance;
	}

	private void computeFeatures(Candle[] candles, double lastPrice, Instance instance) {
		// derive distribution chars from single candle
		// then i can have same for multiple candles, just better?
		// do some statistics on multiple candles?
		Relative r = new Relative(lastPrice);
		int k = 0;
		for (Candle c : candles) {
			if (k < candles.length / 2) {
				//continue;
			}
			k++;
			// TODO moving average
			double hiLo = r.r((c.getHigh() + c.getLow()) / 2);
			double open = r.r(c.getOpen());
			double var = (c.getHigh() - c.getLow()) / lastPrice;
			instance.addNumeric(hiLo);
			instance.addNumeric(open);
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
		double[] open = new double[candles.length];
		for (int i = 0; i < candles.length; i++) {
			Candle c = candles[i];
			high[i] = r.r(c.getHigh());
			low[i] = r.r(c.getLow());
			open[i] = r.r(c.getOpen());
		}
		addFourier(high, instance);
		addFourier(low, instance);
		addFourier(open, instance);

	}

	private void addFourier(double[] inOut, Instance instance) {
		DoubleFFT_1D fft = new DoubleFFT_1D(inOut.length);
		fft.realForward(inOut);
		for (int i = 0; i < inOut.length; i++) {
			instance.addNumeric(inOut[i]);
		}
	}

	public double[] averagePairsFromEnd(double[] a) {
		double[] b = new double[a.length / 2];
		for (int i = 0; i < b.length; i++) {
			double a1 = a[a.length - 1 - 2 * i];
			double a2 = a[a.length - 2 - 2 * i];
			b[i] = (a1 + a2) / 2;
		}
		return b;
	}

	private void computeRelativePriceChange(Candle[] candles, double lastPrice, boolean buying, Instance instance) {
		Relative r = new Relative(lastPrice);
		double sell;
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
		double relativeGain = getSellGain(candles, r);

		instance.addNumeric(relativeGain);
	}

	private void computeAmplitude(Candle[] candles, double previousVolume, Instance instance) {
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
	}

	/*Doubles limitValue = new Doubles();
	Doubles stopValue = new Doubles();
	Doubles averageValue = new Doubles();*/
	private double getMaxPrice(Candle[] candles, Relative r) {
		Sample s = new Sample(candles, c -> c.getClose());
		return s.max();
	}

	private double getMinPrice(Candle[] candles, Relative r) {
		Sample s = new Sample(candles, c -> c.getClose());
		return s.min();
	}

	private double getSellGain(Candle[] candles, Relative r) {
		// VISUALIZE the good ones!!!! ARIMA?
		// finer start and end plots
		// TODO optimizer, see various windows and stops

		//double limit = 0.0015 * 50;
		// PRINT max vs. gain
		// TODO make it relative to gain? or last growth?
		// isolate and optimize this - subset of windows, measure, go through parameters
		double limit = 10;
		double stop = -0.0015;
		double movingStop = stop;
		if (false) { // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			// if profit, start selling proportionally to its size?
			// dump windows predicted high, set stop and limit randomly for each combination of time/10, profit and prediction percentile
			for (int i = 0; i < candles.length; i++) {
				Candle c = candles[i];
				double gain = r.r(c.getClose());
				if (gain > limit) {
					//return limit;
				}
				if (gain < movingStop) {
					return movingStop;
				}
				if (gain + stop > movingStop) {
					movingStop = gain + stop;
				}
			}
		}
		int total = 0;
		double avg = 0;
		//for (int i = 0; i < candles.length; i++) {
		for (int i = candles.length * 9 / 10; i < candles.length; i++) {
			Candle c = candles[i];
			avg += c.getClose();
			total++;
		}
		//System.out.println("total " + total);
		//averageValue.add(r.r(avg / total));
		return r.r(avg / total);
	}

	private double transform(double v) {
		if (v >= 0) {
			return Math.sqrt(v);
		} else {
			return -Math.sqrt(-v);
		}
	}

	public void printStats() {
		/*	System.out.println("limit: " + new Sample(limitValue).average());
		System.out.println("stop:  " + new Sample(stopValue).average());
		System.out.println("avg:   " + new Sample(averageValue).average());*/
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
