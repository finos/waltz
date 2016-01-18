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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.orgunit.ImmutableOrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.model.orgunit.OrganisationalUnitKind;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.common.ListUtilities.filter;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static java.util.Collections.emptyList;


@Repository
public class OrganisationalUnitDao {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisationalUnitDao.class);


    private static final String SEARCH_QUERY_POSTGRES = "SELECT\n" +
            "  *,\n" +
            "  ts_rank_cd(\n" +
            "      setweight(to_tsvector(name), 'A')\n" +
            "      || setweight(to_tsvector(description), 'D'),\n" +
            "      plainto_tsquery(?)) AS rank\n" +
            "FROM organisational_unit\n" +
            "WHERE\n" +
            "  setweight(to_tsvector(name), 'A')\n" +
            "  || setweight(to_tsvector(description), 'D')\n" +
            "  @@ plainto_tsquery(?)\n" +
            "ORDER BY rank DESC\n" +
            "LIMIT 10;\n";


    private static final String SEARCH_QUERY_MARIADB
            = "SELECT * FROM organisational_unit\n"
            + " WHERE\n"
            + "  MATCH(name, description)\n"
            + "  AGAINST (?)\n"
            + " LIMIT 20";



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


    public List<OrganisationalUnit> findByIds(List<Long> ids) {
        return dsl.select(ORGANISATIONAL_UNIT.fields())
                .from(ORGANISATIONAL_UNIT)
                .where(ORGANISATIONAL_UNIT.ID.in(ids))
                .fetch(recordMapper);
    }


    public List<OrganisationalUnit> findDescendants(long orgUnitId) {
        if (dsl.dialect() == SQLDialect.POSTGRES) {
            return OrganisationalUnitDaoPostgresHelper.findDescendants(dsl, orgUnitId);
        } else {
            List<OrganisationalUnit> orgUnits = findAll();
            Optional<OrganisationalUnit> root = orgUnits
                    .stream()
                    .filter(ou -> ou.id().equals(Optional.of(orgUnitId)))
                    .findFirst();

            if (! root.isPresent()) {
                return emptyList();
            }

            Map<Optional<Long>, Collection<OrganisationalUnit>> byParentId = groupBy(
                    ou -> ou.parentId(),
                    filter(
                            ou -> ou.parentId().isPresent(),
                            orgUnits));

            List<OrganisationalUnit> acc = ListUtilities.newArrayList();
            descend(root.get(), acc, byParentId);
            return acc;
        }
    }


    private void descend(OrganisationalUnit ou,
                         List<OrganisationalUnit> acc,
                         Map<Optional<Long>, Collection<OrganisationalUnit>> byParentId) {
        acc.add(ou);
        Collection<OrganisationalUnit> children = byParentId.get(ou.id());
        if (children != null) {
            children.forEach(child -> descend(child, acc, byParentId));
        }
    }




    public List<OrganisationalUnit> findAncestors(long orgUnitId) {

        if (dsl.dialect() == SQLDialect.POSTGRES) {
            return OrganisationalUnitDaoPostgresHelper.findAncestors(dsl, orgUnitId);
        } else {
            List<OrganisationalUnit> orgUnits = findAll();

            Map<Optional<Long>, OrganisationalUnit> byId = indexBy(ou -> ou.id(), orgUnits);
            OrganisationalUnit orgUnit = byId.get(Optional.of(orgUnitId));

            if (orgUnit == null) {
                LOG.warn("Could not find orgUnit: " + orgUnitId + " so cannot calculate ancestors");
                return emptyList();
            }

            List<OrganisationalUnit> acc = ListUtilities.newArrayList();
            ascend(orgUnit, acc, byId);
            return acc;
        }
    }


    private void ascend(OrganisationalUnit orgUnit, List<OrganisationalUnit> acc, Map<Optional<Long>, OrganisationalUnit> byId) {
        acc.add(orgUnit);
        OrganisationalUnit parent = byId.get(orgUnit.parentId());
        if (parent != null) {
            ascend(parent, acc, byId);
        }
    }


    public List<OrganisationalUnit> search(String query) {
        if (dsl.dialect() == SQLDialect.POSTGRES) {
            Result<Record> records = dsl.fetch(SEARCH_QUERY_POSTGRES, query, query);
            return records.map(recordMapper);
        }
        if (dsl.dialect() == SQLDialect.MARIADB) {
            Result<Record> records = dsl.fetch(SEARCH_QUERY_MARIADB, query);
            return records.map(recordMapper);
        }

        LOG.error("Could not find full text query for database dialect: " + dsl.dialect());
        return emptyList();
    }

    @Override
    public String toString() {
        return "OrganisationalUnitDao{" +
                "dsl=" + dsl +
                '}';
    }

}
