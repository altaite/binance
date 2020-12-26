package altaite.binance.data;

import altaite.analysis.Sampling;
import altaite.binance.data.window.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	public void sort() {
		Collections.sort(windows);
	}

	public long getMedianTime() {
		long[] times = new long[windows.size()];
		for (int i = 0; i < times.length; i++) {
			times[i] = windows.get(i).getStart();
		}
		Arrays.sort(times);
		return times[times.length / 2];
	}

	public Windows(Window[] a) {
		for (Window w : a) {
			windows.add(w);
		}
	}

	public void add(Window w) {
		windows.add(w);
	}

	public void add(Windows other) {
		windows.addAll(other.getWindows());
	}

	public List<Window> getWindows() {
		return windows;
	}

	public Window get(int i) {
		return windows.get(i);
	}

	public void invertValues() {
		for (Window w : windows) {
			w.invertCandles();
		}
	}

	public int size() {
		return windows.size();
	}

	public Windows[] split(long time) {
		long span = windows.get(0).getEnd() - windows.get(0).getStart();
		Windows a = new Windows();
		Windows b = new Windows();
		for (Window w : windows) {

			if (w.getStart() < time - span / 2) {
				a.add(w);
			} else if (w.getStart() > time + span / 2) {
				b.add(w);
			}
		}
		Windows[] ws = {a, b};
		return ws;
	}

	public Windows[] split(long[] times) {
		Windows[] split = new Windows[times.length + 1];
		for (int i = 0; i < split.length; i++) {
			split[i] = new Windows();
		}
		for (Window w : windows) {
			for (int i = 0; i <= times.length; i++) {
				long t1 = Long.MIN_VALUE;
				long t2 = Long.MAX_VALUE;
				if (i != 0) {
					t1 = times[i - 1];
				}
				if (i != times.length) {
					t2 = times[i];
				}
				if (t1 < w.getStart() && w.getEnd() < t2) {
					split[i].add(w);
					break;
				}
			}
		}
		return split;
	}

	public Windows[] splitByPercent(double percent) {
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
			if (windows.get(a).getEnd() >= windows.get(b).getStart()) {
				overlap = true;
				if (ab) {
					a--;
				}
				if (bb) {
					b++;
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

	public long getMinTime() {
		long min = Long.MAX_VALUE;
		for (Window w : windows) {
			long t = w.getStart();
			if (t < min) {
				min = t;
			}
		}
		return min;
	}

	public long getMaxTime() {
		long max = Long.MIN_VALUE;
		for (Window w : windows) {
			long t = w.getStart();
			if (t > max) {
				max = t;
			}
		}
		return max;
	}

	@Override
	public Iterator<Window> iterator() {
		return windows.iterator();
	}
}
