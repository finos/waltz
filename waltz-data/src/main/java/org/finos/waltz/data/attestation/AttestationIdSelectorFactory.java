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

package org.finos.waltz.data.attestation;

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import java.util.function.Function;

import static org.finos.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static org.finos.waltz.common.Checks.checkNotNull;

public class AttestationIdSelectorFactory implements Function<IdSelectionOptions, Select<Record1<Long>>> {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();
        switch (ref.kind()) {
            case PERSON:
            case ORG_UNIT:
            case APP_GROUP:
                return mkForRelatedApps(options);
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + ref.kind());
        }
    }

    private Select<Record1<Long>> mkForRelatedApps(IdSelectionOptions options) {
        Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(options);
        return DSL
                .select(ATTESTATION_INSTANCE.ID)
                .from(ATTESTATION_INSTANCE)
                .where(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.in(appIds));
    }
}
