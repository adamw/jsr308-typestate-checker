import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class VariableReceiverState {
    @State public static @interface State1 { Class<?> after() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; }

    public static class Helper {
        public void onlyInState1() /*@State1*/ { }
        public void onlyInState2() /*@State2*/ { }
    }

    public void testOk(@State1 Helper h) {
        h.onlyInState1();
    }

    public void testError(@State1 Helper h) {
        h.onlyInState2(); // error
    }
}
