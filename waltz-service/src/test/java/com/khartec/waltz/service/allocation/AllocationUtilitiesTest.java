package com.khartec.waltz.service.allocation;

import com.khartec.waltz.model.allocation.AllocationType;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.math.BigDecimal;

import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.model.allocation.AllocationType.FIXED;
import static com.khartec.waltz.model.allocation.AllocationType.FLOATING;
import static junit.framework.TestCase.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AllocationUtilitiesTest {

    final Tuple2<AllocationType, BigDecimal> fx110 = tuple(FIXED, BigDecimal.valueOf(110));
    final Tuple2<AllocationType, BigDecimal> fx100 = tuple(FIXED, BigDecimal.valueOf(100));
    final Tuple2<AllocationType, BigDecimal> fx50 = tuple(FIXED, BigDecimal.valueOf(50));
    final Tuple2<AllocationType, BigDecimal> fx25 = tuple(FIXED, BigDecimal.valueOf(25));
    final Tuple2<AllocationType, BigDecimal> fx0 = tuple(FIXED, BigDecimal.ZERO);
    final Tuple2<AllocationType, BigDecimal> fxNeg100 = tuple(FIXED, BigDecimal.valueOf(-100));
    final Tuple2<AllocationType, BigDecimal> fxNeg10 = tuple(FIXED, BigDecimal.valueOf(-10));
    final Tuple2<AllocationType, BigDecimal> anyFloat = tuple(FLOATING, BigDecimal.valueOf(25));

    @Test
    public void allocationsCannotBeEmpty(){
        assertFalse(AllocationUtilities.validateAllocationTuples(asList()));
    }

    @Test
    public void fixedAllocationsCannotExceed100Percent(){
        assertFalse(AllocationUtilities.validateAllocationTuples(asList(fx110)));
    }

    @Test
    public void fixedAllocationsCannotBeNegative(){
        assertFalse(AllocationUtilities.validateAllocationTuples(asList(fx110, fxNeg10)));
        assertFalse(AllocationUtilities.validateAllocationTuples(asList(fxNeg100)));
        assertFalse(AllocationUtilities.validateAllocationTuples(asList(fxNeg10, anyFloat)));
    }

    @Test
    public void fixedAllocationsCanEqual100Percent(){
        assertTrue("1 fixed at 100 should be ok", AllocationUtilities.validateAllocationTuples(asList(fx100)));
        assertTrue("2 fixed at 50 each should be okay", AllocationUtilities.validateAllocationTuples(asList(fx50, fx50)));
        assertTrue("2 fixed at 50 each and a float should be okay", AllocationUtilities.validateAllocationTuples(asList(fx50, fx50, anyFloat)));
    }

    @Test
    public void fixedAllocationsMustTotal100PercentIfNoFloatsGiven(){
        assertFalse(AllocationUtilities.validateAllocationTuples(asList(fx50, fx25)));
    }

    @Test
    public void fixedAllocationsCanTotalLessThan100PercentIfFloatsGiven(){
        assertTrue(AllocationUtilities.validateAllocationTuples(asList(fx50, fx25, anyFloat)));
    }

    @Test
    public void calcFloat() {
        assertEquals(BigDecimal.valueOf(100), AllocationUtilities.calculateFloatingPercentage(asList(anyFloat)));
        assertEquals(BigDecimal.valueOf(100), AllocationUtilities.calculateFloatingPercentage(asList(fx0, anyFloat)));
        assertEquals(BigDecimal.valueOf(50), AllocationUtilities.calculateFloatingPercentage(asList(fx50, anyFloat)));
        assertEquals(BigDecimal.valueOf(25), AllocationUtilities.calculateFloatingPercentage(asList(fx50, anyFloat, anyFloat)));
        assertEquals(BigDecimal.valueOf(0), AllocationUtilities.calculateFloatingPercentage(asList(fx100, anyFloat)));
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotCalculateFloatForNoAllocations() {
        AllocationUtilities.calculateFloatingPercentage(asList());
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotCalculateFloatForAllocationsWithNoFloatWhereFixedIsLessThan100() {
        AllocationUtilities.calculateFloatingPercentage(asList(fx50));
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotCalculateFloatForFixedAllocationsGreaterThan100() {
        AllocationUtilities.calculateFloatingPercentage(asList(fx50, fx100));
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotCalculateFloatForFixedAllocationsGreaterThan100EvenIfYouHaveAFloat() {
        AllocationUtilities.calculateFloatingPercentage(asList(anyFloat, fx50, fx100));
    }

}