import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class ReceiverExceptionState {
    @State public static @interface State1 { public abstract Class<?> after() default NoChange.class; public abstract Class<?> onException() default NoChange.class; }
    @State public static @interface State2 { public abstract Class<?> after() default NoChange.class; public abstract Class<?> onException() default NoChange.class; }
    @State public static @interface ErrorState { public abstract Class<?> after() default NoChange.class; }

    public static class Helper {
		public Helper() /*@State1*/ { }

		public void acceptInState1() /*@State1*/ { }
    	public void acceptInState2() /*@State2*/ { }
    	public void acceptInErrorState() /*@ErrorState*/ { }
		public void transit() /*@State1(after = State2.class, onException = ErrorState.class)*/ { }
	}

    public void testOk() {
		Helper h = new Helper();
        try {
			h.acceptInState1();
			h.transit();
			h.acceptInState2();
		} catch (Exception e) {
			h.acceptInErrorState();
		}
    }

    public void testError() {
        Helper h = new Helper();
        try {
			h.acceptInState1();
			h.transit();
			h.acceptInErrorState();		// error
		} catch (Exception e) {
			h.acceptInState1();			// error
			h.acceptInState2();			// error
		}
    }
}