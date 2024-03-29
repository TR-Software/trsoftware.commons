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

import java.util.function.Function;

/**
 * @param <A> the argument type of the expression. If the expression takes more than 1 arg (e.g. binary expression),
 * the argument type might be {@code Pair<X, Y>}.
 * @param <R> the result type of the expression.
 *
 * @author Alex, 1/11/14
 */
public interface Expression<A, R> extends Function<A, R> {

  Class<R> getResultType();

  Class<A> getArgType();

}
