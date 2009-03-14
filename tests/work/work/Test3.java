package work;

import checkers.typestate.State;
import checkers.typestate.NoChange;
import checkers.typestate.Any;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class Test3 {
	@State public static @interface State1 { public abstract Class<?> after() default NoChange.class; }
    @State public static @interface State2 { public abstract Class<?> after() default NoChange.class; }
	@State public static @interface State3 { public abstract Class<?> after() default NoChange.class; }

    public static class Helper { }

	public void transitToState1(@Any(after=State1.class) Helper h) { }
	public void transitToState2(@Any(after=State2.class) Helper h) { }
	public void transitToState3(@Any(after=State3.class) Helper h) { }

    public void acceptHelperInAnyExceptState3(@Any(except=State3.class) Helper h) { }

    public void testOk1() {
        Helper h = new Helper();
		transitToState1(h);
        acceptHelperInAnyExceptState3(h);
    }

	public void testOk2() {
        Helper h = new Helper();
		transitToState2(h);
        acceptHelperInAnyExceptState3(h);
    }

    public void testError1() {
        Helper h = new Helper();
		transitToState3(h);
        acceptHelperInAnyExceptState3(h); // error
    }
}