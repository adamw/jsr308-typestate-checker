package checkers.typestate.ioexample;

import java.io.InputStream;
import java.io.IOException;

import static checkers.typestate.ioexample.InputStreamStates.*;

/**
 * Reading from stream with a recovery function.
 * @author Adam Warski (adam at warski dot org)
 */
public class Example2 {
	/**
	 * Reads the first byte of the input stream.
	 * @param stream Stream from which to read the byte.
	 * @param numberOfRetries Number of times, which the read should be retries, in case of exceptions.
	 * @return The first byte of the stream.
	 * @throws IOException When the first byte couldn't be read in the specified number of retries.
	 */
	public int readRetry(@Open InputStream stream, int numberOfRetries) throws IOException {
		try {
			// Looping and trying to read from the stream the specified number of times - 1.
			while (numberOfRetries > 1) {
				try {
					return stream.read();
				} catch (IOException e) {
				   	numberOfRetries--;

					// Recovering the stream and trying again.
					recover(stream);
				}
			}

			// Here the state of "stream" is "@Open" for sure:
			// - if no exception is thrown, the state doesn't change
			// - if one is thrown, then the stream is recovered

			// Trying to read one last time. If this throws an exception - the method will just throw it.
			return stream.read();
		} finally {
			stream.close();
		}
	}

	private void recover(@InputStreamError(after = Open.class, onException = InputStreamError.class) InputStream stream) throws IOException {
		// Do some magic.
	}
}