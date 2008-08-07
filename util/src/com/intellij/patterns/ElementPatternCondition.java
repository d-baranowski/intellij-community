/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.patterns;

import com.intellij.util.SmartList;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author peter
 */
public class ElementPatternCondition<T> {
  private final InitialPatternCondition<T> myInitialCondition;
  private final List<PatternCondition<? super T>> myConditions = new SmartList<PatternCondition<? super T>>();

  public ElementPatternCondition(final InitialPatternCondition<T> startCondition) {
    myInitialCondition = startCondition;
  }

  public boolean accepts(@Nullable Object o, final ProcessingContext context) {
    if (!myInitialCondition.accepts(o, context)) return false;
    for (final PatternCondition<? super T> condition : myConditions) {
      if (!condition.accepts((T)o, context)) return false;
    }
    return true;
  }

  public final String toString() {
    StringBuilder builder = new StringBuilder();
    append(builder, "");
    return builder.toString();
  }

  public void append(StringBuilder builder, String indent) {
    myInitialCondition.append(builder, indent);
    final int conditionSize = myConditions.size();

    for (int i = 0; i < conditionSize; ++i) { // for each is slower
      final PatternCondition<? super T> condition = myConditions.get(i);
      condition.append(builder.append(".\n").append(indent), indent);
    }
  }

  public List<PatternCondition<? super T>> getConditions() {
    return myConditions;
  }

  public InitialPatternCondition<T> getInitialCondition() {
    return myInitialCondition;
  }

  public ElementPatternCondition<T> append(PatternCondition<? super T> condition) {
    final ElementPatternCondition<T> copy = new ElementPatternCondition<T>(myInitialCondition);
    copy.myConditions.addAll(myConditions);
    copy.myConditions.add(condition);
    return copy;
  }
}
