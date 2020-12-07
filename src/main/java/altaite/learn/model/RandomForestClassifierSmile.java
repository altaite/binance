package altaite.learn.model;

import altaite.learn.Instance;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.io.Read;
import smile.data.formula.Formula;
import smile.data.type.DataType;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.classification.RandomForest;

public class RandomForestClassifierSmile implements Model {

	private Path home;
	private RandomForest model;

	public RandomForestClassifierSmile(Path modelDir) {
		this.home = modelDir;
		deserialize();
	}

	public RandomForestClassifierSmile(Path dataFile, Path modelDir) {
		this.home = modelDir;
		learn(dataFile);
		serialize();
	}

	private void learn(Path dataFile) {
		try {
			DataFrame a = Read.arff(dataFile);
			Formula formula = Formula.lhs("target");
			model = RandomForest.fit(formula, a);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private File getModelFile() {
		return home.resolve("rf.model").toFile();
	}

	private void serialize() {
		try {
			FileOutputStream fileOut = new FileOutputStream(getModelFile());
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(model);
			out.close();
			fileOut.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void deserialize() {
		try {
			FileInputStream fileIn = new FileInputStream(getModelFile());
			ObjectInputStream in = new ObjectInputStream(fileIn);
			model = (RandomForest) in.readObject();
			in.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public double predict(Instance instance) {
		double[] row = instance.getIndependent();
		DataType t = DataType.of(Double.class);
		StructField[] fields = new StructField[row.length];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = new StructField("f" + i, t);
		}
		StructType schema = new StructType(fields);
		Tuple tuple = Tuple.of(row, schema);
		return model.predict(tuple);
	}

}
