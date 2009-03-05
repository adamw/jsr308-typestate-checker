package work;

import checkers.typestate.State;
import checkers.typestate.NoChange;
import checkers.typestate.Any;

@State @interface State1 { Class<?> after() default NoChange.class; }
@State @interface State2 { Class<?> after() default NoChange.class; }
@State @interface State3 { Class<?> after() default NoChange.class; }

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class Test1 {
    public void test1() {
        Test2 obj = new Test2();
        obj.fromState1ToState2AndState2ToState3();
        obj.onlyInState2();

        obj.fromState1ToState2AndState2ToState3();
        obj.onlyInState3();
    }

    public void m1(@State1 Test2 param) { }
    public void m_trans(@State1(after = State2.class) @State2(after = State3.class) Test2 param) { }
    public void m2(@State2 Test2 param) { }
    public void m3(@State3 Test2 param) { }

    public void test2() {
        Test2 t = new Test2();
        m1(t);
        m_trans(t);
        m2(t);
        m_trans(t);
        m3(t);
    }

    public void anyToState3(@Any(after=State3.class) Test2 t) { }

    public void test3(Test2 t) {
        anyToState3(t);
        t.onlyInState3();
    }

    public static void main(String[] args) { }
}

class Test2 {
    public Test2() /*@State1*/ { }

    public void fromState1ToState2AndState2ToState3() /*@State1(after = State2.class)*/ /*@State2(after = State3.class)*/ { }

    public void fromState1ToState2() /*@State1(after = State2.class)*/ { }

    public void onlyInState2() /*@State2*/ { }

    public void onlyInState3() /*@State3*/ { }
}
