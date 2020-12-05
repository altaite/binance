package altaite.analysis;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Antonin Pavelka
 */
public class Heatmap {

	private final long[][] counts;
	private final double minX;
	private final double minY;
	private final double maxX;
	private final double maxY;
	private final int xn;
	private final int yn;
	private final int[] colors;
	private int pixelSize = 1;

	public Heatmap(double minX, double minY, double maxX, double maxY, int xn, int yn, File colorsFile) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.xn = xn;
		this.yn = yn;
		this.counts = new long[xn][yn];
		colors = readColors(colorsFile);
	}

	public Heatmap(double minX, double minY, double maxX, double maxY, int xn, int yn, int pixelSize,
		File colorsFile) {

		this(minX, minY, maxX, maxY, xn, yn, colorsFile);
		this.pixelSize = pixelSize;
	}

	private int discretize(double v, double min, double max, int bins) {
		return (int) Math.floor((v - min) / (max - min) * bins);
	}

	public void add(double x, double y) {
		if (x > maxX || x < minX || y > maxY || y < minY) {
			return;
		}
		int xi = discretize(x, minX, maxX, xn);
		int yi = discretize(y, minY, maxY, yn);
		if (xi == xn) { // if x == maxX
			xi--;
		}
		if (yi == yn) {
			yi--;
		}
		counts[xi][yi]++;
	}

	public void addAll(Sample2 s) {
		for (Pair p : s.getPairs()) {
			add(p.x, p.y);
		}
	}

	public void save(File imageFile) {
		BufferedImage bi = new BufferedImage(xn * pixelSize, yn * pixelSize, BufferedImage.TYPE_INT_RGB);
		long max = 0;
		for (int x = 0; x < counts.length; x++) {
			for (int y = 0; y < counts[0].length; y++) {
				if (counts[x][y] > max) {
					max = counts[x][y];
				}
			}
		}
		System.out.println("Maximum count per heatmap pixel " + max);
		for (int x = 0; x < counts.length; x++) {
			for (int y = 0; y < counts[0].length; y++) {
				long count = counts[x][y];
				int ci = (int) Math.floor(((double) count - 1) / (max - 1) * colors.length);
				if (ci >= colors.length) {
					ci = colors.length - 1;
				}
				int color = Color.WHITE.getRGB();
				if (count > 0) {
					color = colors[ci];
				}
				for (int ex = 0; ex < pixelSize; ex++) {
					for (int ey = 0; ey < pixelSize; ey++) {
						bi.setRGB(x * pixelSize + ex, (counts[0].length - y - 1) * pixelSize + ey, color);
					}
				}
			}
		}
		try {
			ImageIO.write(bi, "png", imageFile);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private int[] readColors(File colorsFile) {
		try {
			BufferedImage bi = ImageIO.read(colorsFile);
			int[] cols = new int[bi.getWidth()];
			for (int i = 0; i < cols.length; i++) {
				cols[i] = bi.getRGB(i, 0);
			}
			return cols;
		} catch (IOException ex) {
			throw new RuntimeException(colorsFile + "", ex);
		}
	}

	public static void main(String[] args) {
		Heatmap hm = new Heatmap(0, 0, 100, 100, 10, 10, new File("d:/t/data/heatmap/colors.png"));
		hm.add(10, 10);
		hm.add(20, 20);
		hm.add(20, 20);
		/*hm.add(30, 30);
		hm.add(30, 30);
		hm.add(30, 30);
		hm.add(40, 40);
		hm.add(40, 40);
		hm.add(40, 40);
		hm.add(40, 40);*/
		hm.save(new File("d:/t/data/heatmap/heatmap.png"));
	}
}
