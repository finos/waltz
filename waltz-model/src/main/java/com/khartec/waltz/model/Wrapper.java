package com.khartec.waltz.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.immutables.value.Value;

/**
 * Wrapper classes are used to provide type safe alternatives to
 * basic types (i.e. strings).
 *
 * <br>
 *
 * See <a href="https://immutables.github.io/immutable.html#wrapper-types">
 * the immutables docs</a> for more info
 * @param <T>
 */
public abstract class Wrapper<T> {
  @Value.Parameter
  @JsonValue
  public abstract T value();
  @Override
  public String toString() {
    return getClass().getSimpleName() + "(" + value() + ")";
  }
}
