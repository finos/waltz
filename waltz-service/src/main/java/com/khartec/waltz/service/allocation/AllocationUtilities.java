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

package com.khartec.waltz.service.allocation;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.MeasurablePercentageChange;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;

import java.util.Collection;
import java.util.Set;

import static com.khartec.waltz.common.SetUtilities.map;

public class AllocationUtilities {

    public static class ValidationResult {
        private boolean failed = false;
        private String message= "";

        public boolean failed() {
            return failed;
        }

        public String message() {
            return message;
        }

        private void addToMessage(String m) {
            message = message + " " + m;
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


    public static ChangeLog mkBasicLogEntry(EntityReference ref, String message, String userId) {
        return ImmutableChangeLog.builder()
                .message(message)
                .parentReference(ref)
                .childKind(EntityKind.ALLOCATION_SCHEME) // ideally would be 'ALLOCATION' but that does not have an 'id'
                .operation(Operation.UPDATE)
                .userId(userId)
                .severity(Severity.INFORMATION)
                .build();
    }


}
