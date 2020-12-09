package altaite.analysis;

import altaite.analysis.graphics.Plot;
import altaite.analysis.graphics.Plot.Data;
import altaite.analysis.graphics.Plot.PlotOptions;
import altaite.binance.data.Windows;
import altaite.binance.data.window.Window;
import altaite.binance.global.io.ExperimentDirs;
import altaite.binance.global.io.GlobalDirs;
import altaite.binance.scanner.PredictionInterpreter;
import global.io.LineFile;
import java.io.IOException;
import java.nio.file.Path;

public class RegressionResults {

	private Sample2 sample;
	private Path dir;
	private ExperimentDirs dirs;
	private GlobalDirs globalDirs;
	private Windows windows;
	private PredictionInterpreter interpreter;
	private PairInt screen = new PairInt(1920, 1080);

	public RegressionResults(Sample2 sample, Windows windows, GlobalDirs globalDirs, ExperimentDirs dirs) {
		this.dirs = dirs;
		this.windows = windows;
		this.globalDirs = globalDirs;
		this.sample = sample;
		this.dir = dirs.getResultsDir();
		this.interpreter = new PredictionInterpreter(sample);
	}

	public void save() {
		sample.toCsv(dir.resolve("high_regression.csv").toFile());
		time();
		plotUnsmooth();
		correlation();
		saveHeatmap();
		plotRecallToAverage();
		examples();
	}

	private void correlation() {
		System.out.println("Correlation: " + sample.correlation());
	}

	private void saveHeatmap() {
		Sample x = new Sample(sample.getXs());
		Sample y = new Sample(sample.getYs());
		System.out.println("---");
		System.out.println(x.min() + " " + x.max());
		System.out.println(y.min() + " " + y.max());
		Heatmap hm = new Heatmap(x.min(), y.min(), x.max(), y.max(), screen.x, screen.y, 1, globalDirs.getHeatMapColors());
		hm.addAll(sample);
		hm.save(dir.resolve("regression_heatmap.png").toFile());
	}

	private void plotRecallToAverage() {
		Sample2 thresholdToAverage = interpreter.thresholdToAverageOfHighestPredictions();
		thresholdToAverage.toCsv(dir.resolve("threshold_to_average.csv").toFile());
		plot(thresholdToAverage, dir.resolve("threshold_to_average").toString(), screen.x, screen.y);

		Sample2 recallToAverage = new Sample2();
		double i = 0;
		for (Pair p : thresholdToAverage.getPairs()) {
			double percent = i / thresholdToAverage.size();
			recallToAverage.add(percent, p.y);
			i += 1;
		}
		recallToAverage.toCsv(dir.resolve("recall_to_average.csv").toFile());
		plot(recallToAverage, dir.resolve("recall_to_average").toString(), screen.x, screen.y);

		Sample2 head = new Sample2(recallToAverage, p -> p.x <= 0.9);

		// TODO plot 10 % moving average
		// plot all in 90, but chart cutof at 90 value
		plot(head, dir.resolve("recall_to_average_head_90").toString(), screen.x, screen.y);
		//String area = "Area under curve 90 %: " + head.areaUnderCurve();
		String average90 = "90 % best average: " + head.get(head.size() - 1).y;
		LineFile summary = new LineFile(dir.resolve("summary.txt"));
		//summary.writeLine(area);
		summary.writeLine(average90);
		//System.out.println(area);
		System.out.println(average90);

	}

	private void plotUnsmooth() {
		Sample2 byPrediction = sample.createSortedByY();
		Sample2 s = new Sample2();
		int i = 0;
		for (Pair p : byPrediction.getPairs()) {
			s.add(p.y, p.x);
		}
		plot(s, dir.resolve("by_prediction").toString(), screen.x, screen.y);
	}

	private void time() {
		plotRealValuesInTime();
		plotPredictedValuesInTime();
	}

	private void plotRealValuesInTime() {
		Sample2 s = new Sample2();
		int i = 0;
		for (Pair p : sample.getPairs()) {
			s.add(i++, p.x);
		}
		plot(s, dir.resolve("time_real").toString(), screen.x, screen.y);
	}

	private void plotPredictedValuesInTime() {
		Sample2 s = new Sample2();
		int i = 0;
		for (Pair p : sample.getPairs()) {
			s.add(i++, p.y);
		}
		plot(s, dir.resolve("time_prediction").toString(), screen.x, screen.y);
	}

	private void plot(Sample2 s, String path, int witdth, int height) {
		Data data = Plot.data();
		for (Pair p : s.getPairs()) {
			data = data.xy(p.x, p.y);
		}
		PlotOptions o = Plot.plotOpts().width(witdth).height(height);
		Plot plot = Plot.plot(o).series(null, data, null);
		try {
			plot.save(path, "png");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void examples() {
		for (int i = 0; i < windows.size(); i++) {
			Pair p = sample.get(i);
			if (interpreter.percentilePredicted(p.y) > 90 && p.x > 0.002) {
				visualizeWindow(windows.get(i));
			}
		}
	}

	private void visualizeWindow(Window window) {

	}
}
