package checkers.typestate;

import checkers.util.AnnotationUtils;
import checkers.nullness.quals.Nullable;

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
    public static final String EXCEPTION_ELEMENT_NAME = "onException";	
    public static final String WHEN_TRUE_ELEMENT_NAME = "whenTrue";
	public static final String WHEN_FALSE_ELEMENT_NAME = "whenFalse";

    private final TypeMirror stateAnnotationType;
    private final TypeMirror anyStateAnnotationType;
    private final TypeMirror condAnnotationType;

    // Visitor for getting the value of single-annotation-valued elements of other annotations.
    private final AnnotationAsAnnotationValueVisitor singleAnnotationValueVisitor;

	// Visitor for getting the value of multi-annotation-valued elements of other annotations.
    private final AnnotationsAsAnnotationValueVisitor multiAnnotationValueVisitor;

	public TypestateUtil(ProcessingEnvironment env) {
        stateAnnotationType = env.getElementUtils().getTypeElement(State.class.getName()).asType();
        anyStateAnnotationType = env.getElementUtils().getTypeElement(Any.class.getName()).asType();
        condAnnotationType = env.getElementUtils().getTypeElement(Cond.class.getName()).asType();

        singleAnnotationValueVisitor =
                new AnnotationAsAnnotationValueVisitor(AnnotationUtils.getInstance(env), env.getTypeUtils());
        multiAnnotationValueVisitor =
                new AnnotationsAsAnnotationValueVisitor(AnnotationUtils.getInstance(env), env.getTypeUtils());
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
     * @param annotation Annotation to check.
     * @return True iff the given annotation is the cond state annotation (@{@link Cond})
     */
    public boolean isCondAnnotation(AnnotationMirror annotation) {
        return condAnnotationType.equals(annotation.getAnnotationType());
    }

	public boolean anyAnnotationCovers(AnnotationMirror anyAnnotation, Set<AnnotationMirror> actualAnnotations) {
		// Checking if this is an any state annotation at all
		if (!isAnyStateAnnotation(anyAnnotation)) {
			return false;
		}

		// If yes, getting the value of the "except" element
		List<AnnotationMirror> except = getExceptElementValue(anyAnnotation);

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
	public @Nullable List<AnnotationMirror> getExceptElementValue(AnnotationMirror anyStateAnnotation) {
        return getElementValueWithVisitor(anyStateAnnotation, EXCEPT_ELEMENT_NAME, multiAnnotationValueVisitor);
    }

    /**
     * @param dt Type to check.
     * @return True iff the given type is a state annotation, that is, is itself annotated with
     * \@{@link State}.
     */
    public boolean isStateAnnotation(DeclaredType dt) {
        Element el = dt.asElement();
        return el.getKind() == ElementKind.ANNOTATION_TYPE &&
                checkForStateAnnotation(dt.asElement().getAnnotationMirrors());
    }

    /**
     * @param annotation Annotation to check.
     * @return True iff the given annotation is a state annotation, that is, is itself annotated with
     * \@{@link State}.
     */
    public boolean isStateAnnotation(AnnotationMirror annotation) {
        return isStateAnnotation(annotation.getAnnotationType());
    }

    /**
     * @param stateAnnotation State annotation from which to read the "after" element.
     * @return The state annotation representing the value of the "after" element of the given annotation or null,
     * if the element is not specified or is not a (state) annotation.
     */
    public @Nullable AnnotationMirror getAfterElementValue(AnnotationMirror stateAnnotation) {
		return getSingleAnnotationElementValue(stateAnnotation, TRANSITION_ELEMENT_NAME);
    }

	/**
     * @param condAnnotation State annotation from which to read the "whenTrue" element.
     * @return The state annotation representing the value of the "whenTrue" element of the given annotation or null,
     * if the element is not specified or is not a (state) annotation.
     */
    public @Nullable AnnotationMirror getWhenTrueElementValue(AnnotationMirror condAnnotation) {
		return getSingleAnnotationElementValue(condAnnotation, WHEN_TRUE_ELEMENT_NAME);
    }

	/**
     * @param condAnnotation State annotation from which to read the "whenFalse" element.
     * @return The state annotation representing the value of the "whenFalse" element of the given annotation or null,
     * if the element is not specified or is not a (state) annotation.
     */
    public @Nullable AnnotationMirror getWhenFalseElementValue(AnnotationMirror condAnnotation) {
		return getSingleAnnotationElementValue(condAnnotation, WHEN_FALSE_ELEMENT_NAME);
    }

	/**
     * @param stateAnnotation State annotation from which to read the "onException" element.
     * @return The state annotation representing the value of the "after" element of the given annotation or null,
     * if the element is not specified or is not a (state) annotation.
     */
    public @Nullable AnnotationMirror getExceptionElementValue(AnnotationMirror stateAnnotation) {
		return getSingleAnnotationElementValue(stateAnnotation, EXCEPTION_ELEMENT_NAME);
    }

	private @Nullable AnnotationMirror getSingleAnnotationElementValue(AnnotationMirror annotation, String elementName) {
		AnnotationMirror result = getElementValueWithVisitor(annotation, elementName, singleAnnotationValueVisitor);

		if (result != null && isStateAnnotation(result)) {
        	return result;
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
	private </*@Nullable*/ R> R getElementValueWithVisitor(AnnotationMirror annotation, String elementName,
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
