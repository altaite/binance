package altaite.binance.global.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExperimentDirs {

	private final Path home;

	public ExperimentDirs(Path homePath) {
		this.home = homePath;
		createDirs(this.home);
	}

	public Path createDirs(Path p) {
		try {
			if (!Files.exists(p)) {
				Files.createDirectories(p);
			}
			return p;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Path getHome() {
		return home;
	}

	public Path getDatasets() {
		Path p = home.resolve("datasets");
		createDirs(p);
		return p;
	}

	public Path getTrain() {
		return getDatasets().resolve("train.arff");
	}

	public Path getTest() {
		return getDatasets().resolve("test.arff");
	}

	public Path getModels() {
		return createDirs(home.resolve("models"));
	}

	public Path getRandomForest() {
		return createDirs(getModels().resolve("rf"));
	}

	public Path getResultsDir() {
		Path p = home.resolve("results");
		createDirs(p);
		return p;
	}

	public File getResultsCsv() {
		return getResultsDir().resolve("results_raw.csv").toFile();
	}
}
