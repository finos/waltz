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

package com.khartec.waltz.service.data_type;

import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.datatype_decorator.DataTypeDecoratorDao;
import com.khartec.waltz.data.datatype_decorator.DataTypeDecoratorDaoSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.datatype.ImmutableDataTypeDecorator;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsCalculator;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.*;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.model.EntityKind.*;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.stream.Collectors.toList;

@Service
public class DataTypeDecoratorService {

    private final ChangeLogService changeLogService;
    private final PhysicalFlowService physicalFlowService;
    private final DataTypeDecoratorDaoSelectorFactory dataTypeDecoratorDaoSelectorFactory;
    private final LogicalFlowDao logicalFlowDao;
    private final LogicalFlowDecoratorRatingsCalculator ratingsCalculator;
    private final DataTypeUsageService dataTypeUsageService;
    private final DataTypeService dataTypeService;
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final PhysicalSpecificationDao physicalSpecificationDao;


    @Autowired
    public DataTypeDecoratorService(ChangeLogService changeLogService,
                                    LogicalFlowDecoratorService logicalFlowDecoratorService,
                                    PhysicalFlowService physicalFlowService,
                                    DataTypeDecoratorDaoSelectorFactory dataTypeDecoratorDaoSelectorFactory,
                                    LogicalFlowDao logicalFlowDao,
                                    LogicalFlowDecoratorRatingsCalculator ratingsCalculator,
                                    DataTypeUsageService dataTypeUsageService,
                                    DataTypeService dataTypeService,
                                    PhysicalSpecificationDao physicalSpecificationDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowDecoratorService, "logicalFlowDecoratorService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");

        this.changeLogService = changeLogService;
        this.physicalFlowService = physicalFlowService;
        this.logicalFlowDao = logicalFlowDao;
        this.ratingsCalculator = ratingsCalculator;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dataTypeService = dataTypeService;
        this.dataTypeDecoratorDaoSelectorFactory = dataTypeDecoratorDaoSelectorFactory;
        this.physicalSpecificationDao = physicalSpecificationDao;
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

        audit(String.format("Added data types: %s", dataTypeIds.toString()),
                entityReference, userName);

        recalculateDataTypeUsageForApplications(entityReference);
        // now update logical flow data types
        // find all physicals with this spec id, for each physical update it's logical decorators
        if (PHYSICAL_SPECIFICATION.equals(entityReference.kind())) {
            updateDecoratorForLogicalFlow(userName, entityReference, dataTypeIds);
        }

        return result;
    }

    private void updateDecoratorForLogicalFlow(String userName, EntityReference entityReference, Set<Long> dataTypeIds) {
        List<Long> logicalFlowIds = physicalFlowService
                .findBySpecificationId(entityReference.id())
                .stream()
                .map(PhysicalFlow::logicalFlowId)
                .collect(toList());

        logicalFlowIds.forEach(lfId -> addDecorators(userName,
                mkRef(LOGICAL_DATA_FLOW, lfId),
                dataTypeIds));
    }


    public int removeDataTypeDecorator(String userName, EntityReference entityReference, Set<Long> dataTypeIds) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(dataTypeIds, "dataTypeIds cannot be null");

        int result = dataTypeDecoratorDaoSelectorFactory
                .getDao(entityReference.kind())
                .removeDataTypes(entityReference, dataTypeIds);

        audit(String.format("Removed data types: %s", dataTypeIds.toString()),
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
                    dtId -> mkDecorator(userName, entityReference, dtId,
                            Optional.of(AuthoritativenessRating.NO_OPINION)));
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
                                                   Optional<AuthoritativenessRating> rating) {
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
                String auditMessage = String.format("Logical Flow from %s to %s: Data types changed from [%s] to [%s]",
                        logicalFlow.source().name().orElse(""),
                        logicalFlow.target().name().orElse(""),
                        currentDataTypeNames,
                        updatedDataTypeNames);
                audit(auditMessage, logicalFlow.source(), userName);
                audit(auditMessage, logicalFlow.target(), userName);
                break;
            case PHYSICAL_SPECIFICATION:
                PhysicalSpecification physicalSpecification = physicalSpecificationDao.getById(entityReference.id());
                String message = String.format("Physical Specification [%s]: Data types changed from [%s] to [%s]",
                        physicalSpecification,
                        currentDataTypeNames,
                        updatedDataTypeNames);
                audit(message, physicalSpecification.entityReference(), userName);
                break;
        }
    }


    private String getAssociatedDatatypeNamesAsCsv(EntityReference entityReference) {
        IdSelectionOptions idSelectionOptions = IdSelectionOptions.mkOpts(
                entityReference,
                HierarchyQueryScope.EXACT);

        Select<Record1<Long>> dataTypeIdSelector = genericSelectorFactory.applyForKind(DATA_TYPE, idSelectionOptions).selector();

        return dataTypeService.findByIdSelector(dataTypeIdSelector)
                .stream()
                .map(EntityReference::name)
                .map(Optional::get)
                .collect(Collectors.joining(", "));
    }
}
