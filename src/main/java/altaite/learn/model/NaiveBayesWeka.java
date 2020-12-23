package altaite.learn.model;

import altaite.learn.MyInstance;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class NaiveBayesWeka implements Model {

	private Path home;
	private NaiveBayes model;
	private Instances dataset;

	public NaiveBayesWeka(Path modelDir) {
		this.home = modelDir;
		deserialize();
	}

	public NaiveBayesWeka(Path dataFile, Path modelDir) {
		this.home = modelDir;
		learn(dataFile);
		serialize();
	}

	private void learn(Path dataFile) {
		try {
			Instances train = read(dataFile);

			/*ConverterUtils.DataSource source2 = new ConverterUtils.DataSource("./data/test.arff");
			Instances test = source2.getDataSet();
			if (test.classIndex() == -1) {
				test.setClassIndex(train.numAttributes() - 1);
			}*/
			// model
			model = new NaiveBayes();
			model.buildClassifier(train);

			// this does the trick  
			/*double label = naiveBayes.classifyInstance(test.instance(0));
			test.instance(0).setClassValue(label);

			System.out.println(test.instance(0).stringValue(4));*/
			dataset = train; // hack to load format for later
			dataset.clear();

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private Instances read(Path p) {
		try {
			DataSource source = new DataSource(p.toAbsolutePath().toString());
			Instances data = source.getDataSet();
			if (data.classIndex() == -1) {
				data.setClassIndex(data.numAttributes() - 1);
			}


			/*BufferedReader reader = new BufferedReader(new FileReader(p.toFile()));
			ArffReader arff = new ArffReader(reader, 1000000);

			Instances instances = arff.getData();
			if (instances.classIndex() == -1) {
				instances.setClassIndex(instances.numAttributes() - 1);
			}*/
			System.out.println("read  " + data.size() + " instances");
			return data;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void test(Path dataFile) {
		Instances test = read(dataFile);
		System.out.println("test instances " + test.size());
		for (Instance i : test) {
			try {
				double[] p = model.distributionForInstance(i);
				System.out.println(p[0] + " " + p[1]);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private File getModelFile() {
		return home.resolve("rf.model").toFile();
	}

	private void serialize() {
		/*try {
			FileOutputStream fileOut = new FileOutputStream(getModelFile());
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(model);
			out.close();
			fileOut.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}*/
	}

	public void deserialize() {
		/*try {
			FileInputStream fileIn = new FileInputStream(getModelFile());
			ObjectInputStream in = new ObjectInputStream(fileIn);
			model = (RandomForest) in.readObject();
			in.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}*/
	}

	boolean bound = false;

	@Override
	public double predict(MyInstance instance) {
		Instance inst = new DenseInstance(instance.size() + 1);
		for (int i = 0; i < instance.size(); i++) {
			inst.setValue(i, instance.get(i));
		}
		dataset.add(inst);
		inst.setDataset(dataset);

		try {
			double[] p = model.distributionForInstance(inst);
			dataset.clear();
			return p[1];
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		/*if (!bound) {
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
		double[] p = new double[2];
		model.predict(tuple, p);
		return p[1];*/
	}

	public static void main(String[] args) {
		Path home = Paths.get("d:/t/data/binance/");
		Path train = home.resolve("high_train_real_2.arff");
		Path test = home.resolve("high_test_real_2.arff");
		Path model = home.resolve("model_nb");
		try {
			Files.createDirectories(model);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		NaiveBayesWeka nb = new NaiveBayesWeka(train, model);
		MyInstance mi = new MyInstance(false);
		for (int i = 0; i < 781; i++) {
			mi.addNumeric(i);
		}
		mi.addNominal(0);
		double p = nb.predict(mi);
		System.out.println("p = " + p);
		//RandomForestClassifierSmile rf = new RandomForestClassifierSmile(model);
		nb.test(test);
	}
}
