package com.khartec.waltz.service.allocation;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.AllocationType;
import com.khartec.waltz.model.allocation.ImmutableAllocation;
import com.khartec.waltz.model.allocation.MeasurablePercentage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.CollectionUtilities.any;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.SetUtilities.map;

public class AllocationUtilities {

    private static final BigDecimal ONE_HUNDRED_PERCENT = BigDecimal.valueOf(100);


    public static Collection<Allocation> calcAllocations(Collection<Allocation> currentAllocations,
                                                         Collection<MeasurablePercentage> fixedPercentagesToUpdate) {

        validateUpdate(currentAllocations, fixedPercentagesToUpdate);

        BigDecimal perFloatPercentage = calculatePerFloatingPercentage(
                currentAllocations,
                fixedPercentagesToUpdate);

        Map<Long, MeasurablePercentage> fixedPercentagesByMeasurable = indexBy(
                d -> d.measurableId(),
                fixedPercentagesToUpdate);

        return CollectionUtilities.map(
                currentAllocations,
                d -> {
                    MeasurablePercentage updateFixedPercentage = fixedPercentagesByMeasurable.get(d.measurableId());
                    if (updateFixedPercentage != null) {
                        return ImmutableAllocation
                                .copyOf(d)
                                .withPercentage(updateFixedPercentage.percentage())
                                .withType(AllocationType.FIXED);
                    } else {
                        return ImmutableAllocation
                                .copyOf(d)
                                .withPercentage(perFloatPercentage)
                                .withType(AllocationType.FLOATING);
                    }
                });
    }


    private static BigDecimal calculatePerFloatingPercentage(Collection<Allocation> currentAllocations, Collection<MeasurablePercentage> fixedPercentagesToUpdate) {
        BigDecimal fixedTotal = calculateTotal(fixedPercentagesToUpdate);
        int numFloats = currentAllocations.size() - fixedPercentagesToUpdate.size();
        BigDecimal floatingTotal = BigDecimal.valueOf(100).subtract(fixedTotal);

        return (numFloats == 0)
            ? BigDecimal.ZERO
            : floatingTotal.divide(BigDecimal.valueOf(numFloats), 3, RoundingMode.HALF_EVEN);
    }


    private static void validateUpdate(Collection<Allocation> currentAllocations, Collection<MeasurablePercentage> fixedPercentagesToUpdate) {

        if (currentAllocations.size() < fixedPercentagesToUpdate.size()) {
            throw new IllegalArgumentException();
        }

        if (currentAllocations.isEmpty()) {
            // no point checking anything else
            return;
        }

        Set<Long> allMeasurableIds = map(currentAllocations, Allocation::measurableId);
        Set<Long> allFixedIds = map(fixedPercentagesToUpdate, MeasurablePercentage::measurableId);

        if (allMeasurableIds.size() != currentAllocations.size()) {
            throw new IllegalArgumentException("Duplicate allocations detected in current state");
        }

        if (allFixedIds.size() != fixedPercentagesToUpdate.size()) {
            throw new IllegalArgumentException("Duplicate allocations detected in target state");
        }

        if (! allMeasurableIds.containsAll(allFixedIds)) {
            throw new IllegalArgumentException("Not all fixed Ids contained within current allocations");
        }

        if (any(fixedPercentagesToUpdate, d -> d.percentage().signum() == -1)) {
            throw new IllegalArgumentException("Cannot have negative fixed percentages");
        }

        int cmpTotalTo100 = calculateTotal(fixedPercentagesToUpdate).compareTo(ONE_HUNDRED_PERCENT);
        boolean hasFloats = currentAllocations.size() != fixedPercentagesToUpdate.size();

        if (cmpTotalTo100 > 0) {
            throw new IllegalArgumentException("Cannot exceed fixed total greater than 100");
        }

        if (! hasFloats && cmpTotalTo100 != 0) {
            throw new IllegalArgumentException("If no float then fixed total should equal 100");
        }

    }


    private static BigDecimal calculateTotal(Collection<MeasurablePercentage> measurablePercentages){
        return measurablePercentages
                .stream()
                .map(MeasurablePercentage::percentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
