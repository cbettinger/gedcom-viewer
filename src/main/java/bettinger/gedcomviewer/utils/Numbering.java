package bettinger.gedcomviewer.utils;

import java.util.TreeMap;

public class Numbering {

	private static final TreeMap<Integer, String> roman = new TreeMap<>();

	static {
		roman.put(1000, "M");
		roman.put(900, "CM");
		roman.put(500, "D");
		roman.put(400, "CD");
		roman.put(100, "C");
		roman.put(90, "XC");
		roman.put(50, "L");
		roman.put(40, "XL");
		roman.put(10, "X");
		roman.put(9, "IX");
		roman.put(5, "V");
		roman.put(4, "IV");
		roman.put(1, "I");
	}

	public static final String getRoman(final int number) {
		final int next = roman.floorKey(number);
		return number == next ? roman.get(number) : roman.get(next) + getRoman(number - next);
	}

	private static final double LOG_2 = Math.log(2);

	public static final int getGeneration(final int kekule) {
		return (int) (Math.log(kekule) / LOG_2) + 1;
	}

	private Numbering() {}
}
