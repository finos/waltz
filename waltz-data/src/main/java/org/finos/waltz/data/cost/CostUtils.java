package org.finos.waltz.data.cost;

import org.finos.waltz.common.StreamUtilities;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.ImmutableMeasurableCostEntry;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.MeasurableCostEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.CollectionUtilities.filter;
import static org.finos.waltz.common.CollectionUtilities.map;
import static org.finos.waltz.common.CollectionUtilities.sumInts;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;

public class CostUtils {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);


    public static Set<MeasurableCostEntry> calculateAllocatedCosts(Collection<MeasurableCostEntry> costsForMeasurable,
                                                                   Map<Long, Collection<MeasurableCostEntry>> costDataByAppId) {

        StreamUtilities.Siphon<MeasurableCostEntry> noCostsSiphon = mkSiphon(mce -> mce.overallCost() == null);
        StreamUtilities.Siphon<MeasurableCostEntry> noAllocationSiphon = mkSiphon(mce -> mce.allocationPercentage() == null);

        Set<MeasurableCostEntry> explicitCosts = costsForMeasurable
                .stream()
                .filter(noCostsSiphon)
                .filter(noAllocationSiphon)
                .map(d -> {
                    BigDecimal allocPercentage = BigDecimal.valueOf(d.allocationPercentage());

                    BigDecimal allocatedCost = allocPercentage
                            .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                            .multiply(d.overallCost());

                    return ImmutableMeasurableCostEntry.copyOf(d).withAllocatedCost(allocatedCost);
                })
                .collect(toSet());

        Set<MeasurableCostEntry> implicitShareOfCost = noAllocationSiphon
                .stream()
                .map(d -> {
                    long appId = d.appId();
                    Collection<MeasurableCostEntry> otherRatings = costDataByAppId.getOrDefault(appId, emptySet());
                    if (otherRatings.size() == 1) {
                        // only one rating, therefore entire cost is on this measurable
                        return ImmutableMeasurableCostEntry
                                .copyOf(d)
                                .withAllocationPercentage(100)
                                .withAllocatedCost(d.overallCost());
                    } else {
                        Collection<MeasurableCostEntry> othersWithAllocations = filter(otherRatings, r -> r.allocationPercentage() != null);
                        if (othersWithAllocations.isEmpty()) {
                            // no other allocations (but more than 1 rating), therefore share the total out equally
                            BigDecimal otherRatingsCount = BigDecimal.valueOf(otherRatings.size());

                            BigDecimal remainingCost = d.overallCost()
                                    .divide(otherRatingsCount, 2, RoundingMode.HALF_UP);

                            return ImmutableMeasurableCostEntry
                                    .copyOf(d)
                                    .withAllocatedCost(remainingCost)
                                    .withAllocationPercentage(100 / otherRatings.size());
                        } else {
                            Long overallAllocatedPercentage = sumInts(map(othersWithAllocations, MeasurableCostEntry::allocationPercentage));
                            BigDecimal unallocatedPercentage = BigDecimal.valueOf(100 - overallAllocatedPercentage);
                            BigDecimal numUnallocatedRatings = BigDecimal.valueOf(otherRatings.size() - othersWithAllocations.size());

                            BigDecimal shareOfUnallocated = unallocatedPercentage
                                    .divide(numUnallocatedRatings, 2,
                                            RoundingMode.HALF_UP);

                            BigDecimal shareOfRemainingCost = shareOfUnallocated
                                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                                    .multiply(d.overallCost());

                            return ImmutableMeasurableCostEntry
                                    .copyOf(d)
                                    .withAllocationPercentage(shareOfUnallocated.intValue())
                                    .withAllocatedCost(shareOfRemainingCost);
                        }
                    }
                })
                .collect(toSet());

        return union(explicitCosts, implicitShareOfCost);
    }
}
