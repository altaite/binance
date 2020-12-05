package altaite.binance.data;

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
	private BinanceApiRestClient rest;
	private Map<SymbolPair, CandleUpdater> map = new HashMap<>();
	private List<SymbolPair> pairs;

	public CandleStorage(Path dir, BinanceApiRestClient rest) {
		this.dir = dir;
		this.rest = rest;
	}

	public CandleUpdater get(SymbolPair pair) {
		return get(pair, Integer.MAX_VALUE);
	}

	public CandleUpdater get(SymbolPair pair, int readMax) {
		if (!map.containsKey(pair)) {
			CandleUpdater candles = new CandleUpdater(pair, getFile(pair), readMax);
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

	public CandleUpdater updateCandles(SymbolPair pair) {
		CandleUpdater candles = get(pair);
		candles.update(rest);
		System.out.println("saving...");
		candles.save(getFile(pair));
		System.out.println("...saved");
		return candles;
	}

	private File getFile(SymbolPair pair) {
		return dir.resolve(pair.getA() + "-" + pair.getB()).toFile();
	}

}
