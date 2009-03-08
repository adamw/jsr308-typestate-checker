package checkers.typestate;

import checkers.util.AnnotationUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.element.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class TypestateUtil {
    public static final String TRANSITION_ELEMENT_NAME = "after";

    private final TypeMirror stateAnnotationType;
    private final TypeMirror anyStateAnnotationType;

    // Visitor for getting the value of the "after" parameter of state annotations.
    private final AnnotationAsAnnotationValueVisitor afterAnnotationValueVisitor;

    public TypestateUtil(ProcessingEnvironment env) {
        stateAnnotationType = env.getElementUtils().getTypeElement(State.class.getName()).asType();
        anyStateAnnotationType = env.getElementUtils().getTypeElement(Any.class.getName()).asType();

        afterAnnotationValueVisitor =
                new AnnotationAsAnnotationValueVisitor(new AnnotationUtils(env), env.getTypeUtils());
    }

    private boolean checkForStateAnnotation(List<? extends AnnotationMirror> annotations) {
        for (AnnotationMirror am : annotations) {
            if (stateAnnotationType.equals(am.getAnnotationType())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param annotation Annotation to check.
     * @return True iff the given annotation is the any state annotation (@{@link Any})
     */
    public boolean isAnyStateAnnotation(AnnotationMirror annotation) {
        return anyStateAnnotationType.equals(annotation.getAnnotationType());
    }

    /**
     * @param dt Type to check.
     * @return True iff the given type is a state annotation, that is, is itself annotated with
     * @{@link State}.
     */
    public boolean isStateAnnotation(DeclaredType dt) {
        Element el = dt.asElement();
        return el.getKind() == ElementKind.ANNOTATION_TYPE &&
                checkForStateAnnotation(dt.asElement().getAnnotationMirrors());
    }

    /**
     * @param annotation Annotation to check.
     * @return True iff the given annotation is a state annotation, that is, is itself annotated with
     * @{@link State}.
     */
    public boolean isStateAnnotation(AnnotationMirror annotation) {
        return isStateAnnotation(annotation.getAnnotationType());
    }

    /**
     * @param stateAnnotation State annotation from which to read the "after" parameter.
     * @return The state annotation representing the value of the "after" parameter of the given annotation or null,
     * if the parameter is not specified or is not a (state) annotation.
     */
    public AnnotationMirror getAfterParameterValue(AnnotationMirror stateAnnotation) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> elementValue :
                stateAnnotation.getElementValues().entrySet()) {
            if (elementValue.getKey().getSimpleName().contentEquals(TRANSITION_ELEMENT_NAME)) {
                AnnotationMirror afterAnnotation = elementValue.getValue().accept(afterAnnotationValueVisitor, null);
                if (afterAnnotation != null && isStateAnnotation(afterAnnotation)) {
                    return afterAnnotation;
                }
            }
        }

        return null;
    }

	public Set<AnnotationMirror> filterStateAnnotations(Set<AnnotationMirror> annotations) {
		Set<AnnotationMirror> filtered = AnnotationUtils.createAnnotationSet();

		for (AnnotationMirror annotation : annotations) {
			if (isStateAnnotation(annotation) || isAnyStateAnnotation(annotation)) {
				filtered.add(annotation);
			}
		}

		return filtered;
	}
}
