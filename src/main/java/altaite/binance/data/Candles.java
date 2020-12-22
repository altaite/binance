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
import java.util.ArrayList;
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

	public SymbolPair getPair() {
		return pair;
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
		for (long l = last - minutes * 60000L; l < last; l += 60000) {
			a[i++] = map.get(l);
		}
		return a;
	}

	public Candle[] getEndInterval(int a, int b) {
		long last = getLastTime();
		Candle[] candles = new Candle[a - b + 1];
		int i = 0;
		for (long l = last - a * 60000L; l <= last - b * 60000L; l += 60000) {
			candles[i++] = map.get(l);
		}

		/*for (int k = 0; k < candles.length; k++) {
			if (candles[k] == null) {
				throw new RuntimeException("" + k + " " + candles.length);
			}
		}*/
		return candles;
	}

	public long getLastTime() {
		return map.lastKey();
	}

	private void saveAll() {
		int total = 0;
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
			for (Candle c : map.values()) {
				c.write(dos);
				total++;
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		System.out.println("Saved " + total + " candles to " + file);
	}

	void save() {
		int total = 0;
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, true))) {
			//dos.writeInt(map.size());
			for (Candle c : map.values()) {
				if (c.getOpenTime() > lastTimeInFile) {
					c.write(dos);
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
			System.out.println("File " + file + " does not exist.");
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
				//c.invert(); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				map.put(c.getOpenTime(), c);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		//fixOpenCloseCorruption();
		check();
		System.out.println("...loaded " + map.size());
		System.out.println("Last time in file is " + Format.date(lastTimeInFile));
	}

	private void fixOpenCloseCorruption() {
		for (Candle c : map.values()) {
			if (c.getCloseTime() - c.getOpenTime() != 59999) {
				c.setCloseTime(c.getOpenTime() + 59999);
			}
		}
	}

	private void check() {
		Candle old = null;
		List<Long> remove = new ArrayList<>();
		for (Candle c : map.values()) {
			if (old != null) {
				{
					long gap = c.getOpenTime() - old.getOpenTime();
					if (gap > 60000) {
						System.out.println("GAP " + hours(gap) + " " + Time.format(c.getOpenTime()));
					} else if (gap < 60000) {
						System.err.println(Format.date(old.getOpenTime()));
						System.err.println(Format.date(c.getOpenTime()));
						remove.add(old.getOpenTime());
						remove.add(c.getOpenTime());
						//throw new RuntimeException(" " + (c.getOpenTime() - old.getOpenTime()));
					}
				}
				long gap = c.getCloseTime() - old.getCloseTime();
				if (gap > 60000) {
					System.out.println("GAP " + hours(gap) + " " + Time.format(c.getCloseTime()));
				} else if (gap < 60000) {
					System.err.println(Format.date(old.getOpenTime()));
					System.err.println(Format.date(old.getCloseTime()));
					System.err.println(Format.date(c.getOpenTime()));
					System.err.println(Format.date(c.getCloseTime()));
					remove.add(old.getOpenTime());
					remove.add(c.getOpenTime());
					//throw new RuntimeException(" " + (c.getCloseTime() - old.getCloseTime()));
				}
			}
			old = c;
		}
		if (!remove.isEmpty()) {
			System.out.println("Removing " + remove.size() + " corrupted candles.");
			for (long l : remove) {
				map.remove(l);
			}
			saveAll();
		}
	}

	private long hours(long ms) {
		return ms / (1000L * 3600);
	}

	public void update(BinanceApiRestClient rest, double monthsBack) {
		System.out.println("Updating...");
		Long lastInFile = getMaxTime();
		long span = Time.monthsToMilliseconds(monthsBack);
		if (lastInFile == null) {
			long now = System.currentTimeMillis();
			lastInFile = now - span;
		}

		//System.out.println("last " + Time.format(lastInFile));
		long startTime = lastInFile;
		boolean progressing = true;
		while (startTime <= System.currentTimeMillis() & progressing) {
			long endTime = startTime + span;
			List<Candlestick> list = rest.getCandlestickBars(pair.toString(), CandlestickInterval.ONE_MINUTE, 1000,
				startTime, endTime);
			//System.out.println("Request:");
			//System.out.println(Time.format(startTime));
			//System.out.println(Time.format(endTime));
			progressing = false;
			for (Candlestick cs : list) {
				if (cs.getOpenTime() > lastInFile) {
					put(new Candle(cs));
					//System.out.println("! " + Time.format(cs.getOpenTime()));
					progressing = true;
				}
			}
			//System.out.println("updated " + list.size() + " " + Time.format(startTime));

			Long maxTime = getMaxTime();
			if (maxTime != null) {
				startTime = getMaxTime() + 1; // roughly
			} else {
				startTime += 1000;
			}
		}
		check();
		System.out.println("...updated.");
	}

}
