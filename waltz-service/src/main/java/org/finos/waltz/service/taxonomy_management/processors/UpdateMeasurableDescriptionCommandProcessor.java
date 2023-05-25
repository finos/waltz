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

import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.measurable_rating.MeasurableRatingService;
import org.finos.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.taxonomy_management.*;
import org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.findCurrentRatingMappings;

@Service
public class UpdateMeasurableDescriptionCommandProcessor implements TaxonomyCommandProcessor {

    private final MeasurableService measurableService;
    private final MeasurableRatingService measurableRatingService;


    @Autowired
    public UpdateMeasurableDescriptionCommandProcessor(MeasurableService measurableService,
                                                       MeasurableRatingService measurableRatingService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(measurableRatingService, "measurableRatingService cannot be null");
        this.measurableService = measurableService;
        this.measurableRatingService = measurableRatingService;
    }


    @Override
    public Set<TaxonomyChangeType> supportedTypes() {
        return asSet(TaxonomyChangeType.UPDATE_DESCRIPTION);
    }


    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        Measurable m = TaxonomyManagementUtilities.validatePrimaryMeasurable(measurableService, cmd);

        ImmutableTaxonomyChangePreview.Builder preview = ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withPrimaryReference(m.entityReference()));

        if (hasNoChange(m.description(), TaxonomyManagementUtilities.getDescriptionParam(cmd), "Description")) {
            return preview.build();
        }

        TaxonomyManagementUtilities.addToPreview(
                preview,
                findCurrentRatingMappings(measurableRatingService, cmd).size(),
                Severity.INFORMATION,
                "Current app mappings exist to item, these may be misleading if the description change alters the meaning of this item");


        return preview.build();
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        doBasicValidation(cmd);
        TaxonomyManagementUtilities.validatePrimaryMeasurable(measurableService, cmd);

        measurableService.updateDescription(
                cmd.primaryReference().id(),
                TaxonomyManagementUtilities.getDescriptionParam(cmd),
                userId);

        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedBy(userId)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED);
    }
}
