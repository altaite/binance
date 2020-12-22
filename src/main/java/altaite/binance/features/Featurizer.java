package altaite.binance.features;

import altaite.binance.data.window.Window;
import altaite.learn.Instance;

public interface Featurizer {

	public Instance createInstance(Window window);

	public double getSellPrice(Window window);

}
