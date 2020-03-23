package com.khartec.waltz.data.physical_specification_data_type;

import com.khartec.waltz.model.EntityKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataTypeDecoratorDaoSelectorFactory {
    private final DataTypeDecoratorDao physicalSpecDecoratorDao;

    @Autowired
    public DataTypeDecoratorDaoSelectorFactory(DataTypeDecoratorDao physicalSpecDecoratorDao) {
        this.physicalSpecDecoratorDao = physicalSpecDecoratorDao;
    }

    public DataTypeDecoratorDao getDao(EntityKind entityKind) {
        //if(entityKind.equals(EntityKind.PHYSICAL_SPECIFICATION))
            return physicalSpecDecoratorDao;
    }
}
