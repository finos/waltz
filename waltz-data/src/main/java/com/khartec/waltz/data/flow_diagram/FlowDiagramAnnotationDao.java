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
