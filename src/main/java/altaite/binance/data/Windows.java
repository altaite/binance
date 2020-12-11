package altaite.binance.data;

import altaite.analysis.Sampling;
import altaite.binance.data.window.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Windows implements Iterable<Window> {

	private List<Window> windows = new ArrayList<>();

	public Windows() {
	}

	public Windows sample(int howMany) {
		Windows ws = new Windows();
		boolean[] sample = Sampling.sample(1, size(), howMany);
		for (int i = 0; i < windows.size(); i++) {
			if (sample[i]) {
				ws.add(get(i));
			}
		}
		return ws;
	}

	public Windows(Window[] a) {
		for (Window w : a) {
			windows.add(w);
		}
	}

	public Window get(int i) {
		return windows.get(i);
	}

	public void invertValues() {
		for (Window w : windows) {
			w.invertCandles();
		}
	}

	public void add(Window w) {
		windows.add(w);
	}

	public int size() {
		return windows.size();
	}

	public Windows[] splitByTime(double percent) {
		boolean overlap = false;
		int a = 0;
		int b = windows.size() - 1;
		boolean ab, bb;
		while (!overlap) {
			int included = a + (windows.size() - b);
			double fraction = ((double) a) / included;
			if (fraction < percent) {
				a++;
				ab = true;
				bb = false;
			} else {
				b--;
				bb = true;
				ab = false;
			}
			if (windows.get(a).getEnd() > windows.get(b).getStart()) {
				overlap = true;
				if (ab) {
					a--;
				}
				if (bb) {
					b--;
				}
			}
		}
		Window[] wa = new Window[a];
		Window[] wb = new Window[windows.size() - b];
		for (int i = 0; i < wa.length; i++) {
			wa[i] = windows.get(i);
			//System.out.println("wa " + i);
		}
		for (int i = 0; i < wb.length; i++) {
			wb[i] = windows.get(b + i);
			//System.out.println("wb " + (b + i));
		}
		Windows[] w = {new Windows(wa), new Windows(wb)};
		return w;
	}

	@Override
	public Iterator<Window> iterator() {
		return windows.iterator();
	}
}
