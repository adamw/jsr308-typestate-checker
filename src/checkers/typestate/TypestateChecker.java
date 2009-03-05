package checkers.typestate;

import checkers.quals.TypeQualifiers;
import checkers.quals.Unqualified;
import checkers.source.SourceChecker;
import checkers.source.SourceVisitor;
import checkers.util.AnnotationUtils;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;

import javax.lang.model.element.AnnotationMirror;
import java.util.Set;

/**
 * @author Adam Warski (adam at warski dot org)
 */
@TypeQualifiers(Unqualified.class)
public class TypestateChecker extends SourceChecker {
    protected SourceVisitor<?, ?> createSourceVisitor(CompilationUnitTree root) {
        TypestateUtil typestateUtil = new TypestateUtil(getProcessingEnvironment());
        TypestateAnnotatedTypeFactory factory = new TypestateAnnotatedTypeFactory(getProcessingEnvironment(),
                typestateUtil, root);

        // Looking for all annotations annotated with @State
        Set<AnnotationMirror> stateAnnotations = AnnotationUtils.createAnnotationSet();
        new StateAnnotationsDetector(getProcessingEnvironment(), typestateUtil, factory).scan(root, stateAnnotations);

        final TypestateFlow flow = new TypestateFlow(this, stateAnnotations, factory, root, typestateUtil);

        return new SourceVisitor<Void, Void>(this, root) {
            public Void scan(TreePath path, Void o) {
                return flow.scan(path, o);
            }
        };
    }
}
