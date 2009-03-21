package checkers.typestate;

import checkers.util.AnnotationUtils;
import checkers.nullness.quals.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;

/**
 * A visitor which, for the value of a parameter of an annotation, returns the corresponding {@link AnnotationMirror}
 * if the value is an annotation, or null otherwise.
 * @author Adam Warski (adam at warski dot org)
*/
public class AnnotationAsAnnotationValueVisitor implements AnnotationValueVisitor</*@Nullable*/ AnnotationMirror, Void> {
    private final AnnotationUtils annotationUtils;
    private final Types types;

    public AnnotationAsAnnotationValueVisitor(AnnotationUtils annotationUtils, Types types) {
        this.annotationUtils = annotationUtils;
        this.types = types;
    }

    public /*@Nullable*/ AnnotationMirror visit(AnnotationValue av, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visit(AnnotationValue av) { return null; }
    public /*@Nullable*/ AnnotationMirror visitBoolean(boolean b, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitByte(byte b, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitChar(char c, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitDouble(double d, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitFloat(float f, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitInt(int i, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitLong(long i, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitShort(short s, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitString(String s, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitEnumConstant(VariableElement c, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitAnnotation(AnnotationMirror a, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitArray(List<? extends AnnotationValue> vals, Void aVoid) { return null; }
    public /*@Nullable*/ AnnotationMirror visitUnknown(AnnotationValue av, Void aVoid) { return null; }

    // We're only interested in class values, which are additionally annotations.
    public /*@Nullable*/ AnnotationMirror visitType(TypeMirror t, Void aVoid) {
        if (types.asElement(t).getKind() == ElementKind.ANNOTATION_TYPE) {
            return annotationUtils.fromName(t.toString());
        } else {
            return null;
        }
    }
}
