package work;

import checkers.typestate.State;
import checkers.typestate.NoChange;
import checkers.typestate.Any;

import java.lang.annotation.Annotation;

@State @interface State1 { Class<?> after() default NoChange.class; Class<?> onException() default NoChange.class; }
@State @interface State2 { Class<?> after() default NoChange.class; Class<?> onException() default NoChange.class; }
@State @interface ErrorState { Class<?> after() default NoChange.class; }

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class Test1 {
	public void acceptInState1(@State1 Test2 o) { }
	public void acceptInState2(@State2 Test2 o) { }
	public void acceptInErrorState(@ErrorState Test2 o) { }

	public void throwException(@State1(after = State2.class, onException = ErrorState.class) Test2 o) { }

    public void test() {
		Test2 o = new Test2();
		try {
			acceptInState1(o);
			throwException(o);
			acceptInState2(o);
		} catch(Exception e) {
			acceptInErrorState(o);
			//return;
		}

		acceptInState2(o);
	}

    public static void main(String[] args) { }
}

class Test2 {
	public Test2() /*@State1*/ { }
}
