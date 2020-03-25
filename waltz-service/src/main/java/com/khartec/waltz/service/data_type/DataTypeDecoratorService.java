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
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationIdSelectorFactory;
import com.khartec.waltz.data.physical_specification_data_type.DataTypeDecoratorDao;
import com.khartec.waltz.data.physical_specification_data_type.DataTypeDecoratorDaoSelectorFactory;
import com.khartec.waltz.data.physical_specification_data_type.PhysicalSpecDataTypeDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecorator;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.datatype.ImmutableDataTypeDecorator;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.isEmpty;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.model.EntityKind.*;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class DataTypeDecoratorService {

    private final ChangeLogService changeLogService;
    private final LogicalFlowDecoratorService logicalFlowDecoratorService;
    private final PhysicalFlowService physicalFlowService;
    private final PhysicalSpecDataTypeDao physicalSpecDataTypeDao;
    private final DataTypeDecoratorDaoSelectorFactory dataTypeDecoratorDaoSelectorFactory;
    private final PhysicalSpecificationIdSelectorFactory specificationIdSelectorFactory = new PhysicalSpecificationIdSelectorFactory();

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public DataTypeDecoratorService(ChangeLogService changeLogService,
                                    LogicalFlowDecoratorService logicalFlowDecoratorService,
                                    PhysicalFlowService physicalFlowService,
                                    PhysicalSpecDataTypeDao physicalSpecDataTypeDao,
                                    DataTypeDecoratorDaoSelectorFactory dataTypeDecoratorDaoSelectorFactory) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowDecoratorService, "logicalFlowDecoratorService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(physicalSpecDataTypeDao, "physicalSpecDataTypeDao cannot be null");

        this.changeLogService = changeLogService;
        this.logicalFlowDecoratorService = logicalFlowDecoratorService;
        this.physicalFlowService = physicalFlowService;
        this.physicalSpecDataTypeDao = physicalSpecDataTypeDao;
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

        System.out.println("This should be diff than physical spec " + selectionOptions.entityReference());
        System.out.println("This should be physical spec " + entityKind);

        DataTypeDecoratorDao dao = dataTypeDecoratorDaoSelectorFactory
                .getDao(entityKind);
        if(PHYSICAL_SPECIFICATION.equals(entityKind)){
            Select<Record1<Long>> selector = genericSelectorFactory
                    .applyForKind(entityKind, selectionOptions).selector();
            return dao.findByEntityIdSelector(selector, Optional.empty());
        }
        return getSelectorForLogicalFlow(dao, selectionOptions);
    }


    public int[] addDataTypes(String userName, EntityReference entityReference, Set<Long> dataTypeIds) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(dataTypeIds, "dataTypeIds cannot be null");

        Collection<DataTypeDecorator> specificationDataTypes
                = mkSpecificationDataTypes(userName, entityReference, dataTypeIds);

        int[] result = dataTypeDecoratorDaoSelectorFactory
                .getDao(entityReference.kind())
                .addDataTypes(specificationDataTypes);

        audit("Added", dataTypeIds, entityReference, userName);

        // now update logical flow data types
        // find all physicals with this spec id, for each physical update it's logical decorators
        List<Long> logicalFlowIds = physicalFlowService
                .findBySpecificationId(entityReference.id())
                .stream()
                .map(PhysicalFlow::logicalFlowId)
                .collect(toList());

        Set<EntityReference> dataTypeRefs = dataTypeIds.stream()
                .map(id -> EntityReference.mkRef(EntityKind.DATA_TYPE, id))
                .collect(toSet());

        logicalFlowIds.forEach(lfId -> logicalFlowDecoratorService.addDecorators(lfId, dataTypeRefs, userName));

        return result;
    }


    public int[] removeDataTypes(String userName, EntityReference entityReference, Set<Long> dataTypeIds) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(dataTypeIds, "dataTypeIds cannot be null");

        Collection<DataTypeDecorator> specificationDataTypes
                = mkSpecificationDataTypes(userName, entityReference, dataTypeIds);

        int[] result = dataTypeDecoratorDaoSelectorFactory
                .getDao(entityReference.kind())
                .removeDataTypes(specificationDataTypes);

        audit("Removed", dataTypeIds, entityReference, userName);

        return result;
    }


    public int rippleDataTypesToLogicalFlows() {
        return physicalSpecDataTypeDao.rippleDataTypesToLogicalFlows();
    }


    private Collection<DataTypeDecorator> mkSpecificationDataTypes(String userName,
                                                                               EntityReference entityReference,
                                                                               Set<Long> dataTypeIds) {
        return map(
                    dataTypeIds,
                    dtId -> ImmutableDataTypeDecorator.builder()
                            .entityReference(entityReference)
                            .decoratorEntity(mkRef(DATA_TYPE, dtId))
                            .provenance("waltz")
                            .lastUpdatedAt(nowUtc())
                            .lastUpdatedBy(userName)
                            .build());
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
                return dao.findByEntityIdSelector(
                        genericSelectorFactory.applyForKind(APPLICATION, options).selector(),
                        Optional.of(APPLICATION));
            case ACTOR:
                return dao.findByEntityIdSelector(
                        DSL.select(DSL.val(options.entityReference().id())),
                        Optional.of(ACTOR));
            default:
                throw new UnsupportedOperationException("Cannot find decorators for selector kind: " + options.entityReference().kind());
        }
    }

    public Collection<DataTypeDecorator> findByFlowIds(List<Long> ids, EntityKind entityKind) {
        if (isEmpty(ids)) {
            return Collections.emptyList();
        }
        return dataTypeDecoratorDaoSelectorFactory
                .getDao(entityKind)
                .findByFlowIds(ids);
    }
}
