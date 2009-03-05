package checkers.types;

import com.sun.source.tree.*;
import com.sun.source.util.SimpleTreeVisitor;

import javax.lang.model.element.AnnotationMirror;
import java.util.Set;

import checkers.typestate.TypestateUtil;
import checkers.util.AnnotationUtils;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class TypestateTypeFromExpression
        extends SimpleTreeVisitor<Set<AnnotationMirror>, TypestateTypeFromExpression.TypestateTypeFromExpressionEnv> {
    /** The singleton instance. */
    public static final TypestateTypeFromExpression INSTANCE = new TypestateTypeFromExpression();

    private TypestateTypeFromExpression() { }

    protected Set<AnnotationMirror> defaultAction(Tree node, TypestateTypeFromExpressionEnv typestateTypeFromExpressionEnv) {
        return AnnotationUtils.createAnnotationSet();
    }

    public Set<AnnotationMirror> visitNewClass(NewClassTree node, TypestateTypeFromExpressionEnv tenv) {
        // The initial state of a newly constructed object can be defined in the receiver annotation for
        // the method
        Set<AnnotationMirror> ret = super.visitNewClass(node, tenv);

        AnnotatedTypeMirror.AnnotatedExecutableType invocationType = tenv.getFactory().constructorFromUse(node);

        Set<AnnotationMirror> receiverAnnotations = invocationType.getReceiverType().getAnnotations();

        // Only adding state annotations
        for (AnnotationMirror annotation : receiverAnnotations) {
            if (tenv.getTypestateUtil().isStateAnnotation(annotation)) {
                ret.add(annotation);
            }
        }

        return ret;
    }

    // Environment.

    public static TypestateTypeFromExpressionEnv makeEnv(AnnotatedTypeFactory factory, TypestateUtil typestateUtil) {
        return new TypestateTypeFromExpressionEnv(factory, typestateUtil);
    }

    public static class TypestateTypeFromExpressionEnv {
        private final AnnotatedTypeFactory factory;
        private final TypestateUtil typestateUtil;

        private TypestateTypeFromExpressionEnv(AnnotatedTypeFactory factory, TypestateUtil typestateUtil) {
            this.factory = factory;
            this.typestateUtil = typestateUtil;
        }

        public AnnotatedTypeFactory getFactory() {
            return factory;
        }

        public TypestateUtil getTypestateUtil() {
            return typestateUtil;
        }
    }
}
