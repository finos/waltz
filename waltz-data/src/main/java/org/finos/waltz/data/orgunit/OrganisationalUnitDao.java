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

package org.finos.waltz.data.orgunit;

import org.finos.waltz.data.FindEntityReferencesByIdSelector;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.orgunit.ImmutableOrganisationalUnit;
import org.finos.waltz.model.orgunit.OrganisationalUnit;
import org.finos.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


@Repository
public class OrganisationalUnitDao implements FindEntityReferencesByIdSelector {

    private static final org.finos.waltz.schema.tables.OrganisationalUnit ou = ORGANISATIONAL_UNIT.as("ou");


    public static final RecordMapper<Record, OrganisationalUnit> TO_DOMAIN_MAPPER = record -> {
        OrganisationalUnitRecord orgUnitRecord = record.into(OrganisationalUnitRecord.class);
        return ImmutableOrganisationalUnit
                .builder()
                .name(orgUnitRecord.getName())
                .description(orgUnitRecord.getDescription())
                .id(orgUnitRecord.getId())
                .parentId(ofNullable(orgUnitRecord.getParentId()))
                .externalId(ofNullable(orgUnitRecord.getExternalId()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public OrganisationalUnitDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<OrganisationalUnit> findAll() {
        return dsl.select(ou.fields())
                .from(ou)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<OrganisationalUnit> findRelatedByEntityRef(EntityReference ref) {
        Condition joinOnA = ou.ID.eq(ENTITY_RELATIONSHIP.ID_A)
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.ORG_UNIT.name()));
        Condition joinOnB = ou.ID.eq(ENTITY_RELATIONSHIP.ID_B)
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.ORG_UNIT.name()));

        Condition aMatchesEntity = ENTITY_RELATIONSHIP.ID_A.eq(ref.id())
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()));
        Condition bMatchesEntity = ENTITY_RELATIONSHIP.ID_B.eq(ref.id())
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name()));

        SelectConditionStep<Record1<Long>> qry = dsl
                .selectDistinct(ou.ID)
                .from(ou)
                .join(ENTITY_RELATIONSHIP)
                .on(joinOnA.or(joinOnB))
                .where((aMatchesEntity.or(bMatchesEntity)));

        return dsl
                .select(ou.fields())
                .from(ou)
                .where(ou.ID.in(qry))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public OrganisationalUnit getById(long id) {
        return dsl
                .select(ou.fields())
                .from(ou)
                .where(ou.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Integer updateDescription(long id, String description) {
        return dsl.update(ou)
                .set(ou.DESCRIPTION, description)
                .where(ou.ID.eq(id))
                .execute();
    }


    public List<OrganisationalUnit> findBySelector(Select<Record1<Long>> selector) {
        Condition condition = ou.ID.in(selector);
        return findByCondition(condition);
    }


    @Override
    public List<EntityReference> findByIdSelectorAsEntityReference(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        return dsl.select(ou.ID, ou.NAME, DSL.val(EntityKind.ORG_UNIT.name()))
                .from(ou)
                .where(ou.ID.in(selector))
                .fetch(TO_ENTITY_REFERENCE);
    }


    public List<OrganisationalUnit> findByIds(Long... ids) {
        Condition condition = ou.ID.in(ids);
        return findByCondition(condition);
    }


    private List<OrganisationalUnit> findByCondition(Condition condition) {
        return dsl.select(ou.fields())
                .from(ou)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }



    @Override
    public String toString() {
        return "OrganisationalUnitDao{" +
                "dsl=" + dsl +
                '}';
    }

}
