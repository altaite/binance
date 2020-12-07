package altaite.binance.global.io;

import altaite.binance.data.SymbolPair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public Path getExperimentDir(SymbolPair pair) {
		Path p = home.resolve("data_" + pair);
		createDirs(p);
		return p;
	}

	public File getHeatMapColors() {
		return home.resolve("colors.png").toFile();
	}

	public Path getCandleStorage() {
		Path p = home.resolve("candle_storage_00");
		createDirs(p);
		return p;
	}

	public Path getCandleStorageSmall() {
		Path p = home.resolve("candle_storage_small_00");
		createDirs(p);
		return p;
	}

	public File getMostTradedPairs() {
		return home.resolve("most_traded.txt").toFile();
	}

	public File getCurrencySymbols() {
		return home.resolve("currency_symbols.txt").toFile();
	}
}
