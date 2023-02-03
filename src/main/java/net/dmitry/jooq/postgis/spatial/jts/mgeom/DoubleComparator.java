
package net.dmitry.jooq.postgis.spatial.jts.mgeom;

/**
 * This utility class is used to testsuite-suite doubles for equality
 *
 * @author Didier H. Besset
 *
 *
 * Adapted from "Object-oriented implementation of
 *         numerical methods"
 */
//TODO: This class should be removed.
public final class DoubleComparator {

	private final static int radix = computeRadix();

	private final static double machinePrecision = computeMachinePrecision();

	private final static double defaultNumericalPrecision = Math
			.sqrt(machinePrecision);

	private static int computeRadix() {
		int radix = 0;
		double a = 1.0d;
		double tmp1, tmp2;
		do {
			a += a;
			tmp1 = a + 1.0d;
			tmp2 = tmp1 - a;
		} while (tmp2 - 1.0d != 0.0d);
		double b = 1.0d;
		while (radix == 0) {
			b += b;
			tmp1 = a + b;
			radix = (int) (tmp1 - a);
		}
		return radix;
	}

	public static int getRadix() {
		return radix;
	}

	private static double computeMachinePrecision() {
		double floatingRadix = getRadix();
		double inverseRadix = 1.0d / floatingRadix;
		double machinePrecision = 1.0d;
		double tmp = 1.0d + machinePrecision;
		while (tmp - 1.0d != 0.0) {
			machinePrecision *= inverseRadix;
			tmp = 1.0d + machinePrecision;
		}
		return machinePrecision;
	}

	public static double getMachinePrecision() {
		return machinePrecision;
	}

	public static double defaultNumericalPrecision() {
		return defaultNumericalPrecision;
	}

	public static boolean equals(double a, double b) {
		return equals(a, b, defaultNumericalPrecision());
	}

	public static boolean equals(double a, double b, double precision) {
		double norm = Math.max(Math.abs(a), Math.abs(b));
		boolean result = norm < precision || Math.abs(a - b) < precision * norm;
		return result || (Double.isNaN(a) && Double.isNaN(b));
	}

	public static void main(String[] args) {
		System.out.println("Machine precision = " + getMachinePrecision());
		System.out.println("Radix = " + getRadix());
		System.out.println(
				"default numerical precision = "
						+ defaultNumericalPrecision()
		);
	}
}
