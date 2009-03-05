import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class VariableParameterState {
    @State public static @interface State1 { Class<?> after() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; }

    public static class Helper {
        public Helper() /*@State1*/ { }
    }

    public void acceptHelperInState1(@State1 Helper h) { }
    public void acceptHelperInState2(@State2 Helper h) { }

    public void testOk() {
        Helper h = new Helper();
        acceptHelperInState1(h);
    }

    public void testError() {
        Helper h = new Helper();
        acceptHelperInState2(h); // error
    }
}