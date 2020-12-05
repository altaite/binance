package altaite.binance.scanner;

import altaite.analysis.Sample2;
import altaite.analysis.RegressionResults;
import altaite.binance.data.CandleStorage;
import altaite.binance.data.CandleUpdater;
import altaite.binance.data.SymbolPair;
import altaite.binance.data.Windows;
import altaite.binance.data.WindowsFactory;
import altaite.binance.data.window.Window;
import altaite.binance.data.window.ExperimentParameters;
import altaite.binance.features.Featurizer;
import altaite.binance.features.SimpleFeaturizer;
import altaite.binance.global.io.ExperimentDirs;
import altaite.binance.global.io.GlobalDirs;
import altaite.binance.trader.Trader;
import altaite.learn.Dataset;
import altaite.learn.Instance;
import altaite.learn.model.Model;
import altaite.learn.model.RandomForestRegressionSmile;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.event.AllMarketTickersEvent;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class Backtest {

	private GlobalDirs globalDirs = new GlobalDirs("d:/t/data/binance");
	private BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
	private BinanceApiRestClient rest = factory.newRestClient();
	private Path home = Paths.get("d:/t/data/market/binance");
	private ExperimentParameters pars = new ExperimentParameters();
	private boolean update = true;
	private int readMax = Integer.MAX_VALUE;
	// immediate fast growth
	// breaks long term high
	// !!! maybe just accelerating significant growth
	// TODO
	// build minute cache
	// if speed needed, add realtime
	//BinanceApiWebSocketClient client = BinanceApiClientFactory.newInstance().newWebSocketClient();
	// Listen for aggregated trade events for ETH/BTC
	//client.onAggTradeEvent (
	//"ethbtc", response -> System.out.println(response));
	private Map<String, Long> numbers = new HashMap<>();

	private CandleStorage storage = new CandleStorage(home.resolve("candle_storage_00"), rest);
	private boolean compute = true;

	private void evaluate() {
		List<SymbolPair> pairs = getPairs();
		for (int i = 0; i < pairs.size(); i++) {
			String code = pairs.get(i).toString();
			if (!code.contains("BTC")) {
				continue;
			}
			System.out.println("PAIR " + code);
			ExperimentDirs dirs = new ExperimentDirs(globalDirs.getHome().resolve("data_" + pairs.get(i)));
			Sample2 results;
			if (compute) {
				CandleUpdater candles;
				if (update) {
					candles = storage.updateCandles(pairs.get(i));
					candles.check();
				} else {
					candles = storage.get(pairs.get(i), readMax);
				}

				WindowsFactory wf = new WindowsFactory(candles, pars);
				Windows windows = wf.createWindows();

				if (pars.invertCandles) {
					windows.invertValues();
				}

				System.out.println("windows " + windows.size());
				Windows[] ws = windows.splitByTime(0.66);
				Featurizer featurizer = new SimpleFeaturizer();
				createDataset(ws[0], featurizer, dirs.getTrain());
				featurizer.printStats();
				createDataset(ws[1], featurizer, dirs.getTest());
				featurizer.printStats();
				Model model = createModel(dirs);
				results = testModel(model, ws[1], featurizer, dirs); // first simple eval using only arff? for histograms
			} else {
				results = new Sample2(dirs.getResultsCsv());
			}
			RegressionResults rr = new RegressionResults(results, globalDirs, dirs);
			rr.save();

			// later plan?? 
			// visualize sensible information in real time, and on plots
			//Trader trader = new Trader(model);
			//evaluateTrader(trader);
		}
	}

	private void createDataset(Windows windows, Featurizer featurizer, Path file) {
		Dataset dataset = new Dataset();
		int n = 0;
		for (Window w : windows) {
			Instance i = featurizer.createInstance(w);
			dataset.add(i);
			n = i.size();
		}
		dataset.toArff(file.toFile());
		System.out.println("Features: " + n + " Instances: " + windows.size());

	}

	private Model createModel(ExperimentDirs dirs) {
		Model model = new RandomForestRegressionSmile(dirs.getTrain(), dirs.getRandomForest());
		return model;
	}

	private Sample2 testModel(Model model, Windows windows, Featurizer featurizer, ExperimentDirs dirs) {
		System.out.println("Evaluating windows: " + windows.size());
		Sample2 s = new Sample2();
		for (Window w : windows) {
			Instance i = featurizer.createInstance(w);
			double real = i.getTarget();
			double predicted = model.predict(i);
			s.add(real, predicted);
		}
		s.toCsv(dirs.getResultsCsv());
		return s;
		//Trader trader = new Trader();
		//for (Window w : windows) {
		// evaluator should give trader candles one by one
		// instance stripped of target, create from window
		//	double profit = trader.trade();
		//}
	}

	private void evaluateTrader(Trader trader) {

	}

	// SIMPLE
	// just collect values, store double
	// remember addNominal or numberic, check
	// addNominal, addNumberic, newInstance
	// string name of variable as optional parameter
	// TODO
	// reflection, name from java
	// Integer, Double, or arrays of those
	// detect nulls
	// derive nominals before writing
	//

	/*private Dataset createDataset(List<Window> windows) {
		Type type = new Type();

		Feature[] lows = new Feature[windowALength];
		for (int i = 0; i < windowALength; i++) {
			lows[i] = type.addNumericalFeature("low_" + i);
		}
		int[] targetLabels = {0, 1};
		Feature target = type.addNominalFeature("drop", targetLabels);

		Dataset data = new Dataset(type);

		for (Window w : windows) {
			Instance instance = new Instance(type);
			for (int i = 0; i < windowALength; i++) {
				instance.setNumericalValue(lows[i], w.get(i).getLow());
			}
			boolean drop = false;
			double last = w.get(windowALength - 1).getLow() * 0.98;
			for (int i = windowALength; i < windowALength + windowBLength; i++) {
				if (w.get(i).getLow() < last) {
					drop = true;
				}
			}
			instance.setNominalValue(target, drop ? 1 : 0);
			data.add(instance);
		}

		return data;

	}*/
	private List<SymbolPair> getPairs() {
		List<SymbolPair> pairs = new ArrayList<>();
		Set<SymbolPair> set = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(home.resolve("most_traded.txt").toFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "/");
				String a = st.nextToken();
				String b = st.nextToken();
				if (b.equals("BUSD")) {
					b = "USDT";
				}
				SymbolPair pair = new SymbolPair(a, b);
				if (!set.contains(pair)) {
					pairs.add(pair);
					set.add(pair);
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return pairs;
	}

	private void getSymbols() {
		ExchangeInfo info = rest.getExchangeInfo();

		System.out.println(info.getSymbols().size());

		for (SymbolInfo si : info.getSymbols()) {
			System.out.println(si.getSymbol());
		}
	}

	private void process(List<AllMarketTickersEvent> events) {
		for (AllMarketTickersEvent e : events) {
			if (numbers.containsKey(e.getSymbol())) {
				//System.err.println(e.getSymbol());
			} else {
				numbers.put(e.getSymbol(), e.getTotalNumberOfTrades());
				System.out.println(numbers.size());
			}
		}

	}

	public static void main(String[] args) {
		Backtest s = new Backtest();
		s.evaluate();
	}
}
