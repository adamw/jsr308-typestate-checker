import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class MethodReturnParameterState {
    @State public static @interface State1 { Class<?> after() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; }

    public static class Helper { }

    public @State1 Helper get() { return null; }

    public void acceptHelperInState1(@State1 Helper h) { }
    public void acceptHelperInState2(@State2 Helper h) { }

    public void testOk() {
        acceptHelperInState1(get());
    }

    public void testError() {
        acceptHelperInState2(get()); // error
    }
}