package checkers.typestate.ioexample;

import checkers.typestate.State;
import checkers.typestate.NoChange;

import java.lang.annotation.Annotation;

/**
 * The possible states of an input stream.
 * @author Adam Warski (adam at warski dot org)
 */
public class InputStreamStates {
	private InputStreamStates() { }

	public static @State @interface Open {
		Class<? extends Annotation> after() default NoChange.class;
		Class<? extends Annotation> onException() default NoChange.class;
	}

	public static @State @interface Closed {
		Class<? extends Annotation> after() default NoChange.class;
		Class<? extends Annotation> onException() default NoChange.class;
	}

	public static @State @interface InputStreamError {
		Class<? extends Annotation> after() default NoChange.class;
		Class<? extends Annotation> onException() default NoChange.class;  
	}
}
