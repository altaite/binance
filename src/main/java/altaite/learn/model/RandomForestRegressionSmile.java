package altaite.learn.model;

import altaite.analysis.Pair;
import altaite.analysis.Sample2;
import altaite.learn.Instance;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.io.Read;
import smile.data.formula.Formula;
import smile.data.type.DataType;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.regression.RandomForest;

public class RandomForestRegressionSmile implements Model {

	private Path home;
	private RandomForest model;

	public RandomForestRegressionSmile(Path modelDir) {
		this.home = modelDir;
		deserialize();
	}

	public RandomForestRegressionSmile(Path dataFile, Path modelDir) {
		this.home = modelDir;
		learn(dataFile);
		serialize();
	}

	private void learn(Path dataFile) {
		try {
			DataFrame a = Read.arff(dataFile);
			Formula formula = Formula.lhs("target");
			model = RandomForest.fit(formula, a);
			/*double[] importance = model.importance();
			Sample2 sample = new Sample2();
			for (int i =0;i<importance.length;i++) {
				sample.add(i, importance[i]);
			}
			Sample2 byY = sample.createSortedByY();
			System.out.println("Importances");
			for (int i = 0; i < byY.size(); i++) {
				Pair p = byY.get(i);
				System.out.println(p.x + " " + p.y);
			}*/
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private File getModelFile() {
		return home.resolve("rf.model").toFile();
	}

	private void serialize() {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getModelFile()))) {
			out.writeObject(model);
			System.out.println("Serialized " + getModelFile());
			out.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public final void deserialize() {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getModelFile()))) {
			model = (RandomForest) in.readObject();
			System.out.println("Deserialized " + getModelFile());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public double predict(double[] row) {
		Tuple t = doubleToTuple(row);
		double prediction = model.predict(t);
		return prediction;
	}

	@Override
	public double predict(Instance instance) {
		double[] row = instance.getIndependent();
		/*DataType t = DataType.of(Double.class);
		StructField[] fields = new StructField[row.length];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = new StructField("f" + i, t);
		}
		StructType schema = new StructType(fields);*/
 /*StructType schema = model.schema();
		assert schema.length() == row.length : schema.length() + " == " + row.length;
		System.out.println(schema.length() + " == " + row.length);
		Tuple tuple = Tuple.of(row, schema);
		System.out.println("predicting inside " + tuple.length());
		System.out.println("---");
		for (StructField f : schema.fields()) {
			System.out.println(f.name);
		}*/

		Tuple t = doubleToTuple(row);

		double prediction = model.predict(t);
		return prediction;
	}

	public static Tuple doubleToTuple(double[] row) {
		DataType t = DataType.of(Double.class);
		StructField[] fields = new StructField[row.length + 1];
		for (int i = 0; i < fields.length - 1; i++) {
			fields[i] = new StructField("f" + i, t);
		}
		fields[fields.length - 1] = new StructField("target", t);
		StructType schema = new StructType(fields);
		return Tuple.of(row, schema);
	}

	public static void main(String[] args) throws Exception {
		Path f = Paths.get("d:/t/data/smile/train.arff");
		Path modelPath = Paths.get("d:/t/data/smile/model");
		DataFrame a = Read.arff(f);
		//Formula formula = Formula.lhs("target");
		RandomForest rf;// = RandomForest.fit(formula, a);

		/*try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelPath.toFile()))) {
			out.writeObject(rf);
			out.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}*/
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelPath.toFile()))) {
			rf = (RandomForest) in.readObject();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		//	StructType schema = a.get(0).schema();
		//double p = rf.predict(a.get(0));

		/*	Tuple tuple = Tuple.of(new double[900], schema);

		double p = rf.predict(tuple);

		System.out.println(p);
		 */
		Tuple tuple2 = doubleToTuple(new double[900]);
		double p = rf.predict(tuple2);
		System.out.println(p);

	}

	public static void main0(String[] args) throws Exception {
		Path f = Paths.get("d:/t/data/smile/train.arff");
		Path modelPath = Paths.get("d:/t/data/smile/model2");

		RandomForestRegressionSmile model;
		model = new RandomForestRegressionSmile(f, modelPath);
		model.predict(new double[900]);

		model = new RandomForestRegressionSmile(modelPath);

		model.predict(new double[900]);

		//DataFrame a = Read.arff(f);
	}

	public static void main3(String[] args) throws Exception {
		Path f = Paths.get("d:/t/data/smile/train.arff");
		Path modelPath = Paths.get("d:/t/data/smile/model2/rf.model");

		RandomForest model;
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelPath.toFile()))) {
			model = (RandomForest) in.readObject();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		model.predict(doubleToTuple(new double[900]));
	}
}
