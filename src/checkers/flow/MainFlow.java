package checkers.flow;

import checkers.source.SourceChecker;
import checkers.types.*;
import checkers.types.AnnotatedTypeMirror.*;
import checkers.util.*;

import java.util.*;

import com.sun.source.tree.*;
import com.sun.source.util.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;

/**
 * A modified version of {@link Flow}.
 *
 * Should report errors during the flow analysis, as no information is recorded for later retrieval.
 * Also, subtyping isn't checked in any way.
 *
 * Detailed changes:
 * - removed the {@code QualifierHierarchy annoRelations} field and all its usages
 * - removed the {@code AnnotationMirror test(Tree tree)} method
 * - removed the {@code Map<Location, AnnotationMirror> flowResults} field and all its usages
 * - removed the {@code void recordBits(TreePath path)} method and all its usages
 *
 * @author Adam Warski (adam at warski dot org)
 * @author The authors of the {@link Flow} class.
 */
public class MainFlow extends TreePathScanner<Void, Void> {
    /** The checker to which this instance belongs. */
    protected final SourceChecker checker;

    /** The processing environment to use. */
    protected final ProcessingEnvironment env;

    /** The file that's being analyzed. */
    protected final CompilationUnitTree root;

    /**
     * The annotations (qualifiers) to infer.
	 */
    protected final Set<AnnotationMirror> annotations;

    /** Utility class for getting source positions. */
    protected final SourcePositions source;

    /** Utility class for determining annotated types. */
    protected final AnnotatedTypeFactory factory;

    /** Utility class for operations on annotated types. */
    protected final AnnotatedTypes atypes;

    /**
     * Maps variables to a bit index. This index is also used as the bit index
     * to determine a variable's annotatedness using
     * annos/annosWhenTrue/annosWhenFalse.
     */
    protected final List<VariableElement> vars;

    /**
     * Tracks the annotated state of each variable during flow. Bit indices
     * correspond exactly to indices in {@link #vars}. This field is set to
     * null immediately after splitting for a branch, and is set to some
     * combination (usually boolean "and") of {@link #annosWhenTrue} and
     * {@link #annosWhenFalse} after merging. Since it is used when visiting the
     * true and false branches, however, it may be non-null concurrently with
     * {@link #annosWhenTrue} and {@link #annosWhenFalse}.
     */
    protected GenKillBits<AnnotationMirror> annos;

    /**
     * Tracks the annotated state of each variable in a true branch. As in
     * {@code javac}'s {@code Flow}, saving/restoring via local variables
     * handles nested branches. Bit indices correspond exactly to indices in
     * {@link #vars}. This field is copied from {@link #annos} when splitting
     * for a branch and is set to null immediately after merging.
     *
     * @see #annos
     */
    protected GenKillBits<AnnotationMirror> annosWhenTrue;

    /**
     * Tracks the annotated state of each variable in a false branch. As in
     * {@code javac}'s {@code Flow}, saving/restoring via local variables
     * handles nested branches. Bit indices correspond exactly to indices in
     * {@link #vars}. This field is copied from {@link #annos} when splitting
     * for a branch and is set to null immediately after merging.
     *
     * @see #annos
     */
    protected GenKillBits<AnnotationMirror> annosWhenFalse;

    /**
     * Stores the result of liveness analysis, required by the GEN-KILL analysis
     * for proper handling of jumps (break, return, throw, etc.).
     */
    private boolean alive = true;

    /**
	 * Tracks annotations in potential exception-throwing statements in try blocks.
	 */
    protected final Deque<GenKillBits<AnnotationMirror>> tryBits;

	/**
	 * Tracks annotations in potential exception-throwing statements in catch blocks.
	 */
    protected final Deque<GenKillBits<AnnotationMirror>> catchBits;

    /** Visitor state; tracking is required for checking receiver types. */
    private final VisitorState visitorState;

    /** Utilities for {@link Element}s. */
    protected final Elements elements;

    /** Memoization for {@link #varDefHasAnnotation(AnnotationMirror, Element)}. */
    private Map<Element, Boolean> annotatedVarDefs = new HashMap<Element, Boolean>();

