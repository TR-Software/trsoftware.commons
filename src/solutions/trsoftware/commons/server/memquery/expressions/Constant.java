/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 1/11/14
 */
public class Constant<T> extends TypedExpression<Void, T> implements VisitableExpression<Void, T> {

  private final T value;

  @SuppressWarnings("unchecked")
  public Constant(T value) {
    super(Void.class, (Class<T>)value.getClass());
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  public T apply(Void arg) {
    return value;
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visit(this);
  }

}
