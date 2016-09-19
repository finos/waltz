package com.khartec.waltz.data.orphan;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.orphan.ImmutableOrphanRelationship;
import com.khartec.waltz.model.orphan.OrphanRelationship;
import com.khartec.waltz.model.orphan.OrphanSide;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.*;
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


    public List<OrphanRelationship> findApplicationsWithNonExistentOrgUnit() {
        return dsl.select(APPLICATION.ID, APPLICATION.NAME, DSL.val(EntityKind.APPLICATION.name()), APPLICATION.ORGANISATIONAL_UNIT_ID)
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID
                        .notIn(DSL.select(ORGANISATIONAL_UNIT.ID)
                                .from(ORGANISATIONAL_UNIT)))
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(ImmutableEntityReference.builder()
                                .id(r.value1())
                                .kind(EntityKind.valueOf(r.value3()))
                                .build())
                        .entityB(ImmutableEntityReference.builder()
                                .id(r.value4())
                                .kind(EntityKind.ORG_UNIT)
                                .build())
                        .orphanSide(OrphanSide.A)
                        .build());
    }


    public List<OrphanRelationship> findOrphanApplicationCapabilities() {
        Condition missingCapability = APP_CAPABILITY.CAPABILITY_ID
                .notIn(DSL.select(CAPABILITY.ID)
                        .from(CAPABILITY));

        Condition missingApplication = APP_CAPABILITY.APPLICATION_ID
                .notIn(DSL.select(APPLICATION.ID)
                        .from(APPLICATION));


        List<ImmutableOrphanRelationship> missingCaps = dsl.select(APP_CAPABILITY.CAPABILITY_ID,
                APP_CAPABILITY.APPLICATION_ID,
                DSL.val(EntityKind.APP_CAPABILITY.name()))
                .from(APP_CAPABILITY)
                .where(missingCapability)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(ImmutableEntityReference.builder()
                                .id(r.value1())
                                .kind(EntityKind.CAPABILITY)
                                .build())
                        .entityB(ImmutableEntityReference.builder()
                                .id(r.value2())
                                .kind(EntityKind.APPLICATION)
                                .build())
                        .orphanSide(OrphanSide.B)
                        .build());


        List<ImmutableOrphanRelationship> missingApps = dsl.select(APP_CAPABILITY.CAPABILITY_ID,
                APP_CAPABILITY.APPLICATION_ID,
                DSL.val(EntityKind.APP_CAPABILITY.name()))
                .from(APP_CAPABILITY)
                .where(missingApplication)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(ImmutableEntityReference.builder()
                                .id(r.value1())
                                .kind(EntityKind.CAPABILITY)
                                .build())
                        .entityB(ImmutableEntityReference.builder()
                                .id(r.value2())
                                .kind(EntityKind.APPLICATION)
                                .build())
                        .orphanSide(OrphanSide.A)
                        .build());


        List<OrphanRelationship> union = new LinkedList<>();
        union.addAll(missingCaps);
        union.addAll(missingApps);
        return union;
    }


    public List<OrphanRelationship> findOrphanAuthoritativeSources() {
        Condition missingOrgUnit = AUTHORITATIVE_SOURCE.PARENT_ID
                .notIn(DSL.select(ORGANISATIONAL_UNIT.ID)
                        .from(ORGANISATIONAL_UNIT))
                .and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name()));

        Condition missingApplication = AUTHORITATIVE_SOURCE.APPLICATION_ID
                .notIn(DSL.select(APPLICATION.ID)
                        .from(APPLICATION));


        List<ImmutableOrphanRelationship> missingApps = dsl.select(AUTHORITATIVE_SOURCE.ID,
                AUTHORITATIVE_SOURCE.APPLICATION_ID)
                .from(AUTHORITATIVE_SOURCE)
                .where(missingApplication)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(ImmutableEntityReference.builder()
                                .id(r.value1())
                                .kind(EntityKind.AUTHORITATIVE_SOURCE)
                                .build())
                        .entityB(ImmutableEntityReference.builder()
                                .id(r.value2())
                                .kind(EntityKind.APPLICATION)
                                .build())
                        .orphanSide(OrphanSide.A)
                        .build());


        List<ImmutableOrphanRelationship> missingOrgUnits = dsl.select(AUTHORITATIVE_SOURCE.ID,
                AUTHORITATIVE_SOURCE.PARENT_ID)
                .from(AUTHORITATIVE_SOURCE)
                .where(missingOrgUnit)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(ImmutableEntityReference.builder()
                                .id(r.value1())
                                .kind(EntityKind.AUTHORITATIVE_SOURCE)
                                .build())
                        .entityB(ImmutableEntityReference.builder()
                                .id(r.value2())
                                .kind(EntityKind.ORG_UNIT)
                                .build())
                        .orphanSide(OrphanSide.A)
                        .build());


        List<OrphanRelationship> union = new LinkedList<>();
        union.addAll(missingApps);
        union.addAll(missingOrgUnits);
        return union;
    }

}