	/**
     * Creates a new analysis. The analysis will use the given {@link
     * AnnotatedTypeFactory} to obtain annotated types.
     *
     * @param checker the current checker
     * @param root the compilation unit that will be scanned
     * @param annotations the annotations to track
     * @param factory the factory class that will be used to get annotated
     *        types, or {@code null} if the default factory should be used
     */
    public MainFlow(SourceChecker checker, CompilationUnitTree root,
            Set<AnnotationMirror> annotations, AnnotatedTypeFactory factory) {

        this.checker = checker;
        this.env = checker.getProcessingEnvironment();
        this.root = root;
        this.annotations = annotations;

        this.source = Trees.instance(env).getSourcePositions();
        if (factory == null)
            this.factory = new AnnotatedTypeFactory(checker, root);
        else this.factory = factory;

        this.atypes = new AnnotatedTypes(env, factory);

        this.visitorState = this.factory.getVisitorState();

        this.vars = new ArrayList<VariableElement>();

        this.annos = new GenKillBits<AnnotationMirror>(this.annotations);
        this.annosWhenTrue = null;
        this.annosWhenFalse = null;

        this.tryBits = new LinkedList<GenKillBits<AnnotationMirror>>();
		this.catchBits = new LinkedList<GenKillBits<AnnotationMirror>>();

        elements = env.getElementUtils();
    }

    @Override
    public Void scan(Tree tree, Void p) {
        if (tree != null && getCurrentPath() != null)
            this.visitorState.setPath(new TreePath(getCurrentPath(), tree));
        return super.scan(tree, p);
    }

    /**
     * Registers a new variable for flow tracking.
     *
     * @param tree the variable to register
     */
    void newVar(VariableTree tree) {

        VariableElement var = TreeUtils.elementFromDeclaration(tree);
        assert var != null : "no symbol from tree";

        if (vars.contains(var)) {
            return;
        }

        int idx = vars.size();
        vars.add(var);

        AnnotatedTypeMirror type = factory.getAnnotatedType(tree);
        assert type != null : "no type from symbol";

        // Determine the initial status of the variable by checking its
        // annotated type.
        for (AnnotationMirror annotation : annotations) {
            if (hasAnnotation(type, annotation))
                annos.set(annotation, idx);
            else
                annos.clear(annotation, idx);
        }
    }

    /**
     * Determines whether a type has an annotation. If the type is not a
     * wildcard, it checks the type directly; if it is a wildcard, it checks the
     * wildcard's "extends" bound (if it has one).
     *
     * @param type the type to check
     * @param annotation the annotation to check for
     * @return true if the (non-wildcard) type has the annotation or, if a
     *         wildcard, the type has the annotation on its extends bound
     */
    private boolean hasAnnotation(AnnotatedTypeMirror type,
            AnnotationMirror annotation) {
        if (!(type instanceof AnnotatedWildcardType))
            return type.hasAnnotation(annotation);
        AnnotatedWildcardType wc = (AnnotatedWildcardType) type;
        AnnotatedTypeMirror bound = wc.getExtendsBound();
        if (bound != null && bound.hasAnnotation(annotation))
            return true;
        return false;
    }

    /**
     * Moves bits as assignments are made.
     *
     * <p>
     *
     * If only type information (and not a {@link Tree}) is available, use
     * {@link #propagateFromType(Tree, AnnotatedTypeMirror)} instead.
     *
     * @param lhs the left-hand side of the assignment
     * @param rhs the right-hand side of the assignment
     */
    void propagate(Tree lhs, ExpressionTree rhs) {
        // Skip assignment to arrays.
        if (lhs.getKind() == Tree.Kind.ARRAY_ACCESS)
            return;

        // Get the element for the left-hand side.
        Element elt = InternalUtils.symbol(lhs);
        assert elt != null;
        AnnotatedTypeMirror eltType = factory.getAnnotatedType(elt);

        // Get the annotated type of the right-hand side.
        AnnotatedTypeMirror type = factory.getAnnotatedType(rhs);
        if (TreeUtils.skipParens(rhs).getKind() == Tree.Kind.ARRAY_ACCESS) {
            propagateFromType(lhs, type);
            return;
        }
        assert type != null;

        int idx = vars.indexOf(elt);
        if (idx < 0) return;

        // Get the element for the right-hand side.
        Element rElt = InternalUtils.symbol(rhs);
        int rIdx = vars.indexOf(rElt);

        for (AnnotationMirror annotation : annotations) {
            // Propagate/clear the annotation if it's annotated or an annotation
            // had been inferred previously.
            if (hasAnnotation(type, annotation))
                annos.set(annotation, idx);
            else if (rIdx >= 0 && annos.get(annotation, rIdx))
                annos.set(annotation, idx);
            else annos.clear(annotation, idx);
        }
    }

