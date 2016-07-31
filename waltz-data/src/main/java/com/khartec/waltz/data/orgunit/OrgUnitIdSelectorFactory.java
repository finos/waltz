package com.khartec.waltz.data.orgunit;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;

@Service
public class OrgUnitIdSelectorFactory implements IdSelectorFactory {

    private final DSLContext dsl;

    @Autowired
    public OrgUnitIdSelectorFactory(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {

        Select<Record1<Long>> ouSelector = null;
        switch (options.scope()) {
            case EXACT:
                ouSelector = DSL.select(DSL.val(options.entityReference().id()));
                break;
            case CHILDREN:
                ouSelector = DSL.select(ENTITY_HIERARCHY.ID)
                        .from(ENTITY_HIERARCHY)
                        .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(options.entityReference().id()))
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name()));
                break;
            case PARENTS:
                ouSelector = DSL.select(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .from(ENTITY_HIERARCHY)
                        .where(ENTITY_HIERARCHY.ID.eq(options.entityReference().id()))
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name()));
                break;
        }

        return ouSelector;
    }
}
