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

package com.khartec.waltz.data.drill_grid;


import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.drill_grid.DrillGridDefinition;
import com.khartec.waltz.model.drill_grid.ImmutableDrillGridDefinition;
import com.khartec.waltz.schema.tables.records.DrillGridDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.DrillGridDefinition.DRILL_GRID_DEFINITION;
import static java.util.Optional.ofNullable;


@Repository
public class DrillGridDefinitionDao {

    private static final Field<String> X_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            DRILL_GRID_DEFINITION.X_ENTITY_ID,
            DRILL_GRID_DEFINITION.X_ENTITY_KIND,
            newArrayList(EntityKind.MEASURABLE_CATEGORY));

    private static final Field<String> Y_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            DRILL_GRID_DEFINITION.Y_ENTITY_ID,
            DRILL_GRID_DEFINITION.Y_ENTITY_KIND,
            newArrayList(EntityKind.MEASURABLE_CATEGORY));

    private static final RecordMapper<Record, DrillGridDefinition> TO_DOMAIN_MAPPER = record -> {
        DrillGridDefinitionRecord r = record.into(DRILL_GRID_DEFINITION);

        EntityReference xRef = ImmutableEntityReference.builder()
                .kind(EntityKind.valueOf(r.getXEntityKind()))
                .id(r.getXEntityId())
                .name(ofNullable(record.getValue(X_NAME_FIELD)))
                .build();

        EntityReference yRef = ImmutableEntityReference.builder()
                .kind(EntityKind.valueOf(r.getYEntityKind()))
                .id(r.getYEntityId())
                .name(ofNullable(record.getValue(Y_NAME_FIELD)))
                .build();

        return ImmutableDrillGridDefinition
                .builder()
                .id(r.getId())
                .xAxis(xRef)
                .yAxis(yRef)
                .name(r.getName())
                .description(r.getDescription())
                .build();
    };


    private final DSLContext dsl;


    public DrillGridDefinitionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    private DrillGridDefinition getById(long id) {
        return dsl
                .select(DRILL_GRID_DEFINITION.fields())
                .select(X_NAME_FIELD)
                .select(Y_NAME_FIELD)
                .from(DRILL_GRID_DEFINITION)
                .where(DRILL_GRID_DEFINITION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<DrillGridDefinition> findAll() {
        return dsl
                .select(DRILL_GRID_DEFINITION.fields())
                .select(X_NAME_FIELD)
                .select(Y_NAME_FIELD)
                .from(DRILL_GRID_DEFINITION)
                .orderBy(DRILL_GRID_DEFINITION.NAME)
                .fetch()
                .map(TO_DOMAIN_MAPPER);
    }


    public DrillGridDefinition updateDescription(long id,
                                                 String description) {
        dsl.update(DRILL_GRID_DEFINITION)
                .set(DRILL_GRID_DEFINITION.DESCRIPTION, description)
                .where(DRILL_GRID_DEFINITION.ID.eq(id))
                .execute();

        return getById(id);
    }

}
