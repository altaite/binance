package altaite.analysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Sampling {

	public static boolean[] sample(long seed, int originalSize, int sampleSize) {
		Random random = new Random(seed);
		Integer[] array = new Integer[originalSize];
		for (int i = 0; i < array.length; i++) {
			array[i] = i;
		}
		List<Integer> list = Arrays.asList(array);
		Collections.shuffle(list, random);
		list.toArray(array);
		boolean[] sample = new boolean[originalSize];
		for (int i = 0; i < sampleSize; i++) {
			sample[array[i]] = true;
		}
		return sample;
	}

	public static void main(String[] args) {
		for (int seed = 0; seed < 10; seed++) {
			boolean[] a = sample(seed, 10, 5);
			for (boolean b : a) {
				System.out.print(b ? "#" : "O");
			}
			System.out.println("");
		}
	}
}
