package altaite.binance.features;

import altaite.binance.data.window.Window;
import altaite.learn.MyInstance;

public interface Featurizer {

	public MyInstance createInstance(Window window);

	public double getSellPrice(Window window);

}
