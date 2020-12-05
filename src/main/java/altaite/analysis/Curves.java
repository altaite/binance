package altaite.analysis;

import java.io.File;

public class Curves {

	public static Sample2 thresholdToAverageOfHighestPredictions(Sample2 sample) {
		Sample2 thresholdToAverage = new Sample2();
		sample.sortByY(); // predicted
		System.out.println("!! " + sample.get(0).y);
		System.out.println("!! " + sample.get(1).y);
		
		System.out.println("above_predicted number average");
		for (int i = 0; i < sample.size(); i++) {
			double avg = averageStartingWith(sample.getXs(), i);
			double predicted = sample.get(i).y;
			//System.out.println(predicted + " " + i + " " + avg);
			thresholdToAverage.add(predicted, avg);
		}
		return thresholdToAverage;
	}

	private static double averageStartingWith(double[] xs, int i) {
		int total = 0;
		double average = 0;
		for (int k = i; k < xs.length; k++) {
			average += xs[k];
			total++;
		}
		average /= total;
		return average;
	}

	public static void main(String[] args) {
		Sample2 s = new Sample2(new File("d:/t/data/binance/data_001/high_regression.csv"));
		thresholdToAverageOfHighestPredictions(s);
	}
}
