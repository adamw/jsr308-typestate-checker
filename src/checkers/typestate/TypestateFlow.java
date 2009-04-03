package checkers.typestate;

import checkers.flow.MainFlow;
import checkers.flow.GenKillBits;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeFactory;
import checkers.util.InternalUtils;
import checkers.util.AnnotationUtils;
import checkers.source.Result;
import checkers.source.SourceChecker;
import com.sun.source.tree.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class TypestateFlow extends MainFlow {
    // Because AnnotationMirror doesn't implement .equals and .hashCode, a translation map is needed from
    // an annotation mirror that is "equal" to some annotation present int the <code>annotations</code>
    // set, to the annotation mirror used in the <code>GenKillBits</code> set.
    private final Map<AnnotationMirror, AnnotationMirror> annotationsTranslation;

    private final TypestateUtil typestateUtil;

    public TypestateFlow(SourceChecker checker, Set<AnnotationMirror> annotations, AnnotatedTypeFactory factory,
                         CompilationUnitTree root, TypestateUtil typestateUtil) {
        super(checker, root, annotations, factory);

        this.typestateUtil = typestateUtil;

        // This will work because the map uses special ordering.
        annotationsTranslation = AnnotationUtils.createAnnotationMap();
        for (AnnotationMirror stateAnnotation : annotations) {
            annotationsTranslation.put(stateAnnotation, stateAnnotation);
        }
    }

	private AnnotationMirror translateToErrorAnnotation(final AnnotationMirror annotation) {
		if (typestateUtil.isAnyStateAnnotation(annotation)) {
			// Creating the same any-state annotation, with the "except" element set, and other elements removed.
			AnnotationUtils.AnnotationBuilder builder = new AnnotationUtils.AnnotationBuilder(env,
					new AnnotationMirror() {
						@Override
						public DeclaredType getAnnotationType() {
							return annotation.getAnnotationType();
						}

						@Override
						public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
							Map<? extends ExecutableElement, ? extends AnnotationValue> originalValues =
									annotation.getElementValues();
							Map<? extends ExecutableElement, ? extends AnnotationValue> modifiedValues =
									new HashMap<ExecutableElement, AnnotationValue>(originalValues);

							// Only keeping the "except" element, if at all it's present.
							Iterator<? extends ExecutableElement> elementsIterator = modifiedValues.keySet().iterator();
					        while (elementsIterator.hasNext()) {
								if (!elementsIterator.next().getSimpleName().contentEquals(TypestateUtil.EXCEPT_ELEMENT_NAME)) {
									elementsIterator.remove();
								}
							}

							return modifiedValues;
						}
					});

			return builder.build();
		}

		// The annotation surely is a state annotation
		assert annotationsTranslation.containsKey(annotation);
		return annotationsTranslation.get(annotation);
	}

    private Object getErrorAnnotationSetRepresentation(Set<AnnotationMirror> annotations, boolean translate) {
        if (annotations.size() == 0) {
            return "none";
        } else if (annotations.size() == 1) {
            return translate ? translateToErrorAnnotation(annotations.iterator().next()) : annotations.iterator().next();
        } else {
            if (translate) {
                Set<AnnotationMirror> translated = AnnotationUtils.createAnnotationSet();
                for (AnnotationMirror annotation : annotations) {
                    translated.add(translateToErrorAnnotation(annotation));
                }

                return translated;
            } else {
                return annotations;
            }
        }
    }

	private void clearStateAnnotation(AnnotationMirror declaredAnnotation, int elementIdx,
									  GenKillBits<AnnotationMirror> annos) {
		AnnotationMirror receiverAnnTranslation = annotationsTranslation.get(declaredAnnotation);

		// If the "after" annotation is a state annotation, changing the state of the
		// element in the flow.
		if (annotations.contains(receiverAnnTranslation)) {
			// The annotation didn't have to be set, if the transition is caused by the any-state
			// annotation.
			annos.clear(receiverAnnTranslation, elementIdx);
		}
	}

    private void checkStateAnnotationsOnTree(Set<AnnotationMirror> declaredAnnotations, Tree annotatedTree,
                                             MethodInvocationTree methodInvocationTree, String errorMessageKey) {
        // Only checking the state if the declaration specifies any state
        if (declaredAnnotations.size() > 0) {
            Element annotatedElement = InternalUtils.symbol(annotatedTree);

            // Generating the "actual" annotations of the element.
            Set<AnnotationMirror> actualAnnotations = AnnotationUtils.createAnnotationSet();

            // If the element is a variable, getting all annotations currently inferred by the flow.
            @SuppressWarnings({"SuspiciousMethodCalls"}) int elementIdx = vars.indexOf(annotatedElement);
            if (elementIdx >= 0) {
                for (AnnotationMirror stateAnnotation : annotations) {
                    if (annos.get(stateAnnotation, elementIdx)) {
                        actualAnnotations.add(stateAnnotation);
                    }
                }
            } else {
                // Otherwise, adding all annotations which the factory can infer on the element.
                for (AnnotationMirror factoryAnnotation : factory.getAnnotatedType(annotatedTree).getAnnotations()) {
                    // Only adding state annotations
                    if (annotations.contains(factoryAnnotation)) {
                        actualAnnotations.add(factoryAnnotation);
                    }
                }
            }

            boolean stateMatchFound = false;

            // For all declared annotations: if such an annotation is a state annotation, checking if the
            // checked element is in this state. If so, doing possible transitions.
            for (AnnotationMirror declaredAnnotation : declaredAnnotations) {
                // Checking if the declared annotation is a state annotation, which is also present on the element
                // checked, or if it is the any-state annotation, and the actual annotations aren't in the
				// "except" parameter of the annotation.
                // "contains" here is ok as we use the special annotation set (annotation parameter values are ignored).
                if ((annotations.contains(declaredAnnotation) && actualAnnotations.contains(declaredAnnotation))
                        || typestateUtil.anyAnnotationCovers(declaredAnnotation, actualAnnotations)) {
                    stateMatchFound = true;

					// First checking if we are in a try-catch-finally. If so, looking for an exception annotation. If
					// it is present, updating the try bits to be in the new state.
					if (tryBits.size() > 0) {
						AnnotationMirror exceptionAnnotation = typestateUtil.getExceptionParameterValue(
								declaredAnnotation);

						if (exceptionAnnotation != null) {
							// Preparing an annotations bits set with the exception state set
							GenKillBits<AnnotationMirror> exceptionBits = GenKillBits.copy(annos);
							clearStateAnnotation(declaredAnnotation, elementIdx, exceptionBits);
							exceptionBits.set(annotationsTranslation.get(exceptionAnnotation), elementIdx);

							// And updating the exception bits
							updateExceptionBits(exceptionBits);
						}
					}

                    AnnotationMirror afterAnnotation = typestateUtil.getAfterParameterValue(declaredAnnotation);
                    // Currently the transitions will only work for variables - hence checking the elementIdx.
                    if (elementIdx >= 0 && afterAnnotation != null && annotations.contains(afterAnnotation)) {
                        // If the "after" annotation is a state annotation, changing the state of the
                        // element in the flow.
                        clearStateAnnotation(declaredAnnotation, elementIdx, annos);
                        annos.set(annotationsTranslation.get(afterAnnotation), elementIdx);
                    }
                }
            }

            // If none of the actual states matches the declared states, reporting an error.
            if (!stateMatchFound) {
                checker.report(Result.failure(errorMessageKey, annotatedTree,
                        // The declared annotations must be translated to their representation as they may
                        // contain elements - users shouldn't see that in the error message.
                        getErrorAnnotationSetRepresentation(declaredAnnotations, true),
                        getErrorAnnotationSetRepresentation(actualAnnotations, false)),
                        methodInvocationTree);
            }
        }
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        AnnotatedTypeMirror.AnnotatedExecutableType invocationType = factory.methodFromUse(node);

        // Checking the receiver
        Set<AnnotationMirror> receiverAnnotations = invocationType.getReceiverType().getAnnotations();

        if (node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
            checkStateAnnotationsOnTree(typestateUtil.filterStateAnnotations(receiverAnnotations),
                    ((MemberSelectTree) node.getMethodSelect()).getExpression(),
                    node, "receiver.in.wrong.state");
        }

        // Checking parameters; both iterators should have the same number of elements.
        Iterator<AnnotatedTypeMirror> parametersAnnotationsIter = invocationType.getParameterTypes().iterator();
        Iterator<? extends ExpressionTree> argumentsIter = node.getArguments().iterator();
        while (parametersAnnotationsIter.hasNext()) {
            checkStateAnnotationsOnTree(
					typestateUtil.filterStateAnnotations(parametersAnnotationsIter.next().getAnnotations()),
					argumentsIter.next(), node, "parameter.in.wrong.state");
        }

        return super.visitMethodInvocation(node, p);
    }

	@Override
	protected void updateExceptionBits() {
		// Exception states are handled already. Doing nothing here.
	}
}
