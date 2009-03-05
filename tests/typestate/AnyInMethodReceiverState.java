import checkers.typestate.State;
import checkers.typestate.NoChange;
import checkers.typestate.Any;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class AnyInMethodReceiverState {
    @State public static @interface State1 { public abstract Class<?> after() default NoChange.class; }
    @State public static @interface State2 { public abstract Class<?> after() default NoChange.class; }

    public static class Helper {
        public void onlyInState1() /*@State1*/ { }
        public void onlyInState2() /*@State2*/ { }

        public void transit() /*@Any(after=State2.class)*/ { }
    }

    public void testOk() {
        Helper h = new Helper();
        h.transit();
        h.onlyInState2();
    }

    public void testError1() {
        Helper h = new Helper();
        h.transit();
        h.onlyInState1(); // error
    }

    public void testError2() {
        Helper h = new Helper();
        h.onlyInState2(); // error
    }
}