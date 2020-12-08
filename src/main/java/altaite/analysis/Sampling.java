package altaite.analysis;

import java.util.Random;

public class Sampling {

	public static boolean[] sample(long seed, int total, int count) {
		if (count >= total) {
			throw new RuntimeException();
		}
		Random random = new Random(seed);
		boolean[] sample = new boolean[total];
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
