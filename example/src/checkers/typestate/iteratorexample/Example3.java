package checkers.typestate.iteratorexample;

import java.util.*;

/**
 * Reading from stream with a recovery function.
 * @author Adam Warski (adam at warski dot org)
 */
public class Example3 {
	public void test(List coll) {
		Iterator iter = coll.iterator();
		while (iter.hasNext()) {
			iter.next();
		}
	}
}