/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.flow_diagram;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.flow_diagram.FlowDiagram;
import com.khartec.waltz.model.flow_diagram.ImmutableFlowDiagram;
import com.khartec.waltz.schema.tables.records.FlowDiagramRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.FlowDiagram.FLOW_DIAGRAM;
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;

@Repository
public class FlowDiagramDao {

    private static final RecordMapper<Record, FlowDiagram> TO_DOMAIN_MAPPER = r -> {
        FlowDiagramRecord record = r.into(FLOW_DIAGRAM);
        return ImmutableFlowDiagram.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .layoutData(record.getLayoutData())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public FlowDiagramDao(DSLContext dsl) {
        checkNotNull(dsl, " cannot be null");
        this.dsl = dsl;
    }


    public FlowDiagram getById(long id) {
        return dsl
                .selectFrom(FLOW_DIAGRAM)
                .where(FLOW_DIAGRAM.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<FlowDiagram> findByEntityReference(EntityReference ref) {
        return dsl
                .selectDistinct(FLOW_DIAGRAM.fields())
                .from(FLOW_DIAGRAM)
                .innerJoin(FLOW_DIAGRAM_ENTITY)
                .on(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(FLOW_DIAGRAM.ID))
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(ref.id()))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