    /**
     * Moves bits in an assignment using a type instead of a tree.
     *
     * <p>
     *
     * {@link #propagate(Tree, Tree)} is preferred, since it is able to use
     * extra information about the right-hand side (such as its element). This
     * method should only be used when a type (and nothing else) is available,
     * such as when checking the variable in an enhanced for loop against the
     * iterated type (which is the type argument of an {@link Iterable}).
     *
     * @param lhs the left-hand side of the assignment
     * @param rhs the type of the right-hand side of the assignment
     */
    void propagateFromType(Tree lhs, AnnotatedTypeMirror rhs) {

        if (lhs.getKind() == Tree.Kind.ARRAY_ACCESS)
            return;

        Element elt = InternalUtils.symbol(lhs);

        int idx = vars.indexOf(elt);
        if (idx < 0) return;

        for (AnnotationMirror annotation : annotations) {
            if (hasAnnotation(rhs, annotation))
                annos.set(annotation, idx);
            else annos.clear(annotation, idx);
        }
    }

    /**
     * Split the bitset before a conditional branch.
     */
    void split() {
        annosWhenFalse = GenKillBits.copy(annos);
        annosWhenTrue = annos;
        annos = null;
    }

    /**
     * Merge the bitset after a conditional branch.
     */
    void merge() {
        annos = GenKillBits.copy(annos);
        annos.and(annosWhenFalse);
        annosWhenTrue = annosWhenFalse = null;
    }

    // **********************************************************************

    /**
     * Called whenever a definition is scanned.
     *
     * @param tree the definition being scanned
     */
    protected void scanDef(Tree tree) {
        alive = true;
        scan(tree, null);
    }

    /**
     * Called whenever a statement is scanned.
     *
     * @param tree the statement being scanned
     */
    protected void scanStat(StatementTree tree) {
        alive = true;
        scan(tree, null);
    }

    /**
     * Called whenever a block of statements is scanned.
     *
     * @param trees the statements being scanned
     */
    protected void scanStats(List<? extends StatementTree> trees) {
        scan(trees, null);
    }

    /**
     * Called whenever a conditional expression is scanned.
     *
     * @param tree the condition being scanned
     */
    protected void scanCond(Tree tree) {
        alive = true;
        scan(tree, null);
        if (annos != null) split();
        annos = null;
    }

    /**
     * Called whenever an expression is scanned.
     *
     * @param tree the expression being scanned
     */
    protected void scanExpr(ExpressionTree tree) {
        alive = true;
        scan(tree, null);
        if (annos == null) merge();
    }

    // **********************************************************************

    @Override
    public Void visitClass(ClassTree node, Void p) {
        AnnotatedDeclaredType preClassType = visitorState.getClassType();
        ClassTree preClassTree = visitorState.getClassTree();
        AnnotatedDeclaredType preAMT = visitorState.getMethodReceiver();
        MethodTree preMT = visitorState.getMethodTree();

        visitorState.setClassType(factory.getAnnotatedType(node));
        visitorState.setClassTree(node);
        visitorState.setMethodReceiver(null);
        visitorState.setMethodTree(null);

        try {
            scan(node.getModifiers(), p);
            scan(node.getTypeParameters(), p);
            scan(node.getExtendsClause(), p);
            scan(node.getImplementsClause(), p);
            // Ensure that all fields are scanned before scanning methods.
            for (Tree t : node.getMembers()) {
                if (t.getKind() == Tree.Kind.METHOD) continue;
                scan(t, p);
            }
            for (Tree t : node.getMembers()) {
                if (t.getKind() != Tree.Kind.METHOD) continue;
                scan(t, p);
            }
            return null;
        } finally {
            this.visitorState.setClassType(preClassType);
            this.visitorState.setClassTree(preClassTree);
            this.visitorState.setMethodReceiver(preAMT);
            this.visitorState.setMethodTree(preMT);
        }
    }

