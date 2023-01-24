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

package org.finos.waltz.data.change_initiative;

import org.finos.waltz.schema.tables.records.ChangeInitiativeRecord;
import org.finos.waltz.data.FindEntityReferencesByIdSelector;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.change_initiative.ChangeInitiativeKind;
import org.finos.waltz.model.change_initiative.ImmutableChangeInitiative;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static org.finos.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static org.finos.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;

@Repository
public class ChangeInitiativeDao implements FindEntityReferencesByIdSelector {

    public static final RecordMapper<Record, ChangeInitiative> TO_DOMAIN_MAPPER = r -> {
        ChangeInitiativeRecord record = r.into(CHANGE_INITIATIVE);
        return ImmutableChangeInitiative.builder()
                .id(record.getId())
                .parentId(ofNullable(record.getParentId()))
                .name(record.getName())
                .description(ofNullable(record.getDescription()).orElse(""))
                .externalId(ofNullable(record.getExternalId()))
                .changeInitiativeKind(ChangeInitiativeKind.valueOf(record.getKind()))
                .lifecyclePhase(LifecyclePhase.valueOf(record.getLifecyclePhase()))
                .provenance(record.getProvenance())
                .lastUpdate(ofNullable(record.getLastUpdate()))
                .startDate(record.getStartDate())
                .endDate(record.getEndDate())
                .organisationalUnitId(record.getOrganisationalUnitId())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public ChangeInitiativeDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public ChangeInitiative getById(Long id) {
        return dsl.select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    @Override
    public List<EntityReference> findByIdSelectorAsEntityReference(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        return dsl.select(CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.NAME, DSL.val(EntityKind.CHANGE_INITIATIVE.name()))
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.in(selector))
                .fetch(JooqUtilities.TO_ENTITY_REFERENCE);
    }


    public Collection<ChangeInitiative> findForSelector(Select<Record1<Long>> selector) {
        return dsl
                .select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .where(dsl.renderInlined(CHANGE_INITIATIVE.ID.in(selector)))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<ChangeInitiative> findHierarchyForSelector(Select<Record1<Long>> selector) {
        SelectConditionStep<Record1<Long>> ancestors = dsl
                .select(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.ID.in(selector)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())));

        SelectConditionStep<Record1<Long>> descendants = DSL
                .select(ENTITY_HIERARCHY.ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.ANCESTOR_ID.in(selector)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())));

        SelectOrderByStep<Record1<Long>> hierarchySelector = descendants.union(ancestors);

        return findForSelector(hierarchySelector);
    }


    public Collection<ChangeInitiative> findByExternalId(String externalId) {
        return dsl
                .select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.EXTERNAL_ID.eq(externalId))
                .fetch(TO_DOMAIN_MAPPER);
    }

    public Collection<ChangeInitiative> findAll() {
        return dsl
                .select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .fetch(TO_DOMAIN_MAPPER);
    }
}
