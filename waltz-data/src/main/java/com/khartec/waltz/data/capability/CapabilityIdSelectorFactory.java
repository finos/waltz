package com.khartec.waltz.data.capability;


import com.khartec.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;

@Service
public class CapabilityIdSelectorFactory extends AbstractIdSelectorFactory {


    @Autowired
    public CapabilityIdSelectorFactory(DSLContext dsl) {
        super(dsl, EntityKind.CAPABILITY);
    }

    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
                return mkForAppGroup(options);
            default:
                throw new UnsupportedOperationException("Cannot create capability selector from kind: "+options.entityReference().kind());
        }
    }

    private Select<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {
        switch (options.scope()) {
            case PARENTS:
                return mkForAppGroupParents(options.entityReference().id());
            default:
                throw new UnsupportedOperationException("Cannot create capability selector from APP_GROUP with scope: "+options.scope());
        }
    }

    private Select<Record1<Long>> mkForAppGroupParents(long id) {
        return DSL.selectDistinct(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .innerJoin(APP_CAPABILITY)
                    .on(APP_CAPABILITY.CAPABILITY_ID.eq(ENTITY_HIERARCHY.ID))
                .innerJoin(APPLICATION_GROUP_ENTRY)
                    .on(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(APP_CAPABILITY.APPLICATION_ID))
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(id));

    }
}
