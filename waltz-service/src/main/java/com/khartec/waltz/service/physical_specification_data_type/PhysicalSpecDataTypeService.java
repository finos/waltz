package com.khartec.waltz.service.physical_specification_data_type;

import com.khartec.waltz.data.physical_specification.PhysicalSpecificationIdSelectorFactory;
import com.khartec.waltz.data.physical_specification_data_type.PhysicalSpecDataTypeDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.physical_specification_data_type.ImmutablePhysicalSpecificationDataType;
import com.khartec.waltz.model.physical_specification_data_type.PhysicalSpecificationDataType;
import com.khartec.waltz.service.changelog.ChangeLogService;
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

@Service
public class PhysicalSpecDataTypeService {

    private final ChangeLogService changeLogService;
    private final PhysicalSpecDataTypeDao physicalSpecDataTypeDao;
    private final PhysicalSpecificationIdSelectorFactory specificationIdSelectorFactory;


    @Autowired
    public PhysicalSpecDataTypeService(ChangeLogService changeLogService,
                                       PhysicalSpecDataTypeDao physicalSpecDataTypeDao,
                                       PhysicalSpecificationIdSelectorFactory specificationIdSelectorFactory) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(physicalSpecDataTypeDao, "physicalSpecDataTypeDao cannot be null");
        checkNotNull(specificationIdSelectorFactory, "specificationIdSelectorFactory cannot be null");

        this.changeLogService = changeLogService;
        this.physicalSpecDataTypeDao = physicalSpecDataTypeDao;
        this.specificationIdSelectorFactory = specificationIdSelectorFactory;
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
