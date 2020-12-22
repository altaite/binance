package altaite.binance.scanner;

import altaite.analysis.Sample;
import altaite.binance.data.Candle;
import altaite.binance.data.SymbolPair;
import altaite.binance.data.window.ExperimentParameters;

public class QualityEstimator {

	private SymbolPair pair;
	private Candle[] candles;
	private ExperimentParameters pars;

	public QualityEstimator(SymbolPair pair, Candle[] candles, ExperimentParameters pars) {
		this.pair = pair;
		this.candles = candles;
		this.pars = pars;
	}

	public Quality compute() {
		Sequence s = new Sequence(candles);
		Sequence d = s.chipEnd(pars.getDLength());
		Sequence c = s.chipEnd(pars.getCLength());
		Sequence b = s.chipEnd(pars.getBLength());
		Sequence a = s.chipEnd(pars.getALength());

		Quality q = new Quality(pair);
		q.setVolumeA(getAverageVolume(a.getCandles()));
		q.setVolumeB(getAverageVolume(b.getCandles()));
		q.setVolumeC(getAverageVolume(c.getCandles()));
		return q;
	}

	private double getAverageVolume(Candle[] candles) {
		Sample volumes = new Sample(candles, c -> c.getVolume());
		return volumes.average();
	}

}
