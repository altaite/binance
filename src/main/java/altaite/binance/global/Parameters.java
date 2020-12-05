package altaite.binance.global;

import global.io.LineFile;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Antonin Pavelka
 */
public class Parameters implements Serializable {

	private File file;
	// auto-initialized fields:
	private int maxDbSize;

	private Parameters(File file) {
		this.file = file;
	}

	public static Parameters create(File file) {
		Parameters parameters = new Parameters(file);
		LineFile lineFile = new LineFile(file);
		Set<String> names = new HashSet<>();
		for (String line : lineFile.readLines()) {
			if (line.trim().isEmpty() || line.startsWith("#")) {
				continue;
			}
			StringTokenizer st = new StringTokenizer(line, " \t");
			String name = st.nextToken();
			String value = st.nextToken();
			parameters.initialize(name, value);
			names.add(name);
		}
		parameters.checkIfAllAreInitialized(names);
		return parameters;
	}

	private void initialize(String name, String value) {
		Class c = getClass();
		try {
			Field field = c.getDeclaredField(name);
			if (field.getType() == int.class) {
				int i = Integer.parseInt(value);
				field.setInt(this, i);
			} else if (field.getType() == double.class) {
				double d = Double.parseDouble(value);
				field.setDouble(this, d);
			} else if (field.getType() == boolean.class) {
				boolean b = Boolean.parseBoolean(value);
				field.setBoolean(this, b);
			} else if (field.getType() == String.class) {
				field.set(this, value);
			}
		} catch (IllegalAccessException | NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}

	}

	public void checkIfAllAreInitialized(Set<String> names) {
		Class clazz = getClass();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getType() == File.class) {
				continue;
			}
			String name = field.getName();
			if (!names.contains(name)) {
				fieldNotInitialized(name);
			}
		}
	}

	private void fieldNotInitialized(String name) {
		throw new RuntimeException("Field " + name + " is not initialized in parameters file "
			+ file.getAbsolutePath());
	}

}
