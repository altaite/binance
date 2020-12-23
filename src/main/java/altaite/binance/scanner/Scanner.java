package altaite.binance.scanner;

import altaite.binance.data.Candle;
import altaite.binance.data.CandleStorage;
import altaite.binance.data.Candles;
import altaite.binance.data.SymbolPair;
import altaite.binance.data.window.ExperimentParameters;
import altaite.binance.data.window.Window;
import altaite.binance.features.HighFeaturizer;
import altaite.binance.global.io.ExperimentDirs;
import altaite.binance.global.io.GlobalDirs;
import altaite.learn.MyInstance;
import altaite.learn.model.Model;
import altaite.learn.model.RandomForestClassifierSmile;
import com.binance.api.client.domain.event.CandlestickEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Scanner {

	private GlobalDirs globalDirs = new GlobalDirs(GlobalDirs.defaultDir);
	private SymbolPair pair;
	private Candles chart;
	private Model model;
	private PredictionInterpreter interpreter;
	private ExperimentDirs dirs;
	private HighFeaturizer featurizer;
	private ExperimentParameters pars = new ExperimentParameters();
	private CandleStorage storage = new CandleStorage(globalDirs.getCandleStorageSmall());

	public Scanner(GlobalDirs globalDirs, SymbolPair pair) {
		this.globalDirs = globalDirs;
		this.pair = pair;
		this.dirs = new ExperimentDirs(globalDirs, pair, pars.getExperimentDescription());
		//this.model = new RandomForestRegressionSmile(this.dirs.getModel());
		Path modelPath = globalDirs.getModelFull();
		this.model = new RandomForestClassifierSmile(modelPath);
		this.featurizer = new HighFeaturizer(pars);
		this.interpreter = new PredictionInterpreter(dirs.getResultsRawCsv());
		updateCandles();
	}

	public synchronized void process(CandlestickEvent e) {
		try {
			chart.put(new Candle(e));
			predict();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void updateCandles() {
		double monthsBack = ((double) pars.getMonthsBack()) / 60 / 24 / 30;
		System.out.println("Updating ... " + monthsBack);
		chart = storage.update(pair, monthsBack);
		storage.save(pair);
	}

	private synchronized void predict() {
		Candle[] a = chart.getEnd(pars.getFeatureN());
		check(a);
		Window w = new Window(a);
		MyInstance instance = featurizer.createInstance(w);
		double prediction = model.predict(instance);
		System.out.println(prediction + " " + interpreter.percentilePredicted(prediction));
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
