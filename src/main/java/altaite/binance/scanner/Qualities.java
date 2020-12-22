package altaite.binance.scanner;

import altaite.analysis.Sample;
import altaite.binance.data.Candle;
import altaite.binance.data.Candles;
import altaite.binance.data.SymbolPair;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Qualities {

	private long lastTime = 0;
	private Map<SymbolPair, Quality> map = new HashMap<>();
	private boolean savePictures = true;

	public synchronized void conditionalUpdate(Candles candles) {
		long now = System.currentTimeMillis();
		if (now - lastTime > 3_000) {
			lastTime = now;
			update(candles);
			print(savePictures);
			if (savePictures) {
				savePictures = false;
			}
		}
	}

	private void print(boolean savePictures) {
		List<Quality> qs = new ArrayList();
		qs.addAll(map.values());
		Collections.sort(qs);
		System.out.println("QUALITIES " + qs.size());
		int count = 0;
		for (int i = 0; i < qs.size(); i++) {
			Quality q = qs.get(i);
			if (q.isGood()) {
				q.print();
				if (savePictures) {
					// TODO more a systematic window approach using backtest
				}
				count++;
			}
			if (count > 10) {
				break;
			}
		}
		System.out.println("----------------------");
	}

	public void savePictures() {
		List<Quality> qs = new ArrayList();
		qs.addAll(map.values());
		Collections.sort(qs);
		System.out.println("QUALITIES " + qs.size());
		int count = 0;
		for (int i = 0; i < qs.size(); i++) {
			Quality q = qs.get(i);
			if (q.isGood()) {
				q.print();
				count++;
			}
			if (count > 10) {
				break;
			}
		}
		System.out.println("----------------------");
	}

	public void allIntoCsv(File f) {
		List<Quality> qs = new ArrayList();
		qs.addAll(map.values());
		Collections.sort(qs);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (Quality q : qs) {
				//if (q.getHighestRise() != null && q.getVolumeRatio() != null) {
				bw.write(q.getPair() + "," + q.getHighestRise() + "," + "\n");
				//}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * For initial update only.
	 */
	public void update(Candles chart) {
		SymbolPair sp = chart.getPair();
		Quality q = map.get(sp);
		if (q == null) {
			q = new Quality(sp);
			map.put(chart.getPair(), q);
		}

		Candle[] a = chart.getEndInterval(60 * 24 * 30, 60 * 24);
		Candle[] b = chart.getEndInterval(60 * 24, 4);
		Candle[] c = chart.getEnd(4);

		q.setVolumeA(getAverageVolume(a));
		q.setVolumeB(getAverageVolume(b));
		q.setVolumeC(getAverageVolume(c));
		q.setHighestRise(getHighestRise(b));
	}

	private double getHighestRise(Candle[] candles) {
		double maxBase = Double.MAX_VALUE; // for division
		double maxDiff = 0;
		for (int x = 0; x < candles.length; x++) {
			if (candles[x] != null) {
				double base = candles[x].getOpen();
				for (int y = x; y < candles.length; y++) {
					if (candles[y] != null) {
						double diff = candles[y].getClose() - base;
						if (diff > maxDiff) {
							maxDiff = diff;
							maxBase = base;
						}
					}
				}
			}
		}

		return maxDiff / maxBase;
		/*Sample lows = new Sample(candles, c -> c.getLow());
		Sample highs = new Sample(candles, c -> c.getHigh());
		double max = highs.max();
		double min = lows.min();
		return (max - min) / min;*/
	}

	private double getVolumeRatio(Candle[] a, Candle[] b) {
		Sample volumesA = new Sample(a, c -> c.getVolume());
		double aa = volumesA.average();
		Sample volumesB = new Sample(b, c -> c.getVolume());
		double ab = volumesB.average();
		return ab / aa;
	}

	private double getAverageVolume(Candle[] candles) {
		Sample volumes = new Sample(candles, c -> c.getVolume());
		return volumes.average();
	}

}
