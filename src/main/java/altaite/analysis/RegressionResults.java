package altaite.analysis;

import altaite.analysis.graphics.Plot;
import altaite.analysis.graphics.Plot.Data;
import altaite.analysis.graphics.Plot.PlotOptions;
import altaite.binance.global.io.ExperimentDirs;
import altaite.binance.global.io.GlobalDirs;
import global.io.LineFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class RegressionResults {

	private Sample2 sample;
	private Path dir;
	private ExperimentDirs dirs;
	private GlobalDirs globalDirs;

	public RegressionResults(Sample2 sample, GlobalDirs globalDirs, ExperimentDirs dirs) {
		this.dirs = dirs;
		this.globalDirs = globalDirs;
		this.sample = sample;
		this.dir = dirs.getResultsDir();
	}

	public void save() {
		sample.toCsv(dir.resolve("high_regression.csv").toFile());

		correlation();
		saveHeatmap();
		plotRecallToAverage();

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
		Heatmap hm = new Heatmap(x.min(), y.min(), x.max(), y.max(), 100, 100, 10, globalDirs.getHeatMapColors());
		hm.addAll(sample);
		hm.save(dir.resolve("regression_heatmap.png").toFile());
	}

	private void plotRecallToAverage() {
		Sample2 thresholdToAverage = Curves.thresholdToAverageOfHighestPredictions(sample);
		thresholdToAverage.toCsv(dir.resolve("threshold_to_average.csv").toFile());
		plot(thresholdToAverage, dir.resolve("threshold_to_average").toString());

		Sample2 recallToAverage = new Sample2();
		double i = 0;
		for (Pair p : thresholdToAverage.getPairs()) {
			double percent = i / thresholdToAverage.size();
			recallToAverage.add(percent, p.y);
			i += 1;
		}
		recallToAverage.toCsv(dir.resolve("recall_to_average.csv").toFile());
		plot(recallToAverage, dir.resolve("recall_to_average").toString());

		Sample2 head = new Sample2(recallToAverage, p -> p.x <= 0.9);

		plot(head, dir.resolve("recall_to_average_head_90").toString());
		String area = "Area under curve 90 %: " + head.areaUnderCurve();
		String average90 = "90 % best average: " + head.get(head.size() - 1).y;
		LineFile summary = new LineFile(dir.resolve("summary.txt"));
		summary.writeLine(area);
		summary.writeLine(average90);
		System.out.println(area);
		System.out.println(average90);

	}

	private void plot(Sample2 s, String path) {
		Data data = Plot.data();
		for (Pair p : s.getPairs()) {
			data = data.xy(p.x, p.y);
		}
		PlotOptions o = Plot.plotOpts().width(1000).height(1000);
		Plot plot = Plot.plot(o).series(null, data, null);
		try {
			plot.save(path, "png");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
