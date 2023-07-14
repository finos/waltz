package org.finos.waltz.jobs.tools.diagrams;

import org.finos.waltz.data.measurable.MeasurableIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.schema.tables.*;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.CaseConditionStep;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Record6;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static java.lang.String.format;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;

public class FlowDiagramFromMeasurableGenerator {

    private static final MeasurableIdSelectorFactory measurableSelectorFactory = new MeasurableIdSelectorFactory();
    private static final Application app = APPLICATION;
    private static final Measurable m = MEASURABLE;
    private static final MeasurableRating mr = MEASURABLE_RATING;
    private static final FlowDiagram fd = FLOW_DIAGRAM;
    private static final FlowDiagramEntity fde = FLOW_DIAGRAM_ENTITY;
    private static final FlowDiagramOverlayGroup fdog = FLOW_DIAGRAM_OVERLAY_GROUP;
    private static final FlowDiagramOverlayGroupEntry fdoge = FLOW_DIAGRAM_OVERLAY_GROUP_ENTRY;
    private static final LogicalFlow lf = LOGICAL_FLOW;
    private static final EntityHierarchy eh = ENTITY_HIERARCHY;

    private static final Condition activeAppCondition = app.ENTITY_LIFECYCLE_STATUS.notEqual(EntityLifecycleStatus.REMOVED.name()).and(app.IS_REMOVED.isFalse());

    private final DSLContext dsl;
    private final int nodeLimit = 120;


    public FlowDiagramFromMeasurableGenerator(DSLContext dsl) {
        this.dsl = dsl;
    }


    private void go(long measurableId) {
        dsl.transaction(ctx -> {
            DSLContext tx = ctx.dsl();

            Select<Record1<Long>> measurableSelector = mkMeasurableSelector(tx, measurableId);
            SelectConditionStep<Record1<Long>> nodeSelector = mkNodesQuery(tx, measurableSelector);

            checkNodeLimit(tx, nodeSelector);

            String measurableName = loadMeasurableName(tx, measurableId);
            Long diagramId = createDiagram(tx, measurableName);
            createNodes(tx, diagramId, nodeSelector);
            createFlows(tx, diagramId, nodeSelector);
            createLinkedEntities(tx, diagramId, measurableSelector);
            createOverlayGroup(tx, diagramId, measurableId, measurableSelector, measurableName);

            System.out.printf("https://int1.waltz.intranet.db.com/waltz-dev2/flow-diagram/%d\n", diagramId);
        });

        // layout ?
        // annotations ?

    }


    private void checkNodeLimit(DSLContext tx, SelectConditionStep<Record1<Long>> nodeSelector) {
        SelectJoinStep<Record1<Integer>> qry = tx.selectCount().from(nodeSelector);
        Integer nodeCount = qry.fetchOne(0, Integer.class);
        if (nodeCount > nodeLimit) {
            throw new IllegalStateException(format(
                    "Cannot draw, too many nodes: %d, limit is: %d",
                    nodeCount,
                    nodeLimit));
        }
    }


    private Select<Record1<Long>> mkMeasurableSelector(DSLContext tx,
                                                       long measurableId) {
        Select<Record1<Long>> baseSelector = measurableSelectorFactory.apply(toMeasurableRef(measurableId));
        return tx
            .selectDistinct(eh.ANCESTOR_ID)
            .from(mr)
            .innerJoin(eh).on(mr.MEASURABLE_ID.eq(eh.ID).and(eh.KIND.eq(EntityKind.MEASURABLE.name())))
            .innerJoin(app).on(app.ID.eq(mr.ENTITY_ID).and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
            .where(tx.renderInlined(eh.ANCESTOR_ID.in(baseSelector).and(activeAppCondition)));
    }


    private void createOverlayGroup(DSLContext tx,
                                    Long diagramId,
                                    long measurableId,
                                    Select<Record1<Long>> measurableSelector,
                                    String measurableName) {
        Long overlayGroupId = tx
                .insertInto(fdog)
                .columns(fdog.FLOW_DIAGRAM_ID, fdog.NAME, fdog.DESCRIPTION, fdog.IS_DEFAULT, fdog.EXTERNAL_ID)
                .values(DSL.val(diagramId),
                        DSL.val("Overlays for: " + measurableName),
                        DSL.val("Overlays for: " + measurableName + " and any child viewpoints"),
                        DSL.val(true),
                        DSL.val("fdog_" + measurableId))
                .returning(fdog.ID)
                .fetchOne()
                .get(fdog.ID);

        Field<Long> mid = measurableSelector.field(0, Long.class);

        CaseConditionStep<String> colorPick = DSL
                .when(mid.mod(11).eq(0L), DSL.val("red"))
                .when(mid.mod(11).eq(1L), DSL.val("green"))
                .when(mid.mod(11).eq(2L), DSL.val("blue"))
                .when(mid.mod(11).eq(3L), DSL.val("purple"))
                .when(mid.mod(11).eq(4L), DSL.val("orange"))
                .when(mid.mod(11).eq(5L), DSL.val("pink"))
                .when(mid.mod(11).eq(6L), DSL.val("turquoise"))
                .when(mid.mod(11).eq(7L), DSL.val("magenta"))
                .when(mid.mod(11).eq(8L), DSL.val("plum"))
                .when(mid.mod(11).eq(9L), DSL.val("gold"))
                .when(mid.mod(11).eq(10L), DSL.val("teal"));

        CaseConditionStep<String> symbolPick = DSL
                .when(mid.mod(7).eq(0L), DSL.val("circle"))
                .when(mid.mod(7).eq(1L), DSL.val("square"))
                .when(mid.mod(7).eq(2L), DSL.val("diamond"))
                .when(mid.mod(7).eq(3L), DSL.val("wye"))
                .when(mid.mod(7).eq(4L), DSL.val("star"))
                .when(mid.mod(7).eq(5L), DSL.val("triangle"))
                .when(mid.mod(7).eq(6L), DSL.val("cross"));

        SelectJoinStep<Record6<Long, Long, String, String, String, String>> overlaysToInsert = DSL
                .select(DSL.val(overlayGroupId),
                        mid,
                        DSL.val(EntityKind.MEASURABLE.name()),
                        colorPick,
                        DSL.val("grey"),
                        symbolPick)
                .from(measurableSelector);


        tx.insertInto(fdoge)
                .columns(
                        fdoge.OVERLAY_GROUP_ID,
                        fdoge.ENTITY_ID,
                        fdoge.ENTITY_KIND,
                        fdoge.FILL,
                        fdoge.STROKE,
                        fdoge.SYMBOL)
                .select(overlaysToInsert)
                .execute();
    }


    private void createLinkedEntities(DSLContext tx,
                                      Long diagramId,
                                      Select<Record1<Long>> measurableSelector) {
        SelectJoinStep<Record4<Long, Long, String, Boolean>> measurablesToInsert = DSL
                .select(DSL.val(diagramId),
                        measurableSelector.field(0, Long.class),
                        DSL.val(EntityKind.MEASURABLE.name()),
                        DSL.val(true))
                .from(measurableSelector);

        tx.insertInto(fde)
                .columns(fde.DIAGRAM_ID, fde.ENTITY_ID, fde.ENTITY_KIND, fde.IS_NOTABLE)
                .select(measurablesToInsert)
                .execute();
    }


    private void createFlows(DSLContext tx,
                             Long diagramId,
                             SelectConditionStep<Record1<Long>> nodeSelector) {
        Condition sourceInScope = lf.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()).and(lf.SOURCE_ENTITY_ID.in(nodeSelector));
        Condition targetInScope = lf.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()).and(lf.TARGET_ENTITY_ID.in(nodeSelector));
        SelectConditionStep<Record1<Long>> flowSelector = tx
                .select(lf.ID)
                .from(lf)
                .where(sourceInScope.and(targetInScope))
                .and(lf.IS_REMOVED.isFalse())
                .and(lf.ENTITY_LIFECYCLE_STATUS.notEqual(EntityLifecycleStatus.REMOVED.name()));

