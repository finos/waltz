package com.khartec.waltz.data.datatype_decorator;

import com.khartec.waltz.model.EntityKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        switch (entityKind) {
            case PHYSICAL_SPECIFICATION:
                return physicalSpecDecoratorDao;
            case LOGICAL_DATA_FLOW:
                return logicalFlowDecoratorDao;
            default:
                throw new IllegalArgumentException("Cannot create dao for entity kind: " + entityKind);
        }
    }
}
