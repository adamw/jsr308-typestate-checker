package java.io;

import checkers.typestate.*;

import static checkers.typestate.ioexample.InputStreamStates.*;

public abstract class InputStream implements Closeable {
  public InputStream() { throw new RuntimeException("skeleton method"); }
  public abstract int read() /*@Open(onException=InputStreamError.class)*/ throws java.io.IOException;
  public int read(byte[] a1) /*@Open(onException=InputStreamError.class)*/ throws java.io.IOException { throw new RuntimeException("skeleton method"); }
  public int read(byte[] a1, int a2, int a3) /*@Open(onException=InputStreamError.class)*/ throws java.io.IOException { throw new RuntimeException("skeleton method"); }
  public long skip(long a1) /*@Open(onException=InputStreamError.class)*/ throws java.io.IOException { throw new RuntimeException("skeleton method"); }
  public int available() /*@Open(onException=InputStreamError.class)*/ throws java.io.IOException { throw new RuntimeException("skeleton method"); }
  public void close() /*@Any(after=Closed.class)*/ throws java.io.IOException { throw new RuntimeException("skeleton method"); }
  public synchronized void mark(int a1) { throw new RuntimeException("skeleton method"); }
  public synchronized void reset() /*@Open(onException=InputStreamError.class)*/ throws java.io.IOException { throw new RuntimeException("skeleton method"); }
  public boolean markSupported() { throw new RuntimeException("skeleton method"); }
}
