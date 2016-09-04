package com.khartec.waltz.data.entity_hierarchy;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.EntityHierarchy;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;


@Service
public class EntityRootsSelectorFactory implements Function<EntityKind, Select<Record1<Long>>> {

    private final DSLContext dsl;
    private static final EntityHierarchy eh = ENTITY_HIERARCHY;


    @Autowired
    public EntityRootsSelectorFactory(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    @Override
    public Select<Record1<Long>> apply(EntityKind entityKind) {
        return dsl.select(eh.ID)
                .from(eh)
                .where(eh.LEVEL.eq(1)
                        .and(eh.ID.eq(eh.ANCESTOR_ID)
                                .and(eh.KIND.eq(entityKind.name()))));
    }

}
