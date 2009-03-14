import checkers.typestate.State;
import checkers.typestate.NoChange;
import checkers.typestate.Any;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class AnyWithExceptInMethodReceiverState {
    @State public static @interface State1 { public abstract Class<?> after() default NoChange.class; }
    @State public static @interface State2 { public abstract Class<?> after() default NoChange.class; }
	@State public static @interface State3 { public abstract Class<?> after() default NoChange.class; }

    public static class Helper {
        public void onlyInState2() /*@State2*/ { }

        public void transit1() /*@Any(except=State3.class, after=State2.class)*/ { }
		public void transit2() /*@Any(except=State2.class, after=State3.class)*/ { }
    }

    public void testOk() {
        Helper h = new Helper();
        h.transit1();
        h.onlyInState2();
    }

    public void testError1() {
        Helper h = new Helper();
        h.transit1();
        h.onlyInState2();
		h.transit2(); // error
    }

    public void testError2() {
        Helper h = new Helper();
        h.transit2();
		h.transit1(); // error
    }
}