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

package org.finos.waltz.service.data_type;

import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.datatype_decorator.DataTypeDecoratorDao;
import org.finos.waltz.data.datatype_decorator.DataTypeDecoratorDaoSelectorFactory;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.DataTypeUsageCharacteristics;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRule;
import org.finos.waltz.model.logical_flow.DataTypeDecoratorView;
import org.finos.waltz.model.logical_flow.ImmutableDataTypeDecoratorView;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.assessment_definition.AssessmentDefinitionService;
import org.finos.waltz.service.assessment_rating.AssessmentRatingService;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsCalculator;
import org.finos.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import org.finos.waltz.service.flow_classification_rule.FlowClassificationRuleService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.*;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityKind.*;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;

@Service
public class DataTypeDecoratorService {

    private final ChangeLogService changeLogService;
    private final DataTypeDecoratorDaoSelectorFactory dataTypeDecoratorDaoSelectorFactory;
    private final LogicalFlowDao logicalFlowDao;
    private final LogicalFlowService logicalFlowService;
    private final LogicalFlowDecoratorRatingsCalculator ratingsCalculator;
    private final DataTypeUsageService dataTypeUsageService;
    private final DataTypeService dataTypeService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();
    private final PhysicalSpecificationDao physicalSpecificationDao;
    private final PhysicalSpecificationService physicalSpecificationService;
    private final AssessmentRatingService assessmentRatingService;
    private final AssessmentDefinitionService assessmentDefinitionService;
    private final RatingSchemeService ratingSchemeService;

    private final FlowClassificationRuleService flowClassificationRuleService;

    @Autowired
    public DataTypeDecoratorService(ChangeLogService changeLogService,
                                    LogicalFlowDecoratorService logicalFlowDecoratorService,
                                    DataTypeDecoratorDaoSelectorFactory dataTypeDecoratorDaoSelectorFactory,
                                    LogicalFlowDao logicalFlowDao,
                                    LogicalFlowService logicalFlowService,
                                    LogicalFlowDecoratorRatingsCalculator ratingsCalculator,
                                    DataTypeUsageService dataTypeUsageService,
                                    DataTypeService dataTypeService,
                                    PhysicalSpecificationDao physicalSpecificationDao,
                                    PhysicalSpecificationService physicalSpecificationService,
                                    AssessmentRatingService assessmentRatingService,
                                    AssessmentDefinitionService assessmentDefinitionService,
                                    RatingSchemeService ratingSchemeService,
                                    FlowClassificationRuleService flowClassificationRuleService) {

        checkNotNull(assessmentDefinitionService, "assessmentDefinitionService cannot be null");
        checkNotNull(assessmentRatingService, "assessmentRatingService cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(flowClassificationRuleService, "flowClassificationRuleService cannot be null");
        checkNotNull(logicalFlowDecoratorService, "logicalFlowDecoratorService cannot be null");
        checkNotNull(physicalSpecificationService, "physicalSpecificationService cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");

        this.assessmentDefinitionService = assessmentDefinitionService;
        this.assessmentRatingService = assessmentRatingService;
        this.changeLogService = changeLogService;
        this.dataTypeDecoratorDaoSelectorFactory = dataTypeDecoratorDaoSelectorFactory;
        this.dataTypeService = dataTypeService;
        this.dataTypeUsageService = dataTypeUsageService;
        this.flowClassificationRuleService = flowClassificationRuleService;
        this.logicalFlowDao = logicalFlowDao;
        this.logicalFlowService = logicalFlowService;
        this.physicalSpecificationDao = physicalSpecificationDao;
        this.physicalSpecificationService = physicalSpecificationService;
        this.ratingsCalculator = ratingsCalculator;
        this.ratingSchemeService = ratingSchemeService;
    }


    public boolean updateDecorators(String userName,
                                  EntityReference entityReference,
                                  Set<Long> dataTypeIdsToAdd,
                                  Set<Long> dataTypeIdsToRemove) {

        checkNotNull(userName, "userName cannot be null");
        checkNotNull(entityReference, "entityReference cannot be null");

        String currentDataTypeNames = getAssociatedDatatypeNamesAsCsv(entityReference);

        if (notEmpty(dataTypeIdsToAdd)) {
            addDecorators(userName, entityReference, dataTypeIdsToAdd);
        }
        if (notEmpty(dataTypeIdsToRemove)) {
            removeDataTypeDecorator(userName, entityReference, dataTypeIdsToRemove);
        }

        auditEntityDataTypeChanges(userName, entityReference, currentDataTypeNames);
        return true;
    }


    public DataTypeDecorator getByEntityRefAndDataTypeId(EntityReference reference, long dataTypeId) {
        return dataTypeDecoratorDaoSelectorFactory
                .getDao(reference.kind())
                .getByEntityIdAndDataTypeId(reference.id(), dataTypeId);
    }


    public List<DataTypeDecorator> findByEntityId(EntityReference reference) {
        return dataTypeDecoratorDaoSelectorFactory
                .getDao(reference.kind())
                .findByEntityId(reference.id());
    }


    public Collection<DataTypeDecorator> findByEntityIdSelector(
            EntityKind entityKind,
            IdSelectionOptions selectionOptions) {
        checkNotNull(selectionOptions, "selectionOptions cannot be null");

        DataTypeDecoratorDao dao = dataTypeDecoratorDaoSelectorFactory
                .getDao(entityKind);

        return LOGICAL_DATA_FLOW.equals(entityKind)
                ? getSelectorForLogicalFlow(dao, selectionOptions)
                : dao.findByEntityIdSelector(
                        genericSelectorFactory.applyForKind(entityKind, selectionOptions).selector(),
                        Optional.ofNullable(entityKind));
    }


    public int[] addDecorators(String userName,
                               EntityReference entityReference,
                               Set<Long> dataTypeIds) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(dataTypeIds, "dataTypeIds cannot be null");

        Collection<DataTypeDecorator> dataTypeDecorators
                = mkDecorators(userName, entityReference, dataTypeIds);

        int[] result = dataTypeDecoratorDaoSelectorFactory
                .getDao(entityReference.kind())
                .addDecorators(dataTypeDecorators);

        audit(format("Added data types: %s", dataTypeIds.toString()),
                entityReference, userName);

        recalculateDataTypeUsageForApplications(entityReference);

        if (PHYSICAL_SPECIFICATION.equals(entityReference.kind())) {
            physicalSpecificationService.propagateDataTypesToLogicalFlows(userName, entityReference.id());
        }

        return result;
    }


