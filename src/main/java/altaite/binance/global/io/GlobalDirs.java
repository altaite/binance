package altaite.binance.global.io;

import altaite.binance.data.SymbolPair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class GlobalDirs {

	public static final String defaultDir = "d:/t/data/binance";
	private final Path home;

	public GlobalDirs(String homePath) {
		this.home = Paths.get(homePath);
		createDirs(this.home);
	}

	public final Path createDirs(Path p) {
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

	public Path getExperiments() {
		return home.resolve("experiments");
	}

	public Path getExperiment(SymbolPair pair, String description) {
		Path p = getExperiments().resolve(pair + description);
		createDirs(p);
		return p;
	}

	public Path getExperiment(String description) {
		Path p = getExperiments().resolve(description);
		createDirs(p);
		return p;
	}

	public File getHeatMapColors() {
		return home.resolve("colors.png").toFile();
	}

	public Path getPictures() {
		Path p = home.resolve("pictures");
		createDirs(p);
		return p;
	}

	public Path getCandleStorage() {
		Path p = home.resolve("candle_storage_00");
		createDirs(p);
		return p;
	}

	public Path getCandleStorageMedium() {
		Path p = home.resolve("candle_storage_medium_00");
		createDirs(p);
		return p;
	}

	public Path getCandleStorageSmall() {
		Path p = home.resolve("candle_storage_small_00");
		createDirs(p);
		return p;
	}

	public File getCurrencySymbols() {
		return home.resolve("currency_symbols.txt").toFile();
	}

	public List<SymbolPair> getMostTradedPairs() {
		List<SymbolPair> pairs = new ArrayList<>();
		Set<SymbolPair> set = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(home.resolve("most_traded.txt").toFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "/");
				String a = st.nextToken();
				String b = st.nextToken();
				if (b.equals("BUSD")) {
					b = "USDT";
				}
				SymbolPair pair = new SymbolPair(a, b);
				if (!set.contains(pair)) {
					pairs.add(pair);
					set.add(pair);
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return pairs;
	}

	public File getQualitiesCsv() {
		Path p = home.resolve("qualities.csv");
		return p.toFile();
	}

	public Path getModelFull() {
		Path p = home.resolve("model_rf_full_data");
		createDirs(p);
		return p;
	}

	public Path
		getDataFull() {
		Path p = home.resolve("data_full.arff");
		return p;
	}

}
