import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class DoubleReceiverTransitionState {
    @State public static @interface State1 { Class<?> after() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; }
    @State public static @interface State3 { Class<?> after() default NoChange.class; }

    public static class Helper {
        public void onlyInState1() /*@State1*/ { }
        public void onlyInState2() /*@State2*/ { }
        public void onlyInState3() /*@State3*/ { }
        public void transit() /*@State1(after=State2.class)*/ /*@State2(after=State3.class)*/ { }
    }

    public void testOk1(@State1 Helper h) {
        h.onlyInState1();
        h.transit();
        h.onlyInState2();
        h.transit();
        h.onlyInState3();
    }

    public void testOk2(@State2 Helper h) {
        h.onlyInState2();
        h.transit();
        h.onlyInState3();
    }

    public void testError1(@State1 Helper h) {
        h.onlyInState1();
        h.transit();
        h.transit();
        h.onlyInState2(); // error
    }

    public void testError2(@State1 Helper h) {
        h.onlyInState1();
        h.transit();
        h.onlyInState3(); // error
    }

    public void testError3(@State1 Helper h) {
        h.onlyInState1();
        h.transit();
        h.transit();
        h.transit(); // error
    }
}