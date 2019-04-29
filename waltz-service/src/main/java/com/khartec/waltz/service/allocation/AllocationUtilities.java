package com.khartec.waltz.service.allocation;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.ImmutableAllocation;
import com.khartec.waltz.model.allocation.MeasurablePercentage;
import com.khartec.waltz.model.allocation.MeasurablePercentageChange;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.CollectionUtilities.any;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.SetUtilities.map;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AllocationUtilities {

    private static final int ONE_HUNDRED_PERCENT = 100;

    public static class ValidationResult {
        private boolean failed = false;
        private String message= "";

        public boolean failed() {
            return failed;
        }

        public String message() {
            return message;
        }

        private ValidationResult addToMessage(String m) {
            message = message + " " + m;
            return this;
        }

        private void markAsFailed() {
            this.failed = true;
        }

    }

    public static ValidationResult validateAllocationChanges(Collection<Allocation> currentAllocations, Collection<MeasurablePercentageChange> changes) {

        ValidationResult result = new ValidationResult();

        Set<Long> currentMeasurableIds = map(currentAllocations, Allocation::measurableId);
        Set<Long> changedMeasurableIds = map(changes, c -> c.measurablePercentage().measurableId());

        int changeTotal = changes
                .stream()
                .mapToInt(c -> c.measurablePercentage().percentage())
                .sum();

        int residualTotal = currentAllocations
                .stream()
                .filter(a -> !changedMeasurableIds.contains(a.measurableId()))
                .mapToInt(Allocation::percentage)
                .sum();

        int updatedTotal = residualTotal + changeTotal;

        boolean hasNegatives = changes
                .stream()
                .anyMatch(c -> c.measurablePercentage().percentage() < 0);

        boolean operationsAreValid = changes
                .stream()
                .allMatch(c -> {
                    long changeMeasurableId = c.measurablePercentage().measurableId();
                    switch (c.operation()) {
                        case UPDATE:
                        case REMOVE:
                            return currentMeasurableIds.contains(changeMeasurableId);
                        case ADD:
                            return !currentMeasurableIds.contains(changeMeasurableId);
                        default:
                            return false;
                    }
                });

        if (updatedTotal > 100) {
            result.markAsFailed();
            result.addToMessage("Total cannot exceed 100%");
        }

        if (hasNegatives) {
            result.markAsFailed();
            result.addToMessage("Cannot contain percentages less than 0%");
        }

        if (!operationsAreValid) {
            result.markAsFailed();
            result.addToMessage("Operations do not match up with current state");
        }

        return result;
    }

    public static Collection<Allocation> calcAllocations(Collection<Allocation> currentAllocations,
                                                         Collection<MeasurablePercentage> fixedPercentagesToUpdate) {

        validateUpdate(currentAllocations, fixedPercentagesToUpdate);

        int perFloatPercentage = calculatePerFloatingPercentage(
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
                                .withPercentage(updateFixedPercentage.percentage());
                    } else {
                        return ImmutableAllocation
                                .copyOf(d)
                                .withPercentage(perFloatPercentage);
                    }
                });
    }


    private static int calculatePerFloatingPercentage(Collection<Allocation> currentAllocations, Collection<MeasurablePercentage> fixedPercentagesToUpdate) {
        int fixedTotal = calculateTotal(fixedPercentagesToUpdate);
        int numFloats = currentAllocations.size() - fixedPercentagesToUpdate.size();
        int floatingTotal = ONE_HUNDRED_PERCENT - fixedTotal;

        return (numFloats == 0)
            ? 0
            : floatingTotal / numFloats;
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

        if (any(fixedPercentagesToUpdate, d -> Math.signum(d.percentage()) == -1)) {
            throw new IllegalArgumentException("Cannot have negative fixed percentages");
        }

        int cmpTotalTo100 = Integer.compare(calculateTotal(fixedPercentagesToUpdate), ONE_HUNDRED_PERCENT);
        boolean hasFloats = currentAllocations.size() != fixedPercentagesToUpdate.size();

        if (cmpTotalTo100 > 0) {
            throw new IllegalArgumentException("Cannot exceed fixed total greater than 100");
        }

        if (! hasFloats && cmpTotalTo100 != 0) {
            throw new IllegalArgumentException("If no float then fixed total should equal 100");
        }

    }


    private static Integer calculateTotal(Collection<MeasurablePercentage> measurablePercentages){
        return measurablePercentages
                .stream()
                .collect(Collectors.summingInt(MeasurablePercentage::percentage));
    }

    public static List<BigDecimal> balance(List<BigDecimal> decimals, boolean allowIncomplete) {

        validateDecimals(decimals, allowIncomplete);
        List<BigDecimal> normalisedDecimals = normalise(decimals, allowIncomplete);

        return normalisedDecimals;
    }


    private static void validateDecimals(List<BigDecimal> decimals, boolean allowIncomplete) {
        Checks.checkNotNull(decimals, "List of decimals cannot be null");
        Checks.checkAll(decimals, Objects::nonNull, "List of decimals cannot contain nulls");
        Checks.checkAll(decimals, d -> d.signum() != -1, "List of decimals cannot contain negatives");
        Checks.checkTrue( allowIncomplete || decimals.size() != 0, "Empty list cannot be complete");  //why did the decimals.isEmpty() break the other tests?
    }


    public static List<BigDecimal> normalise(List<BigDecimal> decimals, boolean allowIncomplete){
        BigDecimal total = sum(decimals);

        if (total.compareTo(BigDecimal.valueOf(100)) <= 0 && allowIncomplete){
            return decimals;
        } else if (total.equals(BigDecimal.ZERO) && !allowIncomplete) {
            BigDecimal zeroCount = BigDecimal.valueOf(decimals.size());
            BigDecimal zeroTotal = BigDecimal.valueOf(100).divide(zeroCount, 10, RoundingMode.HALF_UP);
            return roundList(ListUtilities.map(decimals, d -> d.add(zeroTotal)));
        } else {
            BigDecimal multiplier = BigDecimal.valueOf(100).divide(total, 10, RoundingMode.HALF_UP);
            return roundList(ListUtilities.map(decimals, d -> d.multiply(multiplier)));
        }
    }


    public static BigDecimal sum(List<BigDecimal> list) {
        return list
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public static List<BigDecimal> roundList(List<BigDecimal> decimalList) {

        AtomicInteger offsetPtr =  new AtomicInteger(0);

        List<Tuple3<Integer, BigDecimal, BigDecimal>> decimalPartsList = decimalList
                .stream()
                .map(d -> tuple(offsetPtr.getAndIncrement()).concat(calcRemainder(d)))
                .collect(Collectors.toList());

        List<BigDecimal> quotients = ListUtilities.map(decimalPartsList, t -> t.v2);

        BigDecimal quotientSum = sum(quotients);
        int gap = 100 - quotientSum.intValue();
        if (gap == 0) {
            return quotients;
        } else {
            boolean isUnder = gap > 0;
            int sizeOfGap = Math.abs(gap);

            List<Tuple3<Integer, BigDecimal, BigDecimal>> decimalPartsOrderedByRemainder = decimalPartsList
                    .stream()
                    .sorted((a, b) -> {
                        int sortDirection = isUnder
                                ? -1  // high -> low
                                : 1;  // low -> high

                        int remainderComparison = a.v3.compareTo(b.v3) * sortDirection; // compare on remainder
                        return remainderComparison == 0
                                ? a.v2.compareTo(b.v2) * sortDirection // tie break on quotient
                                : remainderComparison;
                    })
                    .collect(Collectors.toList());

            Set<Integer> offsetsToAdjust = decimalPartsOrderedByRemainder
                    .stream()
                    .limit(sizeOfGap)
                    .map(t -> t.v1)
                    .collect(Collectors.toSet());

            BigDecimal adjustment = new BigDecimal(isUnder ? 1 : -1);

            return ListUtilities.map(
                    decimalPartsList,
                    t -> offsetsToAdjust.contains(t.v1)
                            ? t.v2.add(adjustment)
                            : t.v2);
        }

    }

    /**
     * @param d  the decimal to split into quotient and remainder
     * @return tuple  (quotient, remainder)
     */
    private static Tuple2<BigDecimal, BigDecimal> calcRemainder(BigDecimal d) {
        BigDecimal[] result = d.divideAndRemainder(BigDecimal.ONE);
        return tuple(
                result[0],
                result[1]);
    }

}
