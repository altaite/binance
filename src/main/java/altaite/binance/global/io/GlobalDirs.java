package altaite.binance.global.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GlobalDirs {

	private final Path home;

	public GlobalDirs(String homePath) {
		this.home = Paths.get(homePath);
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

	public File getHeatMapColors() {
		return home.resolve("colors.png").toFile();
	}

}
