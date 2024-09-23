package org.finos.waltz.data.data_type;

import org.finos.waltz.common.Checks;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataTypeMigrationResult;
import org.finos.waltz.model.datatype.ImmutableDataTypeMigrationResult;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.DataType;
import org.finos.waltz.schema.tables.DataTypeUsage;
import org.finos.waltz.schema.tables.FlowClassificationRule;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.LogicalFlowDecorator;
import org.finos.waltz.schema.tables.PhysicalFlow;
import org.finos.waltz.schema.tables.PhysicalSpecDataType;
import org.finos.waltz.schema.tables.PhysicalSpecification;
import org.finos.waltz.schema.tables.records.LogicalFlowRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.DATA_TYPE;

public class DataTypeUtilities {

    private static final Logger LOG  = LoggerFactory.getLogger(DataTypeUtilities.class);
    private static final DataType dataType = DATA_TYPE;
    private static final LogicalFlow logicalFlow = Tables.LOGICAL_FLOW;
    private static final LogicalFlowDecorator logicalFlowDecorator = Tables.LOGICAL_FLOW_DECORATOR;
    private static final FlowClassificationRule flowClassificationRule = Tables.FLOW_CLASSIFICATION_RULE;
    private static final PhysicalFlow physicalFlow = Tables.PHYSICAL_FLOW;
    private static final PhysicalSpecification physicalSpec = Tables.PHYSICAL_SPECIFICATION;
    private static final PhysicalSpecDataType physicalSpecDataType = Tables.PHYSICAL_SPEC_DATA_TYPE;
    private static final DataTypeUsage dataTypeUsage = Tables.DATA_TYPE_USAGE;

    public static long changeParent(DSLContext dsl,
                                    String childCode,
                                    String newParentCode) {
        LOG.debug("Change parent from: {} to {}", childCode, newParentCode);

        Long childId = dsl
                .select(dataType.ID)
                .from(dataType)
                .where(dataType.CODE.eq(childCode))
                .fetchOne(dataType.ID);

        Long newParentId = dsl
                .select(dataType.ID)
                .from(dataType)
                .where(dataType.CODE.eq(newParentCode))
                .fetchOne(dataType.ID);

        return dsl.update(dataType)
                .set(dataType.PARENT_ID, newParentId)
                .where(dataType.ID.eq(childId))
                .execute();
    }

    public static Map<Tuple2<EntityReference, EntityReference>, LogicalFlowRecord> getAllLogicalFlowsBySourceTargetMap(DSLContext dsl) {
        return dsl
            .selectFrom(logicalFlow)
            .fetch()
            .stream()
            .collect(toMap(
                t -> Tuple.tuple(
                    mkRef(EntityKind.valueOf(t.getSourceEntityKind()), t.getSourceEntityId()),
                    mkRef(EntityKind.valueOf(t.getTargetEntityKind()), t.getTargetEntityId())),
                t -> t));
    }

