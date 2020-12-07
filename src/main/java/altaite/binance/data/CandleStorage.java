package altaite.binance.data;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class CandleStorage {

	private Path dir;
	private Map<SymbolPair, Candles> map = new HashMap<>();
	private List<SymbolPair> pairs;
	private BinanceApiRestClient rest = BinanceApiClientFactory.newInstance().newRestClient();

	public CandleStorage(Path dir) {
		this.dir = dir;
	}

	public Candles get(SymbolPair pair) {
		return get(pair, Integer.MAX_VALUE);
	}

	public Candles get(SymbolPair pair, int readMax) {
		if (!map.containsKey(pair)) {
			File file = dir.resolve(pair.getA() + "-" + pair.getB()).toFile();
			Candles candles = new Candles(pair, file, readMax);
			map.put(pair, candles);
			return candles;
		} else {
			return map.get(pair);
		}
	}

	public Collection<SymbolPair> getPairs() {
		if (pairs == null) {
			pairs = readPairs();
		}
		return pairs;
	}

	private List<SymbolPair> readPairs() {
		List<SymbolPair> pairs = new ArrayList<>();
		for (File f : dir.toFile().listFiles()) {
			if (f.getName().contains("-")) {
				StringTokenizer st = new StringTokenizer(f.getName(), "-");
				String a = st.nextToken();
				String b = st.nextToken();
				SymbolPair pair = new SymbolPair(a, b);
				pairs.add(pair);
			}
		}
		return pairs;
	}

	public Candles update(SymbolPair pair, double monthsBack) {
		Candles candles = get(pair);
		candles.update(rest, monthsBack);

		return candles;
	}

	public void save(SymbolPair pair) {
		Candles candles = get(pair);
		System.out.println("saving...");
		candles.save();
		System.out.println("...saved");
	}

}
