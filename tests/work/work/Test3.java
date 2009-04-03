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
	public void recover(@ErrorState(after = State2.class) Helper h) { }

    public void testOk3() {
		Helper h = new Helper();
        try {
			acceptHelperInState1(h);
			transitHelper(h);
			acceptHelperInState2(h);
		} catch (RuntimeException e) {
			recover(h);
			return;
		} catch (Exception e) {
			acceptHelperInErrorState(h);
			return;
		}

		acceptHelperInState2(h);			// ok - one catch is dead, the other one transits
    }
}