    @Override
    public Void visitImport(ImportTree tree, Void p) {
        return null;
    }

    @Override
    public Void visitAnnotation(AnnotationTree tree, Void p) {
        return null;
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        newVar(node);
        ExpressionTree init = node.getInitializer();
        if (init != null) {
            scanExpr(init);
            VariableElement elem = TreeUtils.elementFromDeclaration(node);
            if (!isNonFinalField(elem) /*&& type.getAnnotations().isEmpty()*/) {
                propagate(node, init);
            }
        }
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        ExpressionTree var = node.getVariable();
        ExpressionTree expr = node.getExpression();
        if (!(var instanceof IdentifierTree))
            scanExpr(var);
        scanExpr(expr);
        propagate(var, expr);
        if (var instanceof IdentifierTree)
            this.scan(var, p);
        return null;
    }

    // This is an exact copy of visitAssignment()
    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
        ExpressionTree var = node.getVariable();
        ExpressionTree expr = node.getExpression();
        if (!(var instanceof IdentifierTree))
            scanExpr(var);
        scanExpr(expr);
        propagate(var, expr);
        return null;
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
        VariableTree var = node.getVariable();
        newVar(var);

        ExpressionTree expr = node.getExpression();
        scanExpr(expr);

        AnnotatedTypeMirror rhs = factory.getAnnotatedType(expr);
        AnnotatedTypeMirror iter = atypes.getIteratedType(rhs);
        if (iter != null)
            propagateFromType(var, iter);

