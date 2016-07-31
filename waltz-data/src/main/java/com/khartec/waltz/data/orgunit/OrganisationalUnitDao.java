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

import com.khartec.waltz.model.orgunit.ImmutableOrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnitKind;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.EnumUtilities.readEnum;
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
                .kind(readEnum(orgUnitRecord.getKind(), OrganisationalUnitKind.class, OrganisationalUnitKind.IT))
                .build();
    };


    private final DSLContext dsl;

    @Autowired
    public OrganisationalUnitDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<OrganisationalUnit> findAll() {
        return dsl.select(ORGANISATIONAL_UNIT.fields())
                .from(ORGANISATIONAL_UNIT)
                .fetch(recordMapper);
    }


    public OrganisationalUnit getById(long id) {
        return dsl.select(ORGANISATIONAL_UNIT.fields())
                .from(ORGANISATIONAL_UNIT)
                .where(ORGANISATIONAL_UNIT.ID.eq(id))
                .fetchOne(recordMapper);
    }


    public Integer updateDescription(long id, String description) {
        return dsl.update(ORGANISATIONAL_UNIT)
                .set(ORGANISATIONAL_UNIT.DESCRIPTION, description)
                .where(ORGANISATIONAL_UNIT.ID.eq(id))
                .execute();
    }


    public List<OrganisationalUnit> findBySelector(Select<Record1<Long>> selector) {
        Condition condition = ORGANISATIONAL_UNIT.ID.in(selector);
        return findByCondition(condition);
    }


    public List<OrganisationalUnit> findByIds(Long... ids) {
        Condition condition = ORGANISATIONAL_UNIT.ID.in(ids);
        return findByCondition(condition);
    }


    private List<OrganisationalUnit> findByCondition(Condition condition) {
        return dsl.select(ORGANISATIONAL_UNIT.fields())
                .from(ORGANISATIONAL_UNIT)
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
