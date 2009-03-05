package checkers.typestate;

import checkers.util.AnnotationUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;

/**
 * A visitor which, for the value of a parameter of an annotation, returns the corresponding {@link AnnotationMirror}
 * if the value is an annotation, or null otherwise.
 * @author Adam Warski (adam at warski dot org)
*/
public class AnnotationAsAnnotationValueVisitor implements AnnotationValueVisitor<AnnotationMirror, Void> {
    private final AnnotationUtils annotationUtils;
    private final Types types;

    public AnnotationAsAnnotationValueVisitor(AnnotationUtils annotationUtils, Types types) {
        this.annotationUtils = annotationUtils;
        this.types = types;
    }

    public AnnotationMirror visit(AnnotationValue av, Void aVoid) { return null; }
    public AnnotationMirror visit(AnnotationValue av) { return null; }
    public AnnotationMirror visitBoolean(boolean b, Void aVoid) { return null; }
    public AnnotationMirror visitByte(byte b, Void aVoid) { return null; }
    public AnnotationMirror visitChar(char c, Void aVoid) { return null; }
    public AnnotationMirror visitDouble(double d, Void aVoid) { return null; }
    public AnnotationMirror visitFloat(float f, Void aVoid) { return null; }
    public AnnotationMirror visitInt(int i, Void aVoid) { return null; }
    public AnnotationMirror visitLong(long i, Void aVoid) { return null; }
    public AnnotationMirror visitShort(short s, Void aVoid) { return null; }
    public AnnotationMirror visitString(String s, Void aVoid) { return null; }
    public AnnotationMirror visitEnumConstant(VariableElement c, Void aVoid) { return null; }
    public AnnotationMirror visitAnnotation(AnnotationMirror a, Void aVoid) { return null; }
    public AnnotationMirror visitArray(List<? extends AnnotationValue> vals, Void aVoid) { return null; }
    public AnnotationMirror visitUnknown(AnnotationValue av, Void aVoid) { return null; }

    // We're only interested in class values, which are additionally annotations.
    public AnnotationMirror visitType(TypeMirror t, Void aVoid) {
        if (types.asElement(t).getKind() == ElementKind.ANNOTATION_TYPE) {
            return annotationUtils.fromName(t.toString());
        } else {
            return null;
        }
    }
}
