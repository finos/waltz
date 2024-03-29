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

package org.finos.waltz.data.flow_diagram;

import org.finos.waltz.schema.tables.records.FlowDiagramOverlayGroupEntryRecord;
import org.finos.waltz.schema.tables.records.FlowDiagramOverlayGroupRecord;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.flow_diagram.FlowDiagramOverlayGroup;
import org.finos.waltz.model.flow_diagram.FlowDiagramOverlayGroupEntry;
import org.finos.waltz.model.flow_diagram.ImmutableFlowDiagramOverlayGroup;
import org.finos.waltz.model.flow_diagram.ImmutableFlowDiagramOverlayGroupEntry;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.finos.waltz.schema.Tables.FLOW_DIAGRAM_OVERLAY_GROUP;
import static org.finos.waltz.schema.Tables.FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY;
import static java.util.stream.Collectors.collectingAndThen;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;


@Repository
public class FlowDiagramOverlayGroupDao {


    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
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


    public FlowDiagramOverlayGroup getById(Long id){
        return dsl
                .select(FLOW_DIAGRAM_OVERLAY_GROUP.fields())
                .from(FLOW_DIAGRAM_OVERLAY_GROUP)
                .where(FLOW_DIAGRAM_OVERLAY_GROUP.ID.eq(id))
                .fetchOne(TO_GROUP_MAPPER);
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


    public Set<FlowDiagramOverlayGroupEntry> findOverlaysByGroupId(long groupId) {
        return dsl
                .select(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY.fields())
                .select(ENTITY_NAME_FIELD)
                .from(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY)
                .where(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY.OVERLAY_GROUP_ID.eq(groupId))
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
                .getId();
    }


    public boolean delete(Long id){
        return dsl
                .deleteFrom(FLOW_DIAGRAM_OVERLAY_GROUP)
                .where(FLOW_DIAGRAM_OVERLAY_GROUP.ID.eq(id))
                .execute() == 1;
    }


    public int deleteOverlaysForDiagram(Long diagramId){
        int deleted = dsl.deleteFrom(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY)
                .where(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY.OVERLAY_GROUP_ID.in(
                        DSL
                                .select(FLOW_DIAGRAM_OVERLAY_GROUP.ID)
                                .from(FLOW_DIAGRAM_OVERLAY_GROUP)
                                .where(FLOW_DIAGRAM_OVERLAY_GROUP.FLOW_DIAGRAM_ID.eq(diagramId))))
                .execute();

        return deleted;
    }


    public int createOverlays(Set<FlowDiagramOverlayGroupEntry> overlays) {

        int[] insertedEntries = overlays
                .stream()
                .map(o -> {

                    FlowDiagramOverlayGroupEntryRecord r = dsl.newRecord(FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY);
                    r.setOverlayGroupId(o.overlayGroupId());
                    r.setEntityId(o.entityReference().id());
                    r.setEntityKind(o.entityReference().kind().name());
                    r.setFill(o.fill());
                    r.setStroke(o.stroke());
                    r.setSymbol(o.symbol());
                    return r;
                })
                .collect(collectingAndThen(Collectors.toSet(), r -> dsl.batchInsert(r).execute()));

        return IntStream.of(insertedEntries).sum();
    }
}
