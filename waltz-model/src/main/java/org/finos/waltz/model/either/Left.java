package org.finos.waltz.model.either;

import org.immutables.value.Value;

// -- LEFT
@Value.Immutable
abstract class Left<L, R> extends Either<L, R> {

    @Override
    public R right() {
        throw new IllegalArgumentException("Cannot right() from a left");
    }

    @Override
    public abstract L left();

    public boolean isRight() { return false; };
    public boolean isLeft() { return true; };
}
