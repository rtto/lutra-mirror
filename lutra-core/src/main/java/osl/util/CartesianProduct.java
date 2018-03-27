package osl.util;

import java.util.Arrays;
import java.util.Iterator;

// https://stackoverflow.com/questions/714108/cartesian-product-of-arbitrary-sets-in-java
public class CartesianProduct implements Iterable<int[]>, Iterator<int[]> {

	private final int[] lengths;
	private final int[] indices;
	private boolean hasNext = true;

	public CartesianProduct(int[] lengths) {
		this.lengths = lengths;
		this.indices = new int[lengths.length];
		// testing input
		for (int i : lengths) {
			if (i < 1) {
				throw new RuntimeException("Lengths must be positive integers, found: " + i);
			}
		}
	}

	public boolean hasNext() {
		return hasNext;
	}

	public int[] next() {
		int[] result = Arrays.copyOf(indices, indices.length);
		for (int i = indices.length - 1; i >= 0; i--) {
			if (indices[i] == lengths[i] - 1) {
				indices[i] = 0;
				if (i == 0) {
					hasNext = false;
				}
			} else {
				indices[i]++;
				break;
			}
		}
		return result;
	}

	public Iterator<int[]> iterator() {
		return this;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Usage example. Prints out
	 * 
	 * <pre>
	 * [0, 0, 0] a, NANOSECONDS, 1
	 * [0, 0, 1] a, NANOSECONDS, 2
	 * [0, 0, 2] a, NANOSECONDS, 3
	 * [0, 0, 3] a, NANOSECONDS, 4
	 * [0, 1, 0] a, MICROSECONDS, 1
	 * [0, 1, 1] a, MICROSECONDS, 2
	 * [0, 1, 2] a, MICROSECONDS, 3
	 * [0, 1, 3] a, MICROSECONDS, 4
	 * [0, 2, 0] a, MILLISECONDS, 1
	 * [0, 2, 1] a, MILLISECONDS, 2
	 * [0, 2, 2] a, MILLISECONDS, 3
	 * [0, 2, 3] a, MILLISECONDS, 4
	 * [0, 3, 0] a, SECONDS, 1
	 * [0, 3, 1] a, SECONDS, 2
	 * [0, 3, 2] a, SECONDS, 3
	 * [0, 3, 3] a, SECONDS, 4
	 * [0, 4, 0] a, MINUTES, 1
	 * [0, 4, 1] a, MINUTES, 2
	 * ...
	 * </pre>
	 */

	/*
    public static void main(String[] args) {
        String[] list1 = { "a", "b", "c", };
        TimeUnit[] list2 = TimeUnit.values();
        int[] list3 = new int[] { 1, 2, 3, 4 };

        int[] lengths = new int[] { list1.length, list2.length, list3.length };
        for (int[] indices : new CartesianProduct(lengths)) {
            System.out.println(Arrays.toString(indices) //
                    + " " + list1[indices[0]] //
                    + ", " + list2[indices[1]] //
                    + ", " + list3[indices[2]]);
        }
    }
	 */
}