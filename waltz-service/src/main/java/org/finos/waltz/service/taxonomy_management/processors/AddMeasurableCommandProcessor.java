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

import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.measurable.ImmutableMeasurable;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.taxonomy_management.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.service.taxonomy_management.TaxonomyManagementUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.StringUtilities.mkSafe;

@Service
public class AddMeasurableCommandProcessor implements TaxonomyCommandProcessor {

    private final MeasurableService measurableService;


    @Autowired
    public AddMeasurableCommandProcessor(MeasurableService measurableService) {
        checkNotNull(measurableService, "measurableService cannot be null");
        this.measurableService = measurableService;
    }


    @Override
    public Set<TaxonomyChangeType> supportedTypes() {
        return asSet(
                TaxonomyChangeType.ADD_CHILD,
                TaxonomyChangeType.ADD_PEER);
    }


    @Override
    public EntityKind domain() {
        return EntityKind.MEASURABLE_CATEGORY;
    }


    public TaxonomyChangePreview preview(TaxonomyChangeCommand cmd) {
        doBasicValidation(cmd);
        Measurable m = validatePrimaryMeasurable(measurableService, cmd);

        return ImmutableTaxonomyChangePreview
                .builder()
                .command(ImmutableTaxonomyChangeCommand
                        .copyOf(cmd)
                        .withPrimaryReference(m.entityReference()))
                .build();
    }


    public TaxonomyChangeCommand apply(TaxonomyChangeCommand cmd, String userId) {
        doBasicValidation(cmd);
        validatePrimaryMeasurable(measurableService, cmd);

        Measurable primaryReference = measurableService.getById(cmd.primaryReference().id());

        Optional<Long> parentId = cmd.changeType() == TaxonomyChangeType.ADD_CHILD
                ? primaryReference.id()
                : primaryReference.parentId(); // peer

        Optional<String> externalParentId = parentId
                .map(measurableService::getById)
                .flatMap(ExternalIdProvider::externalId);

        Measurable newMeasurable = ImmutableMeasurable
                .builder()
                .categoryId(cmd.changeDomain().id())
                .externalId(Optional.ofNullable(getExternalIdParam(cmd)))
                .externalParentId(externalParentId)
                .parentId(parentId)
                .name(getNameParam(cmd))
                .description(mkSafe(getDescriptionParam(cmd)))
                .concrete(getConcreteParam(cmd, true))
                .lastUpdatedBy(userId)
                .lastUpdatedAt(DateTimeUtilities.nowUtc())
                .build();

        measurableService.create(newMeasurable, userId);

        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withLastUpdatedAt(DateTimeUtilities.nowUtc())
                .withLastUpdatedBy(userId)
                .withStatus(TaxonomyChangeLifecycleStatus.EXECUTED);
    }

}
