/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.physical_specification_data_type;

import com.khartec.waltz.data.physical_specification.PhysicalSpecificationIdSelectorFactory;
import com.khartec.waltz.data.physical_specification_data_type.PhysicalSpecDataTypeDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.physical_specification_data_type.ImmutablePhysicalSpecificationDataType;
import com.khartec.waltz.model.physical_specification_data_type.PhysicalSpecificationDataType;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class PhysicalSpecDataTypeService {

    private final ChangeLogService changeLogService;
    private final LogicalFlowDecoratorService logicalFlowDecoratorService;
    private final PhysicalFlowService physicalFlowService;
    private final PhysicalSpecDataTypeDao physicalSpecDataTypeDao;
    private final PhysicalSpecificationIdSelectorFactory specificationIdSelectorFactory;


    @Autowired
    public PhysicalSpecDataTypeService(ChangeLogService changeLogService,
                                       LogicalFlowDecoratorService logicalFlowDecoratorService,
                                       PhysicalFlowService physicalFlowService,
                                       PhysicalSpecDataTypeDao physicalSpecDataTypeDao,
                                       PhysicalSpecificationIdSelectorFactory specificationIdSelectorFactory) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowDecoratorService, "logicalFlowDecoratorService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(physicalSpecDataTypeDao, "physicalSpecDataTypeDao cannot be null");
        checkNotNull(specificationIdSelectorFactory, "specificationIdSelectorFactory cannot be null");

        this.changeLogService = changeLogService;
        this.logicalFlowDecoratorService = logicalFlowDecoratorService;
        this.physicalFlowService = physicalFlowService;
        this.physicalSpecDataTypeDao = physicalSpecDataTypeDao;
        this.specificationIdSelectorFactory = specificationIdSelectorFactory;
    }


    public PhysicalSpecificationDataType getBySpecIdAndDataTypeID(long specId, long dataTypeId) {
        return physicalSpecDataTypeDao.getBySpecIdAndDataTypeID(specId, dataTypeId);
    }


    public List<PhysicalSpecificationDataType> findBySpecificationId(long specId) {
        return physicalSpecDataTypeDao.findBySpecificationId(specId);
    }


    public List<PhysicalSpecificationDataType> findBySpecificationIdSelector(IdSelectionOptions selectionOptions) {
        checkNotNull(selectionOptions, "selectionOptions cannot be null");

        Select<Record1<Long>> selector = specificationIdSelectorFactory.apply(selectionOptions);
        return physicalSpecDataTypeDao.findBySpecificationIdSelector(selector);
    }


    public int[] addDataTypes(String userName, long specificationId, Set<Long> dataTypeIds) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(dataTypeIds, "dataTypeIds cannot be null");

        Collection<PhysicalSpecificationDataType> specificationDataTypes
                = mkSpecificationDataTypes(userName, specificationId, dataTypeIds);

        int[] result = physicalSpecDataTypeDao.addDataTypes(specificationDataTypes);

        audit("Added", dataTypeIds, specificationId, userName);

        // now update logical flow data types
        // find all physicals with this spec id, for each physical update it's logical decorators
        List<Long> logicalFlowIds = physicalFlowService
                .findBySpecificationId(specificationId)
                .stream()
                .map(f -> f.logicalFlowId())
                .collect(toList());

        Set<EntityReference> dataTypeRefs = dataTypeIds.stream()
                .map(id -> EntityReference.mkRef(EntityKind.DATA_TYPE, id))
                .collect(toSet());

        logicalFlowIds.forEach(lfId -> logicalFlowDecoratorService.addDecorators(lfId, dataTypeRefs, userName));

        return result;
    }


    public int[] removeDataTypes(String userName, long specificationId, Set<Long> dataTypeIds) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(dataTypeIds, "dataTypeIds cannot be null");

        Collection<PhysicalSpecificationDataType> specificationDataTypes
                = mkSpecificationDataTypes(userName, specificationId, dataTypeIds);

        int[] result = physicalSpecDataTypeDao.removeDataTypes(specificationDataTypes);

        audit("Removed", dataTypeIds, specificationId, userName);

        return result;
    }


    public int rippleDataTypesToLogicalFlows() {
        return physicalSpecDataTypeDao.rippleDataTypesToLogicalFlows();
    }


    private Collection<PhysicalSpecificationDataType> mkSpecificationDataTypes(String userName,
                                                                               long specificationId,
                                                                               Set<Long> dataTypeIds) {
        return map(
                    dataTypeIds,
                    dtId -> ImmutablePhysicalSpecificationDataType.builder()
                            .specificationId(specificationId)
                            .dataTypeId(dtId)
                            .provenance("waltz")
                            .lastUpdatedAt(nowUtc())
                            .lastUpdatedBy(userName)
                            .build());
    }


    private void audit(String verb,
                       Set<Long> dataTypeIds,
                       long specificationId,
                       String username) {

        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specificationId))
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
}
