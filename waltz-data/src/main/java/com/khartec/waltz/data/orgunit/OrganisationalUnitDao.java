/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.orgunit;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.orgunit.ImmutableOrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnitKind;
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
import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


@Repository
public class OrganisationalUnitDao {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisationalUnitDao.class);


    public static final RecordMapper<Record, OrganisationalUnit> recordMapper = record -> {
        OrganisationalUnitRecord orgUnitRecord = record.into(OrganisationalUnitRecord.class);
        return ImmutableOrganisationalUnit.builder()
                .name(orgUnitRecord.getName())
                .description(orgUnitRecord.getDescription())
                .id(orgUnitRecord.getId())
                .parentId(Optional.ofNullable(orgUnitRecord.getParentId()))
                .kind(readEnum(orgUnitRecord.getKind(), OrganisationalUnitKind.class, (s) -> OrganisationalUnitKind.IT))
                .build();
    };

    private static final com.khartec.waltz.schema.tables.OrganisationalUnit ou = ORGANISATIONAL_UNIT.as("ou");

    private final DSLContext dsl;

    @Autowired
    public OrganisationalUnitDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<OrganisationalUnit> findAll() {
        return dsl.select(ou.fields())
                .from(ou)
                .fetch(recordMapper);
    }


    public OrganisationalUnit getById(long id) {
        return dsl.select(ou.fields())
                .from(ou)
                .where(ou.ID.eq(id))
                .fetchOne(recordMapper);
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
                .fetch(recordMapper);
    }


    @Override
    public String toString() {
        return "OrganisationalUnitDao{" +
                "dsl=" + dsl +
                '}';
    }
}
