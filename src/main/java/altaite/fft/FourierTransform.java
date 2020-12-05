package altaite.fft;

import org.jtransforms.fft.DoubleFFT_1D;

public class FourierTransform {
	
	public static void main(String[] args) {
		int n = 100;
		double data[] = new double[n];
		for (int i = 0; i < n; i++) {
			double d = (double) i;
			data[i] = 2 * Math.sin(d / n * Math.PI * 2 * 3.5) + Math.cos(d / 2 * 5 + 1)/2;
		}
		double[][] out = computeFreqMagPhase(data);
		for (double[] a : out) {
			for (double d : a) {
				System.out.print(d + " ");
			}
			System.out.println("");
		}
	}
	
	public static double[][] computeFreqMagPhase(double[] data) {
		int n = data.length;
		if (data.length != n) {
			throw new RuntimeException("input.length != n");
		}
		
		DoubleFFT_1D fft = new DoubleFFT_1D(n);
		
		double T = 1;
		
		double fftInput[] = copyDoubleArray(data);
		
		fft.realForward(fftInput);
		
		for (double d : fftInput) {
			System.out.println(d);
		}
		
		double[] magnitude = extractMagnitude(fftInput);
		double[] phase = extractPhaseRadians(fftInput);
		double[] frequency = new double[n / 2];
		for (int i = 0; i < n / 2; i++) {
			frequency[i] = (i) / T;
		}
		
		return new double[][]{frequency, magnitude, phase};
	}
	
	private static double[] copyDoubleArray(double[] in) {
		double[] ret = new double[in.length];
		
		for (int i = 0; i < in.length; i++) {
			ret[i] = in[i];
		}
		
		return ret;
	}
	
	private static double[] extractMagnitude(double[] fftInput) {
		int n = fftInput.length;

		//    if (n % 2 != 0)
		//        throw new RuntimeException("Must be even length!");
		double[] magnitude = new double[n / 2];
		
		for (int k = 0; k < n / 2; k++) {
			double real = fftInput[2 * k];
			double imag = fftInput[2 * k + 1];
			
			if (k == 0) {
				imag = 0; // This is due to storing a[1] = Re[n/2] when n is even and = Im[n/2-1] when n is odd, as per the fft specs. Just ignore that term for now...
			}
			double mag = Math.sqrt(real * real + imag * imag);
			magnitude[k] = mag;

			//       magnitude[k] = 20.0 * Math.log10(mag);
		}
		
		return magnitude;
	}
	
	private static double[] extractPhaseRadians(double[] fftInput) {
		int n = fftInput.length;

		//    if (n % 2 != 0)
		//        throw new RuntimeException("Must be even length!");
		double[] phase = new double[n / 2];
		
		for (int k = 0; k < n / 2; k++) {
			double real = fftInput[2 * k];
			double imag = fftInput[2 * k + 1];
			
			if (k == 0) {
				imag = 0; // This is due to storing a[1] = Re[n/2] when n is even and = Im[n/2-1] when n is odd, as per the fft specs. Just ignore that term for now...
			}
			phase[k] = Math.atan2(imag, real);
		}
		
		return phase;
	}
	
}
