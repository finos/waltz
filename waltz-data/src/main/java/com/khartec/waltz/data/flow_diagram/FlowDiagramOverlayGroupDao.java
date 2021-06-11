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

package com.khartec.waltz.data.flow_diagram;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.flow_diagram.FlowDiagramOverlayGroup;
import com.khartec.waltz.model.flow_diagram.FlowDiagramOverlayGroupEntry;
import com.khartec.waltz.model.flow_diagram.ImmutableFlowDiagramOverlayGroup;
import com.khartec.waltz.model.flow_diagram.ImmutableFlowDiagramOverlayGroupEntry;
import com.khartec.waltz.schema.tables.records.FlowDiagramOverlayGroupEntryRecord;
import com.khartec.waltz.schema.tables.records.FlowDiagramOverlayGroupRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.FLOW_DIAGRAM_OVERLAY_GROUP;
import static com.khartec.waltz.schema.Tables.FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY;


@Repository
public class FlowDiagramOverlayGroupDao {


    private static Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY.ENTITY_ID,
            FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY.ENTITY_KIND,
            newArrayList(EntityKind.MEASURABLE, EntityKind.APP_GROUP));


    private static final RecordMapper<Record, FlowDiagramOverlayGroup> TO_GROUP_MAPPER = r -> {
        FlowDiagramOverlayGroupRecord record = r.into(FLOW_DIAGRAM_OVERLAY_GROUP);
        return ImmutableFlowDiagramOverlayGroup.builder()
                .id(record.getId())
                .diagramId(record.getFlowDiagramId())
                .name(record.getName())
                .description(record.getDescription())
                .externalId(record.getExternalId())
                .isDefault(record.getIsDefault())
                .build();
    };

    private static final RecordMapper<Record, FlowDiagramOverlayGroupEntry> TO_OVERLAY_ENTRY_MAPPER = r -> {
        FlowDiagramOverlayGroupEntryRecord record = r.into(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY);
        return ImmutableFlowDiagramOverlayGroupEntry.builder()
                .overlayGroupId(record.getOverlayGroupId())
                .id(record.getId())
                .entityReference(mkRef(
                        EntityKind.valueOf(record.getEntityKind()),
                        record.getEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
                .symbol(record.getSymbol())
                .fill(record.getFill())
                .stroke(record.getStroke())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public FlowDiagramOverlayGroupDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Set<FlowDiagramOverlayGroup> findByDiagramId(long diagramId) {
        return dsl
                .select(FLOW_DIAGRAM_OVERLAY_GROUP.fields())
                .from(FLOW_DIAGRAM_OVERLAY_GROUP)
                .where(FLOW_DIAGRAM_OVERLAY_GROUP.FLOW_DIAGRAM_ID.eq(diagramId))
                .fetchSet(TO_GROUP_MAPPER);
    }


    public Set<FlowDiagramOverlayGroupEntry> findOverlaysByDiagramId(long diagramId) {
        return dsl
                .select(ENTITY_NAME_FIELD)
                .select(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY.fields())
                .from(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY)
                .innerJoin(FLOW_DIAGRAM_OVERLAY_GROUP)
                .on(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY.OVERLAY_GROUP_ID.eq(FLOW_DIAGRAM_OVERLAY_GROUP.ID))
                .where(FLOW_DIAGRAM_OVERLAY_GROUP.FLOW_DIAGRAM_ID.eq(diagramId))
                .fetchSet(TO_OVERLAY_ENTRY_MAPPER);
    }


    public Long create(FlowDiagramOverlayGroup group) {

        FlowDiagramOverlayGroupRecord r = dsl.newRecord(FLOW_DIAGRAM_OVERLAY_GROUP);

        r.setName(group.name());
        r.setDescription(group.description());
        r.setExternalId(group.externalId());
        r.setIsDefault(group.isDefault());
        r.setFlowDiagramId(group.diagramId());

        return dsl
                .insertInto(FLOW_DIAGRAM_OVERLAY_GROUP)
                .set(r)
                .returning(FLOW_DIAGRAM_OVERLAY_GROUP.ID)
                .fetchOne()
                .getFlowDiagramId();
    }
}
