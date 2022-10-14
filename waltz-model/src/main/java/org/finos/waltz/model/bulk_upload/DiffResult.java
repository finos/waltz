package org.finos.waltz.model.bulk_upload;

import org.finos.waltz.common.MapUtilities;
import org.immutables.value.Value;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class DiffResult<T> {

    public abstract Collection<T> intersection();

    public abstract Collection<T> otherOnly();

    public abstract Collection<T> waltzOnly();

    public abstract Collection<T> differingIntersection();

    public static <Z> DiffResult<Z> mkDiff(Collection<Z> waltzRecords,
                                           Collection<Z> otherRecords) {
        return mkDiff(
                waltzRecords,
                otherRecords,
                Function.identity(),
                Object::equals);
    }


    public static <Z, K> DiffResult<Z> mkDiff(Collection<Z> waltzRecords,
                                              Collection<Z> otherRecords,
                                              Function<Z, K> keyFn,
                                              BiPredicate<Z, Z> equalityPredicate) {
        Map<K, Z> waltzByKey = MapUtilities.indexBy(waltzRecords, keyFn);
        Map<K, Z> othersByKey = MapUtilities.indexBy(otherRecords, keyFn);

        Set<Z> otherOnly = otherRecords.stream()
                .filter(f -> !waltzByKey.containsKey(keyFn.apply(f)))
                .collect(Collectors.toSet());

        Set<Z> waltzOnly = waltzRecords.stream()
                .filter(f -> !othersByKey.containsKey(keyFn.apply(f)))
                .collect(Collectors.toSet());

        Set<Z> intersect = otherRecords.stream()
                .filter(f -> waltzByKey.containsKey(keyFn.apply(f)))
                .collect(Collectors.toSet());

        // non equal intersection
        BiPredicate<Z, Z> areEqual = equalityPredicate == null
                ? (si1, si2) -> si1.equals(si2)
                : equalityPredicate;

        Set<Z> differingIntersection = intersect.stream()
                .filter(f -> {
                    Z item = waltzByKey.get(keyFn.apply(f));
                    boolean different = !areEqual.test(f, item);
                    return different;
                })
                .collect(Collectors.toSet());

        return ImmutableDiffResult
                .<Z>builder()
                .otherOnly(otherOnly)
                .waltzOnly(waltzOnly)
                .intersection(intersect)
                .differingIntersection(differingIntersection)
                .build();
    }

    @Override
    public String toString() {
        String s = String.format("%s - [Intersection: %s records, Other: %s records, Waltz: %s records, Differing Intersection: %s records]",
                getClass().getName(),
                intersection().size(),
                otherOnly().size(),
                waltzOnly().size(),
                differingIntersection().size());
        return s;
    }
}
