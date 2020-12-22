package altaite.binance.scanner;

import altaite.binance.data.CandleStorage;
import altaite.binance.data.Candles;
import altaite.binance.data.SymbolPair;
import altaite.binance.data.Windows;
import altaite.binance.data.WindowsFactory;
import altaite.binance.data.window.Window;
import altaite.binance.data.window.ExperimentParameters;
import altaite.binance.features.HighFeaturizer;
import altaite.binance.global.io.GlobalDirs;
import altaite.learn.Dataset;
import altaite.learn.Instance;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Backtest {

	private GlobalDirs globalDirs = new GlobalDirs(GlobalDirs.defaultDir);
	private ExperimentParameters pars = new ExperimentParameters();
	private HighFeaturizer featurizer = new HighFeaturizer(pars);
	private boolean update = true;
	private int readMax = Integer.MAX_VALUE;
	private Map<String, Long> numbers = new HashMap<>();
	private CandleStorage storage = new CandleStorage(globalDirs.getCandleStorage());

	private void evaluate() {
		List<SymbolPair> pairs = globalDirs.getMostTradedPairs();
		Windows allPositive = new Windows();
		Windows allNegative = new Windows();

		// TRY FIRST real life data, simple, telling, just limit 1000 per pair
		// probablky will not work with RF, but maybe bayes, or cost matrix
		//for (int i = 10; i < 11; i++) {
		int done = 0;
		for (int pi = 0; pi < 1/*pairs.size()*/; pi++) {
			/*if (done > 20) {
				break;
			}*/
			// TODO test on each separatelly, generate files, plus generate normal distribution
			SymbolPair pair = pairs.get(pi);

			// TODO full set and btc uncorelated manually in file
			// TODO real life test same as before, possibly breakdown by market
			if (!pair.toString().contains("BTC")
				|| pair.toString().contains("USDC")
				|| pair.toString().contains("BUSD")) {
				System.out.println("SKIPPING PAIR " + pair);
				continue;
			}
			System.out.println("PAIR " + pair);
			Candles candles;
			if (update) {
				candles = storage.update(pair, pars.getMonthsBack());
				storage.save(pair);
			} else {
				candles = storage.get(pair, readMax);
			}
			WindowsFactory wf = new WindowsFactory(candles, pars);
			Windows windows = wf.createWindows();

			Windows positive = new Windows();

			for (int i = 0; i < windows.size(); i++) {
				Window w = windows.get(i);
				if (featurizer.isHighRise(w)) {
					positive.add(w);
				}
			}

			Windows negative = new Windows();

			for (int i = 0; i < windows.size(); i++) {
				Window w = windows.get(i);
				if (!featurizer.isHighRise(w)) {
					negative.add(w);
				}
			}
			negative = negative.sample(positive.size());

			System.out.println("POSITIVE " + positive.size());
			System.out.println("NEGATIVE " + negative.size());
			int sampleSize = Math.min(positive.size(), 10000); 

			Windows ns = negative.sample(sampleSize);
			Windows ps = positive.sample(sampleSize);

			System.out.println("POSITIVE S " + ps.size());
			System.out.println("NEGATIVE S " + ns.size());

			allNegative.add(ns);
			allPositive.add(ps);
			done++;
		}
		System.out.println("ALL POSITIVE " + allPositive.size());
		System.out.println("ALL NEGATIVE " + allNegative.size());
		allPositive.sort();
		allNegative.sort();
		long time = (allPositive.getMedianTime() + allNegative.getMedianTime()) / 2;
		System.out.println("median time " + time);
		Windows[] positiveFolds = allPositive.split(time);
		Windows[] negativeFolds = allNegative.split(time);

		System.out.println("p1 " + positiveFolds[0].size());
		System.out.println("p2 " + positiveFolds[1].size());

		System.out.println("n1 " + negativeFolds[0].size());
		System.out.println("n2 " + negativeFolds[1].size());

		int one = Math.min(positiveFolds[0].size(), negativeFolds[0].size());
		int two = Math.min(positiveFolds[1].size(), negativeFolds[1].size());
		positiveFolds[0] = positiveFolds[0].sample(one);
		negativeFolds[0] = negativeFolds[0].sample(one);

		Windows reals = new Windows();
		reals.add(positiveFolds[1]);
		reals.add(negativeFolds[1]);
		createDataset(reals, Paths.get("d:/t/data/binance/high_test_real.arff"));

		positiveFolds[1] = positiveFolds[1].sample(two);
		negativeFolds[1] = negativeFolds[1].sample(two);

		Windows ones = new Windows();
		ones.add(positiveFolds[0]);
		ones.add(negativeFolds[0]);
		createDataset(ones, Paths.get("d:/t/data/binance/high_train.arff"));

		Windows twos = new Windows();
		twos.add(positiveFolds[1]);
		twos.add(negativeFolds[1]);
		createDataset(twos, Paths.get("d:/t/data/binance/high_test.arff"));

		// try different stop-limit on third fold, just optimize total earnings
		// also threshold for buy
		// if fixed combination is found and strongly helps:
		// combination which works vs. not work, predict which ones will, then generate all and let model pick one
		// TODO sample third fold, real life distribution
		// DO this for fall
	}

	private void createDataset(Windows windows, Path file) {
		Dataset dataset = new Dataset();
		int n = 0;
		for (int i = 0; i < windows.size(); i++) {
			Window w = windows.get(i);
			Instance instance = featurizer.createInstance(w);
			dataset.add(instance);
			n = instance.size();
		}
		dataset.toArff(file.toFile());
		System.out.println("Features: " + n + " Instances: " + windows.size() + " from total " + windows.size());

	}

	public static void main(String[] args) {
		Backtest s = new Backtest();
		s.evaluate();
	}
}
