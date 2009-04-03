package work;

import checkers.typestate.State;
import checkers.typestate.NoChange;
import checkers.typestate.Any;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class Test3 {
	@State public static @interface State1 { Class<?> after() default NoChange.class; Class<?> onException() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; Class<?> onException() default NoChange.class; }
    @State public static @interface ErrorState { Class<?> after() default NoChange.class; }

    public static class Helper {
		public Helper() /*@State1*/ { }
	}

    public void acceptHelperInState1(@State1 Helper h) { }
    public void acceptHelperInState2(@State2 Helper h) { }
    public void acceptHelperInErrorState(@ErrorState Helper h) { }
	public void transitHelper(@State1(after = State2.class, onException = ErrorState.class) Helper h) { }

    public void testError() {
        Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInErrorState(h);	// error
		} catch (Exception e) {
			acceptHelperInState1(h);		// error
		}
    }
}