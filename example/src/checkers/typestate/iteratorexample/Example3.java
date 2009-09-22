package checkers.typestate.iteratorexample;

import java.util.Iterator;
import java.util.Collection;

/**
 * Reading from stream with a recovery function.
 * @author Adam Warski (adam at warski dot org)
 */
public class Example3 {
	public void test(Collection coll) {
		Iterator iter = coll.iterator();
		while (iter.hasNext()) {
			iter.next();
		}
	}
}