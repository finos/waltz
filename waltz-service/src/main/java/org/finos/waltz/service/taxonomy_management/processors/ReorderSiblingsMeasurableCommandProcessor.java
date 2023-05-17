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

package org.finos.waltz.service.taxonomy_management.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.taxonomy_management.*;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.validateMeasurableInCategory;
import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.validateMeasurablesInCategory;

@Service
public class ReorderSiblingsMeasurableCommandProcessor implements TaxonomyCommandProcessor {

    private final MeasurableService measurableService;


    @Autowired
    public ReorderSiblingsMeasurableCommandProcessor(MeasurableService measurableService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        this.measurableService = measurableService;
    }


    @Override
    public Set<TaxonomyChangeType> supportedTypes() {
        return SetUtilities.asSet(TaxonomyChangeType.REORDER_SIBLINGS);
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
        Measurable primaryMeasurable = validate(cmd);
        TaxonomyChangeLifecycleStatus outcome = measurableService.reorder(primaryMeasurable.categoryId(), getReorderedListOfIds(cmd), userId)
                ? TaxonomyChangeLifecycleStatus.EXECUTED
                : TaxonomyChangeLifecycleStatus.FAILED;

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
        List<Long> idList = getReorderedListOfIds(cmd);
        if (CollectionUtilities.notEmpty(idList)) {
            validateMeasurablesInCategory(measurableService, idList, categoryId);
        }
        return m;
    }


    private List<Long> getReorderedListOfIds(TaxonomyChangeCommand cmd) {
        try {
            return cmd.paramAsLongList("list");
        } catch (JsonProcessingException jpe) {
            LOG.warn("Could not parse list from cmd: {}", cmd);
            return Collections.emptyList();
        }
    }


}
