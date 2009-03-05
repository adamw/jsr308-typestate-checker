import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class ConstructorState {
    @State public static @interface State1 { Class<?> after() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; }

    public static class Helper {
        public Helper() /*@State1*/ { }
        public void onlyInState1() /*@State1*/ { }
        public void onlyInState2() /*@State2*/ { }
    }

    public void testOk1() {
        Helper h = new Helper();
        h.onlyInState1();
    }

    public void testOk2() {
        new Helper().onlyInState1();
    }

    public void testError1() {
        Helper h = new Helper();
        h.onlyInState2(); // error
    }

    public void testError2() {
        new Helper().onlyInState2(); // error
    }
}