    public int removeDataTypeDecorator(String userName, EntityReference entityReference, Set<Long> dataTypeIds) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(dataTypeIds, "dataTypeIds cannot be null");

        int result = dataTypeDecoratorDaoSelectorFactory
                .getDao(entityReference.kind())
                .removeDataTypes(entityReference, dataTypeIds);

        audit(format("Removed data types: %s", dataTypeIds.toString()),
                entityReference, userName);

        recalculateDataTypeUsageForApplications(entityReference);

        return result;
    }


    private void recalculateDataTypeUsageForApplications(EntityReference associatedEntityReference) {
        if(LOGICAL_DATA_FLOW.equals(associatedEntityReference.kind())) {
            LogicalFlow flow = logicalFlowDao.getByFlowId(associatedEntityReference.id());
            dataTypeUsageService.recalculateForApplications(newArrayList(flow.source(), flow.target()));
        }
    }


    private Collection<DataTypeDecorator> mkDecorators(String userName,
                                                       EntityReference entityReference,
                                                       Set<Long> dataTypeIds) {

        if(LOGICAL_DATA_FLOW.equals(entityReference.kind())) {
            Collection<DataTypeDecorator> decorators = map(dataTypeIds,
                    dtId -> mkDecorator(
                            userName,
                            entityReference,
                            dtId,
                            Optional.of(AuthoritativenessRatingValue.NO_OPINION)));
            LogicalFlow flow = logicalFlowDao.getByFlowId(entityReference.id());
            boolean requiresRating = flow.source().kind() == APPLICATION && flow.target().kind() == APPLICATION;

            return requiresRating
                    ? ratingsCalculator.calculate(decorators)
                    : decorators;
        }

        return map(dataTypeIds,
                dtId -> mkDecorator(userName, entityReference, dtId, Optional.empty()));
    }


    private ImmutableDataTypeDecorator mkDecorator(String userName,
                                                   EntityReference entityReference,
                                                   Long dtId,
                                                   Optional<AuthoritativenessRatingValue> rating) {
        return ImmutableDataTypeDecorator.builder()
                .rating(rating)
                .entityReference(entityReference)
                .decoratorEntity(mkRef(DATA_TYPE, dtId))
                .provenance("waltz")
                .lastUpdatedAt(nowUtc())
                .lastUpdatedBy(userName)
                .build();
    }


    private Collection<DataTypeDecorator> getSelectorForLogicalFlow(DataTypeDecoratorDao dao, IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case ALL:
            case APPLICATION:
            case APP_GROUP:
            case ORG_UNIT:
            case PERSON:
            case MEASURABLE:
            case SCENARIO:
            case CHANGE_INITIATIVE:
                return dao.findByAppIdSelector(
                        genericSelectorFactory.applyForKind(APPLICATION, options).selector());
            case ACTOR:
                return dao.findByEntityIdSelector(
                        DSL.select(DSL.val(options.entityReference().id())),
                        Optional.of(ACTOR));
            case DATA_TYPE:
                return dao.findByDataTypeIdSelector(
                        genericSelectorFactory.applyForKind(DATA_TYPE, options).selector());
            case LOGICAL_DATA_FLOW:
                return dao.findByFlowIdSelector(logicalFlowIdSelectorFactory.apply(options));
            default:
                throw new UnsupportedOperationException("Cannot find decorators for selector kind: " + options.entityReference().kind());
        }
    }


    public Collection<DataTypeDecorator> findByFlowIds(Collection<Long> ids, EntityKind entityKind) {
        if (isEmpty(ids)) {
            return Collections.emptyList();
        }
        return dataTypeDecoratorDaoSelectorFactory
                .getDao(entityKind)
                .findByFlowIds(ids);
    }


    private void audit(String message,
                       EntityReference entityReference,
                       String username) {

        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(entityReference)
                .userId(username)
                .severity(Severity.INFORMATION)
                .message(message)
                .childKind(EntityKind.DATA_TYPE)
                .operation(Operation.UPDATE)
                .build();

        changeLogService.write(logEntry);
    }


    private void auditEntityDataTypeChanges(String userName, EntityReference entityReference, String currentDataTypeNames) {
        String updatedDataTypeNames = getAssociatedDatatypeNamesAsCsv(entityReference);
        switch(entityReference.kind()) {
            case LOGICAL_DATA_FLOW:
                LogicalFlow logicalFlow = logicalFlowDao.getByFlowId(entityReference.id());
                String auditMessage = format("Logical Flow from %s to %s: Data types changed from [%s] to [%s]",
                        logicalFlow.source().name().orElse(""),
                        logicalFlow.target().name().orElse(""),
                        currentDataTypeNames,
                        updatedDataTypeNames);
                audit(auditMessage, logicalFlow.source(), userName);
                audit(auditMessage, logicalFlow.target(), userName);
                break;
            case PHYSICAL_SPECIFICATION:
                PhysicalSpecification physicalSpecification = physicalSpecificationDao.getById(entityReference.id());
                logicalFlowService
                        .findBySelector(mkOpts(entityReference))
                        .forEach(lf -> {
                            String message = format("Physical Specification [%s]: Data types changed from [%s] to [%s]",
                                    physicalSpecification.name(),
                                    currentDataTypeNames,
                                    updatedDataTypeNames);
                            audit(message, physicalSpecification.entityReference(), userName);
                            audit(message, lf.source(), userName);
                            audit(message, lf.target(), userName);
                        });
                break;
        }
    }


    private String getAssociatedDatatypeNamesAsCsv(EntityReference entityReference) {
        IdSelectionOptions idSelectionOptions = mkOpts(
                entityReference,
                HierarchyQueryScope.EXACT);

        Select<Record1<Long>> dataTypeIdSelector = genericSelectorFactory.applyForKind(DATA_TYPE, idSelectionOptions).selector();

        return dataTypeService.findByIdSelector(dataTypeIdSelector)
                .stream()
                .map(EntityReference::name)
                .map(Optional::get)
                .collect(Collectors.joining(", "));
    }


    public Collection<DataTypeUsageCharacteristics> findDatatypeUsageCharacteristics(EntityReference ref) {
        return dataTypeDecoratorDaoSelectorFactory
                .getDao(ref.kind())
                .findDatatypeUsageCharacteristics(ref);
    }


    /**
     * Given a entity (e.g. logical flow or physical specification) produces a view of decorators and related assessments
     * @param parentEntityRef the reference of the parent page
     * @return DataTypeDecoratorView
     */
    public DataTypeDecoratorView getDecoratorView(EntityReference parentEntityRef) {

        DataTypeDecoratorDao dao = dataTypeDecoratorDaoSelectorFactory.getDao(parentEntityRef.kind());
        EntityKind decoratorKind = getDecoratorKind(parentEntityRef.kind());

        List<DataTypeDecorator> decorators = dao.findByEntityId(parentEntityRef.id());

        List<AssessmentRating> decoratorAssessmentRatings = assessmentRatingService.findByTargetKindForRelatedSelector(decoratorKind, mkOpts(parentEntityRef));

        Set<AssessmentDefinition> primaryDefs = assessmentDefinitionService.findByPrimaryDefinitionsForKind(EntityKind.LOGICAL_DATA_FLOW_DATA_TYPE_DECORATOR, Optional.empty());

        Set<Long> uniqueRatingIds = map(decoratorAssessmentRatings, AssessmentRating::ratingId);
        Set<RatingSchemeItem> ratings = ratingSchemeService.findRatingSchemeItemsByIds(uniqueRatingIds);

        Set<FlowClassificationRule> flowClassificationRules = flowClassificationRuleService.findAppliedClassificationRulesForFlow(parentEntityRef.id());

        return ImmutableDataTypeDecoratorView.builder()
                .dataTypeDecorators(decorators)
                .decoratorRatings(decoratorAssessmentRatings)
                .primaryAssessmentDefinitions(primaryDefs)
                .ratingSchemeItems(ratings)
                .flowClassificationRules(flowClassificationRules)
                .build();
    }

    private EntityKind getDecoratorKind(EntityKind entityKind) {
        switch (entityKind) {
            case PHYSICAL_SPECIFICATION:
                return EntityKind.PHYSICAL_SPEC_DATA_TYPE_DECORATOR;
            case LOGICAL_DATA_FLOW:
                return EntityKind.LOGICAL_DATA_FLOW_DATA_TYPE_DECORATOR;
            default:
                throw new IllegalArgumentException("Cannot determine decorator entity kind for parent entity kind: " + entityKind);
        }
    }

}
