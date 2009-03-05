import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class SimpleReceiverTransitionState {
    @State public static @interface State1 { Class<?> after() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; }

    public static class Helper {
        public void onlyInState1() /*@State1*/ { }
        public void fromState1ToState2() /*@State1(after=State2.class)*/ { }
        public void onlyInState2() /*@State2*/ { }
    }

    public void testOk(@State1 Helper h) {
        h.onlyInState1();
        h.fromState1ToState2();
        h.onlyInState2();
    }

    public void testError1(@State1 Helper h) {
        h.onlyInState1();
        h.fromState1ToState2();
        h.onlyInState1(); // error
    }

    public void testError2(@State1 Helper h) {
        h.onlyInState1();
        h.fromState1ToState2();
        h.fromState1ToState2(); // error
    }
}