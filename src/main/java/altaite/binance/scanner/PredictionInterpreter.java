package altaite.binance.scanner;

import altaite.analysis.Pair;
import altaite.analysis.Sample;
import altaite.analysis.Sample2;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;
// visualize over whole market history - generate picture
// percentiles
// raw profit
// profit smoothened - linear decrease around  
// learn model using everything
// TODO histogram of values for neighborhood of predicted value, also do that as heatmap?

public class PredictionInterpreter implements Serializable {

	private Sample2 byTime;
	private Sample2 byX;
	private Sample2 byY;

	public PredictionInterpreter(Sample2 byTime) {
		this.byTime = byTime;
		this.byX = byTime.createSortedByX();
		this.byY = byTime.createSortedByY();
	}

	public PredictionInterpreter(File timedCsv) {
		byTime = new Sample2();
		try (BufferedReader br = new BufferedReader(new FileReader(timedCsv))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ",");
				double x = Double.parseDouble(st.nextToken());
				double y = Double.parseDouble(st.nextToken());
				byTime.add(x, y);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		this.byX = byTime.createSortedByX();
		this.byY = byTime.createSortedByY();
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

	// inefficient@@@@@@@@@@@@@@@@@@@@@@
	public Sample2 thresholdToAverageOfHighestPredictions() {
		System.out.println("ineficient start");
		Sample2 thresholdToAverage = new Sample2();
		for (int i = 0; i < byY.size(); i++) {
			double avg = averageStartingWith(byY.getXs(), i);
			double predicted = byY.get(i).y;
			thresholdToAverage.add(predicted, avg);
		}
		System.out.println("ineficient end");
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
