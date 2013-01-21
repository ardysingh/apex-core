/**
 * Copyright (c) 2012-2012 Malhar, Inc.
 * All rights reserved.
 */
package com.malhartech.api;

import com.malhartech.api.Operator.Unifier;

/**
 * Output ports are declared as annotated typed fields by the operator. The
 * operator processing logic simply calls emit on the port object. Output ports
 * also define how output from replicated operators is merged.
 *
 * @param <T> - Type of the object emitted by this port.
 */
public class DefaultOutputPort<T> implements Operator.OutputPort<T>
{
  private final Operator operator;
  private transient Sink<T> sink;

  public DefaultOutputPort(Operator operator)
  {
    this.operator = operator;
  }

  @Override
  final public Operator getOperator()
  {
    return operator;
  }

  /**
   * Emit the given object as a payload for downstream operators interested in this port.
   * @param tuple payload which needs to be emitted.
   */
  public void emit(T tuple)
  {
    sink.process(tuple);
  }

  /**
   * Called by execution engine to inject sink at deployment time.
   *
   * @param s
   */
  @Override
  final public void setSink(Sink<T> s)
  {
    this.sink = s;
  }

  /**
   * Opportunity for user code to check whether the port is connected, if
   * optional.
   *
   * @return true when connected, false otherwise.
   */
  public boolean isConnected()
  {
    return sink != null;
  }

  /**
   * Module developer can override for getUnifier functionality
   *
   * @return Unifier<T>
   */
  @Override
  public Unifier<T> getUnifier()
  {
    return null;
  }

}
