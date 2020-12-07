package altaite.binance.scanner;

import altaite.binance.data.Candle;
import altaite.binance.data.CandleStorage;
import altaite.binance.data.Candles;
import altaite.binance.data.SymbolPair;
import altaite.binance.data.window.ExperimentParameters;
import altaite.binance.data.window.Window;
import altaite.binance.features.Featurizer;
import altaite.binance.features.SimpleFeaturizer;
import altaite.binance.global.io.ExperimentDirs;
import altaite.binance.global.io.GlobalDirs;
import altaite.learn.Instance;
import altaite.learn.model.Model;
import altaite.learn.model.RandomForestRegressionSmile;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import global.io.LineFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

	private GlobalDirs globalDirs = new GlobalDirs(GlobalDirs.defaultDir);
	private CandleStorage storage = new CandleStorage(globalDirs.getCandleStorageSmall());
	private List<SymbolPair> pairs = new ArrayList<>();
	private Map<SymbolPair, Candles> charts = new HashMap<>();
	private Map<SymbolPair, Model> models = new HashMap<>();
	private ExperimentParameters pars = new ExperimentParameters();
	private String[] currencySymbols;
	private Featurizer featurizer;

	public Scanner() {
		pairs.add(new SymbolPair("btc", "usdt"));
		loadSymbols();
		featurizer = new SimpleFeaturizer(pars);
	}

	public void run() {
		updateCandles();
		createModels();
		createListeners();
	}

	public void test() {
		ExperimentDirs dirs = new ExperimentDirs(globalDirs, new SymbolPair("btc", "usdt"));
		Model m = new RandomForestRegressionSmile(dirs.getModel());
		Instance instance = new Instance(true);
		for (int i = 0; i < 901; i++) {
			instance.addNumeric(i);
		}
		m.predict(instance);
		System.out.println(m.getClass());
	}

	private void loadSymbols() {
		LineFile lf = new LineFile(globalDirs.getCurrencySymbols());
		currencySymbols = lf.asArray();
	}

	private void createModels() {
		for (SymbolPair pair : pairs) {
			ExperimentDirs dirs = new ExperimentDirs(globalDirs, pair);
			Model m = new RandomForestRegressionSmile(dirs.getModel());
			models.put(pair, m);
		}
	}

	private void updateCandles() {
		for (SymbolPair pair : pairs) {
			double monthsBack = ((double) pars.getIndependentWindowLength()) / 60 / 24 / 30;
			System.out.println("Months back update " + monthsBack);
			Candles candles = storage.update(pair, monthsBack);
			storage.save(pair);
			charts.put(pair, candles);
		}
	}

	private void createListeners() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pairs.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			SymbolPair pair = pairs.get(i);
			sb.append(pair.toString().toLowerCase());
		}
		createListener(sb.toString().toLowerCase());
		System.out.println("Listening to " + sb.toString().toLowerCase());
	}

	private void createListener(String market) {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		BinanceApiWebSocketClient client = factory.newWebSocketClient();
		client.onCandlestickEvent(market, CandlestickInterval.ONE_MINUTE, response -> process(response));

	}

	private synchronized void process(CandlestickEvent e) {
		SymbolPair pair = SymbolPair.create(e.getSymbol(), currencySymbols);
		Candles candles = charts.get(pair);
		candles.put(new Candle(e));
		Candle[] a = candles.getEnd(pars.getIndependentWindowLength());
		check(a);
		Window w = new Window(a); // TODO without end
		Model m = models.get(pair);
		Instance instance = featurizer.createInstance(w);
		double prediction = m.predict(instance);
		System.out.println(prediction);
	}

	private void check(Candle[] a) {
		for (int i = 0; i < a.length - 1; i++) {
			long d = a[i + 1].getOpenTime() - a[i].getOpenTime();
			if (d != 60000) {
				throw new RuntimeException();
			}
		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner();
		scanner.run();
		//scanner.test();
	}
}
