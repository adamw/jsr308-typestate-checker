package checkers.typestate;

import checkers.util.AnnotationUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Set;

/**
 * A visitor which, for the value of a parameter of an annotation,
 * returns the corresponding set of {@link javax.lang.model.element.AnnotationMirror}
 * if the value is a set of annotations, or null otherwise.
 * @author Adam Warski (adam at warski dot org)
*/
public class AnnotationsAsAnnotationValueVisitor implements AnnotationValueVisitor<Set<AnnotationMirror>, Void> {
    private final AnnotationAsAnnotationValueVisitor annotationAsAnnotationValueVisitor;

    public AnnotationsAsAnnotationValueVisitor(AnnotationUtils annotationUtils, Types types) {
        this.annotationAsAnnotationValueVisitor = new AnnotationAsAnnotationValueVisitor(annotationUtils, types);
    }

    public Set<AnnotationMirror> visit(AnnotationValue av, Void aVoid) { return null; }
    public Set<AnnotationMirror> visit(AnnotationValue av) { return null; }
    public Set<AnnotationMirror> visitBoolean(boolean b, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitByte(byte b, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitChar(char c, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitDouble(double d, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitFloat(float f, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitInt(int i, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitLong(long i, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitShort(short s, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitString(String s, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitEnumConstant(VariableElement c, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitAnnotation(AnnotationMirror a, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitUnknown(AnnotationValue av, Void aVoid) { return null; }
    public Set<AnnotationMirror> visitType(TypeMirror t, Void aVoid) { return null; }

    public Set<AnnotationMirror> visitArray(List<? extends AnnotationValue> vals, Void aVoid) {
		Set<AnnotationMirror> result = AnnotationUtils.createAnnotationSet();
		for (AnnotationValue val : vals) {
			AnnotationMirror annotation = annotationAsAnnotationValueVisitor.visit(val);

			if (annotation != null) {
				result.add(annotation);
			}
		}

		return result;
	}
}