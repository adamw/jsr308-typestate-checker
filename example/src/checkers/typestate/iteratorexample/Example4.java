package checkers.typestate.iteratorexample;

import java.util.*;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class Example4 {
	public void test(List coll) {
		Iterator iter = coll.iterator();
		while (iter.hasNext()) {
			iter.next();
		}

		// Error: calling next() without checking if hasNext() returns true
		iter.next();
	}
}