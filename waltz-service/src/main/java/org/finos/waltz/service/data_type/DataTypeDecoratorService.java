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

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsCalculator;
import org.finos.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.service.usage_info.DataTypeUsageService;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.datatype_decorator.DataTypeDecoratorDao;
import org.finos.waltz.data.datatype_decorator.DataTypeDecoratorDaoSelectorFactory;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.DataTypeUsageCharacteristics;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.*;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.SetUtilities.asSet;
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
    private final PhysicalSpecificationDao physicalSpecificationDao;
    private final PhysicalSpecificationService physicalSpecificationService;


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
                                    PhysicalSpecificationService physicalSpecificationService) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowDecoratorService, "logicalFlowDecoratorService cannot be null");
        checkNotNull(physicalSpecificationService, "physicalSpecificationService cannot be null");

        this.changeLogService = changeLogService;
        this.logicalFlowDao = logicalFlowDao;
        this.logicalFlowService = logicalFlowService;
        this.ratingsCalculator = ratingsCalculator;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dataTypeService = dataTypeService;
        this.dataTypeDecoratorDaoSelectorFactory = dataTypeDecoratorDaoSelectorFactory;
        this.physicalSpecificationDao = physicalSpecificationDao;
        this.physicalSpecificationService = physicalSpecificationService;
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


    public List<DataTypeDecorator> findByEntityIdSelector(
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


    private List<DataTypeDecorator> getSelectorForLogicalFlow(DataTypeDecoratorDao dao, IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
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


    public Collection<DataType> findSuggestedByEntityRef(EntityReference entityReference) {

        if (!asSet(LOGICAL_DATA_FLOW, PHYSICAL_SPECIFICATION).contains(entityReference.kind())){
            throw new UnsupportedOperationException(format("Cannot find suggested data types for entity kind: %s", entityReference.kind()));
        }

        // finds all flows for this entity
        List<LogicalFlow> logicalFlows = logicalFlowService.findBySelector(mkOpts(entityReference));

        if(isEmpty(logicalFlows)){
            return emptyList();
        } else {
            //finds the first flow -- used by logical flows and specs only!
            LogicalFlow flow = first(logicalFlows);
            return dataTypeService.findSuggestedBySourceEntityRef(flow.source());
        }
    }


    public Collection<DataTypeUsageCharacteristics> findDatatypeUsageCharacteristics(EntityReference ref) {
        return dataTypeDecoratorDaoSelectorFactory
            .getDao(ref.kind())
            .findDatatypeUsageCharacteristics(ref);
    }
}
