package checkers.typestate;

import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.TypestateTypeFromExpression;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;

import java.util.Set;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class TypestateAnnotatedTypeFactory extends AnnotatedTypeFactory {
    private final TypestateTypeFromExpression.TypestateTypeFromExpressionEnv typestateTypeFromExpressionEnv;

    public TypestateAnnotatedTypeFactory(ProcessingEnvironment env, TypestateUtil typestateUtil,
                                         CompilationUnitTree root) {
        super(env, null, root);

        this.typestateTypeFromExpressionEnv = TypestateTypeFromExpression.makeEnv(this, typestateUtil);
    }

    public AnnotatedTypeMirror fromExpression(ExpressionTree tree) {
        AnnotatedTypeMirror result = super.fromExpression(tree);

        Set<AnnotationMirror> stateAnnotations =
                TypestateTypeFromExpression.INSTANCE.visit(tree, typestateTypeFromExpressionEnv);
        for (AnnotationMirror annotation : stateAnnotations) {
            result.addAnnotation(annotation);
        }
        
        return result;
    }
}
