package checkers.typestate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;

/**
 * A special state annotation, specifying that an object may be in an arbitrary state. As a lack of a state annotation
 * means "any annotation", this is only useful when specifying state transitions.
 * @author Adam Warski (adam at warski dot org)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Any {
    Class<?> after();
}