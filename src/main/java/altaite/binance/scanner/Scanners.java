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

public class Scanners {

	private GlobalDirs globalDirs = new GlobalDirs(GlobalDirs.defaultDir);
	private List<SymbolPair> pairs = new ArrayList<>();
	private Map<SymbolPair, Scanner> scanners = new HashMap<>();

	public Scanners() {
		pairs.add(new SymbolPair("btc", "usdt"));
	}

	public void run() {
		createScanners();
	}

	private void createScanners() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		BinanceApiWebSocketClient client = factory.newWebSocketClient();
		for (SymbolPair pair : pairs) {
			Scanner scanner = new Scanner(globalDirs, pair);
			scanners.put(pair, scanner);
			client.onCandlestickEvent(pair.toString().toLowerCase(),
				CandlestickInterval.ONE_MINUTE,
				response -> scanner.process(response));
		}
	}

	public static void main(String[] args) {
		Scanners scanner = new Scanners();
		scanner.run();
	}
}
