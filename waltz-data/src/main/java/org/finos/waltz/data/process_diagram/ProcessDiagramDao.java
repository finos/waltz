package org.finos.waltz.data.process_diagram;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.process_diagram.ImmutableProcessDiagram;
import org.finos.waltz.model.process_diagram.ImmutableProcessDiagramEntity;
import org.finos.waltz.model.process_diagram.ProcessDiagram;
import org.finos.waltz.model.process_diagram.ProcessDiagramEntity;
import org.finos.waltz.model.process_diagram.ProcessDiagramKind;
import org.finos.waltz.schema.tables.records.ProcessDiagramEntityRecord;
import org.finos.waltz.schema.tables.records.ProcessDiagramRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.finos.waltz.schema.Tables.PROCESS_DIAGRAM;
import static org.finos.waltz.schema.Tables.PROCESS_DIAGRAM_ENTITY;

@Repository
public class ProcessDiagramDao {

    private static final Set<Field<?>> FIELDS_WITHOUT_LAYOUT = SetUtilities.minus(
            SetUtilities.asSet(PROCESS_DIAGRAM.fields()),
            SetUtilities.asSet(PROCESS_DIAGRAM.LAYOUT_DATA));

    private static final RecordMapper<? super Record, ProcessDiagram> RECORD_TO_DIAGRAM_MAPPER = record -> {
        ProcessDiagramRecord r = record.into(PROCESS_DIAGRAM);
        return ImmutableProcessDiagram
                .builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .externalId(ofNullable(r.getExternalId()))
                .diagramKind(readEnum(r.getDiagramKind(), ProcessDiagramKind.class, (x) -> ProcessDiagramKind.WALTZ_SPARX))
                .layoutData(ofNullable(r.getLayoutData()))
                .provenance(r.getProvenance())
                .createdAt(toLocalDateTime(r.getCreatedAt()))
                .createdBy(r.getCreatedBy())
                .lastUpdatedAt(toLocalDateTime(r.getLastUpdatedAt()))
                .lastUpdatedBy(r.getLastUpdatedBy())
                .build();
    };


    private static final RecordMapper<? super Record, ProcessDiagramEntity> RECORD_TO_DIAGRAM_ENTITY_MAPPER = record -> {
        ProcessDiagramEntityRecord r = record.into(PROCESS_DIAGRAM_ENTITY);
        return ImmutableProcessDiagramEntity
                .builder()
                .diagramId(r.getDiagramId())
                .entityReference(JooqUtilities.readRef(r, PROCESS_DIAGRAM_ENTITY.ENTITY_KIND, PROCESS_DIAGRAM_ENTITY.ENTITY_ID))
                .isNotable(r.getIsNotable())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public ProcessDiagramDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public ProcessDiagram getDiagramById(long diagramId) {
        return dsl
                .select(PROCESS_DIAGRAM.fields())
                .from(PROCESS_DIAGRAM)
                .where(PROCESS_DIAGRAM.ID.eq(diagramId))
                .fetchOne(RECORD_TO_DIAGRAM_MAPPER);
    }


    public ProcessDiagram getDiagramByExternalId(String externalId) {
        return dsl
                .select(PROCESS_DIAGRAM.fields())
                .from(PROCESS_DIAGRAM)
                .where(PROCESS_DIAGRAM.EXTERNAL_ID.eq(externalId))
                .fetchOne(RECORD_TO_DIAGRAM_MAPPER);
    }


    public Set<ProcessDiagramEntity> findDiagramEntitiesById(long diagramId) {
        return dsl
                .select(PROCESS_DIAGRAM_ENTITY.fields())
                .from(PROCESS_DIAGRAM_ENTITY)
                .where(PROCESS_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .fetchSet(RECORD_TO_DIAGRAM_ENTITY_MAPPER);
    }


    public Set<ProcessDiagram> findByGenericSelector(GenericSelector selector) {
        SelectConditionStep<Record> qry = dsl
                .selectDistinct(FIELDS_WITHOUT_LAYOUT)
                .from(PROCESS_DIAGRAM)
                .innerJoin(PROCESS_DIAGRAM_ENTITY).on(PROCESS_DIAGRAM_ENTITY.DIAGRAM_ID.eq(PROCESS_DIAGRAM.ID))
                .where(dsl.renderInlined(
                    PROCESS_DIAGRAM_ENTITY.ENTITY_ID.in(selector.selector())
                        .and(PROCESS_DIAGRAM_ENTITY.ENTITY_KIND.eq(selector.kind().name()))));

        return qry
                .fetchSet(RECORD_TO_DIAGRAM_MAPPER);
    }


    public Set<ProcessDiagram> findBySelector(Select<Record1<Long>> diagramSelector) {
        SelectConditionStep<Record> qry = dsl
                .selectDistinct(FIELDS_WITHOUT_LAYOUT)
                .from(PROCESS_DIAGRAM)
                .where(PROCESS_DIAGRAM.ID.in(diagramSelector));

        return qry
                .fetchSet(RECORD_TO_DIAGRAM_MAPPER);
    }


}
