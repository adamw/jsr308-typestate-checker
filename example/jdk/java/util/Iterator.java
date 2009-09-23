package java.util;

import static checkers.typestate.iteratorexample.IteratorStates.*;

public interface Iterator<E> {
  public abstract boolean hasNext()
  	@CheckHasNext(afterTrue=ReadNext.class)
  	@ReadNext;
  public abstract E next()
  	@ReadNext(after=CheckHasNext.class);
  public abstract void remove();
}
