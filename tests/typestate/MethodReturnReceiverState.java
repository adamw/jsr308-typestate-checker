import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class MethodReturnReceiverState {
    @State public static @interface State1 { Class<?> after() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; }

    public static class Helper {
        public void onlyInState1() /*@State1*/ { }
        public void onlyInState2() /*@State2*/ { }
    }

    @State1 Helper getHelperInState1() { return null; }

    public void testOk() {
        getHelperInState1().onlyInState1();
    }

    public void testError() {
        getHelperInState1().onlyInState2(); // error
    }
}