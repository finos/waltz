package com.khartec.waltz.data.process_diagram_entity;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.process_diagram.ImmutableProcessDiagramEntity;
import com.khartec.waltz.model.process_diagram.ImmutableProcessDiagramEntityApplicationAlignment;
import com.khartec.waltz.model.process_diagram.ProcessDiagramEntity;
import com.khartec.waltz.model.process_diagram.ProcessDiagramEntityApplicationAlignment;
import com.khartec.waltz.schema.tables.Measurable;
import com.khartec.waltz.schema.tables.records.ProcessDiagramEntityRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;

@Repository
public class ProcessDiagramEntityDao {

    private final DSLContext dsl;

    private static final Measurable parent_measurable = MEASURABLE.as("parent_measurable");
    private static final Measurable child_measurable = MEASURABLE.as("child_measurable");

    private static final RecordMapper<? super Record, ProcessDiagramEntity> TO_DOMAIN_MAPPER = record -> {
        ProcessDiagramEntityRecord r = record.into(PROCESS_DIAGRAM_ENTITY);
        return ImmutableProcessDiagramEntity
                .builder()
                .diagramId(r.getDiagramId())
                .entityReference(JooqUtilities.readRef(r, PROCESS_DIAGRAM_ENTITY.ENTITY_KIND, PROCESS_DIAGRAM_ENTITY.ENTITY_ID))
                .isNotable(r.getIsNotable())
                .build();
    };

    @Autowired
    public ProcessDiagramEntityDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Set<ProcessDiagramEntity> findDiagramEntitiesById(long diagramId) {
        return dsl
                .select(PROCESS_DIAGRAM_ENTITY.fields())
                .from(PROCESS_DIAGRAM_ENTITY)
                .where(PROCESS_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Set<ProcessDiagramEntityApplicationAlignment> findApplicationAlignmentsByDiagramId(Long diagramId){
        return dsl
                .select(parent_measurable.ID,
                        parent_measurable.NAME,
                        parent_measurable.DESCRIPTION,
                        parent_measurable.EXTERNAL_ID,
                        child_measurable.ID,
                        child_measurable.NAME,
                        child_measurable.DESCRIPTION,
                        child_measurable.EXTERNAL_ID,
                        APPLICATION.ID,
                        APPLICATION.NAME,
                        APPLICATION.DESCRIPTION,
                        APPLICATION.ASSET_CODE)
                .from(PROCESS_DIAGRAM_ENTITY)
                .innerJoin(ENTITY_HIERARCHY).on(PROCESS_DIAGRAM_ENTITY.ENTITY_ID.eq(ENTITY_HIERARCHY.ANCESTOR_ID)
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name()))
                .and(PROCESS_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                .innerJoin(parent_measurable).on(PROCESS_DIAGRAM_ENTITY.ENTITY_ID.eq(parent_measurable.ID))
                .innerJoin(child_measurable).on(ENTITY_HIERARCHY.ID.eq(child_measurable.ID))
                .innerJoin(MEASURABLE_RATING).on(child_measurable.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .innerJoin(APPLICATION).on(MEASURABLE_RATING.ENTITY_ID.eq(APPLICATION.ID)
                .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(PROCESS_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()))
                .and(APPLICATION.IS_REMOVED.isFalse())
                .fetchSet(r -> ImmutableProcessDiagramEntityApplicationAlignment.builder()
                        .diagramMeasurableRef(mkRef(EntityKind.MEASURABLE, r.get(parent_measurable.ID), r.get(parent_measurable.NAME), r.get(parent_measurable.DESCRIPTION), r.get(parent_measurable.EXTERNAL_ID)))
                        .referencedMeasurableRef(mkRef(EntityKind.MEASURABLE, r.get(child_measurable.ID), r.get(child_measurable.NAME), r.get(child_measurable.DESCRIPTION), r.get(child_measurable.EXTERNAL_ID)))
                        .applicationRef(mkRef(EntityKind.APPLICATION, r.get(APPLICATION.ID), r.get(APPLICATION.NAME), r.get(APPLICATION.DESCRIPTION), r.get(APPLICATION.ASSET_CODE)))
                        .build());
    }


}
