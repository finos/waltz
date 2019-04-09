package com.khartec.waltz.service.allocation;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.allocation.*;
import org.jooq.lambda.function.Consumer3;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;

import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.model.allocation.AllocationType.FIXED;
import static com.khartec.waltz.model.allocation.AllocationType.FLOATING;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class AllocationUtilitiesTest {


    private static final BigDecimal _100 = BigDecimal.valueOf(100) ;
    private static final BigDecimal _neg100 = BigDecimal.valueOf(-100) ;
    private static final BigDecimal _150 = BigDecimal.valueOf(150) ;
    private static final BigDecimal _50 = BigDecimal.valueOf(50) ;
    private static final BigDecimal _20 = BigDecimal.valueOf(20) ;
    private static final BigDecimal _10 = BigDecimal.valueOf(10) ;
    private static final BigDecimal _0 = BigDecimal.ZERO ;

    static Allocation mkAlloc(AllocationType type, Long measurableId, BigDecimal percentage) {
        return ImmutableAllocation.builder()
                .entityReference(EntityReference.mkRef(EntityKind.APPLICATION, 1))
                .percentage(percentage)
                .type(type)
                .measurableId(measurableId)
                .schemeId(1)
                .lastUpdatedBy("test")
                .build();
    }

    static MeasurablePercentage mkMeasurablePercentage(Long measurableId, BigDecimal percentage) {
        return ImmutableMeasurablePercentage.builder()
                .percentage(percentage)
                .measurableId(measurableId)
                .build();
    }

    @Test
    public void emptyAllocationsGivesEmptyListToSave(){
        Collection<Allocation> allocationsToSave = AllocationUtilities.calcAllocations(
                emptyList(),
                emptyList());
        assertTrue(allocationsToSave.isEmpty());
    }



    Consumer3<Runnable, String, String> assertThrows = (runnable, message, matchingText) -> {
        try {
            runnable.run();
            fail(message);
        } catch (Exception e) {
            if (matchingText != null) {
                assertTrue(
                        "Exception message should have contained: " + matchingText + " but was: "+e.getMessage(),
                        e.getMessage().contains(matchingText));
            }
        }
    };


    @Test
    public void cannotIntroduceNewMeasurable() {
        assertThrows.accept(
                () -> AllocationUtilities.calcAllocations(
                        asList(mkAlloc(FLOATING, 1L, _100)),
                        asList(mkMeasurablePercentage(2L, _100))),
                "Should have reported that measurable 2 is not in the current list",
                null);

        assertThrows.accept(
                () -> AllocationUtilities.calcAllocations(
                        emptyList(),
                        asList(mkMeasurablePercentage(2L, _100))),
                "Should have reported that there are more measurables to save than exist",
                null);
    }

    @Test
    public void cannotHaveNegativeFixedPercentages() {
        assertThrows.accept(
                () -> AllocationUtilities.calcAllocations(
                        asList(mkAlloc(FLOATING, 1L, _100)),
                        asList(mkMeasurablePercentage(1L, _neg100))),
                "Should have reported that fixed percentage is negative",
                "negative");

    }

    @Test
    public void totalOfFixedCannotExceed100() {
        assertThrows.accept(
                () -> AllocationUtilities.calcAllocations(
                        asList(mkAlloc(FLOATING, 1L, _100),
                                mkAlloc(FIXED, 2L, _0)),
                        asList(mkMeasurablePercentage(1L, _50),
                                mkMeasurablePercentage(2L, _100))),
                "Should have reported that fixed total exceeds 100",
                "exceed");
    }

    @Test
    public void mustTotal100IfNoFloats() {
        assertThrows.accept(
                () -> AllocationUtilities.calcAllocations(
                        asList(mkAlloc(FIXED, 1L, _0)),
                        asList(mkMeasurablePercentage(1L, _50))),
                "Should have reported that fixed total does not sum to 100 as there are no floats to mop up the shortfall",
                "no float");
    }

}