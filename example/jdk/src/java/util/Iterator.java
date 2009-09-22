package java.util;

import static checkers.typestate.iteratorexample.IteratorStates.*;

public interface Iterator<E> {
  public abstract boolean hasNext()
  	@CheckHasNext(afterTrue=ReadNext.class, afterFalse=NoNext.class)
  	@NoNext(after=NoNext.class)
  	@ReadNext;
  public abstract E next()
  	@ReadNext(after=CheckHasNext.class);
  public abstract void remove();
}
