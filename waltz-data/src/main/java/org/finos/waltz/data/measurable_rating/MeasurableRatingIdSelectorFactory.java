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

package org.finos.waltz.data.measurable_rating;

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import org.finos.waltz.data.end_user_app.EndUserAppIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import java.util.function.Function;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static org.finos.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;

public class MeasurableRatingIdSelectorFactory implements Function<IdSelectionOptions, Select<Record1<Long>>> {

    private static final MeasurableRating mr = Tables.MEASURABLE_RATING;
    private static final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private static final ChangeInitiativeIdSelectorFactory ciIdSelectorFactory = new ChangeInitiativeIdSelectorFactory();
    private static final EndUserAppIdSelectorFactory eudaSelectorFactory = new EndUserAppIdSelectorFactory();

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();
        switch (ref.kind()) {
            case APPLICATION:
            case END_USER_APPLICATION:
            case ACTOR:
            case CHANGE_INITIATIVE:
                return mkForDirectEntity(options);
            case ORG_UNIT:
            case APP_GROUP:
            case MEASURABLE:
            case PERSON:
            case FLOW_DIAGRAM:
            case PROCESS_DIAGRAM:
            case ALL:
                return mkForAggregateGroup(options);
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + ref.kind());
        }
    }

    private Select<Record1<Long>> mkForAggregateGroup(IdSelectionOptions options) {

        Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(options);
        Select<Record1<Long>> eudaSelector = eudaSelectorFactory.apply(options);
        Select<Record1<Long>> ciSelector = ciIdSelectorFactory.apply(options);

        Condition appCond = mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()).and(mr.ENTITY_ID.in(appSelector));
        Condition eudaCond = mr.ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name()).and(mr.ENTITY_ID.in(eudaSelector));
        Condition ciCond = mr.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()).and(mr.ENTITY_ID.in(ciSelector));

        return DSL
                .select(mr.ID)
                .from(mr)
                .where(appCond
                        .or(eudaCond)
                        .or(ciCond));
    }


    private Select<Record1<Long>> mkForDirectEntity(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL
                .select(mr.ID)
                .from(mr)
                .where(mr.ENTITY_KIND.eq(options.entityReference().kind().name())
                        .and(mr.ENTITY_ID.eq(options.entityReference().id())));
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
