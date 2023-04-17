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

package org.finos.waltz.data.legal_entity;

import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.LEGAL_ENTITY_RELATIONSHIP;


public class LegalEntityRelationshipIdSelectorFactory implements IdSelectorFactory {


    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch (options.entityReference().kind()) {
            case ACTOR:
                return mkForActor(options);
            case APPLICATION:
            case APP_GROUP:
            case MEASURABLE:
            case ORG_UNIT:
            case PERSON:
                return wrapAppIdSelector(options);
            case LEGAL_ENTITY:
                return mkForLegalEntity(options);
            case LEGAL_ENTITY_RELATIONSHIP_KIND:
                return mkForLegalEntityRelationshipKind(options);
            default:
                throw new UnsupportedOperationException("Cannot create physical specification selector from options: " + options);
        }
    }

    private Select<Record1<Long>> mkForLegalEntityRelationshipKind(IdSelectionOptions options) {
        return DSL
                .select(LEGAL_ENTITY_RELATIONSHIP.ID)
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .where(LEGAL_ENTITY_RELATIONSHIP.RELATIONSHIP_KIND_ID.eq(options.entityReference().id()));
    }

    private Select<Record1<Long>> mkForLegalEntity(IdSelectionOptions options) {
        return DSL
                .select(LEGAL_ENTITY_RELATIONSHIP.ID)
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .where(LEGAL_ENTITY_RELATIONSHIP.LEGAL_ENTITY_ID.eq(options.entityReference().id()));
    }

    private Select<Record1<Long>> mkForActor(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);

        return DSL
                .select(LEGAL_ENTITY_RELATIONSHIP.ID)
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .where(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND.eq(EntityKind.ACTOR.name())
                        .and(LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.eq(options.entityReference().id())));
    }

    private Select<Record1<Long>> wrapAppIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(options);

        return DSL
                .select(LEGAL_ENTITY_RELATIONSHIP.ID)
                .from(LEGAL_ENTITY_RELATIONSHIP)
                .where(LEGAL_ENTITY_RELATIONSHIP.TARGET_KIND.eq(EntityKind.APPLICATION.name())
                        .and(LEGAL_ENTITY_RELATIONSHIP.TARGET_ID.in(appIdSelector)));
    }

}
