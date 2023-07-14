package org.finos.waltz.data.attestation;


import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.attestation.ImmutableLogicalFlowAttestationPreChecks;
import org.finos.waltz.model.attestation.LogicalFlowAttestationPreChecks;
import org.finos.waltz.schema.tables.ApplicationGroup;
import org.finos.waltz.schema.tables.ApplicationGroupEntry;
import org.finos.waltz.schema.tables.DataType;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.LogicalFlowDecorator;
import org.jooq.CommonTableExpression;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.SelectOrderByStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.APPLICATION_GROUP;
import static org.finos.waltz.schema.Tables.APPLICATION_GROUP_ENTRY;
import static org.finos.waltz.schema.Tables.LOGICAL_FLOW;
import static org.finos.waltz.schema.Tables.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.schema.tables.DataType.DATA_TYPE;

@Repository
public class AttestationPreCheckDao {

    public static final String EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK = "EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK";
    public static final String EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK = "EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK";
    public static final String EXEMPT_FROM_FLOW_COUNT_CHECK = "EXEMPT_FROM_FLOW_COUNT_CHECK";
    public static final String GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK = "LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK";
    public static final String GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK = "LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK";
    public static final String GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK = "LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK";

    private static final LogicalFlow lf = LOGICAL_FLOW;
    private static final LogicalFlowDecorator lfd = LOGICAL_FLOW_DECORATOR;
    private static final ApplicationGroupEntry age = APPLICATION_GROUP_ENTRY;
    private static final ApplicationGroup ag = APPLICATION_GROUP;
    private static final DataType dt = DATA_TYPE;

    private final DSLContext dsl;

    @Autowired
    public AttestationPreCheckDao(DSLContext dsl) {
        this.dsl = checkNotNull(dsl, "DSL cannot be null");
    }

    
    public LogicalFlowAttestationPreChecks calcLogicalFlowAttestationPreChecks(EntityReference ref) {

        Condition upstreamFlowsCondition = lf.TARGET_ENTITY_KIND.eq(ref.kind().name()).and(lf.TARGET_ENTITY_ID.eq(ref.id()));
        Condition downstreamFlowsCondition = lf.SOURCE_ENTITY_KIND.eq(ref.kind().name()).and(lf.SOURCE_ENTITY_ID.eq(ref.id()));

        CommonTableExpression<Record1<Long>> upstreamFlows = mkInScopeFlowsQry("upstream_flows", upstreamFlowsCondition);
        CommonTableExpression<Record1<Long>> anyFlows = mkInScopeFlowsQry("any_flows", upstreamFlowsCondition.or(downstreamFlowsCondition));

        CommonTableExpression<Record2<String, Integer>> flowCount = DSL
                .name("flow_count")
                .as(DSL
                        .select(DSL.val("FLOWS").as("chk"),
                                DSL.count().as("count"))
                        .from(anyFlows));

        CommonTableExpression<Record2<String, Integer>> unknownFlowCount = DSL
                .name("unknown_flow_count")
                .as(DSL
                        .select(DSL.val("UNKNOWN").as("chk"),
                                DSL.count().as("count"))
                        .from(lfd)
                        .where(lfd.LOGICAL_FLOW_ID.in(DSL
                                .select(upstreamFlows.field(0, Long.class))
                                .from(upstreamFlows)))
                        .and(lfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                        .and(lfd.DECORATOR_ENTITY_ID.in(DSL.select(dt.ID).from(dt).where(dt.UNKNOWN.isTrue()))));

        CommonTableExpression<Record2<String, Integer>> deprecatedFlowCount = DSL
                .name("deprecated_flow_count")
                .as(DSL
                        .select(DSL.val("DEPRECATED").as("chk"),
                                DSL.count().as("count"))
                        .from(lfd)
                        .where(lfd.LOGICAL_FLOW_ID.in(DSL
                                .select(upstreamFlows.field(0, long.class))
                                .from(upstreamFlows)))
                        .and(lfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                        .and(lfd.DECORATOR_ENTITY_ID.in(DSL.select(dt.ID).from(dt).where(dt.DEPRECATED.isTrue()))));

        CommonTableExpression<Record2<String, Integer>> exemptForMustHaveFlows = mkExemptionCTE(
                ref,
                EXEMPT_FROM_FLOW_COUNT_CHECK,
                GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_FLOW_COUNT_CHECK);

        CommonTableExpression<Record2<String, Integer>> exemptForUnknownFlows = mkExemptionCTE(
                ref,
                EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK,
                GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK);

        CommonTableExpression<Record2<String, Integer>> exemptForDeprecatedFlows = mkExemptionCTE(
                ref,
                EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK,
                GROUP_LOGICAL_FLOW_ATTESTATION_EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK);


        SelectOrderByStep<Record> qry = dsl
                .with(upstreamFlows)
                .with(anyFlows)
                .with(unknownFlowCount)
                .with(deprecatedFlowCount)
                .with(exemptForMustHaveFlows)
                .with(exemptForUnknownFlows)
                .with(exemptForDeprecatedFlows)
                .with(flowCount)
                .select(unknownFlowCount.asterisk()).from(unknownFlowCount)
                .union(DSL.select(deprecatedFlowCount.asterisk()).from(deprecatedFlowCount))
                .union(DSL.select(exemptForMustHaveFlows.asterisk()).from(exemptForMustHaveFlows))
                .union(DSL.select(exemptForUnknownFlows.asterisk()).from(exemptForUnknownFlows))
                .union(DSL.select(exemptForDeprecatedFlows.asterisk()).from(exemptForDeprecatedFlows))
                .union(DSL.select(flowCount.asterisk()).from(flowCount));

        ImmutableLogicalFlowAttestationPreChecks.Builder builder = ImmutableLogicalFlowAttestationPreChecks.builder();

        qry.forEach(r -> {
            String check = r.get("chk", String.class);
            int count = r.get("count", Integer.class);
            switch (check) {
                case "FLOWS":
                    builder.flowCount(count);
                    break;
                case "UNKNOWN":
                    builder.unknownCount(count);
                    break;
                case "DEPRECATED":
                    builder.deprecatedCount(count);
                    break;
                case EXEMPT_FROM_FLOW_COUNT_CHECK:
                    builder.exemptFromFlowCountCheck(count > 0);
                    break;
                case EXEMPT_FROM_UNKNOWN_DATA_TYPE_CHECK:
                    builder.exemptFromUnknownCheck(count > 0);
                    break;
                case EXEMPT_FROM_DEPRECATED_DATA_TYPE_CHECK:
                    builder.exemptFromDeprecatedCheck(count > 0);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected check: " + check);
            }
        });

        return builder.build();
    }

    private CommonTableExpression<Record1<Long>> mkInScopeFlowsQry(String cteName, Condition inScopeFlowsCondition) {
        return DSL
                .name(cteName)
                .as(DSL
                        .select(lf.ID)
                        .from(lf)
                        .where(inScopeFlowsCondition)
                        .and(lf.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
                        .and(lf.IS_REMOVED.isFalse()));
    }


    private CommonTableExpression<Record2<String, Integer>> mkExemptionCTE(EntityReference ref,
                                                                           String checkName,
                                                                           String groupExtId) {
        return DSL
                .name(checkName)
                .as(DSL
                    .select(DSL.val(checkName).as("chk"),
                            DSL.count().as("count"))
                    .from(age)
                    .where(age.APPLICATION_ID.eq(ref.id()))
                    .and(age.GROUP_ID.eq(DSL
                            .select(ag.ID)
                            .from(ag)
                            .where(ag.EXTERNAL_ID.eq(groupExtId)))));
    }


}
