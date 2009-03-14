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
    public static final String EXCEPT_ELEMENT_NAME = "except";

    private final TypeMirror stateAnnotationType;
    private final TypeMirror anyStateAnnotationType;

    // Visitor for getting the value of the "after" parameter of state annotations.
    private final AnnotationAsAnnotationValueVisitor afterAnnotationValueVisitor;

	// Visitor for getting the value of the "except" parameter of any state annotations.
    private final AnnotationsAsAnnotationValueVisitor exceptAnnotationValueVisitor;

    public TypestateUtil(ProcessingEnvironment env) {
        stateAnnotationType = env.getElementUtils().getTypeElement(State.class.getName()).asType();
        anyStateAnnotationType = env.getElementUtils().getTypeElement(Any.class.getName()).asType();

        afterAnnotationValueVisitor =
                new AnnotationAsAnnotationValueVisitor(new AnnotationUtils(env), env.getTypeUtils());
        exceptAnnotationValueVisitor =
                new AnnotationsAsAnnotationValueVisitor(new AnnotationUtils(env), env.getTypeUtils());
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

	public boolean anyAnnotationCovers(AnnotationMirror anyAnnotation, Set<AnnotationMirror> actualAnnotations) {
		// Checking if this is an any state annotation at all
		if (!isAnyStateAnnotation(anyAnnotation)) {
			return false;
		}

		// If yes, getting the value of the "except" element
		Set<AnnotationMirror> except = getExceptParameterValue(anyAnnotation);

		// And checking if any of the "except" states are in the actual annotations
		if (except != null) {
			for (AnnotationMirror exceptAnnotation : except) {
				if (actualAnnotations.contains(exceptAnnotation)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
     * @param anyStateAnnotation Any-state annotation from which to read the "except" parameter.
     * @return The set of annotations representing the value of the "except" parameter of the given annotation or null,
     * if the parameter is not specified.
     */
    private Set<AnnotationMirror> getExceptParameterValue(AnnotationMirror anyStateAnnotation) {
        return getElementValueWithVisitor(anyStateAnnotation, EXCEPT_ELEMENT_NAME, exceptAnnotationValueVisitor);
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
		AnnotationMirror afterAnnotation = getElementValueWithVisitor(stateAnnotation,
				TRANSITION_ELEMENT_NAME, afterAnnotationValueVisitor);

		if (afterAnnotation != null && isStateAnnotation(afterAnnotation)) {
        	return afterAnnotation;
        }

		return null;
    }

	/**
	 * Applies the given visitor to the given element of the given annotation.
	 * @param annotation Annotation, from which to get the elements.
	 * @param elementName Name of the element, for which the visitor should be applied.
	 * @param visitor Visitor to use.
	 * @param <R> Return value of the visitor.
	 * @return The value returned by the visitor or null, if the element was not found in the annotation.
	 */
	private <R> R getElementValueWithVisitor(AnnotationMirror annotation, String elementName,
										   AnnotationValueVisitor<R, Void> visitor) {
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> elementValue :
                annotation.getElementValues().entrySet()) {
            if (elementValue.getKey().getSimpleName().contentEquals(elementName)) {
                return elementValue.getValue().accept(visitor, null);
            }
        }

        return null;
	}

	/**
	 * @param annotations Set of annotations to filter.
	 * @return A set of annotation mirrors containing all state annotations from the given set.
	 */
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
