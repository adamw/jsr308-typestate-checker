import checkers.nullness.quals.NonNull;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class NonNullOnParameterWithoutState {
    public static class Helper {
        public Helper() { }

		public void doNothing(@NonNull Object parameter) {
			System.out.println("parameter = " + parameter);
		}
    }

    public void testOk() {
        Helper h = new Helper();
        h.doNothing(null);
    }
}