    public static Map<Long, Set<Long>> getLogicalFlowToPhysicalSpecId(DSLContext dsl) {
        return dsl
            .select(physicalFlow.LOGICAL_FLOW_ID, physicalFlow.SPECIFICATION_ID)
            .from(physicalFlow)
            .where(physicalFlow.IS_REMOVED.eq(false)
            .and(physicalFlow.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name())))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                t -> t.get(physicalFlow.LOGICAL_FLOW_ID),
                Collectors.mapping(
                    t -> t.get(physicalFlow.SPECIFICATION_ID),
                    toSet())));
    }

    private static long removeDataType(DSLContext dsl,
                                      String fromCode) {
        return dsl
                .delete(dataType)
                .where(dataType.CODE.eq(fromCode))
                .execute();
    }


    private static boolean removeDataType(DSLContext dsl,
                                          long fromId) {
        return dsl
                .delete(dataType)
                .where(dataType.ID.eq(fromId))
                .execute() > 0;
    }


    public static void migrate(DSLContext dsl,
                        String fromCode,
                        String toCode,
                        boolean deleteOldDataType){

        Long fromId = dsl
                .select(dataType.ID)
                .from(dataType)
                .where(dataType.CODE.eq(fromCode))
                .fetchOne(dataType.ID);

        Long toId = dsl
                .select(dataType.ID)
                .from(dataType)
                .where(dataType.CODE.eq(toCode))
                .fetchOne(dataType.ID);

        LOG.debug("Migrate from: {}/{} to {}/{}", fromCode, fromId, toCode, toId);


        // 1) update data_type_usage set code = toCode where code = fromCode
        // 2) update logical_flow_decorator set decorator_entity_id = (toId) where decorator_entity_id = (fromId) and decorator_entity_kind = 'DATA_TYPE'
        // 3) update physical_spec_data_type set data_type_id = (toId) where data_type_id = (fromId)
        // 4) update flow_classification_rules set data_type_id = toId where data_type_id = fromId
        // x) delete from dataType where code = 'fromCode'

        migrate(dsl, fromId, toId, deleteOldDataType);

    }

    private static void verifyDataTypeHasNoChildren(DSLContext dsl, Long fromId) {
        int childCount = dsl.fetchCount(DATA_TYPE, DATA_TYPE.PARENT_ID.eq(fromId));
        Checks.checkTrue(childCount == 0, "Data Type, %d has %d children", fromId, childCount);
    }

    public static DataTypeMigrationResult migrate(DSLContext dsl, Long fromId, Long toId, boolean deleteOldDataType) {
        if (deleteOldDataType) {
            verifyDataTypeHasNoChildren(dsl, fromId);
        }

        int dtuCount = migrateDataTypeUsage(dsl, fromId, toId);
        int crCount = migrateFlowClassificationRules(dsl, fromId, toId);
        int lfCount = migrateLogicalFlowDecorator(dsl, fromId, toId);
        int psCount = migratePhysicalSpecDataType(dsl, fromId, toId);
        boolean dataTypeRemoved = deleteOldDataType
                ? removeDataType(dsl, fromId)
                : false;

        return ImmutableDataTypeMigrationResult
                .builder()
                .usageCount(dtuCount)
                .classificationRuleCount(crCount)
                .logicalFlowDataTypeCount(lfCount)
                .physicalSpecDataTypeCount(psCount)
                .dataTypeRemoved(dataTypeRemoved)
                .build();
    }

    private static int migratePhysicalSpecDataType(DSLContext dsl,
                                                   Long fromId,
                                                   Long toId) {
        PhysicalSpecDataType physicSpec = physicalSpecDataType.as("physicSpec");
        Condition notAlreadyExists = DSL
                .notExists(DSL
                        .select(physicSpec.SPECIFICATION_ID)
                        .from(physicSpec)
                        .where(physicSpec.SPECIFICATION_ID.eq(physicalSpecDataType.SPECIFICATION_ID)
                            .and(physicSpec.DATA_TYPE_ID.eq(toId))));;

        int updateCount = dsl
                .update(physicalSpecDataType)
                .set(physicalSpecDataType.DATA_TYPE_ID, toId)
                .where(physicalSpecDataType.DATA_TYPE_ID.eq(fromId)
                        .and(notAlreadyExists))
                .execute();

        int rmCount = dsl
                .delete(physicalSpecDataType)
                .where(physicalSpecDataType.DATA_TYPE_ID.eq(fromId))
                .execute();

        LOG.info("Migrate Phys Spec Data Type Usage: {} -> {},  updated: {}, removed: {}", fromId, toId, updateCount, rmCount);
        return updateCount + rmCount;

    }

    private static int migrateLogicalFlowDecorator(DSLContext dsl,
                                                   Long fromId,
                                                   Long toId) {
        LogicalFlowDecorator decorator = logicalFlowDecorator.as("decorator");

        Condition notAlreadyExists = DSL.notExists(DSL
                .select(decorator.LOGICAL_FLOW_ID)
                .from(decorator)
                .where(decorator.LOGICAL_FLOW_ID.eq(logicalFlowDecorator.LOGICAL_FLOW_ID)
                        .and(decorator.DECORATOR_ENTITY_ID.eq(toId))
                        .and(decorator.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))));

        int updateCount = dsl
                .update(logicalFlowDecorator)
                .set(logicalFlowDecorator.DECORATOR_ENTITY_ID, toId)
                .where(logicalFlowDecorator.DECORATOR_ENTITY_ID.eq(fromId)
                        .and(logicalFlowDecorator.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                        .and(notAlreadyExists))
                .execute();

        int rmCount = dsl
                .delete(logicalFlowDecorator)
                .where(logicalFlowDecorator.DECORATOR_ENTITY_ID.eq(fromId)
                        .and(logicalFlowDecorator.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .execute();

        LOG.info("Migrate Logical Flow Decorator: {} -> {},  updated: {}, removed: {}", fromId, toId, updateCount, rmCount);

        return updateCount + rmCount;
    }


    private static int migrateFlowClassificationRules(DSLContext dsl,
                                                      Long fromId,
                                                      Long toId) {

        FlowClassificationRule authSrc = flowClassificationRule.as("authSrc");

        Condition notAlreadyExists = DSL.notExists(DSL
                .select(authSrc.ID)
                .from(authSrc)
                .where(authSrc.PARENT_KIND.eq(flowClassificationRule.PARENT_KIND)
                        .and(authSrc.PARENT_ID.eq(flowClassificationRule.PARENT_ID))
                        .and(authSrc.DATA_TYPE_ID.eq(toId))
                        .and(authSrc.SUBJECT_ENTITY_ID.eq(flowClassificationRule.SUBJECT_ENTITY_ID)
                            .and(authSrc.SUBJECT_ENTITY_KIND.eq(flowClassificationRule.SUBJECT_ENTITY_KIND)))));

        int updateCount = dsl
                .update(flowClassificationRule)
                .set(flowClassificationRule.DATA_TYPE_ID, toId)
                .where(flowClassificationRule.DATA_TYPE_ID.eq(fromId)
                        .and(notAlreadyExists))
                .execute();

        int rmCount = dsl
                .delete(flowClassificationRule)
                .where(flowClassificationRule.DATA_TYPE_ID.eq(fromId))
                .execute();

        LOG.info("Migrate Flow Classification Rules: {} -> {},  updated: {}, removed: {}", fromId, toId, updateCount, rmCount);
        return updateCount + rmCount;

    }


    private static int migrateDataTypeUsage(DSLContext dsl,
                                            Long fromId,
                                            Long toId) {
        DataTypeUsage dtu = dataTypeUsage.as("dtu");

        Condition condition = DSL.notExists(DSL
                .select(dtu.ENTITY_ID)
                .from(dtu)
                .where(dataTypeUsage.ENTITY_ID.eq(dtu.ENTITY_ID)
                        .and(dataTypeUsage.ENTITY_KIND.eq(dtu.ENTITY_KIND))
                        .and(dtu.DATA_TYPE_ID.eq(toId))
                        .and(dataTypeUsage.USAGE_KIND.eq(dtu.USAGE_KIND))));

        int updateCount = dsl
                .update(dataTypeUsage)
                .set(dataTypeUsage.DATA_TYPE_ID, toId)
                .where(dataTypeUsage.DATA_TYPE_ID.eq(fromId)
                        .and(condition))
                .execute();

        int rmCount = dsl
                .delete(dataTypeUsage)
                .where(dataTypeUsage.DATA_TYPE_ID.eq(fromId))
                .execute();

        LOG.info("Migrate DataType Usage: {} -> {},  updated: {}, removed: {}", fromId, toId, updateCount, rmCount);

        return updateCount + rmCount;
    }

    public static List<Long> findLogicalFlowIdsForDataType(DSLContext dsl, Long datatype, Set<Long> logicalFlowIds) {
        return dsl
                .select(logicalFlowDecorator.LOGICAL_FLOW_ID)
                .from(logicalFlowDecorator)
                .where(logicalFlowDecorator.LOGICAL_FLOW_ID.in(logicalFlowIds)
                        .and(logicalFlowDecorator.DECORATOR_ENTITY_ID.eq(datatype))
                        .and(logicalFlowDecorator.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .fetch(logicalFlowDecorator.LOGICAL_FLOW_ID);
    }


    public static long deleteLogicalFlowDecorators(DSLContext dsl,
                                                   Long datatype,
                                                   Collection<Long> logicalFlowIds) {
        return dsl.delete(logicalFlowDecorator)
                .where(logicalFlowDecorator.LOGICAL_FLOW_ID.in(logicalFlowIds)
                        .and(logicalFlowDecorator.DECORATOR_ENTITY_ID.eq(datatype))
                        .and(logicalFlowDecorator.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .execute();
    }


    public static long updatePhysicalFlowDecorators(DSLContext dsl,
                                                    Long oldDataType,
                                                    Long newDataType,
                                                    Set<Long> specIds) {
        Set<Long> existingSpecIds = dsl
            .selectDistinct(physicalSpecDataType.SPECIFICATION_ID)
            .from(physicalSpecDataType)
            .where(physicalSpecDataType.DATA_TYPE_ID.eq(newDataType)
                .and(physicalSpecDataType.SPECIFICATION_ID.in(specIds)))
            .fetchSet(physicalSpecDataType.SPECIFICATION_ID);

        Set<Long> remainingSpecIds = SetUtilities.minus(specIds, existingSpecIds);

        long execute = dsl.update(physicalSpecDataType)
            .set(physicalSpecDataType.DATA_TYPE_ID, newDataType)
            .where(physicalSpecDataType.SPECIFICATION_ID.in(remainingSpecIds)
                .and(physicalSpecDataType.DATA_TYPE_ID.eq(oldDataType)))
            .execute();

        long deleteCount = dsl.delete(physicalSpecDataType)
            .where(physicalSpecDataType.SPECIFICATION_ID.in(specIds)
                .and(physicalSpecDataType.DATA_TYPE_ID.eq(oldDataType)))
            .execute();

        System.out.println("============= ()() Physical spec decorator removed are " + deleteCount);

        return  execute;
    }

    public static long deletePhysicalFlowDecorators(DSLContext dsl,
                                                    Long datatype,
                                                    Set<Long> specIds) {
        return dsl.delete(physicalSpecDataType)
            .where(physicalSpecDataType.SPECIFICATION_ID.in(specIds)
                .and(physicalSpecDataType.DATA_TYPE_ID.eq(datatype)))
            .execute();
    }

    public static long changeTitleAndDescription(DSLContext dsl,
                                                 String title,
                                                 String description,
                                                 String code) {
        return dsl.update(dataType)
                .set(dataType.NAME, title)
                .set(dataType.DESCRIPTION, description)
                .where(dataType.CODE.eq(code)).execute();
    }


    public static long markDataTypeAsDeprecated(DSLContext dsl,
                                                Set<String> codeListToBeDeprecated) {
        String deprecatedIdentifier = "DEPRECATED - ";
        dsl
                .update(dataType)
                .set(dataType.NAME, DSL.concat(deprecatedIdentifier, dataType.NAME))
                .set(dataType.DESCRIPTION, DSL.concat(deprecatedIdentifier, dataType.DESCRIPTION))
                .where(dataType.CODE.in(codeListToBeDeprecated)
                    .and(dataType.NAME.startsWith(deprecatedIdentifier).not()))
                .execute();

        return dsl
                .update(dataType)
                .set(dataType.DEPRECATED, true)
                .set(dataType.CONCRETE, false)
                .where(dataType.CODE.in(codeListToBeDeprecated))
                .execute();
    }

    public static long unMarkDataTypeAsDeprecated(DSLContext dsl,
                                                Set<String> dataTypeCodes) {
        return dsl
                .update(dataType)
                .set(dataType.DEPRECATED, false)
                .where(dataType.CODE.in(dataTypeCodes))
                .execute();
    }

    public static long markDataTypeAsConcrete(DSLContext dsl,
                                                  Set<String> dataTypeCodes) {
        return dsl
                .update(dataType)
                .set(dataType.CONCRETE, true)
                .where(dataType.CODE.in(dataTypeCodes))
                .execute();
    }
}
