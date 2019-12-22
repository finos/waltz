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

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.flow_diagram.FlowDiagramEntity;
import com.khartec.waltz.model.flow_diagram.ImmutableFlowDiagramEntity;
import com.khartec.waltz.schema.tables.records.FlowDiagramEntityRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static java.util.stream.Collectors.toList;


@Repository
public class FlowDiagramEntityDao {

    private static final com.khartec.waltz.schema.tables.FlowDiagramEntity fde = FLOW_DIAGRAM_ENTITY.as("fde");


    private static Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            fde.ENTITY_ID,
            fde.ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));


    private static final RecordMapper<Record, FlowDiagramEntity> TO_DOMAIN_MAPPER = r -> {
        FlowDiagramEntityRecord record = r.into(FLOW_DIAGRAM_ENTITY);
        return ImmutableFlowDiagramEntity.builder()
                .diagramId(record.getDiagramId())
                .entityReference(mkRef(
                        EntityKind.valueOf(record.getEntityKind()),
                        record.getEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
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
     * @param diagramId the diagram to remove entites from
     * @return count of removed diagrams
     */
    public int deleteForDiagram(long diagramId) {
        return dsl
                .deleteFrom(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.notIn(EntityKind.MEASURABLE.name(), EntityKind.CHANGE_INITIATIVE.name()))
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
                .from(fde)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


}
