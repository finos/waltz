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
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.datatype_decorator.DataTypeDecoratorDao;
import com.khartec.waltz.data.datatype_decorator.DataTypeDecoratorDaoSelectorFactory;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.datatype.ImmutableDataTypeDecorator;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsCalculator;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.CollectionUtilities.map;
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
    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public DataTypeDecoratorService(ChangeLogService changeLogService,
                                    LogicalFlowDecoratorService logicalFlowDecoratorService,
                                    PhysicalFlowService physicalFlowService,
                                    DataTypeDecoratorDaoSelectorFactory dataTypeDecoratorDaoSelectorFactory,
                                    LogicalFlowDao logicalFlowDao,
                                    LogicalFlowDecoratorRatingsCalculator ratingsCalculator,
                                    DataTypeUsageService dataTypeUsageService) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowDecoratorService, "logicalFlowDecoratorService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");

        this.changeLogService = changeLogService;
        this.physicalFlowService = physicalFlowService;
        this.logicalFlowDao = logicalFlowDao;
        this.ratingsCalculator = ratingsCalculator;
        this.dataTypeUsageService = dataTypeUsageService;
        this.dataTypeDecoratorDaoSelectorFactory = dataTypeDecoratorDaoSelectorFactory;
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

        audit("Added", dataTypeIds, entityReference, userName);

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

        audit("Removed", dataTypeIds, entityReference, userName);
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


    private void audit(String verb,
                       Set<Long> dataTypeIds,
                       EntityReference entityReference,
                       String username) {

        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(entityReference)
                .userId(username)
                .severity(Severity.INFORMATION)
                .message(String.format(
                        "%s data types: %s",
                        verb,
                        dataTypeIds.toString()))
                .childKind(EntityKind.DATA_TYPE)
                .operation(Operation.UPDATE)
                .build();

        changeLogService.write(logEntry);
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
}
