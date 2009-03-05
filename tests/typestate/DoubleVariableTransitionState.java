import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class DoubleVariableTransitionState {
    @State public static @interface State1 { Class<?> after() default NoChange.class; }
    @State public static @interface State2 { Class<?> after() default NoChange.class; }
    @State public static @interface State3 { Class<?> after() default NoChange.class; }

    public static class Helper {
        public Helper() /*@State1*/ { }
    }

    public void acceptHelperInState1(@State1 Helper h) { }
    public void acceptHelperInState2(@State2 Helper h) { }
    public void acceptHelperInState3(@State3 Helper h) { }
    public void transit(@State1(after=State2.class) @State2(after=State3.class) Helper h) { }

    public void testOk() {
        Helper h = new Helper();
        acceptHelperInState1(h);
        transit(h);
        acceptHelperInState2(h);
        transit(h);
        acceptHelperInState3(h);
    }

    public void testError1() {
        Helper h = new Helper();
        acceptHelperInState1(h);
        transit(h);
        acceptHelperInState2(h);
        transit(h);
        acceptHelperInState2(h); // error
    }

    public void testError2() {
        Helper h = new Helper();
        acceptHelperInState1(h);
        transit(h);
        acceptHelperInState3(h); // error
    }

    public void testError3() {
        Helper h = new Helper();
        transit(h);
        transit(h);
        transit(h); // error
    }
}
