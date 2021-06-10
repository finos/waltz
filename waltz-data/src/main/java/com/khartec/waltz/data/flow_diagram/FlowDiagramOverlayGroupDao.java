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
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;


@Repository
public class FlowDiagramOverlayGroupDao {

    private static final com.khartec.waltz.schema.tables.FlowDiagramEntity fde = FLOW_DIAGRAM_ENTITY.as("fde");


    private static Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            fde.ENTITY_ID,
            fde.ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


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
                .entityReference(mkRef(
                        EntityKind.valueOf(record.getEntityKind()),
                        record.getEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
                .symbol(record.getSymbol())
                .fill(record.getFill())
                .fill(record.getStroke())
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

}
