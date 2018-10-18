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

package com.khartec.waltz.data.orgunit;

import com.khartec.waltz.data.FindEntityReferencesByIdSelector;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableLeveledEntityReference;
import com.khartec.waltz.model.LeveledEntityReference;
import com.khartec.waltz.model.orgunit.ImmutableOrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


@Repository
public class OrganisationalUnitDao implements FindEntityReferencesByIdSelector {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisationalUnitDao.class);
    private static final com.khartec.waltz.schema.tables.OrganisationalUnit ou = ORGANISATIONAL_UNIT.as("ou");


    public static final RecordMapper<Record, OrganisationalUnit> TO_DOMAIN_MAPPER = record -> {
        OrganisationalUnitRecord orgUnitRecord = record.into(OrganisationalUnitRecord.class);
        return ImmutableOrganisationalUnit.builder()
                .name(orgUnitRecord.getName())
                .description(orgUnitRecord.getDescription())
                .id(orgUnitRecord.getId())
                .parentId(Optional.ofNullable(orgUnitRecord.getParentId()))
                .build();
    };


    private static final RecordMapper<Record, LeveledEntityReference> TO_LEVELED_ENTITY_REF_MAPPER = record -> {
        EntityReference entityRef = EntityReference.mkRef(
                EntityKind.ORG_UNIT,
                record.getValue(ORGANISATIONAL_UNIT.ID),
                record.getValue(ORGANISATIONAL_UNIT.NAME));

        return ImmutableLeveledEntityReference.builder()
                .entityReference(entityRef)
                .level(record.getValue(ENTITY_HIERARCHY.LEVEL))
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

        return dsl.selectFrom(ou)
                .where(ou.ID.in(qry))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public OrganisationalUnit getById(long id) {
        return dsl.select(ou.fields())
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



    public List<LeveledEntityReference> findImmediateHierarchy(long id) {

        Select<Record3<Long, String, Integer>> immediateChildren = DSL.select(
                    ORGANISATIONAL_UNIT.ID,
                    ORGANISATIONAL_UNIT.NAME,
                    ENTITY_HIERARCHY.LEVEL.plus(1).as(ENTITY_HIERARCHY.LEVEL))  // immediate children always at 'level + 1'
                .from(ORGANISATIONAL_UNIT)
                .innerJoin(ENTITY_HIERARCHY)
                .on(ENTITY_HIERARCHY.ID.eq(ORGANISATIONAL_UNIT.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())))
                .where(ORGANISATIONAL_UNIT.PARENT_ID.eq(id))
                .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(id));

        Select<Record3<Long, String, Integer>> allParents = DSL.select(
                    ORGANISATIONAL_UNIT.ID,
                    ORGANISATIONAL_UNIT.NAME,
                    ENTITY_HIERARCHY.LEVEL)
                .from(ORGANISATIONAL_UNIT)
                .innerJoin(ENTITY_HIERARCHY)
                .on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(ORGANISATIONAL_UNIT.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())))
                .where(ENTITY_HIERARCHY.ID.eq(id));

        return dsl.selectFrom(immediateChildren.asTable())
                .unionAll(allParents)
                .orderBy(ENTITY_HIERARCHY.LEVEL, ORGANISATIONAL_UNIT.NAME)
                .fetch(TO_LEVELED_ENTITY_REF_MAPPER);
    }


    public List<OrganisationalUnit> findDescendants(long id) {
        return dsl.select(ORGANISATIONAL_UNIT.fields())
                .from(ORGANISATIONAL_UNIT)
                .innerJoin(ENTITY_HIERARCHY)
                .on(ENTITY_HIERARCHY.ID.eq(ORGANISATIONAL_UNIT.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())))
                .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(id))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public OrganisationalUnit getByAppId(long id) {
        return dsl.select(ORGANISATIONAL_UNIT.fields())
                .from(ORGANISATIONAL_UNIT)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .where(APPLICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    @Override
    public String toString() {
        return "OrganisationalUnitDao{" +
                "dsl=" + dsl +
                '}';
    }

}
