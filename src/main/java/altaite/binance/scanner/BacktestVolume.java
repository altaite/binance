package altaite.binance.scanner;

import altaite.analysis.Sample;
import altaite.analysis.graphics.WindowPicture;
import altaite.binance.data.Candle;
import altaite.binance.data.CandleStorage;
import altaite.binance.data.Candles;
import altaite.binance.data.SymbolPair;
import altaite.binance.data.Windows;
import altaite.binance.data.WindowsFactory;
import altaite.binance.data.window.Window;
import altaite.binance.data.window.ExperimentParameters;
import altaite.binance.features.HighFeaturizer;
import altaite.binance.features.SimpleFeaturizer;
import altaite.binance.global.io.GlobalDirs;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BacktestVolume {

	private GlobalDirs globalDirs = new GlobalDirs(GlobalDirs.defaultDir);
	private ExperimentParameters pars = new ExperimentParameters();
	private HighFeaturizer featurizer = new HighFeaturizer(pars);
	private boolean update = true;
	private int readMax = Integer.MAX_VALUE;
	private Map<String, Long> numbers = new HashMap<>();
	private CandleStorage storage = new CandleStorage(globalDirs.getCandleStorage());

	private void evaluate() {
		List<SymbolPair> pairs = globalDirs.getMostTradedPairs();

		int done = 0;
		for (int pi = 0; pi < pairs.size(); pi++) {
			SymbolPair pair = pairs.get(pi);

			System.out.println("PAIR " + pair);
			Candles candles;
			if (update) {
				candles = storage.update(pair, pars.getMonthsBack());
				storage.save(pair);
			} else {
				candles = storage.get(pair, readMax);
			}
			WindowsFactory wf = new WindowsFactory(candles, pars);
			System.out.println("creating windows ...");
			Windows windows = wf.createWindows();
			System.out.println("... windows created");

			SimpleFeaturizer featurizer = new SimpleFeaturizer(pars);
			int i = 0;
			int good = 0;
			int goodRise = 0;
			int bad = 0;
			int badRise = 0;
			
			
			for (Window w : windows.getWindows()) {
				System.out.println(i);
				Candle[] a = featurizer.getFeatureCandles(w);
				Candle[] b = featurizer.getTargetCandles(w);

				double buyPrice = a[a.length - 1].getClose();
				double sellPrice = new Sample(b, c -> c.getHigh()).max();
				WindowPicture wp = new WindowPicture(w, a.length, sellPrice);

				QualityEstimator qe = new QualityEstimator(pair, a, pars);
				Quality q = qe.compute();

				double rise = (sellPrice - buyPrice) / buyPrice;

				if (q.isGood()) {
					//wp.save(globalDirs.getPictures().resolve(i + ".png").toFile());
					good++;
					if (rise > 0.01) {
						goodRise++;
					}
				} else {
					bad++;
					if (rise > 0.01) {
						badRise++;
					}
					//q.print();
				}

				i++;
			}
			System.out.println("RESULTS " + pair + " !!!!!!!!!!!!!!!!!! ");
			System.out.println(good);
			System.out.println(bad);
			System.out.println(goodRise);
			System.out.println(badRise);
			System.out.println(((double) goodRise) / good);
			System.out.println(((double) badRise) / bad);

		}

	}

	public static void main(String[] args) {
		BacktestVolume s = new BacktestVolume();
		s.evaluate();
	}
}
