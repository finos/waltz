package org.finos.waltz.data.attestation;


import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.attestation.CapabilitiesAttestationPreChecks;
import org.finos.waltz.model.attestation.ImmutableCapabilitiesAttestationPreChecks;
import org.finos.waltz.model.attestation.ImmutableLogicalFlowAttestationPreChecks;
import org.finos.waltz.model.attestation.LogicalFlowAttestationPreChecks;
import org.finos.waltz.schema.tables.*;
import org.finos.waltz.schema.tables.DataType;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.*;
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

    private static final MeasurableCategory mc = MEASURABLE_CATEGORY;
    private static final Measurable measurable = MEASURABLE;
    private static final MeasurableRating mr = MEASURABLE_RATING;
    private static final Allocation allocation = ALLOCATION;

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

    public CapabilitiesAttestationPreChecks calcCapabilitiesAttestationPreChecks(EntityReference ref,
                                                                                 Long categoryId) {

        CommonTableExpression<Record3<Long, Boolean, Long>> capabilities = DSL
                .name("capabilities")
                .as(DSL
                        .select(measurable.ID, measurable.CONCRETE, mr.ID.as("mr_id"))
                        .from(mc)
                        .innerJoin(measurable)
                        .on(measurable.MEASURABLE_CATEGORY_ID.eq(mc.ID))
                        .innerJoin(mr)
                        .on(mr.MEASURABLE_ID.eq(measurable.ID))
                        .where(mr.ENTITY_ID.eq(ref.id()))
                        .and(mc.ID.eq(categoryId)));

        CommonTableExpression<Record1<Long>> nonConcreteCapabilities = DSL
                .name("non_concrete_capabilities")
                .as(DSL
                    .select(capabilities.field(0, Long.class))
                    .from(capabilities)
                    .where(capabilities.field(1, Boolean.class).isFalse()));

        CommonTableExpression<Record2<String, Integer>> mappingCount = DSL
                .name("mapping_count")
                .as(DSL
                        .select(DSL.val("CAPABILITIES").as("chk"),
                                DSL.count().as("count"))
                        .from(capabilities));

        CommonTableExpression<Record2<String, Integer>> nonConcreteCount = DSL
                .name("non_concrete_count")
                .as(DSL
                        .select(DSL.val("NON_CONCRETE").as("chk"),
                                DSL.count().as("count"))
                        .from(nonConcreteCapabilities));

        CommonTableExpression<Record1<Integer>> totalAllocation = DSL
                .name("total_allocation")
                .as(DSL
                        .select(allocation.ALLOCATION_PERCENTAGE)
                        .from(capabilities)
                        .innerJoin(allocation)
                        .on(allocation.MEASURABLE_RATING_ID.eq(capabilities.field("mr_id", Long.class))));

        CommonTableExpression<Record2<String, Serializable>> totalAllocationCount = DSL
                .name("total_allocation_count")
                .as(DSL
                        .select(DSL.val("TOTAL_ALLOCATION").as("chk"),
                                DSL.coalesce(
                                        DSL.sum(totalAllocation.field(0, Long.class)), 0)
                                        .as("count"))
                        .from(totalAllocation));


        CommonTableExpression<Record1<Long>> zeroAllocation = DSL
                .name("zero_allocation")
                .as(DSL
                        .select(capabilities.field(0, Long.class))
                        .from(capabilities)
                        .leftJoin(allocation)
                        .on(allocation.MEASURABLE_RATING_ID.eq(capabilities.field("mr_id", Long.class)))
                        .where(allocation.ALLOCATION_PERCENTAGE.isNull()));

        CommonTableExpression<Record2<String, Integer>> zeroAllocationCount = DSL
                .name("zero_allocation_count")
                .as(DSL
                        .select(DSL.val("ZERO_ALLOCATION").as("chk"),
                                DSL.count().as("count"))
                        .from(zeroAllocation));

        SelectOrderByStep<Record> qry = dsl
                .with(capabilities)
                .with(nonConcreteCapabilities)
                .with(mappingCount)
                .with(nonConcreteCount)
                .with(totalAllocation)
                .with(totalAllocationCount)
                .with(zeroAllocation)
                .with(zeroAllocationCount)
                .select(mappingCount.asterisk()).from(mappingCount)
                .union(DSL.select(nonConcreteCount.asterisk()).from(nonConcreteCount))
                .union(DSL.select(totalAllocationCount.asterisk()).from(totalAllocationCount))
                .union(DSL.select(zeroAllocationCount.asterisk()).from(zeroAllocationCount));


        ImmutableCapabilitiesAttestationPreChecks.Builder builder = ImmutableCapabilitiesAttestationPreChecks.builder();

        System.out.println("Query: " + qry);
        qry.forEach(r -> {
            String check = r.get("chk", String.class);
            int count = r.get("count", Integer.class);
            switch (check) {
                case "CAPABILITIES":
                    builder.mappingCount(count);
                    break;
                case "NON_CONCRETE":
                    builder.nonConcreteCount(count);
                    break;
                case "TOTAL_ALLOCATION":
                    builder.totalAllocation(count);
                    break;
                case "ZERO_ALLOCATION":
                    builder.zeroAllocationCount(count);
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
