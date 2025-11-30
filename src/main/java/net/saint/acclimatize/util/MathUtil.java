package net.saint.acclimatize.util;

public final class MathUtil {

	// Square Root

	public static double sqrt(double x) {
		// Native implementation preferred.
		return Math.sqrt(x);
	}

	// Trigonometrics

	public static double sin(double x) {
		// Range reduction to [-π, π]
		x = x % (2 * Math.PI);

		if (x > Math.PI) {
			x -= 2 * Math.PI;
		}

		if (x < -Math.PI) {
			x += 2 * Math.PI;
		}

		// Bhaskara I's approximation
		return 1.27323954 * x - 0.405284735 * x * Math.abs(x);
	}

	public static double cos(double x) {
		// cos(x)=sin(x+π/2)
		return sin(x + Math.PI / 2);
	}

	public static double tan(double x) {
		// tan(x)=sin(x)/cos(x)
		double sin = sin(x);
		double cos = cos(x);

		if (cos == 0) {
			return Double.POSITIVE_INFINITY;
		}

		return sin / cos;
	}

	// Clamp

	public static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	public static float clamp(float value, float min, float max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	// Linear Interpolation

	public static float lerp(float a, float b, float t) {
		return a + (b - a) * clamp(t, 0, 1);
	}

	public static double lerp(double a, double b, double t) {
		return a + (b - a) * clamp(t, 0, 1);
	}

}
