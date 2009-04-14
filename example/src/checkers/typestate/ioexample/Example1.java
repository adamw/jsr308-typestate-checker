package checkers.typestate.ioexample;

import java.io.InputStream;
import java.io.IOException;

import static checkers.typestate.ioexample.InputStreamStates.*;

/**
 * Cannot read from an input stream after it's closed.
 * @author Adam Warski (adam at warski dot org)
 */
public class Example1 {
	public int read(@Open InputStream stream) throws IOException {
		int ret = 0;
		try {
			int input;
			while ((input = stream.read()) != -1) {
				ret += input;
			}
		} finally {
			stream.close();
		}

		// This results in an error: the stream is in the "closed" state.
		ret += stream.read();

		return ret;
	}
}