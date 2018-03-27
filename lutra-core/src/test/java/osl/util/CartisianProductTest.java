package osl.util;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CartisianProductTest {

	@Test
	public void cartTest1a () {
		int[] lengths = new int[] { 3, 2, 1 };
		cartTester (lengths);
	}
	
	@Test
	public void cartTest1b () {
		int[] lengths = new int[] { 10, 10, 10 };
		cartTester (lengths);
	}
	
	@Test
	public void cartTest2a () {
		int[] lengths = new int[] { 1, 1, 1 };
		cartTester (lengths);
	}
	
	@Test
	public void cartTest2b () {
		int[] lengths = new int[] { 1, 1, 1, 1, 1, 1};
		cartTester (lengths);
	}
	
	@Test
	public void cartTest3a () {
		int[] lengths = new int[] { 1 };
		cartTester (lengths);
	}
	
	//@Test
	public void cartTest4a () {
		int[] lengths = new int[] { 0, 0, 0 };
		cartTester (lengths);
	}
	
	//@Test
	public void cartTest5a () {
		int[] lengths = new int[] { 0 };
		cartTester (lengths);
	}

	public void cartTester (int[] lengths) {
		Set<int[]> indices = new LinkedHashSet<>();
		new CartesianProduct(lengths).forEach(i -> indices.add(i));

		int size = 1;
		for (int l : lengths) {
			size *= l;
		}

		Assert.assertEquals(indices.size(), size);

		for (int[] i : indices) {
			Assert.assertEquals(i.length, lengths.length);
		}

		/*
		System.out.println(Arrays.toString(lengths));
		System.out.print(size + ": ");
		System.out.println(Arrays.deepToString(indices.toArray()));
		System.out.println("\n");
		*/
	}
}
