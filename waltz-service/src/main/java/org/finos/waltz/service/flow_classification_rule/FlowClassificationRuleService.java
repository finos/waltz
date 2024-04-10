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

package org.finos.waltz.service.flow_classification_rule;

import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.actor.ActorDao;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.data_type.DataTypeIdSelectorFactory;
import org.finos.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationDao;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationRuleDao;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.data.orgunit.OrganisationalUnitDao;
import org.finos.waltz.model.DiffResult;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.datatype.FlowDataType;
import org.finos.waltz.model.entity_hierarchy.EntityHierarchy;
import org.finos.waltz.model.flow_classification.FlowClassification;
import org.finos.waltz.model.flow_classification_rule.DiscouragedSource;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRule;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleCreateCommand;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleUpdateCommand;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.model.logical_flow.FlowClassificationRulesView;
import org.finos.waltz.model.logical_flow.ImmutableFlowClassificationRulesView;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.DiffResult.mkDiff;
import static org.finos.waltz.model.EntityKind.ACTOR;
import static org.finos.waltz.model.EntityKind.ORG_UNIT;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.rating._AuthoritativenessRatingValue.DISCOURAGED;
import static org.finos.waltz.model.rating._AuthoritativenessRatingValue.NO_OPINION;
import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.service.flow_classification_rule.FlowClassificationRuleUtilities.applyVantagePoints;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Service
public class FlowClassificationRuleService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowClassificationRuleService.class);

    private final ActorDao actorDao;
    private final ApplicationDao applicationDao;
    private final ChangeLogService changeLogService;
    private final DataTypeDao dataTypeDao;
    private final EntityHierarchyService entityHierarchyService;
    private final FlowClassificationCalculator ratingCalculator;
    private final FlowClassificationDao flowClassificationDao;
    private final FlowClassificationRuleDao flowClassificationRuleDao;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;
    private final OrganisationalUnitDao organisationalUnitDao;

    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    @Autowired
    public FlowClassificationRuleService(FlowClassificationRuleDao flowClassificationRuleDao,
                                         FlowClassificationDao flowClassificationDao,
                                         DataTypeDao dataTypeDao,
                                         OrganisationalUnitDao organisationalUnitDao,
                                         ApplicationDao applicationDao,
                                         ActorDao actorDao,
                                         FlowClassificationCalculator ratingCalculator,
                                         ChangeLogService changeLogService,
                                         EntityHierarchyService entityHierarchyService,
                                         LogicalFlowDecoratorDao logicalFlowDecoratorDao) {
        checkNotNull(flowClassificationRuleDao, "flowClassificationRuleDao must not be null");
        checkNotNull(flowClassificationDao, "flowClassificationDao must not be null");
        checkNotNull(actorDao, "actorDao must not be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(organisationalUnitDao, "organisationalUnitDao cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(ratingCalculator, "ratingCalculator cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowDecoratorDao, "logicalFlowDecoratorDao cannot be null");
        checkNotNull(entityHierarchyService, "entityHierarchyService cannot be null");

        this.actorDao = actorDao;
        this.applicationDao = applicationDao;
        this.changeLogService = changeLogService;
        this.dataTypeDao = dataTypeDao;
        this.entityHierarchyService = entityHierarchyService;
        this.flowClassificationRuleDao = flowClassificationRuleDao;
        this.flowClassificationDao = flowClassificationDao;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
        this.organisationalUnitDao = organisationalUnitDao;
        this.ratingCalculator = ratingCalculator;
    }


    public List<FlowClassificationRule> findByEntityKind(EntityKind kind) {
        return flowClassificationRuleDao.findByEntityKind(kind);
    }


    public FlowClassificationRule getById(long id) {
        return flowClassificationRuleDao.getById(id);
    }


    public List<FlowClassificationRule> findByEntityReference(EntityReference ref) {
        return flowClassificationRuleDao.findByEntityReference(ref);
    }


    public List<FlowClassificationRule> findByApplicationId(long applicationId) {
        return flowClassificationRuleDao.findByApplicationId(applicationId);
    }


    public int update(FlowClassificationRuleUpdateCommand command, String username) {
        int updateCount = flowClassificationRuleDao.update(command);
        long ruleId = command
                .id()
                .orElseThrow(() -> new IllegalArgumentException("cannot update an flow classification rule without an id"));
        FlowClassificationRule updatedClassificationRule = getById(ruleId);
        ratingCalculator.update(updatedClassificationRule.dataTypeId(), updatedClassificationRule.vantagePointReference());
        logUpdate(command, username);
        return updateCount;
    }


    public long insert(FlowClassificationRuleCreateCommand command, String username) {
        long classificationRuleId = flowClassificationRuleDao.insert(command, username);

        if (command.parentReference().kind() == ORG_UNIT) {
            ratingCalculator.update(command.dataTypeId(), command.parentReference());
        }

        logInsert(classificationRuleId, command, username);
        flowClassificationRuleDao.updatePointToPointFlowClassificationRules(FlowDirection.OUTBOUND);

        return classificationRuleId;
    }


    public int remove(long id, String username) {

        FlowClassificationRule classificationRuleToDelete = getById(id);

        if(classificationRuleToDelete == null){
            throw new NotFoundException("ASRM-NF", "Flow Classification Rule not found");
        }

        logRemoval(id, username);

        int deletedCount = flowClassificationRuleDao.remove(id);

        //set any point-to-point overrides as no opinion first then recalculate for all rules
        LOG.debug("Updating point-point ratings");
        flowClassificationRuleDao.clearRatingsForPointToPointFlows(classificationRuleToDelete);

        LOG.debug("Updated point-point");
        if (classificationRuleToDelete.vantagePointReference().kind() != ACTOR) {
            LOG.debug("Updating org unit /app flow ratings");
            ratingCalculator.update(classificationRuleToDelete.dataTypeId(), classificationRuleToDelete.vantagePointReference());
        }

        return deletedCount;
    }


    public List<FlowClassificationRule> findAll() {
        return flowClassificationRuleDao.findAll();
    }


    @Deprecated
    public boolean recalculateAllFlowRatings() {
        logicalFlowDecoratorDao.resetRatingsAndFlowClassificationRulesCondition(DSL.trueCondition());
        findAll().forEach(
                classificationRule -> ratingCalculator.update(
                        classificationRule.dataTypeId(),
                        classificationRule.vantagePointReference()));
        return true;
    }


    public int fastRecalculateAllFlowRatingsOld() {
        logicalFlowDecoratorDao.resetRatingsAndFlowClassificationRulesCondition(DSL.trueCondition());

        //finds all the vantage points to apply using parent as selector
        List<FlowClassificationRuleVantagePoint> flowClassificationRuleVantagePoints = flowClassificationRuleDao
                .findFlowClassificationRuleVantagePoints(FlowDirection.OUTBOUND);

        int updatedRuleDecorators = flowClassificationRuleVantagePoints
                .stream()
                .mapToInt(logicalFlowDecoratorDao::updateDecoratorsForFlowClassificationRule)
                .sum();

        //overrides rating for point to point flows (must run after the above)
        int updatedPointToPointDecorators = 0; //flowClassificationRuleDao.updatePointToPointFlowClassificationRules(FlowDirection.OUTBOUND);

        LOG.info(
                "Updated decorators for: {} for general rules and {} point-to-point flows",
                updatedRuleDecorators,
                updatedPointToPointDecorators);

        return updatedRuleDecorators + updatedPointToPointDecorators;
    }


    public int recalculateFlowRatingsForSelector(IdSelectionOptions options) {
        Select<Record1<Long>> flowSelector = logicalFlowIdSelectorFactory.apply(options);
        Set<FlowDataType> population = logicalFlowDecoratorDao.fetchFlowDataTypePopulationForFlowSelector(flowSelector);
        return recalculateRatingsForPopulation(population);
    }

    public int fastRecalculateAllFlowRatings() {
        LOG.debug("Loading decorator population points");
        Set<FlowDataType> population = logicalFlowDecoratorDao.fetchFlowDataTypePopulation(DSL.trueCondition());

        return recalculateRatingsForPopulation(population);
    }

    private int recalculateRatingsForPopulation(Set<FlowDataType> population) {

        LOG.debug("Loading rule vantage points");
        List<FlowClassificationRuleVantagePoint> inboundRuleVantagePoints = flowClassificationRuleDao.findFlowClassificationRuleVantagePoints(FlowDirection.INBOUND);
        List<FlowClassificationRuleVantagePoint> outboundRuleVantagePoints = flowClassificationRuleDao.findFlowClassificationRuleVantagePoints(FlowDirection.OUTBOUND);

        Map<Long, String> inboundRatingCodeByRuleId = indexBy(inboundRuleVantagePoints, FlowClassificationRuleVantagePoint::ruleId, FlowClassificationRuleVantagePoint::classificationCode);
        Map<Long, String> outboundRatingCodeByRuleId = indexBy(outboundRuleVantagePoints, FlowClassificationRuleVantagePoint::ruleId, FlowClassificationRuleVantagePoint::classificationCode);


        LOG.debug(
                "Loaded: {} inbound and {} outbound vantage point rules, and a population of: {} flows with datatypes",
                inboundRuleVantagePoints.size(),
                outboundRuleVantagePoints.size(),
                population.size());

        LOG.debug("Loading hierarchies");
        EntityHierarchy ouHierarchy = entityHierarchyService.fetchHierarchyForKind(ORG_UNIT);
        EntityHierarchy dtHierarchy = entityHierarchyService.fetchHierarchyForKind(EntityKind.DATA_TYPE);

        LOG.debug("Applying rules to population");
        Map<Long, Tuple2<Long, FlowClassificationRuleUtilities.MatchOutcome>> lfdIdToOutboundRuleIdMap = time("inbound vps", () -> applyVantagePoints(FlowDirection.OUTBOUND, outboundRuleVantagePoints, population, ouHierarchy, dtHierarchy));
        Map<Long, Tuple2<Long, FlowClassificationRuleUtilities.MatchOutcome>> lfdIdToInboundRuleIdMap = time("outbound vps", () -> applyVantagePoints(FlowDirection.INBOUND, inboundRuleVantagePoints, population, ouHierarchy, dtHierarchy));

        LOG.debug("Calculating diff");
        Set<Tuple5<Long, AuthoritativenessRatingValue, AuthoritativenessRatingValue, Long, Long>> existingDecoratorRatingInfo = map(
                population,
                d -> tuple(d.lfdId(), d.sourceOutboundRating(), d.targetInboundRating(), d.outboundRuleId(), d.inboundRuleId()));

        Set<Tuple5<Long, AuthoritativenessRatingValue, AuthoritativenessRatingValue, Long, Long>> requiredDecoratorRatingInfo = mkRequiredDecoratorRatingInfo(
                population,
                outboundRatingCodeByRuleId,
                inboundRatingCodeByRuleId,
                lfdIdToOutboundRuleIdMap,
                lfdIdToInboundRuleIdMap);


        DiffResult<Tuple5<Long, AuthoritativenessRatingValue, AuthoritativenessRatingValue, Long, Long>> decoratorRatingDiff = mkDiff(
                existingDecoratorRatingInfo,
                requiredDecoratorRatingInfo,
                d -> d.v1,
                (newRecord, existingRecord) -> {
                    boolean sameOutboundFcr = (newRecord.v4 == null && existingRecord.v4 == null) || Objects.equals(newRecord.v4, existingRecord.v4);
                    boolean sameInboundFcr = (newRecord.v5 == null && existingRecord.v5 == null) || Objects.equals(newRecord.v5, existingRecord.v5);
                    boolean sameOutboundRating = newRecord.v2.value().equals(existingRecord.v2.value());
                    boolean sameInboundRating = newRecord.v3.value().equals(existingRecord.v3.value());
                    return sameOutboundRating && sameInboundRating && sameOutboundFcr && sameInboundFcr;
                });

        LOG.debug("Preparing to update {} logical flow decorators with new rating classifications", decoratorRatingDiff.differingIntersection().size());
        Set<UpdateConditionStep<LogicalFlowDecoratorRecord>> updateStmts = map(
                decoratorRatingDiff.differingIntersection(),
                d -> DSL
                        .update(LOGICAL_FLOW_DECORATOR)
                        .set(LOGICAL_FLOW_DECORATOR.RATING, d.v2.value())
                        .set(LOGICAL_FLOW_DECORATOR.TARGET_INBOUND_RATING, d.v3.value())
                        .set(LOGICAL_FLOW_DECORATOR.FLOW_CLASSIFICATION_RULE_ID, d.v4)
                        .set(LOGICAL_FLOW_DECORATOR.INBOUND_FLOW_CLASSIFICATION_RULE_ID, d.v5)
                        .where(LOGICAL_FLOW_DECORATOR.ID.eq(d.v1)));

        int updatedRecords = flowClassificationRuleDao.updateDecoratorsWithClassifications(updateStmts);
        LOG.debug("Updated {} logical flow decorators with a classification", updatedRecords);

        return updatedRecords;
    }

    private Set<Tuple5<Long, AuthoritativenessRatingValue, AuthoritativenessRatingValue, Long, Long>> mkRequiredDecoratorRatingInfo(Set<FlowDataType> population,
                                                                                                                                    Map<Long, String> outboundRatingCodeByRuleId,
                                                                                                                                    Map<Long, String> inboundRatingCodeByRuleId,
                                                                                                                                    Map<Long, Tuple2<Long, FlowClassificationRuleUtilities.MatchOutcome>> lfdIdToOutboundRuleIdMap,
                                                                                                                                    Map<Long, Tuple2<Long, FlowClassificationRuleUtilities.MatchOutcome>> lfdIdToInboundRuleIdMap) {

        Tuple2<Long, FlowClassificationRuleUtilities.MatchOutcome> defaultOutcome = tuple(null, FlowClassificationRuleUtilities.MatchOutcome.NOT_APPLICABLE);

        return population
                .stream()
                .filter(d -> lfdIdToInboundRuleIdMap.containsKey(d.lfdId()) || lfdIdToOutboundRuleIdMap.containsKey(d.lfdId()))
                .map(d -> {
                    Tuple2<Long, FlowClassificationRuleUtilities.MatchOutcome> outboundFlowRating = lfdIdToOutboundRuleIdMap.getOrDefault(d.lfdId(), defaultOutcome);
                    Tuple2<Long, FlowClassificationRuleUtilities.MatchOutcome> inboundFlowRating = lfdIdToInboundRuleIdMap.getOrDefault(d.lfdId(), defaultOutcome);

                    String outboundRatingCode = outboundRatingCodeByRuleId.get(outboundFlowRating.v1);
                    String inboundRatingCode = inboundRatingCodeByRuleId.get(inboundFlowRating.v1);

                    AuthoritativenessRatingValue outboundRating = FlowClassificationRuleUtilities.MatchOutcome.POSITIVE_MATCH.equals(outboundFlowRating.v2)
                            ? AuthoritativenessRatingValue.of(outboundRatingCode)
                            : DISCOURAGED;

                    AuthoritativenessRatingValue inboundRating = FlowClassificationRuleUtilities.MatchOutcome.POSITIVE_MATCH.equals(inboundFlowRating.v2)
                            ? AuthoritativenessRatingValue.of(inboundRatingCode)
                            : NO_OPINION;

                    return tuple(d.lfdId(), outboundRating, inboundRating, outboundFlowRating.v1, inboundFlowRating.v1);
                })
                .collect(Collectors.toSet());
    }

    public Map<EntityReference, Collection<EntityReference>> calculateConsumersForDataTypeIdSelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = dataTypeIdSelectorFactory.apply(options);
        return flowClassificationRuleDao.calculateConsumersForDataTypeIdSelector(selector);
    }


    public Integer cleanupOrphans(String userId) {
        Set<EntityReference> entityReferences = flowClassificationRuleDao.cleanupOrphans();

        entityReferences
                .forEach(ref -> {
                    String message = ref.kind() == EntityKind.APPLICATION
                            ? "Removed as a flow classification rule source as declaring Org Unit no longer exists"
                            : "Application removed as an flow classification rule source as it no longer exists";

                    ChangeLog logEntry = ImmutableChangeLog.builder()
                            .parentReference(ref)
                            .message(message)
                            .severity(Severity.INFORMATION)
                            .operation(Operation.UPDATE)
                            .userId(userId)
                            .build();

                    changeLogService.write(logEntry);
                });

        return entityReferences.size();
    }


    public List<DiscouragedSource> findDiscouragedSources(IdSelectionOptions options) {
        Condition customSelectionCriteria;
        switch(options.entityReference().kind()) {
            case DATA_TYPE:
                GenericSelector dataTypeSelector = genericSelectorFactory.apply(options);
                customSelectionCriteria = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeSelector.selector())
                    .and(FlowClassificationRuleDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;
            case ORG_UNIT:
                GenericSelector orgUnitSelector = genericSelectorFactory.apply(options);
                customSelectionCriteria = FlowClassificationRuleDao.CONSUMER_APP.ORGANISATIONAL_UNIT_ID.in(orgUnitSelector.selector())
                    .and(FlowClassificationRuleDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;
            case ALL:
            case APP_GROUP:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case PERSON:
                customSelectionCriteria = mkConsumerSelectionCondition(options);
                break;

            default:
                throw new UnsupportedOperationException("Cannot calculate discouraged sources for ref" + options.entityReference());
        }

        return flowClassificationRuleDao.findDiscouragedSourcesBySelector(customSelectionCriteria);
    }


    public Set<FlowClassificationRule> findClassificationRules(IdSelectionOptions options) {

        Condition customSelectionCriteria;

        switch(options.entityReference().kind()) {
            case ORG_UNIT:
                GenericSelector orgUnitSelector = genericSelectorFactory.apply(options);
                customSelectionCriteria = Tables.FLOW_CLASSIFICATION_RULE.PARENT_ID.in(orgUnitSelector.selector())
                        .and(FlowClassificationRuleDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;
            case DATA_TYPE:
                GenericSelector dataTypeSelector = genericSelectorFactory.apply(options);
                customSelectionCriteria = Tables.FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID.in(dataTypeSelector.selector())
                        .and(FlowClassificationRuleDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;
            case APPLICATION:
            case ACTOR:
            case END_USER_APPLICATION:
                customSelectionCriteria = Tables.FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(options.entityReference().kind().name())
                        .and(Tables.FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID.eq(options.entityReference().id()))
                        .and(FlowClassificationRuleDao.SUPPLIER_APP.KIND.notIn(options.filters().omitApplicationKinds()));
                break;
            case ALL:
            case APP_GROUP:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case PERSON:
                customSelectionCriteria = mkConsumerSelectionCondition(options);
                break;
            default:
                throw new UnsupportedOperationException("Cannot calculate flow classification rules for ref" + options.entityReference());
        }

        return flowClassificationRuleDao.findClassificationRules(customSelectionCriteria);

    }


    // -- HELPERS

    private Condition mkConsumerSelectionCondition(IdSelectionOptions options) {
        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(options);
        return FlowClassificationRuleDao.CONSUMER_APP.ID.in(appIdSelector);
    }


    private void logRemoval(long id, String username) {
        FlowClassificationRule rule = getById(id);

        if (rule == null) {
            return;
        }

        String parentName = getParentEntityName(rule.vantagePointReference());
        DataType dataType = dataTypeDao.getById(rule.dataTypeId());
        EntityReference subjectRef = enrichSubjectRef(rule.subjectReference());


        if (subjectRef != null && dataType != null && parentName != null) {
            String msg = format(
                    "Removed the flow classification rule where %s [%s/%d] is a source for type: %s [%d] for %s: %s",
                    subjectRef.name(),
                    subjectRef.kind().name(),
                    subjectRef.id(),
                    dataType.name(),
                    dataType.id().get(),
                    rule.vantagePointReference().kind().prettyName(),
                    parentName);

            multiLog(username,
                    id,
                    rule.vantagePointReference(),
                    dataType,
                    subjectRef,
                    msg,
                    Operation.REMOVE);
        }
    }


    private void logInsert(Long ruleId, FlowClassificationRuleCreateCommand command, String username) {

        String parentName = getParentEntityName(command.parentReference());
        DataType dataType = dataTypeDao.getById(command.dataTypeId());
        EntityReference subjectRef = enrichSubjectRef(command.subjectReference());

        if (subjectRef != null && dataType != null && parentName != null) {
            String msg = format(
                    "Registered the flow classification rule with %s [%s/%d] as the source for type: %s [%d] for %s: %s",
                    subjectRef.name(),
                    subjectRef.kind().name(),
                    subjectRef.id(),
                    dataType.name(),
                    dataType.id().get(),
                    command.parentReference().kind().prettyName(),
                    parentName);

            multiLog(
                    username,
                    ruleId,
                    command.parentReference(),
                    dataType,
                    subjectRef,
                    msg,
                    Operation.ADD);
        }
    }


    private String getParentEntityName(EntityReference entityReference) {
        switch (entityReference.kind()) {
            case ORG_UNIT:
                return organisationalUnitDao.getById(entityReference.id()).name();
            case APPLICATION:
                return applicationDao.getById(entityReference.id()).name();
            case ACTOR:
                return actorDao.getById(entityReference.id()).name();
            default:
                throw new IllegalArgumentException(format("Cannot find name for entity kind: %s", entityReference.kind()));
        }
    }


    private void logUpdate(FlowClassificationRuleUpdateCommand command, String username) {
        FlowClassificationRule rule = getById(command.id().get());

        if (rule == null) {
            return;
        }

        String parentName = getParentEntityName(rule.vantagePointReference());
        DataType dataType = dataTypeDao.getById(rule.dataTypeId());
        EntityReference subjectRef = enrichSubjectRef(rule.subjectReference());

        FlowClassification classification = flowClassificationDao.getById(command.classificationId());

        if (subjectRef != null && dataType != null && parentName != null) {

            String msg = format(
                    "Updated flow classification rule: %s [%s/%d] as the source, with rating: %s, for type: %s[%d], for %s: %s",
                    subjectRef.name(),
                    subjectRef.kind().name(),
                    subjectRef.id(),
                    classification.name(),
                    dataType.name(),
                    dataType.id().get(),
                    rule.vantagePointReference().kind().prettyName(),
                    parentName);

            multiLog(username,
                    rule.id().get(),
                    rule.vantagePointReference(),
                    dataType,
                    subjectRef,
                    msg,
                    Operation.UPDATE);
        }
    }


    private EntityReference enrichSubjectRef(EntityReference subjectReference) {
        if (subjectReference.kind().equals(EntityKind.APPLICATION)) {
            return applicationDao.getById(subjectReference.id()).entityReference();
        } else if (subjectReference.kind().equals(EntityKind.ACTOR)) {
            return actorDao.getById(subjectReference.id()).entityReference();
        } else {
            return null;
        }
    }


    private void multiLog(String username,
                          Long classificationRuleId,
                          EntityReference parentRef,
                          DataType dataType,
                          EntityReference subjectRef,
                          String msg,
                          Operation operation) {

        ChangeLog parentLog = ImmutableChangeLog.builder()
                .message(msg)
                .severity(Severity.INFORMATION)
                .userId(username)
                .parentReference(parentRef)
                .childKind(EntityKind.FLOW_CLASSIFICATION_RULE)
                .childId(classificationRuleId)
                .operation(operation)
                .build();

        ChangeLog subjectLog = ImmutableChangeLog
                .copyOf(parentLog)
                .withParentReference(subjectRef);

        ChangeLog dtLog = ImmutableChangeLog
                .copyOf(parentLog)
                .withParentReference(dataType.entityReference());

        ChangeLog authLog = ImmutableChangeLog
                .copyOf(parentLog)
                .withParentReference(mkRef(EntityKind.FLOW_CLASSIFICATION_RULE, classificationRuleId));

        changeLogService.write(parentLog);
        changeLogService.write(subjectLog);
        changeLogService.write(dtLog);
        changeLogService.write(authLog);
    }


    public Set<FlowClassificationRule> findCompanionEntityRules(long ruleId) {
        return flowClassificationRuleDao.findCompanionEntityRules(ruleId);
    }

    public Collection<FlowClassificationRule> findCompanionDataTypeRules(long ruleId) {
        return flowClassificationRuleDao.findCompanionDataTypeRules(ruleId);
    }

    public Set<FlowClassificationRule> findAppliedClassificationRulesForFlow(Long logicalFlowId) {
        return flowClassificationRuleDao.findAppliedClassificationRulesForFlow(logicalFlowId);
    }

    public FlowClassificationRulesView getFlowClassificationsViewForFlow(long flowId) {

        Set<FlowClassificationRule> flowClassificationRules = findAppliedClassificationRulesForFlow(flowId);
        Set<FlowClassification> classifications = flowClassificationDao.findByIds(map(flowClassificationRules, FlowClassificationRule::classificationId));

        List<DataType> dataTypes = dataTypeDao.findByIds(map(flowClassificationRules, FlowClassificationRule::dataTypeId));

        return ImmutableFlowClassificationRulesView
                .builder()
                .flowClassificationRules(flowClassificationRules)
                .flowClassifications(classifications)
                .dataTypes(dataTypes)
                .build();
    }
}
