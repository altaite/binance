package altaite.learn.model;

import altaite.learn.MyInstance;

public interface Model {

	public double predict(MyInstance instance);

	public void deserialize();
}
