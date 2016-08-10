package com.khartec.waltz.data.data_type;


import com.khartec.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataTypeIdSelectorFactory extends AbstractIdSelectorFactory {


    @Autowired
    public DataTypeIdSelectorFactory(DSLContext dsl) {
        super(dsl, EntityKind.DATA_TYPE);
    }

    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        throw new UnsupportedOperationException("Cannot create dataType selector from kind: " +
                options.entityReference().kind());
    }
}
