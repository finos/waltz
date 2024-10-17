package org.finos.waltz.model;

import org.immutables.value.Value;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Value.Immutable
public abstract class PairDiffResult<A, B> {

    public abstract Collection<Tuple2<A, B>> allIntersection();
    public abstract Collection<B> otherOnly();
    public abstract Collection<A> waltzOnly();
    public abstract Collection<Tuple2<A, B>> differingIntersection();


    public static <A, B, K> PairDiffResult<A, B> mkPairDiff(Collection<A> waltzRecords,
                                                     Collection<B> otherRecords,
                                                     Function<A, K> aKeyFn,
                                                     Function<B, K> bKeyFn,
                                                     BiPredicate<A, B> equalityPredicate) {
        Map<K, A> waltzByKey = indexBy(waltzRecords, aKeyFn);
        Map<K, B> othersByKey = indexBy(otherRecords, bKeyFn);

        Set<B> otherOnly = otherRecords.stream()
                .filter(f -> !waltzByKey.containsKey(bKeyFn.apply(f)))
                .collect(Collectors.toSet());

        Set<A> waltzOnly = waltzRecords.stream()
                .filter(f -> !othersByKey.containsKey(aKeyFn.apply(f)))
                .collect(Collectors.toSet());

        Set<Tuple2<A, B>> intersect = otherRecords.stream()
            .map(other -> tuple(
                waltzByKey.get(bKeyFn.apply(other)),
                other))
            .filter(t -> t.v1 != null)
            .collect(Collectors.toSet());

        Set<Tuple2<A, B>> differingIntersection = intersect.stream()
                .filter(t -> ! equalityPredicate.test(t.v1, t.v2))
                .collect(Collectors.toSet());

        return ImmutablePairDiffResult
                .<A, B>builder()
                .otherOnly(otherOnly)
                .waltzOnly(waltzOnly)
                .allIntersection(intersect)
                .differingIntersection(differingIntersection)
                .build();
    }

    @Override
    public String toString() {
        return String.format(
            "%s - [Intersection: %s records, Other: %s records, Waltz: %s records, Differing Intersection: %s records]",
            getClass().getName(),
            allIntersection().size(),
            otherOnly().size(),
            waltzOnly().size(),
            differingIntersection().size());
    }
}
