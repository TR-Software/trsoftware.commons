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

package solutions.trsoftware.commons.shared.validation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.Collection;

import static solutions.trsoftware.commons.shared.validation.ValidationResult.success;

/**
 * Composes several {@link ValidationRule}s for the same field.
 *
 * @since Oct 24, 2007
 * @author Alex
 */
public class CompositeValidationRule<V> implements ValidationRule<V> {

  private final ImmutableList<ValidationRule<V>> rules;

  public CompositeValidationRule(ImmutableList<ValidationRule<V>> rules) {
    Preconditions.checkArgument(!CollectionUtils.isEmpty(rules), "no rules given");
    this.rules = rules;
  }

  public CompositeValidationRule(Collection<ValidationRule<V>> rules) {
    this(ImmutableList.copyOf(rules));
  }

  @SafeVarargs
  public CompositeValidationRule(ValidationRule<V>... rules) {
    this(ImmutableList.copyOf(rules));
  }

  /**
   * @return the first non-null name in {@link #rules}, or {@code null}
   */
  @Override
  public String getFieldName() {
    for (ValidationRule<V> rule : rules) {
      String fieldName = rule.getFieldName();
      if (fieldName != null)
        return fieldName;
    }
    return null;
  }

  @Override
  public ValidationResult validate(V value) {
    for (ValidationRule<V> rule : rules) {
      ValidationResult validationResult = rule.validate(value);
      if (!validationResult.isValid())
        return validationResult;
    }
    return success();
  }
}
