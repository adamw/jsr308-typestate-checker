package checkers.typestate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Annotation;

/**
 * Annotation for methods, for which the receiver's state changes depending on the boolean result of the method.
 * @author Adam Warski (adam at warski dot org)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cond {
	/**
	 * @return The state of the object if the annotated method returns true.
	 */
    Class<? extends Annotation> whenTrue()			default NoChange.class;

	/**
	 * @return The state of the object if the annotated method returns false.
	 */
    Class<? extends Annotation> whenFalse()			default NoChange.class;
}
