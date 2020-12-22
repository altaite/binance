package altaite.binance.scanner;

import altaite.binance.data.Candle;
import altaite.binance.data.CandleStorage;
import altaite.binance.data.Candles;
import altaite.binance.data.SymbolPair;
import altaite.binance.data.window.ExperimentParameters;
import altaite.binance.global.io.ExperimentDirs;
import altaite.binance.global.io.GlobalDirs;
import com.binance.api.client.domain.event.CandlestickEvent;

// TODO pictures of big rises in hour, with volumes
// classifier instead?
// rise much bigger than fees !!! all markets
public class SimpleScanner {

	private GlobalDirs globalDirs = new GlobalDirs(GlobalDirs.defaultDir);
	private SymbolPair pair;
	private Candles chart;
	private ExperimentDirs dirs;
	private ExperimentParameters pars = new ExperimentParameters();
	private CandleStorage storage;
	private Qualities qualities;

	public SimpleScanner(GlobalDirs globalDirs, SymbolPair pair, CandleStorage storage, Qualities qualities) {
		this.globalDirs = globalDirs;
		this.pair = pair;
		this.dirs = new ExperimentDirs(globalDirs, pair, pars.getExperimentDescription());
		this.storage = storage;
		this.qualities = qualities;
		updateCandles();
	}

	public synchronized void process(CandlestickEvent e) {
		try {
			chart.put(new Candle(e));
			qualities.conditionalUpdate(chart);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void updateCandles() {
		double monthsBack = pars.getMediumRangeMonthsBack();
		System.out.println("Updating ... " + monthsBack);
		chart = storage.update(pair, monthsBack);
		qualities.update(chart);
		qualities.allIntoCsv(globalDirs.getQualitiesCsv());	
		storage.save(pair);

	}

	private void check(Candle[] a) {
		for (int i = 0; i < a.length - 1; i++) {
			long d = a[i + 1].getOpenTime() - a[i].getOpenTime();
			if (d != 60000) {
				throw new RuntimeException();
			}
		}
	}

}
