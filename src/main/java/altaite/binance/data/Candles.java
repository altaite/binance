package altaite.binance.data;

import altaite.binance.analysis.CandleArray;
import altaite.format.Format;
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

public class Candles {

	private SymbolPair pair;
	private SortedMap<Long, Candle> map = new TreeMap<>();
	private long lastTimeInFile;
	private File file;

	public Candles(SymbolPair pair, File file, int readMax) {
		this.pair = pair;
		this.file = file;
		load(readMax);
	}

	public Candles(SymbolPair pair, File file) {
		this(pair, file, Integer.MAX_VALUE);
	}

	public void put(Candle c) {
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

	public CandleArray getCandleArray() {
		Candle[] a = new Candle[map.size()];
		int i = 0;
		for (Candle c : map.values()) {
			a[i++] = c;
		}
		return new CandleArray(a);
	}

	public Candle[] getEnd(int minutes) {
		long last = getLastTime();
		Candle[] a = new Candle[minutes];
		int i = 0;
		for (long l = last - minutes * 60000; l < last; l += 60000) {
			a[i++] = map.get(l);
		}
		return a;
	}

	public long getLastTime() {
		return map.lastKey();
	}

	void save() {
		int total = 0;
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
			//dos.writeInt(map.size());
			for (Candle c : map.values()) {
				if (c.getOpenTime() > lastTimeInFile) {
					c.write(dos);
					System.out.println(Format.date(c.getOpenTime()) + " saved.");
					total++;
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		System.out.println("Saved " + total + " candles to " + file);
	}

	private void load(int readMax) {
		int candlesRead = 0;
		System.out.println("Loading from " + file);
		if (!file.exists()) {
			System.out.println("File does not exist.");
			return;
		}
		System.out.println("loading " + file.getAbsolutePath() + " ...");
		try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
			/*int n = dis.readInt();
			if (n > readMax) {
				n = readMax;
			}*/
			while (dis.available() > 0) {
				//for (int i = 0; i < n; i++) {
				Candle c = new Candle(dis);
				if (c.getOpenTime() > lastTimeInFile) {
					lastTimeInFile = c.getOpenTime();
				}
				map.put(c.getOpenTime(), c);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		check();
		System.out.println("...loaded " + map.size());
		System.out.println("Last time in file is " + Format.date(lastTimeInFile));
	}

	private void check() {
		Candle old = null;
		for (Candle c : map.values()) {
			if (old != null) {
				long gap = c.getOpenTime() - old.getOpenTime();
				if (gap > 60000) {
					System.out.println("GAP " + hours(gap) + " " + Time.format(c.getOpenTime()));
				}
			}
			old = c;
		}
	}

	private long hours(long ms) {
		return ms / (1000L * 3600);
	}

	// suspicious, why always 1 updated
	public void update(BinanceApiRestClient rest, double monthsBack) {
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
					put(new Candle(cs));
					progressing = true;
				}
			}
			//System.out.println("updated " + list.size() + " " + Time.format(startTime));
			startTime = getMaxTime() + 1; // roughly
		}
		check();
	}

}
