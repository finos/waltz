/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.change_initiative;

import com.khartec.waltz.data.FindEntityReferencesByIdSelector;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.change_initiative.ChangeInitiativeKind;
import com.khartec.waltz.model.change_initiative.ImmutableChangeInitiative;
import com.khartec.waltz.schema.tables.records.ChangeInitiativeRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static java.util.Optional.ofNullable;

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
                .fetch(TO_ENTITY_REFERENCE);
    }


    public Collection<ChangeInitiative> findForSelector(Select<Record1<Long>> selector) {
        return dsl
                .selectFrom(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<ChangeInitiative> findHierarchyForSelector(Select<Record1<Long>> selector) {
        SelectConditionStep<Record1<Long>> hierarchySelector = dsl
                .selectDistinct(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.ID.in(selector)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())));

        return findForSelector(hierarchySelector);
    }
}
