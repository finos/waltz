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

package com.khartec.waltz.service.flow_classification_rule;

import com.khartec.waltz.common.exception.NotFoundException;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.data.flow_classification_rule.FlowClassificationDao;
import com.khartec.waltz.data.flow_classification_rule.FlowClassificationRuleDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.flow_classification.FlowClassification;
import com.khartec.waltz.model.flow_classification_rule.*;
import com.khartec.waltz.model.rating.AuthoritativenessRatingValue;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.ACTOR;
import static com.khartec.waltz.model.EntityKind.ORG_UNIT;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static java.lang.String.format;


@Service
public class FlowClassificationRuleService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowClassificationRuleService.class);

    private final FlowClassificationRuleDao flowClassificationRuleDao;
    private final FlowClassificationDao flowClassificationDao;
    private final DataTypeDao dataTypeDao;
    private final OrganisationalUnitDao organisationalUnitDao;
    private final ApplicationDao applicationDao;
    private final ActorDao actorDao;
    private final FlowClassificationCalculator ratingCalculator;
    private final ChangeLogService changeLogService;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;
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

        this.flowClassificationRuleDao = flowClassificationRuleDao;
        this.flowClassificationDao = flowClassificationDao;
        this.dataTypeDao = dataTypeDao;
        this.organisationalUnitDao = organisationalUnitDao;
        this.applicationDao = applicationDao;
        this.actorDao = actorDao;
        this.ratingCalculator = ratingCalculator;
        this.changeLogService = changeLogService;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
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
        ratingCalculator.update(updatedClassificationRule.dataTypeId(), updatedClassificationRule.parentReference());
        logUpdate(command, username);
        return updateCount;
    }


    public long insert(FlowClassificationRuleCreateCommand command, String username) {
        long classificationRuleId = flowClassificationRuleDao.insert(command, username);

        if (command.parentReference().kind() == ORG_UNIT) {
            ratingCalculator.update(command.dataTypeId(), command.parentReference());
        }

        logInsert(classificationRuleId, command, username);
        flowClassificationRuleDao.updatePointToPointFlowClassificationRules();

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
        if(classificationRuleToDelete.parentReference().kind() != ACTOR){
            LOG.debug("Updating org unit /app flow ratings");
            ratingCalculator.update(classificationRuleToDelete.dataTypeId(), classificationRuleToDelete.parentReference());
        }

        return deletedCount;
    }


    public List<FlowClassificationRule> findAll() {
        return flowClassificationRuleDao.findAll();
    }


    @Deprecated
    public boolean recalculateAllFlowRatings() {
        logicalFlowDecoratorDao.updateRatingsByCondition(AuthoritativenessRatingValue.NO_OPINION, DSL.trueCondition());
        findAll().forEach(
                classificationRule -> ratingCalculator.update(
                        classificationRule.dataTypeId(),
                        classificationRule.parentReference()));
        return true;
    }


    public boolean fastRecalculateAllFlowRatings() {
        logicalFlowDecoratorDao.updateRatingsByCondition(AuthoritativenessRatingValue.NO_OPINION, DSL.trueCondition());

        //finds all the vantage points to apply using parent as selector - 1646
        List<FlowClassificationRuleVantagePoint> flowClassificationRuleVantagePoints = flowClassificationRuleDao
                .findFlowClassificationRuleVantagePoints();

        flowClassificationRuleVantagePoints
                .forEach(a -> {
                    LOG.info("Updating decorators for: {}", a);
                    int updateCount = logicalFlowDecoratorDao.updateDecoratorsForFlowClassificationRule(a);
                    LOG.info("Updated {} decorators for: {}", updateCount, a);
        });

        //overrides rating for point to point flows (must run after the above)
        int updatedDecoratorRatings = flowClassificationRuleDao.updatePointToPointFlowClassificationRules();
        LOG.info("Updated decorators for: {} point-to-point flows", updatedDecoratorRatings);

        return true;
    }


    public Map<EntityReference, Collection<EntityReference>> calculateConsumersForDataTypeIdSelector(
            IdSelectionOptions options) {
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

            case APP_GROUP:
            case FLOW_DIAGRAM:
            case MEASURABLE:
            case PERSON:
                customSelectionCriteria = mkConsumerSelectionCondition(options);
                break;

            default:
                throw new UnsupportedOperationException("Cannot calculate non-auth sources for ref" + options.entityReference());
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

        String parentName = getParentEntityName(rule.parentReference());
        DataType dataType = dataTypeDao.getById(rule.dataTypeId());
        Application app = applicationDao.getById(rule.applicationReference().id());


        if (app != null && dataType != null && parentName != null) {
            String msg = format(
                    "Removed the flow classification rule where %s is a source app for type: %s for %s: %s",
                    app.name(),
                    dataType.name(),
                    rule.parentReference().kind().prettyName(),
                    parentName);

            multiLog(username, id, rule.parentReference(), dataType, app, msg, Operation.REMOVE);
        }
    }


    private void logInsert(Long ruleId, FlowClassificationRuleCreateCommand command, String username) {

        String parentName = getParentEntityName(command.parentReference());
        DataType dataType = dataTypeDao.getById(command.dataTypeId());
        Application app = applicationDao.getById(command.applicationId());

        if (app != null && dataType != null && parentName != null) {
            String msg = format(
                    "Registered the flow classification rule with %s as the source app for type: %s for %s: %s",
                    app.name(),
                    dataType.name(),
                    command.parentReference().kind().prettyName(),
                    parentName);

            multiLog(
                    username,
                    ruleId,
                    command.parentReference(),
                    dataType,
                    app,
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

        String parentName = getParentEntityName(rule.parentReference());
        DataType dataType = dataTypeDao.getById(rule.dataTypeId());
        Application app = applicationDao.getById(rule.applicationReference().id());
        FlowClassification classification = flowClassificationDao.getById(command.classificationId());

        if (app != null && dataType != null && parentName != null) {
            String msg = format(
                    "Updated flow classification rule: %s as the source application with rating: %s, for type: %s, for %s: %s",
                    app.name(),
                    classification.name(),
                    dataType.name(),
                    rule.parentReference().kind().prettyName(),
                    parentName);

            multiLog(
                    username,
                    rule.id().get(),
                    rule.parentReference(),
                    dataType,
                    app,
                    msg,
                    Operation.UPDATE);
        }
    }


    private void multiLog(String username,
                          Long classificationRuleId,
                          EntityReference parentRef,
                          DataType dataType,
                          Application app,
                          String msg,
                          Operation operation) {

        ChangeLog parentLog = ImmutableChangeLog.builder()
                .message(msg)
                .severity(Severity.INFORMATION)
                .userId(username)
                .parentReference(parentRef)
                .childKind(EntityKind.FLOW_CLASSIFICATION_RULE)
                .operation(operation)
                .build();

        ChangeLog appLog = ImmutableChangeLog
                .copyOf(parentLog)
                .withParentReference(app.entityReference());

        ChangeLog dtLog = ImmutableChangeLog
                .copyOf(parentLog)
                .withParentReference(dataType.entityReference());

        ChangeLog authLog = ImmutableChangeLog
                .copyOf(parentLog)
                .withParentReference(mkRef(EntityKind.FLOW_CLASSIFICATION_RULE, classificationRuleId));

        changeLogService.write(parentLog);
        changeLogService.write(appLog);
        changeLogService.write(dtLog);
        changeLogService.write(authLog);
    }

}
