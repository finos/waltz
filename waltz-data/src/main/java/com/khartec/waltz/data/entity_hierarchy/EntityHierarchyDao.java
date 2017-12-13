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

package com.khartec.waltz.data.entity_hierarchy;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.entity_hierarchy.EntityHierarchyItem;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.EntityHierarchy;
import com.khartec.waltz.schema.tables.records.EntityHierarchyRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.data.JooqUtilities.TO_STRING_TALLY;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;

@Repository
public class EntityHierarchyDao {

    private static final Logger LOG = LoggerFactory.getLogger(EntityHierarchyDao.class);

    private static final EntityHierarchy eh = ENTITY_HIERARCHY;

    private static final Function<EntityHierarchyItem, EntityHierarchyRecord> ITEM_TO_RECORD_MAPPER =
            item -> new EntityHierarchyRecord(
                    item.kind().name(),
                    item.id().get(),
                    item.parentId().orElse(null),
                    item.level());

    private final DSLContext dsl;

    @Autowired
    public EntityHierarchyDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public int replaceHierarchy(EntityKind kind, List<EntityHierarchyItem> hierarchyItems) {
        checkNotNull(kind, "kind cannot be null");
        checkNotNull(hierarchyItems, "hierarchyItems cannot be null");

        LOG.info("Replacing hierarchy items for kind: {}, deleting existing", kind);
        dsl.deleteFrom(eh)
                .where(eh.KIND.eq(kind.name()))
                .execute();

        LOG.info("Replacing hierarchy items for kind: {}, inserting new record (#{})", kind, hierarchyItems.size());

        List<EntityHierarchyRecord> records = map(hierarchyItems, ITEM_TO_RECORD_MAPPER);
        dsl.batchInsert(records)
                .execute();

        return records.size();

    }


    public List<Tally<String>> tallyByKind() {
        return JooqUtilities.calculateStringTallies(dsl, eh, eh.KIND, DSL.trueCondition());
    }


    public List<Tally<String>> getRootTallies() {
        return dsl.select(eh.KIND, DSL.count())
                .from(eh)
                .where(eh.LEVEL.eq(1)
                        .and(eh.ID.eq(eh.ANCESTOR_ID)))
                .groupBy(eh.KIND)
                .fetch(TO_STRING_TALLY);
    }

}
