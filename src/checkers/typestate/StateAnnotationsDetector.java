package checkers.typestate;

import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.source.tree.*;

import java.util.Set;
import java.util.List;

import checkers.util.AnnotationUtils;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeFactory;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * Detects all annotations annotated with {@link State}, used on any program element.
 * @author Adam Warski (adam at warski dot org)
 */
public class StateAnnotationsDetector extends TreePathScanner<Void, Set<AnnotationMirror>> {
    private final AnnotationUtils annotationUtils;
    private final ProcessingEnvironment env;
    private final TypestateUtil typestateUtil;
    private final AnnotatedTypeFactory factory;

    public StateAnnotationsDetector(ProcessingEnvironment env, TypestateUtil typestateUtil,
                                    AnnotatedTypeFactory factory) {
        this.annotationUtils = AnnotationUtils.getInstance(env);
        this.env = env;
        this.typestateUtil = typestateUtil;
        this.factory = factory;
    }

    private void addStateAnnotation(AnnotationMirror stateAnnotation, boolean isPure, Set<AnnotationMirror> to) {
        if (!to.contains(stateAnnotation)) {
            // Adding "pure" annotations, that is, without any parameters set.
            if (isPure) {
                to.add(stateAnnotation);
            } else {
                to.add(annotationUtils.fromName(stateAnnotation.getAnnotationType().toString()));
            }
        }
    }

    private void addStateAnnotations(Set<AnnotationMirror> from, Set<AnnotationMirror> to) {
        for (AnnotationMirror annotation : from) {
			boolean isStateAnnotation = typestateUtil.isStateAnnotation(annotation);

            if (isStateAnnotation) {
                addStateAnnotation(annotation, false, to);
            }

			if (typestateUtil.isAnyStateAnnotation(annotation) || isStateAnnotation) {
                // Checking if the annotation doesn't define a transition to another state. If so, adding that state.
                AnnotationMirror afterAnnotation = typestateUtil.getAfterParameterValue(annotation);
                if (afterAnnotation != null) {
                    addStateAnnotation(afterAnnotation, true, to);
                }

				// Checking if the annotation doesn't define an exception-state. If so, adding that state.
                AnnotationMirror exceptionAnnotation = typestateUtil.getExceptionParameterValue(annotation);
                if (exceptionAnnotation != null) {
                    addStateAnnotation(exceptionAnnotation, true, to);
                }

				// And, in case of an any-state annotation, if the "except" element is set and if it contains
				// state annotations.
				List<AnnotationMirror> exceptAnnotations = typestateUtil.getExceptParameterValue(annotation);
				if (exceptAnnotations != null) {
					for (AnnotationMirror exceptAnnotation : exceptAnnotations) {
					    if (typestateUtil.isStateAnnotation(exceptAnnotation)) {
							addStateAnnotation(exceptAnnotation, true, to);
						}
					}
				}
			}
        }
    }

    public Void visitMethodInvocation(MethodInvocationTree node, Set<AnnotationMirror> stateAnnotations) {
        // Adding all annotations that may be present on the invoked method return type, parameters and receiver.
        AnnotatedTypeMirror.AnnotatedExecutableType invocationType = factory.methodFromUse(node);

        addStateAnnotations(invocationType.getReceiverType().getAnnotations(), stateAnnotations);
        addStateAnnotations(invocationType.getReturnType().getAnnotations(), stateAnnotations);
        for (AnnotatedTypeMirror parameter : invocationType.getParameterTypes()) {
            addStateAnnotations(parameter.getAnnotations(), stateAnnotations);
        }

        return super.visitMethodInvocation(node, stateAnnotations);
    }

    public Void visitMethod(MethodTree node, Set<AnnotationMirror> stateAnnotations) {
        // Adding all annotations that may be present on the declared method return type, parameters and receiver.
        AnnotatedTypeMirror.AnnotatedExecutableType methodType = factory.getAnnotatedType(node);

        addStateAnnotations(methodType.getReceiverType().getAnnotations(), stateAnnotations);
        addStateAnnotations(methodType.getReturnType().getAnnotations(), stateAnnotations);
        for (AnnotatedTypeMirror parameter : methodType.getParameterTypes()) {
            addStateAnnotations(parameter.getAnnotations(), stateAnnotations);
        }

        return super.visitMethod(node, stateAnnotations);   
    }

    public Void visitAnnotation(AnnotationTree node, Set<AnnotationMirror> stateAnnotations) {
        TypeMirror nodeTypeMirror = Trees.instance(env).getTypeMirror(getCurrentPath());

        if (nodeTypeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) nodeTypeMirror;
            if (typestateUtil.isStateAnnotation(dt)) {
                addStateAnnotation(annotationUtils.fromName(dt.toString()), true, stateAnnotations);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported type of annotation: "
                    + nodeTypeMirror.getKind() + ".");
        }

        return super.visitAnnotation(node, stateAnnotations);
    }
}
