package org.pcollections.tests;

import java.util.*;

import junit.framework.TestCase;

import org.pcollections.Empty;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import org.pcollections.PSet;

public class OrderedPSetTest extends TestCase {

	public void testPlus() {
		POrderedSet<Integer> s = Empty.orderedSet();
		s = s.plus(3).plus(2).plus(1).plus(1).plus(2).plus(3).plus(4);

		int vals[] = new int[] { 3, 2, 1, 4 };
		assertEquals(vals.length, s.size());
		
		Iterator<Integer> it = s.iterator();
		for (int i = 0; i < vals.length; i++) {
			assertEquals(vals[i], s.get(i).intValue());
			assertEquals(vals[i], it.next().intValue());
		}
	}

	public void testPlusMinus() {
		POrderedSet<Integer> s = Empty.orderedSet();
		s = s.plus(3).plus(2).plus(1).minus(1).plus(2).plus(3).minus(17)
				.plus(5).plus(1).plus(4);

		int vals[] = new int[] { 3, 2, 5, 1, 4 };
		assertEquals(vals.length, s.size());
		
		Iterator<Integer> it = s.iterator();
		for (int i = 0; i < vals.length; i++) {
			assertEquals(vals[i], s.get(i).intValue());
			assertEquals(vals[i], it.next().intValue());
		}
	}

	public void testBehavesLikePSet() {
		PSet<Integer> s = Empty.set();
		POrderedSet<Integer> os = Empty.orderedSet();

		Random r = new Random();
		for (int i = 0; i < 100000; i++) {
			int v = r.nextInt(1000);
			if (r.nextFloat() < 0.8) {
				s = s.plus(v);
				os = os.plus(v);
			} else {
				s = s.minus(v);
				os = os.minus(v);
			}
		}

		assertEquals(s, os);
	}

	public void testAllowsNulls() {
		POrderedSet<Long> s = Empty.orderedSet();
		s = s.plus(1L).plus(2L).plus(null).plusAll(Arrays.asList(2L, 1L, null));

		List<Long> expectedOrdered = Arrays.asList(1L, 2L, null);
		Set<Long> expected = new LinkedHashSet<Long>(expectedOrdered);

		UtilityTest.assertEqualsAndHash(expected, s);
		UtilityTest.assertEqualsAndHash(expectedOrdered, new ArrayList<Long>(s));
	}

	public void testIterator() {
		UtilityTest.iteratorExceptions(Empty.orderedSet().iterator());
		UtilityTest.iteratorExceptions(OrderedPSet.singleton(10).iterator());
	}

}
