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
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.measurable.ImmutableMeasurable;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.taxonomy_management.*;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.taxonomy_management.TaxonomyCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.service.taxonomy_management.TaxonomyManagementUtilities.*;

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
                : primaryReference.parentId();

        Measurable newMeasurable = ImmutableMeasurable
                .builder()
                .categoryId(cmd.changeDomain().id())
                .parentId(parentId)
                .name(getNameParam(cmd))
                .description(mkSafe(getDescriptionParam(cmd)))
                .externalId(Optional.ofNullable(getExternalIdParam(cmd)))
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
