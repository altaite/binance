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
	private PairInt screen = new PairInt(1920 - 100, 1080 - 100);
	
	public RegressionResults(Sample2 sample, Windows windows, GlobalDirs globalDirs, ExperimentDirs dirs) {
		this.dirs = dirs;
		this.windows = windows;
		this.globalDirs = globalDirs;
		this.sample = sample;
		this.dir = dirs.getResultsDir();
		this.interpreter = new PredictionInterpreter(sample);
	}
	
	public void save() {
		summary();
		sample.toCsv(dir.resolve("high_regression.csv").toFile());
		time();
		plotUnsmooth();
		correlation();
		saveHeatmap();
		//plotRecallToAverage();
		examples();
		percentileToLocalAverage();
	}
	
	private void summary() {
		Sample2 sorted = sample.createSortedByY(); // by prediction
		LineFile summary = new LineFile(dir.resolve("summary.txt"));
		Sample real = new Sample(sorted.getXs()); // real values
		summary.writeLine("Correlation: " + sample.correlation());
		summary.writeLine("90 % highest predictions: " + real.head(0.9).average());
		summary.writeLine("95 % highest predictions: " + real.head(0.95).average());
		summary.writeLine("98 % highest predictions: " + real.head(0.98).average());
		summary.writeLine("99 % highest predictions: " + real.head(0.99).average());
	}
	
	public PredictionInterpreter getInterpreter() {
		return interpreter;
	}
	
	private void percentileToLocalAverage() {
		Sample2 byY = interpreter.getByY();
		Sample2 plot = new Sample2();
		int n = byY.size();
		double[] xs = byY.getXs();
		double[] ys = byY.getYs();
		for (int i = 0; i < byY.size(); i++) {
			double di = i;
			double percentile = di / n;
			double a = percentile - 0.05;
			double b = percentile + 0.05;
			int ai = (int) Math.floor(a * n);
			if (ai < 0) {
				ai = 0;
			}
			int bi = (int) Math.ceil(b * n);
			if (bi >= n) {
				bi = n - 1;
			}
			int count = 0;
			double sum = 0;
			// TODO max 10
			//System.out.println(ai + " -> " + bi + " " + percentile);
			/*while (bi - ai > 200) {
				ai++;
				bi--;
			}*/
			for (int k = ai; k <= bi; k++) {
				sum += xs[k];
				count++;
			}
			double avg = sum / count;
			plot.add(percentile, avg);
		}
		MyPlot main = new MyPlot();
		main.add(plot);
		main.setRangeX(-0.01, 0.01);
		main.draw(dir.resolve("pct_to_local_avg_comp").toString());
		
		MyPlot main2 = new MyPlot();
		main2.add(plot);
		main2.setRangeX(-0.001, 0.0025);
		main2.draw(dir.resolve("pct_to_local_avg").toString());
		//plot(plot, dir.resolve("pct_to_local_avg").toString(), screen.x, screen.y);
		plot(plot.filter(p -> p.x >= 0.95), dir.resolve("pct_to_local_avg_95").toString(), screen.x, screen.y);
		plot(plot.filter(p -> p.x <= 0.05), dir.resolve("pct_to_local_avg_05").toString(), screen.x, screen.y);
	}
	
	private void correlation() {
		System.out.println("Correlation: " + sample.correlation());
	}
	
	private void saveHeatmap() {
		Sample x = new Sample(sample.getXs());
		Sample y = new Sample(sample.getYs());
		double outlierPercent = 0.01;
		double xa = x.getValueForPercentile(outlierPercent);
		double xb = x.getValueForPercentile(1 - outlierPercent);
		double ya = y.getValueForPercentile(outlierPercent);
		double yb = y.getValueForPercentile(1 - outlierPercent);
		Heatmap hm = new Heatmap(xa, ya, xb, yb, screen.x / 10, screen.y / 10, 10, globalDirs.getHeatMapColors());
		hm.addAll(sample.box(xa, ya, xb, yb));
		hm.save(dir.resolve("heatmap.png").toFile());
	}
	
	private void plotRecallToAverage() {
		Sample2 thresholdToAverage = interpreter.thresholdToAverageAboveThreshold();
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
