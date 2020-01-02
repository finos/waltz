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


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.flow_diagram.FlowDiagramAnnotation;
import com.khartec.waltz.model.flow_diagram.ImmutableFlowDiagramAnnotation;
import com.khartec.waltz.schema.tables.records.FlowDiagramAnnotationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.FLOW_DIAGRAM_ANNOTATION;
import static java.util.stream.Collectors.toList;

@Repository
public class FlowDiagramAnnotationDao {


    private static final RecordMapper<Record, FlowDiagramAnnotation> TO_DOMAIN_MAPPER = r -> {
        FlowDiagramAnnotationRecord record = r.into(FLOW_DIAGRAM_ANNOTATION);
        return ImmutableFlowDiagramAnnotation.builder()
                .annotationId(record.getAnnotationId())
                .diagramId(record.getDiagramId())
                .entityReference(mkRef(
                        EntityKind.valueOf(record.getEntityKind()),
                        record.getEntityId()))
                .note(record.getNote())
                .build();
    };


    public static final Function<FlowDiagramAnnotation, FlowDiagramAnnotationRecord> TO_RECORD_MAPPER = a -> {
        EntityReference entity = a.entityReference();

        FlowDiagramAnnotationRecord record = new FlowDiagramAnnotationRecord();
        record.setAnnotationId(a.annotationId());
        record.setDiagramId(a.diagramId().get());
        record.setEntityId(entity.id());
        record.setEntityKind(entity.kind().name());
        record.setNote(a.note());

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public FlowDiagramAnnotationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<FlowDiagramAnnotation> findByDiagramId(long diagramId) {
        return dsl
                .selectFrom(FLOW_DIAGRAM_ANNOTATION)
                .where(FLOW_DIAGRAM_ANNOTATION.DIAGRAM_ID.eq(diagramId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int[] createAnnotations(List<FlowDiagramAnnotation> annotations) {
        List<FlowDiagramAnnotationRecord> records = annotations.stream()
                .map(TO_RECORD_MAPPER::apply)
                .collect(toList());

        return dsl.batchInsert(records)
                .execute();
    }


    public int deleteForDiagram(long diagramId) {
        return dsl.deleteFrom(FLOW_DIAGRAM_ANNOTATION)
                .where(FLOW_DIAGRAM_ANNOTATION.DIAGRAM_ID.eq(diagramId))
                .execute();
    }

    public void clone(long diagramId, Long clonedDiagramId) {
        List<FlowDiagramAnnotation> diagramAnnotations = findByDiagramId(diagramId);
        List<FlowDiagramAnnotation> clonedDiagramAnnotations = map(diagramAnnotations, d -> ImmutableFlowDiagramAnnotation
                .copyOf(d)
                .withDiagramId(clonedDiagramId));
        createAnnotations(clonedDiagramAnnotations);

    }
}
