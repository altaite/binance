package altaite.binance.scanner;

import altaite.analysis.Pair;
import altaite.analysis.Sample;
import altaite.analysis.Sample2;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
// visualize over whole market history - generate picture
// percentiles
// raw profit
// profit smoothened - linear decrease around  
// learn model using everything

public class PredictionInterpreter implements Serializable {

	private Sample2 byTime;
	private Sample2 byX;
	private Sample2 byY;

	public PredictionInterpreter(Sample2 results) {
		this.byTime = results;
		this.byX = results.createSortedByX();
		this.byY = results.createSortedByY();
	}

	public double percentilePredicted(double predicted) {
		Sample s = new Sample(byY.getYs());
		return s.percentile(predicted);
	}

	public double percentileReal(double real) {
		Sample s = new Sample(byX.getXs());
		return s.percentile(real);
	}
	
	/*public double predictedToReal(double predicted) {
	}*/

	public double areaUnderCurve() {
		double area = 0;
		for (int i = 0; i < byX.size() - 1; i++) {
			Pair a = byX.get(i);
			Pair b = byX.get(i + 1);
			area += (b.x - a.x) * (a.y + b.y) / 2;
		}
		return area;
	}

	public Sample2 thresholdToAverageOfHighestPredictions() {
		Sample2 thresholdToAverage = new Sample2();
		for (int i = 0; i < byY.size(); i++) {
			double avg = averageStartingWith(byY.getXs(), i);
			double predicted = byY.get(i).y;
			thresholdToAverage.add(predicted, avg);
		}
		return thresholdToAverage;
	}

	private double averageStartingWith(double[] xs, int i) {
		int total = 0;
		double average = 0;
		for (int k = i; k < xs.length; k++) {
			average += xs[k];
			total++;
		}
		average /= total;
		return average;
	}
}
