package altaite.learn.model;

import altaite.learn.Instance;

public interface Model {
	public double predict(Instance instance);
}
