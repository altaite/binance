package altaite.binance.scanner;

import altaite.binance.data.CandleStorage;
import altaite.binance.data.Candles;
import altaite.binance.data.SymbolPair;
import altaite.binance.data.Windows;
import altaite.binance.data.WindowsFactory;
import altaite.binance.data.window.Window;
import altaite.binance.data.window.ExperimentParameters;
import altaite.binance.features.HighFeaturizer;
import altaite.binance.global.io.ExperimentDirs;
import altaite.binance.global.io.GlobalDirs;
import altaite.format.Format;
import altaite.learn.Dataset;
import altaite.learn.MyInstance;
import altaite.learn.model.RandomForestClassifierSmile;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static smile.data.formula.Terms.all;

public class Backtest {

	private GlobalDirs globalDirs = new GlobalDirs(GlobalDirs.defaultDir);
	private ExperimentParameters pars = new ExperimentParameters();
	private HighFeaturizer featurizer = new HighFeaturizer(pars);
	private boolean update = true;
	private int readMax = Integer.MAX_VALUE;
	private CandleStorage storage = new CandleStorage(globalDirs.getCandleStorage());

	private void evaluate() {
		List<SymbolPair> pairs = globalDirs.getMostTradedPairs();

		Date[] dates = {Format.parseDate("2019-05-01"), Format.parseDate("2020-09-01")};
		Folds folds = new Folds(dates);

		for (int pi = 0; pi < 10/*pairs.size()*/; pi++) {
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
			Windows w = wf.createWindows();
			folds.add(pair, w);
		}

		for (int targetFold = 1; targetFold < folds.getNumberOfFolds(); targetFold++) {
			Windows train = folds.getFold(0, targetFold - 1);
			ExperimentDirs ed = new ExperimentDirs(globalDirs, "_target_fold_" + targetFold);
			Windows test = folds.getFold(targetFold);
			evaluateFold(ed, test);
			//for (SymbolPair pair : pairs) {
			//}
		}
	}

	private void evaluateFold(ExperimentDirs ed, Windows testWindows) {
		
		RandomForestClassifierSmile model = new RandomForestClassifierSmile(ed.getTrain(), ed.getModel());
		List<Classification> classifications = new ArrayList<>();
		for (Window w : testWindows) {
			MyInstance instance = featurizer.createInstance(w);
			double p = model.predict(instance);
			double[] realLowHigh = new double[2];
			boolean isHigh = featurizer.isHighRise(w, realLowHigh);
			Classification c = new Classification(p, isHigh, realLowHigh[0], realLowHigh[1]);
			classifications.add(c);
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(ed.getClassifications()))) {
			for (Classification c : classifications) {
				bw.write(c.probability + "," + (c.isHigh ? "1" : "0") + "," + c.low + "," + c.high);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(ed.getThresholds()))) {
			bw.write("threshold,tp,total,highAmongPredicted\n");
			for (double threshold = 0; threshold <= 1; threshold += 0.01) {
				int tp = 0;
				int total = 0;
				for (Classification c : classifications) {
					if (c.probability >= threshold) {
						total++;
						if (c.isHigh) {
							tp++;
						}
					}
				}
				double highAmongPredicted = ((double) tp) / total;
				System.out.println(threshold + "," + tp + "," + total + "," + highAmongPredicted + "\n");
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void evaluateSingleSplit() {
		List<SymbolPair> pairs = globalDirs.getMostTradedPairs();
		Windows all = new Windows();

		//for (int i = 10; i < 11; i++) {
		int done = 0;
		for (int pi = 0; pi < 10/*pairs.size()*/; pi++) {
			/*if (done > 20) {
				break;
			}*/
			SymbolPair pair = pairs.get(pi);

			// TODO full set and btc uncorelated manually in file
			// TODO real life test same as before, possibly breakdown by market
			/*if (!pair.toString().contains("BTC")
				|| pair.toString().contains("USDC")
				|| pair.toString().contains("BUSD")) {
				System.out.println("SKIPPING PAIR " + pair);
				continue;
			}*/
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
			all.add(windows);
		}
		all.sort();
		long time = all.getMedianTime();

		createDataset(all, globalDirs.getDataFull());

		Windows[] folds = all.split(time);

		createDataset(folds[0], Paths.get("d:/t/data/binance/high_train_real_2.arff"));
		createDataset(folds[1], Paths.get("d:/t/data/binance/high_test_real_2.arff"));

		evaluateClasses(folds[0]);
		evaluateClasses(folds[1]);

		Path modelPath = Paths.get("d:/t/data/binance/model_real_2");
		try {
			Files.createDirectories(modelPath);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		RandomForestClassifierSmile model = new RandomForestClassifierSmile(
			Paths.get("d:/t/data/binance/high_train_real_2.arff"),
			modelPath);
		for (Window w : folds[1]) {
			MyInstance instance = featurizer.createInstance(w);
			double d = model.predict(instance);
			System.out.println("   " + d);
		}

		RandomForestClassifierSmile fullModel = new RandomForestClassifierSmile(globalDirs.getDataFull(),
			globalDirs.getModelFull());

	}

	private void evaluateClasses(Windows windows) {
		int one = 0;
		int two = 0;
		for (Window w : windows.getWindows()) {
			if (featurizer.isHighRise(w)) {
				one++;
			} else {
				two++;
			}
		}
		System.out.println("one " + one + " two " + two);
	}

	private void evaluateBalancing() {
		List<SymbolPair> pairs = globalDirs.getMostTradedPairs();
		Windows allPositive = new Windows();
		Windows allNegative = new Windows();

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
			MyInstance instance = featurizer.createInstance(w);
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
