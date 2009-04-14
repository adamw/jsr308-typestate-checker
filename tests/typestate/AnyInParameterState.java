import checkers.typestate.State;
import checkers.typestate.NoChange;
import checkers.typestate.Any;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class AnyInParameterState {
    @State public static @interface State1 { public abstract Class<?> after() default NoChange.class; }
    @State public static @interface State2 { public abstract Class<?> after() default NoChange.class; }

    public static class Helper { public Helper() /*@State1*/ { } }

    public void acceptHelperInState1(@State1 Helper h) { }
    public void acceptHelperInState2(@State2 Helper h) { }
    public void transit(@Any(after=State2.class) Helper h) { }

    public void testOk() {
        Helper h = new Helper();
        transit(h);
        acceptHelperInState2(h);
    }

    public void testError1() {
        Helper h = new Helper();
        transit(h);
        acceptHelperInState1(h); // error
    }

    public void testError2() {
        Helper h = new Helper();
        acceptHelperInState2(h); // error
    }
}