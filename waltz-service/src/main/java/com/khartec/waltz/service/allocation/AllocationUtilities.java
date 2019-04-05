package com.khartec.waltz.service.allocation;

import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.AllocationType;
import com.khartec.waltz.model.allocation.MeasurablePercentage;
import org.jooq.lambda.tuple.Tuple2;

import java.math.BigDecimal;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.CollectionUtilities.any;
import static com.khartec.waltz.common.CollectionUtilities.countWhere;
import static com.khartec.waltz.model.allocation.MeasurablePercentage.mkMeasurablePercentage;
import static java.util.stream.Collectors.reducing;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AllocationUtilities {

    private static final BigDecimal ONE_HUNDRED_PERCENT = BigDecimal.valueOf(100);


    public static Tuple2<AllocationType, MeasurablePercentage> fromAllocation(Allocation allocation) {
        return tuple(
                allocation.type(),
                mkMeasurablePercentage(allocation.measurableId(), allocation.percentage()));
    }


    public static boolean validateAllocationTuples(List<Tuple2<AllocationType, BigDecimal>> tuples) {

        if (anyNegatives(tuples)) {
            return false;
        }

        BigDecimal fixedTotal = calculateFixedTotal(tuples);

        if (hasFloats(tuples)) {
            return fixedTotal.compareTo(ONE_HUNDRED_PERCENT) <= 0;
        } else {
            return fixedTotal.equals(ONE_HUNDRED_PERCENT);
        }
    }


    public static BigDecimal calculateFloatingPercentage(List<Tuple2<AllocationType, BigDecimal>> allocations) {
        checkTrue(validateAllocationTuples(allocations), "Allocations failed validation");

        long numFloats = countFloats(allocations);

        BigDecimal floatingPoolPercentage = ONE_HUNDRED_PERCENT.subtract(calculateFixedTotal(allocations));
        BigDecimal floatPercentage = floatingPoolPercentage.divide(BigDecimal.valueOf(numFloats));

        return floatPercentage;
    }


    private static BigDecimal calculateFixedTotal(List<Tuple2<AllocationType, BigDecimal>> allocations){
        return allocations
                .stream()
                .filter(t -> t.v1 == AllocationType.FIXED)
                .map(t -> t.v2)
                .collect(reducing(BigDecimal.ZERO, (acc, d) -> acc.add(d)));
    }



    private static boolean anyNegatives(List<Tuple2<AllocationType, BigDecimal>> tuples) {
        return any(tuples, t -> t.v2.signum() == -1);
    }


    private static boolean hasFloats(List<Tuple2<AllocationType, BigDecimal>> tuples) {
        return any(tuples, t -> t.v1 == AllocationType.FLOATING);
    }


    private static long countFloats(List<Tuple2<AllocationType, BigDecimal>> allocations) {
        return countWhere(allocations, t -> t.v1 == AllocationType.FLOATING);
    }



}
