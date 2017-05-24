package com.khartec.waltz.service.physical_specification_data_type;

import com.khartec.waltz.data.physical_specification.PhysicalSpecificationSelectorFactory;
import com.khartec.waltz.data.physical_specification_data_type.PhysicalSpecDataTypeDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.physical_specification_data_type.PhysicalSpecificationDataType;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class PhysicalSpecDataTypeService {

    private final PhysicalSpecDataTypeDao physicalSpecDataTypeDao;
    private final PhysicalSpecificationSelectorFactory specificationSelectorFactory;


    @Autowired
    public PhysicalSpecDataTypeService(PhysicalSpecDataTypeDao physicalSpecDataTypeDao,
                                       PhysicalSpecificationSelectorFactory specificationSelectorFactory) {
        checkNotNull(physicalSpecDataTypeDao, "physicalSpecDataTypeDao cannot be null");
        checkNotNull(specificationSelectorFactory, "specificationSelectorFactory cannot be null");

        this.physicalSpecDataTypeDao = physicalSpecDataTypeDao;
        this.specificationSelectorFactory = specificationSelectorFactory;
    }


    public List<PhysicalSpecificationDataType> findBySpecificationId(long specId) {
        return physicalSpecDataTypeDao.findBySpecificationId(specId);
    }


    public List<PhysicalSpecificationDataType> findBySpecificationIdSelector(IdSelectionOptions selectionOptions) {
        checkNotNull(selectionOptions, "selectionOptions cannot be null");

        Select<Record1<Long>> selector = specificationSelectorFactory.apply(selectionOptions);
        return physicalSpecDataTypeDao.findBySpecificationIdSelector(selector);
    }
}
