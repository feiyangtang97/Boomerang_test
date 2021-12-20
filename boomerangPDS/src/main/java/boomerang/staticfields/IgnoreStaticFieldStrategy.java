package boomerang.staticfields;

import boomerang.scene.ControlFlowGraph.Edge;
import boomerang.scene.StaticFieldVal;
import boomerang.scene.Val;
import java.util.Set;
import wpds.impl.Weight;
import wpds.interfaces.State;

public class IgnoreStaticFieldStrategy<W extends Weight> implements StaticFieldStrategy<W> {

  @Override
  public void handleForward(
      Edge storeStmt, Val storedVal, StaticFieldVal staticVal, Set<State> out) {}

  @Override
  public void handleBackward(
      Edge loadStatement, Val loadedVal, StaticFieldVal staticVal, Set<State> out) {}
}
