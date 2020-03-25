package com.khartec.waltz.data.physical_specification_data_type;

import com.khartec.waltz.model.EntityKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.khartec.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;

@Component
public class DataTypeDecoratorDaoSelectorFactory {
    private final PhysicalSpecDataTypeDecoratorDao physicalSpecDataTypeDecoratorDao;
    private final LogicalFlowDataTypeDecoratorDao logicalFlowDataTypeDecoratorDao;

    @Autowired
    public DataTypeDecoratorDaoSelectorFactory(PhysicalSpecDataTypeDecoratorDao physicalSpecDataTypeDecoratorDao,
                                               LogicalFlowDataTypeDecoratorDao logicalFlowDataTypeDecoratorDao) {
        this.physicalSpecDataTypeDecoratorDao = physicalSpecDataTypeDecoratorDao;
        this.logicalFlowDataTypeDecoratorDao = logicalFlowDataTypeDecoratorDao;
    }

    public DataTypeDecoratorDao getDao(EntityKind entityKind) {
        return PHYSICAL_SPECIFICATION.equals(entityKind)
                ? physicalSpecDataTypeDecoratorDao
                : logicalFlowDataTypeDecoratorDao;
    }
}
