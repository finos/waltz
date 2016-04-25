package com.khartec.waltz.data.entity_relationship;

import com.khartec.waltz.common.Checks;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class EntityRelationshipDao {

    private final DSLContext dsl;

    @Autowired
    public EntityRelationshipDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

}
