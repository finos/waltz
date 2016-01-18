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

import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import org.jooq.*;

import java.util.List;

import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static org.jooq.impl.DSL.*;
import static org.jooq.impl.DSL.select;


public class OrganisationalUnitDaoPostgresHelper {


    private static final Table<Record> orgTree = table(name("orgTree"));
    private static final Field<Long> ouIdField = field(name("orgTree", "id"), Long.class);
    private static final Field<Long> pIdField = field(name("orgTree", "parent_id"), Long.class);
    private static final Field<String> nameField = field(name("orgTree", "name"), String.class);
    private static final Field<String> descriptionField = field(name("orgTree", "description"), String.class);
    private static final Field<String> kindField = field(name("orgTree", "kind"), String.class);

    public static List<OrganisationalUnit> findDescendants(DSLContext dsl, long orgUnitId) {

        Table<Record> orgTree = table(name("orgTree"));
        Field<Long> ouIdField = field(name("orgTree", "id"), Long.class);
        Field<Long> pIdField = field(name("orgTree", "parent_id"), Long.class);
        Field<String> nameField = field(name("orgTree", "name"), String.class);
        Field<String> descriptionField = field(name("orgTree", "description"), String.class);
        Field<String> kindField = field(name("orgTree", "kind"), String.class);

        Field[] tableFields = new Field[] {
                ORGANISATIONAL_UNIT.ID,
                ORGANISATIONAL_UNIT.PARENT_ID,
                ORGANISATIONAL_UNIT.NAME,
                ORGANISATIONAL_UNIT.DESCRIPTION,
                ORGANISATIONAL_UNIT.KIND
        };

        return dsl.withRecursive("orgTree", "id", "parent_id", "name", "description", "kind")
                .as(select(tableFields)
                        .from(ORGANISATIONAL_UNIT).where(ORGANISATIONAL_UNIT.ID.eq(orgUnitId))
                        .unionAll(
                                select(tableFields)
                                        .from(ORGANISATIONAL_UNIT, orgTree)
                                        .where(ORGANISATIONAL_UNIT.PARENT_ID.eq(ouIdField))))
                .select(ouIdField, pIdField, nameField, descriptionField, kindField)
                .from(orgTree)
                .fetch(OrganisationalUnitDao.recordMapper);
    }


    public static List<OrganisationalUnit> findAncestors(DSLContext dsl, long orgUnitId) {

        Select<? extends Record> recursiveStep = select(ORGANISATIONAL_UNIT.fields())
                .from(ORGANISATIONAL_UNIT, orgTree)
                .where(ORGANISATIONAL_UNIT.ID.eq(pIdField));

        return findRecursively(dsl, orgUnitId, recursiveStep);
    }


    private static List<OrganisationalUnit> findRecursively(DSLContext dsl, long orgUnitId, Select<? extends Record> recursiveStep) {
        return dsl.withRecursive("orgTree", "id", "parent_id", "name", "description", "kind")
                .as(select(ORGANISATIONAL_UNIT.fields())
                        .from(ORGANISATIONAL_UNIT).where(ORGANISATIONAL_UNIT.ID.eq(orgUnitId))
                        .unionAll(recursiveStep))
                .select(ouIdField, pIdField, nameField, descriptionField, kindField)
                .from(orgTree)
                .fetch(OrganisationalUnitDao.recordMapper);
    }
}
