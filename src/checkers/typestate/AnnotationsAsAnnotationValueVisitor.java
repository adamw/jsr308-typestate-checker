package checkers.typestate;

import checkers.util.AnnotationUtils;
import checkers.nullness.quals.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.ArrayList;

/**
 * A visitor which, for the value of a parameter of an annotation,
 * returns the corresponding set of {@link javax.lang.model.element.AnnotationMirror}
 * if the value is a set of annotations, or null otherwise.
 * @author Adam Warski (adam at warski dot org)
*/
public class AnnotationsAsAnnotationValueVisitor implements AnnotationValueVisitor</*@Nullable*/ List<AnnotationMirror>, Void> {
    private final AnnotationAsAnnotationValueVisitor annotationAsAnnotationValueVisitor;

    public AnnotationsAsAnnotationValueVisitor(AnnotationUtils annotationUtils, Types types) {
        this.annotationAsAnnotationValueVisitor = new AnnotationAsAnnotationValueVisitor(annotationUtils, types);
    }

    public /*@Nullable*/ List<AnnotationMirror> visit(AnnotationValue av, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visit(AnnotationValue av) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitBoolean(boolean b, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitByte(byte b, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitChar(char c, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitDouble(double d, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitFloat(float f, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitInt(int i, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitLong(long i, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitShort(short s, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitString(String s, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitEnumConstant(VariableElement c, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitAnnotation(AnnotationMirror a, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitUnknown(AnnotationValue av, Void aVoid) { return null; }
    public /*@Nullable*/ List<AnnotationMirror> visitType(TypeMirror t, Void aVoid) { return null; }

    public /*@Nullable*/ List<AnnotationMirror> visitArray(List<? extends AnnotationValue> vals, Void aVoid) {
		List<AnnotationMirror> result = new ArrayList<AnnotationMirror>();
		for (AnnotationValue val : vals) {
			AnnotationMirror annotation = val.accept(annotationAsAnnotationValueVisitor, null);

			if (annotation != null) {
				result.add(annotation);
			}
		}

		return result;
	}
}