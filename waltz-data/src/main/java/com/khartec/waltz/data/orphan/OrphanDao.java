package com.khartec.waltz.data.orphan;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

@Repository
public class OrphanDao {

    private static final Logger LOG = LoggerFactory.getLogger(OrphanDao.class);
    private final DSLContext dsl;

    @Autowired
    public OrphanDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<EntityReference> findApplicationsWithNonExistingOrgUnit() {
        return dsl.select(APPLICATION.ID, APPLICATION.NAME, DSL.val(EntityKind.APPLICATION.name()))
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID
                        .notIn(DSL.select(ORGANISATIONAL_UNIT.ID)
                                .from(ORGANISATIONAL_UNIT)))
                .fetch(TO_ENTITY_REFERENCE);
    }

}
