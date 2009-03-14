package checkers.typestate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A special state annotation, specifying that an object may be in an arbitrary state. As a lack of a state annotation
 * means "any annotation", this is only useful when specifying state transitions or when specifying the except element.
 * @author Adam Warski (adam at warski dot org)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Any {
	/**
	 * @return The state after the annotated method completes.
	 */
    Class<?> after()	default NoChange.class;

	/**
	 * @return States that this annotation does not cover. If the object will be in a state that is listed here,
	 * the transition specified by this annotation won't take effect, nor will this annotation accept the object.
	 */
	Class<?>[] except()	default {};
}