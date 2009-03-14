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
	
	public void transitToState(@Any(after=State3.class) Helper h) { }

    public void acceptHelperInAnyExceptState3(@Any(except=State3.class, after=State2.class) Helper h) { }
	public void acceptHelperInState2(@State2 Helper h) { }
	public void acceptHelperInState3(@State3 Helper h) { }

    public void testError1() {
        Helper h = new Helper();
		transitToState(h);
        acceptHelperInAnyExceptState3(h); // error
		acceptHelperInState2(h);
    }
}

// TODO: fix state annotations detector, to also detect annotations in annotation element values
// Right now, if an annotation is not used on an element directly, it won't be detected