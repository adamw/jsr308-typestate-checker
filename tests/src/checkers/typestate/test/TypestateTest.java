package checkers.typestate.test;

import org.junit.Test;
import tests.CheckerTest;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class TypestateTest extends CheckerTest {
    public TypestateTest() {
        super("checkers.typestate.TypestateChecker", "typestate", "-Anomsgtext");
    }

    /** Test checking the state of a receiver, which is a variable */
    @Test
    public void testVariableReceiverState() {
        test();
    }

    /** Test checking the state of a receiver, which is the result of a method invocation */
    @Test
    public void testMethodReturnReceiverState() {
        test();
    }

    /** Test checking the state of a newly constructed object */
    @Test
    public void testConstructorState() {
        test();
    }

    /** Test checking a simple state transition of the method receiver */
    @Test
    public void testSimpleReceiverTransitionState() {
        test();
    }

    /** Test checking a double state transition of the method receiver */
    @Test
    public void testDoubleReceiverTransitionState() {
        test();
    }

    /** Test checking state when passing a parameter from a variable */
    @Test
    public void testVariableParameterState() {
        test();
    }

    /** Test checking state when passing a parameter, which is the result of a method invocation */
    @Test
    public void testMethodReturnParameterState() {
        test();
    }

    /** Test checking a simple state transition of a parameter */
    @Test
    public void testSimpleVariableTransitionState() {
        test();
    }

    /** Test checking a double state transition of a parameter */
    @Test
    public void testDoubleVariableTransitionState() {
        test();
    }

    /** Test checking transitions with the usage of the any-state annotation used in the method receiver */
    @Test
    public void testAnyInMethodReceiverState() {
        test();
    }

    /** Test checking transitions with the usage of the any-state annotation used in the parameter */
    @Test
    public void testAnyInParameterState() {
        test();
    }

    /** Test checking if other annotations on parameters (in this case: @NonNull) don't interfere with typestate checking */
    @Test
    public void testNonNullOnParameterWithoutState() {
        test();
    }

    /** Test checking with the any-state annotation with the except element specified */
    @Test
    public void testAnyWithExceptInParameterState() {
        test();
    }
}
