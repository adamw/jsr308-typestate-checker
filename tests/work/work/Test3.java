package work;

import checkers.nullness.quals.NonNull;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class Test3 {
	public void test(@NonNull String parameter) {
		System.out.println("parameter = " + parameter);
	}

	public static void main(String[] args) {
		System.out.println("Hello World!");

		Object a = new Object();

		new Test3().test("a");
		//new Main2().test2(null, null);
	}
}