package altaite.binance.scanner;

import altaite.binance.data.SymbolPair;
import altaite.binance.data.Windows;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Folds {

	private List<Map<SymbolPair, Windows>> table; // split first by time and then by market
	private long[] times;
	private int numberOfFolds;

	public Folds(Date... dates) {
		numberOfFolds = dates.length + 1;
		table = new ArrayList<>();
		for (int i = 0; i < dates.length + 1; i++) {
			table.add(new HashMap<>());
		}
		times = new long[dates.length];
		for (int i = 0; i < dates.length; i++) {
			times[i] = dates[i].getTime();
		}
	}

	public void add(SymbolPair pair, Windows windows) {
		Windows[] split = windows.split(times);
		for (int i = 0; i < split.length; i++) {
			table.get(i).put(pair, split[i]);
		}
	}

	public Windows getFold(int i) {
		Windows all = new Windows();
		for (Windows w : table.get(i).values()) {
			all.add(w);
		}
		return all;
	}

	public Windows getFold(SymbolPair pair, int i) {
		return table.get(i).get(pair);
	}

	public Windows getFold(int a, int b) {
		Windows all = new Windows();
		for (int i = a; i <= b; i++) {
			all.add(getFold(i));
		}
		return all;
	}

	public Windows getFold(SymbolPair pair, int a, int b) {
		Windows all = new Windows();
		for (int i = a; i <= b; i++) {
			all.add(getFold(pair, i));
		}
		return all;
	}

	public int getNumberOfFolds() {
		return numberOfFolds;
	}
}
