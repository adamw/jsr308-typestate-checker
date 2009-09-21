import checkers.typestate.State;
import checkers.typestate.NoChange;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class BooleanMethodReceiverState {
	@State public static @interface State0 { public abstract Class<?> afterTrue() default NoChange.class; public abstract Class<?> afterFalse() default NoChange.class; }
    @State public static @interface State1 { }
    @State public static @interface State2 { }

    public static class Helper {
        public void onlyInState1() /*@State1*/ { }
        public void onlyInState2() /*@State2*/ { }

		public boolean modify() /*@State0(afterTrue=State1.class, afterFalse=State2.class)*/ { return true; }
    }

    public void testOk1(@State0 Helper h) {
        if (h.modify()) {
			h.onlyInState1();
		} else {
			h.onlyInState2();
		}
    }

	public void testOk2(@State0 Helper h) {
        if (h.modify() == true) {
			h.onlyInState1();
		} else {
			h.onlyInState2();
		}
    }

	public void testOk3(@State0 Helper h) {
        if (h.modify() == false) {
			h.onlyInState2();
		} else {
			h.onlyInState1();
		}
    }

	public void testOk4(@State0 Helper h) {
        if (h.modify() != true) {
			h.onlyInState2();
		} else {
			h.onlyInState1();
		}
    }

	public void testOk5(@State0 Helper h) {
        if (!(h.modify() == false)) {
			h.onlyInState1();
		} else {
			h.onlyInState2();
		}
    }

    public void testError(@State0 Helper h) {
        if (h.modify()) {
			h.onlyInState2();
		} else {
			h.onlyInState1();
		}
    }
}