        return super.visitEnhancedForLoop(node, p);
    }

    @Override
    public Void visitAssert(AssertTree node, Void p) {
        scanCond(node.getCondition());
        GenKillBits<AnnotationMirror> annosAfterAssert = GenKillBits.copy(annosWhenTrue);
        annos = GenKillBits.copy(annosWhenFalse);
        scanExpr(node.getDetail());
        annos = annosAfterAssert;
        return null;
    }

    @Override
    public Void visitIf(IfTree node, Void p) {
        scanCond(node.getCondition());

        GenKillBits<AnnotationMirror> before = annosWhenFalse;
        annos = annosWhenTrue;

        boolean aliveBefore = alive;

        scanStat(node.getThenStatement());
        StatementTree elseStmt = node.getElseStatement();
        if (elseStmt != null) {
            boolean aliveAfter = alive;
            alive = aliveBefore;
            GenKillBits<AnnotationMirror> after = GenKillBits.copy(annos);
            annos = before;
            scanStat(elseStmt);
            alive &= aliveAfter;
            if (!alive)
                annos = GenKillBits.copy(after);
            else
                annos.and(after);
        } else {
            alive &= aliveBefore;
            if (!alive)
                annos = GenKillBits.copy(before);
            else
                annos.and(before);
        }

        return null;
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node,
            Void p) {

        // Split and merge as for an if/else.
        scanCond(node.getCondition());

        GenKillBits<AnnotationMirror> before = annosWhenFalse;
        annos = annosWhenTrue;

        scanExpr(node.getTrueExpression());
        GenKillBits<AnnotationMirror> after = GenKillBits.copy(annos);
        annos = before;

        scanExpr(node.getFalseExpression());
        annos.and(after);

        return null;
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
        boolean pass = false;
        GenKillBits<AnnotationMirror> annoCond;
        do {
            GenKillBits<AnnotationMirror> annoEntry = GenKillBits.copy(annos);
            scanCond(node.getCondition());
            annoCond = annosWhenFalse;
            annos = annosWhenTrue;
            scanStat(node.getStatement());
            if (pass) break;
            annosWhenTrue.and(annoEntry);
            pass = true;
        } while (true);
        annos = annoCond;
        return null;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
        boolean pass = false;
        GenKillBits<AnnotationMirror> annoCond;
        do {
            GenKillBits<AnnotationMirror> annoEntry = GenKillBits.copy(annos);
            scanStat(node.getStatement());
            scanCond(node.getCondition());
            annoCond = annosWhenFalse;
            annos = annosWhenTrue;
            if (pass) break;
            annosWhenTrue.and(annoEntry);
            pass = true;
        } while (true);
        annos = annoCond;
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Void p) {
        boolean pass = false;
        for (StatementTree initalizer : node.getInitializer())
            scanStat(initalizer);
        GenKillBits<AnnotationMirror> annoCond;
        do {
            GenKillBits<AnnotationMirror> annoEntry = GenKillBits.copy(annos);
            scanCond(node.getCondition());
            annoCond = annosWhenFalse;
            annos = annosWhenTrue;
            scanStat(node.getStatement());
            for (StatementTree tree : node.getUpdate())
                scanStat(tree);
            if (pass) break;
            annosWhenTrue.and(annoEntry);
            pass = true;
        } while (true);
        annos = annoCond;
        return null;
    }

    @Override
    public Void visitBreak(BreakTree node, Void p) {
        alive = false;
        return null;
    }

    @Override
    public Void visitContinue(ContinueTree node, Void p) {
        alive = false;
        return null;
    }

    @Override
    public Void visitReturn(ReturnTree node, Void p) {
        if (node.getExpression() != null)
            scanExpr(node.getExpression());
        alive = false;
        return null;
    }

    @Override
    public Void visitThrow(ThrowTree node, Void p) {
        scanExpr(node.getExpression());
        alive = false;
        return null;
    }

	@Override
	public Void visitTry(TryTree node, Void p) {
		// The try bits will be updated on the first potential exception-throwing statement
		tryBits.push(null);
		scan(node.getBlock(), p);

		GenKillBits<AnnotationMirror> annosAfterBlock = annos;
		// This can be null if no exception-throwing statements where found. This bit-set is a conjunction of
		// annotation bit-sets for all potentially exception-throwing statements.
		GenKillBits<AnnotationMirror> annosForCatch = tryBits.pop();

		// Annotations for the finally block, which will be executed after any dead catches. They have to include
		// the "annosForCatch", as an exception may pass-through all catches in this try. If an exception passes-through
		// all catches, this is considered the same as a "dead catch", as the code after finally won't be executed.
		GenKillBits<AnnotationMirror> annosForFinallyDeadCatches = null;
		if (annosForCatch != null) {
			annosForFinallyDeadCatches = GenKillBits.copy(annosForCatch);
		}

		// The code after the finally block may be executed either when an exception was caught and handled by an
		// alive catch, or when no excpetion was thrown at all. So the intial annotations for finally in this case
		// are "annosAfterBlock", and later conjunctions with annotations after alive catches will be added.
		GenKillBits<AnnotationMirror> annosForFinallyAliveCatches = GenKillBits.copy(annosAfterBlock);

		if (node.getCatches() != null && annosForCatch != null) {
			boolean aliveBefore = alive;

			try {
				for (CatchTree ct : node.getCatches()) {
					// The catch bits will be updated on the first potential exception-throwing statement (if any)
					catchBits.push(null);

					alive = true;
					annos = GenKillBits.copy(annosForCatch);
					scan(ct, p);

					// Updating annotations for finally depending if the catch is alive or not (finally will be executed
					// after the catch completes, so we have to make a conjunction with "annos")
					if (alive) {
						annosForFinallyAliveCatches.and(annos);
					} else {
						if (annosForFinallyDeadCatches != null) {
							annosForFinallyDeadCatches.and(annos);
						} else {
							annosForFinallyDeadCatches = GenKillBits.copy(annos);
						}
					}

					GenKillBits<AnnotationMirror> annosForFinallyFromCatch = catchBits.pop();
					if (annosForFinallyFromCatch != null) {
						// In such case, the catch may potentially throw an exception. If it does, it's a "dead" catch,
						// and after executing finally, the exception will be propagated further.
						if (annosForFinallyDeadCatches != null) {
							annosForFinallyDeadCatches.and(annosForFinallyFromCatch);
						} else {
							annosForFinallyDeadCatches = GenKillBits.copy(annosForFinallyFromCatch);
						}
					}
				}
			} finally {
				alive = aliveBefore;
			}
		}
		
		// Evaluating the finally block

		// For the dead catches (if any)
		if (annosForFinallyDeadCatches != null) {
			annos = annosForFinallyDeadCatches;
			scan(node.getFinallyBlock(), p);
		}

		// And then for the alive catches; this is always not-null
		annos = annosForFinallyAliveCatches;
		scan(node.getFinallyBlock(), p);
		// The annotations after scanning finally with alive-catches are then used to scan the rest

		return null;
	}

	/**
	 * Updates the current try and catch bits on an exception-throwing statement.
	 * @param exceptionBits The annotations on the statement, in case an exception is thrown.                   
	 */
	protected void updateExceptionBits(GenKillBits<AnnotationMirror> exceptionBits) {
		updateExceptionBits(tryBits, exceptionBits);
		updateExceptionBits(catchBits, exceptionBits);
	}

	private void updateExceptionBits(Deque<GenKillBits<AnnotationMirror>> bitsStack, GenKillBits<AnnotationMirror> exceptionBits) {
		int popped = 0;

		// First removing any null bits
		while (bitsStack.size() > 0 && bitsStack.peek() == null) {
			bitsStack.pop();
			popped++;
		}

		// Updating all catch bits, as the exception can be propagated
		for (GenKillBits<AnnotationMirror> catchBit : bitsStack) {
			catchBit.and(exceptionBits);
		}

		// And pushing the initial catch bits in place for the null ones
		while (popped > 0) {
			bitsStack.push(GenKillBits.copy(exceptionBits));
			popped--;
		}
	}

	protected void updateExceptionBits() {
		updateExceptionBits(annos);
	}

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        super.visitMethodInvocation(node, p);

        ExecutableElement method = TreeUtils.elementFromUse(node);
        if (method.getSimpleName().contentEquals("exit")
                && method.getEnclosingElement().getSimpleName().contentEquals("System"))
            alive = false;

        final String methodPackage = elements.getPackageOf(method).getQualifiedName().toString();
        boolean isJDKMethod = methodPackage.startsWith("java") || methodPackage.startsWith("com.sun");
        for (int i = 0; i < vars.size(); i++) {
            Element var = vars.get(i);
            for (AnnotationMirror a : annotations)
                if (!isJDKMethod && isNonFinalField(var) && !varDefHasAnnotation(a, var))
                    annos.clear(a, i);
        }

		updateExceptionBits();

        return null;
    }

    @Override
    public Void visitBlock(BlockTree node, Void p) {
        if (node.isStatic()) {
            GenKillBits<AnnotationMirror> prev = GenKillBits.copy(annos);
            try {
                super.visitBlock(node, p);
                return null;
            } finally {
                annos = prev;
            }
        }
        return super.visitBlock(node, p);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        AnnotatedDeclaredType preMRT = visitorState.getMethodReceiver();
        MethodTree preMT = visitorState.getMethodTree();
        visitorState.setMethodReceiver(
                factory.getAnnotatedType(node).getReceiverType());
        visitorState.setMethodTree(node);

        // Intraprocedural, so save and restore bits.
        GenKillBits<AnnotationMirror> prev = GenKillBits.copy(annos);
        try {
            super.visitMethod(node, p);
            return null;
        } finally {
            annos = prev;
            visitorState.setMethodReceiver(preMRT);
            visitorState.setMethodTree(preMT);
        }
    }

    // **********************************************************************

    /**
     * Determines whether a variable definition has been annotated.
     *
     * @param annotation the annotation to check for
     * @param var the variable to check
     * @return true if the variable has the given annotation, false otherwise
     */
    private boolean varDefHasAnnotation(AnnotationMirror annotation, Element var) {

        if (annotatedVarDefs.containsKey(var))
            return annotatedVarDefs.get(var);

        boolean result = hasAnnotation(factory.getAnnotatedType(var), annotation);
        annotatedVarDefs.put(var, result);
        return result;
    }

    /**
     * Tests whether the element is of a non-final field
     *
     * @return true iff element is a non-final field
     */
    private static boolean isNonFinalField(Element element) {
        return (element.getKind().isField()
                && !ElementUtils.isFinal(element));
    }
}