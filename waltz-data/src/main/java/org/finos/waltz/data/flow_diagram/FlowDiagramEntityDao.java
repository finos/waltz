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

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.flow_diagram.FlowDiagramEntity;
import org.finos.waltz.model.flow_diagram.ImmutableFlowDiagramEntity;
import org.finos.waltz.schema.tables.records.FlowDiagramEntityRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;


@Repository
public class FlowDiagramEntityDao {

    private static final org.finos.waltz.schema.tables.FlowDiagramEntity fde = FLOW_DIAGRAM_ENTITY.as("fde");

    private static final ArrayList<EntityKind> POSSIBLE_ENTITY_KINDS = newArrayList(
            EntityKind.APPLICATION,
            EntityKind.ACTOR,
            EntityKind.CHANGE_INITIATIVE,
            EntityKind.DATA_TYPE,
            EntityKind.LOGICAL_DATA_FLOW,
            EntityKind.MEASURABLE,
            EntityKind.PHYSICAL_FLOW);

    private static Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            fde.ENTITY_ID,
            fde.ENTITY_KIND,
            POSSIBLE_ENTITY_KINDS);


    private static Field<String> ENTITY_LIFECYCLE_PHASE_FIELD = InlineSelectFieldFactory.mkEntityLifecycleField(
            fde.ENTITY_ID,
            fde.ENTITY_KIND,
            POSSIBLE_ENTITY_KINDS);



    private static final RecordMapper<Record, FlowDiagramEntity> TO_DOMAIN_MAPPER = r -> {
        FlowDiagramEntityRecord record = r.into(FLOW_DIAGRAM_ENTITY);
        EntityReference ref = mkRef(
                EntityKind.valueOf(record.getEntityKind()),
                record.getEntityId(),
                Optional.ofNullable(r.getValue(ENTITY_NAME_FIELD)).orElse(format("Deleted %s", record.getEntityKind())));

        EntityLifecycleStatus entityLifecycleStatus = readEnum(
                r.getValue(ENTITY_LIFECYCLE_PHASE_FIELD),
                EntityLifecycleStatus.class,
                t -> EntityLifecycleStatus.REMOVED);

        return ImmutableFlowDiagramEntity.builder()
                .diagramId(record.getDiagramId())
                .entityReference(ImmutableEntityReference
                        .copyOf(ref)
                        .withEntityLifecycleStatus(entityLifecycleStatus))
                .isNotable(record.getIsNotable())
                .build();
    };


    public static final Function<FlowDiagramEntity, FlowDiagramEntityRecord> TO_RECORD_MAPPER = fde -> {
        EntityReference entityRef = fde.entityReference();

        FlowDiagramEntityRecord record = new FlowDiagramEntityRecord();
        record.setDiagramId(fde.diagramId().get());
        record.setEntityId(entityRef.id());
        record.setEntityKind(entityRef.kind().name());
        record.setIsNotable(fde.isNotable());

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public FlowDiagramEntityDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<FlowDiagramEntity> findForDiagram(long diagramId) {
        return doBasicQuery(fde.DIAGRAM_ID.eq(diagramId));
    }


    public List<FlowDiagramEntity> findForEntity(EntityReference ref) {
        Condition condition = fde.ENTITY_KIND.eq(ref.kind().name())
                .and(fde.ENTITY_ID.eq(ref.id()));
        return doBasicQuery(condition);
    }


    public List<FlowDiagramEntity> findForDiagramSelector(Select<Record1<Long>> selector) {
        return doBasicQuery(fde.DIAGRAM_ID.in(selector));
    }


    public List<FlowDiagramEntity> findForEntitySelector(EntityKind kind, Select<Record1<Long>> selector) {
        Condition condition = fde.ENTITY_ID.in(selector)
                .and(fde.ENTITY_KIND.eq(kind.name()));
        return doBasicQuery(condition);
    }


    public int[] createEntities(List<FlowDiagramEntity> entities) {
        List<FlowDiagramEntityRecord> records = entities
                .stream()
                .map(TO_RECORD_MAPPER::apply)
                .collect(toList());

        return dsl.batchInsert(records)
                .execute();
    }


    /**
     * Removes entities associated with diagram except for measurables
     * which remain as they are explicitly linked to diagrams, not implicitly
     * stored as part of the the diagram picture.
     *
     * @param diagramId the diagram to remove entities from
     * @return count of removed diagrams
     */
    public int deleteForDiagram(long diagramId) {
        List<String> relatedEntityKinds = asList(EntityKind.MEASURABLE.name(), EntityKind.CHANGE_INITIATIVE.name(), EntityKind.DATA_TYPE.name());
        return dsl
                .deleteFrom(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.notIn(relatedEntityKinds))
                .execute();
    }


    /**
     * Deletes a specific entity associated with the given diagram id.
     *
     * @param diagramId the diagram to remove the entity from
     * @param entityReference the entity to remove
     * @return boolean indicating success or failure
     */
    public boolean deleteEntityForDiagram(long diagramId, EntityReference entityReference) {
        return dsl
                .deleteFrom(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(entityReference.kind().name()))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(entityReference.id()))
                .execute() == 1;
    }


    /**
     * Deletes all references to entities captured by the generic selector.
     *
     * @param selector generic selector
     * @return count of removed entities
     */
    public int deleteForGenericEntitySelector(GenericSelector selector) {
        return dsl
                .deleteFrom(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(selector.kind().name()))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_ID.in(selector.selector()))
                .execute();
    }


    /**
     * Duplicates the entities referenced by `diagramId` into a new diagram, referenced
     * by `clonedDiagramId`
     *
     * @param diagramId the diagram to copy from
     * @param clonedDiagramId the target diagram
     */
    public void clone(long diagramId, Long clonedDiagramId) {
        List<FlowDiagramEntity> diagramEntities = findForDiagram(diagramId);
        List<FlowDiagramEntity> clonedDiagramEntities = map(diagramEntities, d -> ImmutableFlowDiagramEntity
                .copyOf(d)
                .withDiagramId(clonedDiagramId));
        createEntities(clonedDiagramEntities);
    }


    // --- helpers

    private List<FlowDiagramEntity> doBasicQuery(Condition condition) {
        return dsl
                .select(fde.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ENTITY_LIFECYCLE_PHASE_FIELD)
                .from(fde)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


}
