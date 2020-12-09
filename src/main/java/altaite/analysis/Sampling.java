package altaite.analysis;

import java.util.Random;

public class Sampling {

	public static boolean[] sample(long seed, int total, int count) {
		boolean[] sample = new boolean[total];
		if (count > total) {
			throw new RuntimeException();
		} else if (count == total) {
			for (int i = 0; i < sample.length; i++) {
				sample[i] = true;
			}
			return sample;
		} else if (count > total / 2) {
			throw new RuntimeException("Inefficient");
		}

		Random random = new Random(seed);

		int sampled = 0;
		while (sampled < count) {
			int r = random.nextInt(total);
			if (!sample[r]) {
				sample[r] = true;
				sampled++;
			}
		}
		return sample;
	}
}
