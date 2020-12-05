package altaite.learn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Instance {

	private List<Double> list = new ArrayList<>();
	private List<Boolean> isNominal = new ArrayList<>();

	public void addNominal(int feature) {
		list.add((double) feature);
		isNominal.add(true);
	}

	public void addNumeric(double feature) {
		list.add(feature);
		isNominal.add(false);
	}

	public boolean isCompatible(Instance other) {
		if (list.size() != other.list.size() || isNominal.size() != other.isNominal.size()) {
			return false;
		}
		for (int i = 0; i < isNominal.size(); i++) {
			if (!Objects.equals(isNominal.get(i), other.isNominal.get(i))) {
				return false;
			}
		}
		return true;
	}

	public boolean isNominal(int i) {
		return isNominal.get(i);
	}

	public int size() {
		return list.size();
	}

	public double get(int i) {
		return list.get(i);
	}

	public double getTarget() {
		return list.get(list.size() - 1);
	}
	
	public double[] getIndependent() {
		double[] a = new double[list.size() - 1];
		for (int i = 0; i < a.length; i++) {
			a[i] = list.get(i);
		}
		return a;
	}
}
