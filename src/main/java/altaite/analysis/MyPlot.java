package altaite.analysis;

import altaite.analysis.graphics.Plot;
import java.io.IOException;

public class MyPlot {

	private PairInt screen = new PairInt(1920 - 100, 1080 - 100);
	private Sample2 sample;
	private Pair rangeX;

	public MyPlot() {

	}

	public void setRangeX(double a, double b) {
		rangeX = new Pair(a, b);
	}

	public void add(Sample2 s) {
		sample = s;

	}

	public void draw(String path) {
		Plot.Data data = Plot.data();
		for (Pair p : sample.getPairs()) {
			data = data.xy(p.x, p.y);
		}

		Plot.PlotOptions o = Plot.plotOpts().width(screen.x).height(screen.y);
		Plot plot = Plot.plot(o).series(null, data, null);
		if (rangeX != null) {
			plot.yAxis("", Plot.axisOpts().range(rangeX.x, rangeX.y));
		} else {		
			plot.yAxis("", Plot.axisOpts());
		}
		try {
			plot.save(path, "png");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
