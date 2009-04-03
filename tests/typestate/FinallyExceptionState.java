import checkers.typestate.*;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class FinallyExceptionState {
    @State public static @interface State1 { public abstract Class<?> after() default NoChange.class; public abstract Class<?> onException() default NoChange.class; }
    @State public static @interface State2 { public abstract Class<?> after() default NoChange.class; public abstract Class<?> onException() default NoChange.class; }
    @State public static @interface ErrorState { public abstract Class<?> after() default NoChange.class; }

    public static class Helper {
		public Helper() /*@State1*/ { }
	}

    public void acceptHelperInState1(@State1 Helper h) { }
    public void acceptHelperInState2(@State2 Helper h) { }
    public void acceptHelperInErrorState(@ErrorState Helper h) { }
	public void transitHelper(@State1(after = State2.class, onException = ErrorState.class) Helper h) { }
	public void useHelper(@Any(onException = ErrorState.class) Helper h) { }
	public void recover(@ErrorState(after = State2.class) Helper h) { }

    public void testOk1() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
		} finally {
			transitHelper(h);
			acceptHelperInState2(h);
		}
		
		acceptHelperInState2(h);
    }

	public void testOkError1() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);
		} catch (Exception e) {
			acceptHelperInErrorState(h);
			return;
		} finally {
			acceptHelperInState1(h);		// error: state undefined
			acceptHelperInState2(h);        // error
			acceptHelperInErrorState(h);	// error
		}

		acceptHelperInState2(h);			// ok: dead catch
    }

	public void testError1() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);
		} catch (Exception e) {
			acceptHelperInErrorState(h);
		} finally {
			acceptHelperInState1(h);		// error: state undefined
			acceptHelperInState2(h);        // error
			acceptHelperInErrorState(h);	// error
		}

		acceptHelperInState1(h);			// error: alive catch
		acceptHelperInState2(h);			// error
		acceptHelperInErrorState(h);		// error
    }

	public void testError2() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
		} finally {
			transitHelper(h);
			acceptHelperInState2(h);
		}

		acceptHelperInState1(h);			// error
		acceptHelperInErrorState(h);		// error
    }

	public void testOk2() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
		} catch (Exception e) {
			acceptHelperInState1(h);
		} finally {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);
		}

		acceptHelperInState2(h);
    }

	public void testError3() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			useHelper(h);
		} catch (Exception e) {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);
			return;
		} finally {
			acceptHelperInState1(h);		// error: catch may throw exception, undefined state
			acceptHelperInState2(h);		// error
			acceptHelperInErrorState(h);	// error
		}

		acceptHelperInState1(h); 			// ok: dead catch
    }
}