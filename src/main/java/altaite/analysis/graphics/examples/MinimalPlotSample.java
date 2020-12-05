package altaite.analysis.graphics.examples;

import altaite.analysis.graphics.Plot;
import java.io.IOException;

/**
 * Minimal plot sample
 * 
 * @author Yuriy Guskov
 *
 */
public class MinimalPlotSample {

	public static void main(String[] args) throws IOException {
		// configuring everything by default
		Plot plot = Plot.plot(null).
			// setting data
			series(null, Plot.data().
				xy(1, 2).
				xy(3, 4), null);
		// saving sample_minimal.png
		plot.save("d:/t/data/binance/sample_minimal", "png");
	}
}
