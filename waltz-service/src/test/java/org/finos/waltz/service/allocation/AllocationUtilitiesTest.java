/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.allocation;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.allocation.ImmutableAllocation;
import org.finos.waltz.model.allocation.ImmutableMeasurablePercentageChange;
import org.finos.waltz.model.allocation.MeasurablePercentageChange;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.service.allocation.AllocationUtilities.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.allocation.MeasurableRatingPercentage.mkMeasurableRatingPercentage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllocationUtilitiesTest {

    private static Allocation mkAllocation(long measurableRatingId, int percentage) {
        return ImmutableAllocation.builder()
                .schemeId(1L)
                .measurableRatingId(measurableRatingId)
                .percentage(percentage)
                .lastUpdatedBy("test")
                .build();
    }


    private static MeasurablePercentageChange mkChange(Operation op, long measurableRatingId, int percentage) {
        return ImmutableMeasurablePercentageChange.builder()
                .operation(op)
                .measurablePercentage(mkMeasurableRatingPercentage(measurableRatingId, percentage))
                .build();
    }


    @Test
    public void validChangesPass() {
        List<Allocation> current = asList(mkAllocation(1L, 50), mkAllocation(2L, 30));
        List<MeasurablePercentageChange> changes = asList(
                mkChange(Operation.UPDATE, 1L, 40),
                mkChange(Operation.ADD, 3L, 20),
                mkChange(Operation.REMOVE, 2L, 0));

        ValidationResult result = AllocationUtilities.validateAllocationChanges(current, changes);

        assertFalse(result.failed());
        assertEquals("", result.message());
    }


    @Test
    public void emptyChangesAgainstEmptyStatePass() {
        ValidationResult result = AllocationUtilities.validateAllocationChanges(
                Collections.emptyList(),
                Collections.emptyList());

        assertFalse(result.failed());
    }


    @Test
    public void totalExceedingOneHundredFails() {
        List<Allocation> current = asList(mkAllocation(1L, 60), mkAllocation(2L, 30));
        List<MeasurablePercentageChange> changes = asList(mkChange(Operation.UPDATE, 2L, 50));

        ValidationResult result = AllocationUtilities.validateAllocationChanges(current, changes);

        assertTrue(result.failed());
        assertTrue(result.message().contains("Total cannot exceed 100%"));
    }


    @Test
    public void residualAllocationsAreIncludedInTotal() {
        // unchanged allocation (1L / 60) plus new allocation (3L / 40) is exactly 100
        List<Allocation> current = asList(mkAllocation(1L, 60));
        List<MeasurablePercentageChange> changes = asList(mkChange(Operation.ADD, 3L, 40));

        assertFalse(AllocationUtilities.validateAllocationChanges(current, changes).failed());

        changes = asList(mkChange(Operation.ADD, 3L, 41));
        assertTrue(AllocationUtilities.validateAllocationChanges(current, changes).failed());
    }


    @Test
    public void negativePercentagesFail() {
        List<Allocation> current = asList(mkAllocation(1L, 50));
        List<MeasurablePercentageChange> changes = asList(mkChange(Operation.UPDATE, 1L, -5));

        ValidationResult result = AllocationUtilities.validateAllocationChanges(current, changes);

        assertTrue(result.failed());
        assertTrue(result.message().contains("Cannot contain percentages less than 0%"));
    }


    @Test
    public void updateOfUnknownRatingFails() {
        List<Allocation> current = asList(mkAllocation(1L, 50));
        List<MeasurablePercentageChange> changes = asList(mkChange(Operation.UPDATE, 99L, 10));

        ValidationResult result = AllocationUtilities.validateAllocationChanges(current, changes);

        assertTrue(result.failed());
        assertTrue(result.message().contains("Operations do not match up with current state"));
    }


    @Test
    public void removeOfUnknownRatingFails() {
        List<Allocation> current = asList(mkAllocation(1L, 50));
        List<MeasurablePercentageChange> changes = asList(mkChange(Operation.REMOVE, 99L, 0));

        assertTrue(AllocationUtilities.validateAllocationChanges(current, changes).failed());
    }


    @Test
    public void addOfExistingRatingFails() {
        List<Allocation> current = asList(mkAllocation(1L, 50));
        List<MeasurablePercentageChange> changes = asList(mkChange(Operation.ADD, 1L, 10));

        ValidationResult result = AllocationUtilities.validateAllocationChanges(current, changes);

        assertTrue(result.failed());
        assertTrue(result.message().contains("Operations do not match up with current state"));
    }


    @Test
    public void unsupportedOperationFails() {
        List<Allocation> current = asList(mkAllocation(1L, 50));
        List<MeasurablePercentageChange> changes = asList(mkChange(Operation.UNKNOWN, 1L, 10));

        assertTrue(AllocationUtilities.validateAllocationChanges(current, changes).failed());
    }


    @Test
    public void multipleFailuresAreAccumulatedInMessage() {
        List<Allocation> current = asList(mkAllocation(1L, 90));
        List<MeasurablePercentageChange> changes = asList(
                mkChange(Operation.ADD, 2L, 120),
                mkChange(Operation.ADD, 1L, -1));

        ValidationResult result = AllocationUtilities.validateAllocationChanges(current, changes);

        assertTrue(result.failed());
        assertTrue(result.message().contains("Total cannot exceed 100%"));
        assertTrue(result.message().contains("Cannot contain percentages less than 0%"));
        assertTrue(result.message().contains("Operations do not match up with current state"));
    }


    @Test
    public void mkBasicLogEntryPopulatesFields() {
        EntityReference ref = mkRef(EntityKind.APPLICATION, 42L);

        ChangeLog log = AllocationUtilities.mkBasicLogEntry(ref, "hello", "bob");

        assertEquals("hello", log.message());
        assertEquals(ref, log.parentReference());
        assertEquals(EntityKind.ALLOCATION, log.childKind().get());
        assertEquals(Operation.UPDATE, log.operation());
        assertEquals("bob", log.userId());
        assertEquals(Severity.INFORMATION, log.severity());
    }

}
