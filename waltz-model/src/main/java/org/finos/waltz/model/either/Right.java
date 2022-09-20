package org.finos.waltz.model.either;

import org.immutables.value.Value;

// -- RIGHT
@Value.Immutable
abstract class Right<L, R> extends Either<L, R> {

    @Override
    public L left() {
        throw new IllegalArgumentException("Cannot left() from a right");
    }

    @Override
    public abstract R right();

    public boolean isRight() { return true; };
    public boolean isLeft() { return false; };

}
