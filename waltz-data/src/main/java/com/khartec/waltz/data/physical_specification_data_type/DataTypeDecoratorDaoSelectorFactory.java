package com.khartec.waltz.data.physical_specification_data_type;

import com.khartec.waltz.model.EntityKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.khartec.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;

@Component
public class DataTypeDecoratorDaoSelectorFactory {
    private final PhysicalSpecDecoratorDao physicalSpecDecoratorDao;
    private final LogicalFlowDecoratorDao logicalFlowDecoratorDao;

    @Autowired
    public DataTypeDecoratorDaoSelectorFactory(PhysicalSpecDecoratorDao physicalSpecDecoratorDao,
                                               LogicalFlowDecoratorDao logicalFlowDecoratorDao) {
        this.physicalSpecDecoratorDao = physicalSpecDecoratorDao;
        this.logicalFlowDecoratorDao = logicalFlowDecoratorDao;
    }

    public DataTypeDecoratorDao getDao(EntityKind entityKind) {
        return PHYSICAL_SPECIFICATION.equals(entityKind)
                ? physicalSpecDecoratorDao
                : logicalFlowDecoratorDao;
    }
}
