package checkers.typestate.iteratorexample;

import checkers.typestate.State;
import checkers.typestate.NoChange;

import java.lang.annotation.Annotation;

/**
 * Possible states of an iterator.
 * @author Adam Warski (adam at warski dot org)
 */
public class IteratorStates {
	private IteratorStates() { }

	public static @State @interface CheckHasNext {
		Class<? extends Annotation> after() default NoChange.class;
		Class<? extends Annotation> afterTrue() default NoChange.class;
		Class<? extends Annotation> afterFalse() default NoChange.class;
		Class<? extends Annotation> onException() default NoChange.class;
	}

	public static @State @interface ReadNext {
		Class<? extends Annotation> after() default NoChange.class;
		Class<? extends Annotation> afterTrue() default NoChange.class;
		Class<? extends Annotation> afterFalse() default NoChange.class;
		Class<? extends Annotation> onException() default NoChange.class;
	}
}
