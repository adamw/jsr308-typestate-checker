import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class ParameterExceptionState {
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
	public void recover(@ErrorState(after = State2.class) Helper h) { }

    public void testOk1() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);
		} catch (Exception e) {
			acceptHelperInErrorState(h);
		}
    }

	public void testOk2() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);
		} catch (Exception e) {
			acceptHelperInErrorState(h);
			return;
		}

		acceptHelperInState2(h);			// ok - the catch is dead
    }

	public void testOk3() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);
		} catch (RuntimeException e) {
			recover(h);
		} catch (Exception e) {
			acceptHelperInErrorState(h);
			return;
		}

		acceptHelperInState2(h);			// ok - one catch is dead, the other one tran
    }

    public void testError1() {
        Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInErrorState(h);	// error
		} catch (Exception e) {
			acceptHelperInState1(h);		// error
			acceptHelperInState2(h);		// error
		}
    }

	public void testError2() {
        Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);		// error
		} catch (Exception e) {
			acceptHelperInErrorState(h);
		}

		acceptHelperInState1(h);			// error - the catch is dead, no state defined
		acceptHelperInState2(h);			// error
		acceptHelperInErrorState(h);		// error
    }
}