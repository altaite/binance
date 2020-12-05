package altaite.binance.data;

import altaite.binance.analysis.Candles;
import altaite.time.Time;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class CandleUpdater {

	private SymbolPair pair;
	private SortedMap<Long, Candle> map = new TreeMap<>();
	private final double monthsBack = 500;

	public CandleUpdater(SymbolPair pair, File file, int readMax) {
		this.pair = pair;
		load(file, readMax);
	}

	public void add(Candle c) {
		long t = c.getOpenTime();
		map.put(t, c);
	}

	public int size() {
		return map.size();
	}

	public Long getMinTime() {
		if (map.isEmpty()) {
			return null;
		}
		return map.firstKey();
	}

	public Long getMaxTime() {
		if (map.isEmpty()) {
			return null;
		}
		return map.lastKey();
	}

	public Candles getSortedArray() {
		Candle[] a = new Candle[map.size()];
		int i = 0;
		for (Candle c : map.values()) {
			a[i++] = c;
		}
		return new Candles(a);
	}

	public void save(File file) {
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
			dos.writeInt(map.size());
			for (Candle c : map.values()) {
				c.write(dos);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void load(File file, int readMax) {
		if (!file.exists()) {
			return;
		}
		System.out.println("loading " + file.getAbsolutePath() + " ...");
		try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
			int n = dis.readInt();
			if (n > readMax) n = readMax;
			for (int i = 0; i < n; i++) {
				Candle c = new Candle(dis);
				map.put(c.getOpenTime(), c);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		System.out.println("...loaded " + map.size());
	}

	public void check() {
		Candle old = null;
		for (Candle c : map.values()) {
			if (old != null) {
				long gap = c.getOpenTime() - old.getOpenTime();
				if (gap > 60000) {
					System.out.println(hours(gap) + " " + Time.format(c.getOpenTime()));
				}
			}
			old = c;
		}
	}

	private long hours(long ms) {
		return ms / (1000L * 3600);
	}

	// suspicious, why always 1 updated
	public void update(BinanceApiRestClient rest) {
		Long last = getMaxTime();
		long span = Time.monthsToMilliseconds(monthsBack);
		if (last == null) {
			long now = System.currentTimeMillis();
			last = now - span;
		}
		System.out.println("last " + Time.format(last));
		long startTime = last;
		boolean progressing = true;
		while (startTime <= System.currentTimeMillis() & progressing) {
			long endTime = last + span;
			List<Candlestick> list = rest.getCandlestickBars(pair.toString(), CandlestickInterval.ONE_MINUTE, 1000,
				startTime, endTime);
			progressing = false;
			for (Candlestick cs : list) {
				if (cs.getOpenTime() > last) {
					add(new Candle(cs));
					progressing = true;
				}
			}
			System.out.println("updated " + list.size() + " " + Time.format(startTime));
			startTime = getMaxTime() + 1; // roughly
		}
	}

}
