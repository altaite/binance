package altaite.learn.model;

import altaite.learn.Instance;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

			// TODO prune?
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void test(Path dataFile) {
		try {
			System.out.println("Testing...");
			DataFrame a = Read.arff(dataFile);
			byte[] clazz = new byte[a.size()];
			double[] p = new double[a.size()];
			System.out.println("data size " + a.size());
			for (int i = 0; i < a.size(); i++) {
				Tuple t = a.get(i);
				double[] posteriori = new double[2];
				model.predict(t, posteriori);
				clazz[i] = (byte) t.get(t.length() - 1);
				p[i] = posteriori[1];
				//System.out.println(t.get(t.length() - 1) + " " + posteriori[0] + " -- " + posteriori[1]);
			}

			//double threshold = 0.5;
			for (double threshold = 0.5; threshold <= 1; threshold += 0.01) {
				int tp = 0;
				int total = 0;
				for (int i = 0; i < p.length; i++) {
					if (p[i] >= threshold) {
						total++;
						if (clazz[i] == 1) {
							tp++;
						}
					}
				}
				System.out.println(tp + " / " + total + "     " + threshold);
			}
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

	boolean bound = false;

	@Override
	public double predict(Instance instance) {
		if (!bound) {
			Path home = Paths.get("d:/t/data/binance/");
			Path train = home.resolve("high_train.arff");
			Path test = home.resolve("high_test.arff");
			Path model = home.resolve("model_rf");
			test(test);
			bound = true;
		}
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

	public static void main(String[] args) {
		Path home = Paths.get("d:/t/data/binance/");
		Path train = home.resolve("high_train.arff");
		Path test = home.resolve("high_test.arff");
		Path model = home.resolve("model_rf");
		try {
			Files.createDirectories(model);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		RandomForestClassifierSmile rf = new RandomForestClassifierSmile(train, model);
		//RandomForestClassifierSmile rf = new RandomForestClassifierSmile(model);
		rf.test(test);
	}
}
