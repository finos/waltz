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

package com.khartec.waltz.service.taxonomy_management.processors;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.service.taxonomy_management.TaxonomyManagementUtilities.*;

@Service
public class MoveMeasurableCommandProcessor implements TaxonomyCommandProcessor {

    private final MeasurableService measurableService;


    @Autowired
    public MoveMeasurableCommandProcessor(MeasurableService measurableService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        this.measurableService = measurableService;
    }


    @Override
    public Set<TaxonomyChangeType> supportedTypes() {
        return SetUtilities.asSet(TaxonomyChangeType.MOVE);
    }


    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        Measurable primaryRef = validate(cmd);
        // can't think of any impacts that could be meaningfully calculated....
        return ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withPrimaryReference(primaryRef.entityReference()))
                .build();
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        Measurable measurableToMove = validate(cmd);
        TaxonomyChangeLifecycleStatus outcome = measurableToMove
                .id()
                .map(id -> measurableService
                    .updateParentId(
                        id,
                        getDestination(cmd),
                        userId))
                .map(success -> success
                        ? TaxonomyChangeLifecycleStatus.EXECUTED
                        : TaxonomyChangeLifecycleStatus.FAILED)
                .orElse(TaxonomyChangeLifecycleStatus.FAILED);

        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedBy(userId)
                .withStatus(outcome);
    }


    private Measurable validate(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        long categoryId = cmd.changeDomain().id();
        Measurable m = validateMeasurableInCategory(measurableService, cmd.primaryReference().id(), categoryId);
        Long destinationId = getDestination(cmd);
        if (destinationId != null) {
            validateMeasurableInCategory(measurableService, destinationId, categoryId);
        }
        return m;
    }


    private Long getDestination(TaxonomyChangeCommand cmd) {
        return cmd.paramAsLong("destinationId", null);
    }


    private String getDestinationName(TaxonomyChangeCommand cmd) {
        return cmd.param("destinationName");
    }

}
