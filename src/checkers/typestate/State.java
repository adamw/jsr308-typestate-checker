package checkers.typestate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;

/**
 * Marks an annotation as a state annotation. Correct usage and placement of such annotations will be checked
 * by the typestate checker.
 *
 * Each state annotation should have an element with signature: {@code Class<?> after default NoChange.class}.
 * The value of this element specifies to which state an objects transits, when the object is used/ is a result of
 * an annotated method or constructor.
 *
 * Optionally, the annotation can also define an element with signature:
 * {@code Class<?> onException default NoChange.class}, which specifies to which state the object
 * transits, if an exception is thrown by the annotated method or constructor.
 *
 * State annotations (that is, annotations annotated with {@code @State}) can be used on method return,
 * method parameters, method receivers and constructor receivers.
 *
 * An example of a state annotation:
 * <br />
 * <br />
 * <tt>
 * \@State @interface State1 { Class<?> after() default NoChange.class; Class<?> onException() default NoChange.class; }
 * </tt>
 *
 * @author Adam Warski (adam at warski dot org)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface State {}