        SelectJoinStep<Record4<Long, Long, String, Boolean>> flowsToInsert = DSL
                .select(DSL.val(diagramId),
                        flowSelector.field(lf.ID),
                        DSL.val(EntityKind.LOGICAL_DATA_FLOW.name()),
                        DSL.val(false))
                .from(flowSelector);

        tx.insertInto(fde)
                .columns(fde.DIAGRAM_ID, fde.ENTITY_ID, fde.ENTITY_KIND, fde.IS_NOTABLE)
                .select(flowsToInsert)
                .execute();
    }


    private String loadMeasurableName(DSLContext tx,
                                      long measurableId) {
        String measurableName = tx
                .select(m.NAME)
                .from(m)
                .where(m.ID.eq(measurableId))
                .fetchOne(m.NAME);
        return measurableName;
    }


    private void createNodes(DSLContext tx,
                             Long diagramId,
                             Select<Record1<Long>> nodeSelector) {
        SelectJoinStep<Record4<Long, Long, String, Boolean>> nodesToInsert = DSL
                .select(DSL.val(diagramId),
                        nodeSelector.field(mr.ENTITY_ID),
                        DSL.val(EntityKind.APPLICATION.name()),
                        DSL.val(true))
                .from(nodeSelector);

        tx.insertInto(fde)
                .columns(fde.DIAGRAM_ID, fde.ENTITY_ID, fde.ENTITY_KIND, fde.IS_NOTABLE)
                .select(nodesToInsert)
                .execute();
    }


    private Long createDiagram(DSLContext tx,
                               String measurableName) {
        Long diagramId = tx
                .insertInto(fd)
                .columns(
                        fd.NAME,
                        fd.DESCRIPTION,
                        fd.LAYOUT_DATA,
                        fd.LAST_UPDATED_BY,
                        fd.EDITOR_ROLE)
                .values(
                        measurableName + " : Diagram",
                        "Auto generated diagram for : " + measurableName,
                        "{\"positions\": {}, \"diagramTransform\": \"\"}",
                        "FlowDiagramFromMeasurableGenerator",
                        "DIAGRAM_ADMIN")
                .returning(fd.ID)
                .fetchOne()
                .get(fd.ID);
        return diagramId;
    }


    private SelectConditionStep<Record1<Long>> mkNodesQuery(DSLContext tx,
                                                            Select<Record1<Long>> measurableSelector) {
        Condition cond = mr.MEASURABLE_ID.in(measurableSelector)
                .and(activeAppCondition);
        return tx
                .selectDistinct(mr.ENTITY_ID)
                .from(mr)
                .innerJoin(app).on(app.ID.eq(mr.ENTITY_ID).and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(tx.renderInlined(cond));
    }


    private IdSelectionOptions toMeasurableRef(long measurableId) {
        return mkOpts(mkRef(EntityKind.MEASURABLE, measurableId));
    }


    // -- BOOT --

    public static void main(String[] args) {
        long measurableId = 37694L; // cash mgmt

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        new FlowDiagramFromMeasurableGenerator(dsl).go(measurableId);
    }

}
