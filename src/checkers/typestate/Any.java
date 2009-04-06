package checkers.typestate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Annotation;

/**
 * A special state annotation, specifying that an object may be in an arbitrary state. As a lack of a state annotation
 * means "any annotation", this is only useful when specifying state transitions, the {@code except} element or the
 * {@code onException} element.
 * @author Adam Warski (adam at warski dot org)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Any {
	/**
	 * @return The state of the object after the method in which the annotations is used completes.
	 */
    Class<? extends Annotation> after()			default NoChange.class;

	/**
	 * <b>Please note:</b> support for the except parameter is still not complete. It may yield inappropriate results
	 * when the object to which this annotation applies is used is in an undefined state, as may be the case for
	 * example after an {@code if-else} or {@code try-catch-finally} block.
	 *
	 * @return States that this annotation does not cover. If the object will be in a state that is listed here,
	 * the transition specified by this annotation won't take effect, nor will this annotation accept the object.
	 */
	Class<? extends Annotation>[] except()		default {};

	/**
	 * @return The state of the object if the method in which the annotation is used throws an exception.
	 */
    Class<? extends Annotation> onException()	default NoChange.class;
}