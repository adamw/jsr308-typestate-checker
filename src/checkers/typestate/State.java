package checkers.typestate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;

/**
 * Marks an annotation as a state annotation. Correct usage and placement of such annotations will be checked
 * by the typestate checker.
 * @author Adam Warski (adam at warski dot org)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface State {}
