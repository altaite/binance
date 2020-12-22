package altaite.binance.scanner;

import altaite.binance.data.SymbolPair;
import altaite.binance.global.io.GlobalDirs;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanners {

	private GlobalDirs globalDirs = new GlobalDirs(GlobalDirs.defaultDir);
	private List<SymbolPair> pairs = new ArrayList<>();
	private Map<SymbolPair, Scanner> scanners = new HashMap<>();

	public Scanners() {
		int i = 0;
		//for (SymbolPair p : globalDirs.getMostTradedPairs()) {
		SymbolPair p = new SymbolPair("btc", "usdt");
		System.out.println("!!! " + p);
		pairs.add(p);
		i++;
		/*if (i > 50000000) {
			break;
		}*/
		//}
		//pairs.add(new SymbolPair("btc", "usdt"));
	}

	public void run() {
		initializeScanners();
		hookScanners();
	}

	private void initializeScanners() {
		for (SymbolPair pair : pairs) {
			try {
				Scanner scanner = new Scanner(globalDirs, pair);
				scanners.put(pair, scanner);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void hookScanners() {
		// update here to minimize missing candle while saving
		//storage.save(pair);

		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		BinanceApiWebSocketClient client = factory.newWebSocketClient();
		for (SymbolPair pair : pairs) {
			Scanner scanner = scanners.get(pair);
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
