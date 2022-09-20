package org.finos.waltz.model.either;

import java.util.function.Function;

public abstract class Either<L, R> {

    public static <L, R> Either<L, R> left(L left) {
        return ImmutableLeft
                .<L, R>builder()
                .left(left)
                .build();
    }

    public static <L, R> Either<L, R> right(R right) {
        return ImmutableRight
                .<L, R>builder()
                .right(right)
                .build();

    }

    public abstract R right();
    public abstract L left();
    public abstract boolean isRight();
    public abstract boolean isLeft();


    public <T> T map(Function<L, T> leftFn,
                     Function<R, T> rightFn) {
        if (isRight()) {
            return rightFn.apply(right());
        } else {
            return leftFn.apply(left());
        }
    }


}
