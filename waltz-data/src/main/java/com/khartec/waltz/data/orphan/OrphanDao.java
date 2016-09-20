package com.khartec.waltz.data.orphan;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.orphan.ImmutableOrphanRelationship;
import com.khartec.waltz.model.orphan.OrphanRelationship;
import com.khartec.waltz.model.orphan.OrphanSide;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

@Repository
public class OrphanDao {

    private final DSLContext dsl;

    @Autowired
    public OrphanDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<OrphanRelationship> findApplicationsWithNonExistentOrgUnit() {
        return dsl.select(APPLICATION.ID, APPLICATION.NAME, APPLICATION.ORGANISATIONAL_UNIT_ID)
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID
                        .notIn(DSL.select(ORGANISATIONAL_UNIT.ID)
                                .from(ORGANISATIONAL_UNIT)))
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.APPLICATION, r.value1(), r.value2()))
                        .entityB(EntityReference.mkRef(EntityKind.ORG_UNIT, r.value3()))
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


        List<OrphanRelationship> missingCaps = dsl.select(APP_CAPABILITY.CAPABILITY_ID,
                APP_CAPABILITY.APPLICATION_ID)
                .from(APP_CAPABILITY)
                .where(missingCapability)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.CAPABILITY, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.B)
                        .build());


        List<OrphanRelationship> missingApps = dsl.select(APP_CAPABILITY.CAPABILITY_ID,
                APP_CAPABILITY.APPLICATION_ID)
                .from(APP_CAPABILITY)
                .where(missingApplication)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.CAPABILITY, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());


        return ListUtilities.concat(missingApps, missingCaps);
    }


    public List<OrphanRelationship> findOrphanAuthoritativeSourceByOrgUnit() {
        Condition missingOrgUnit = AUTHORITATIVE_SOURCE.PARENT_ID
                .notIn(DSL.select(ORGANISATIONAL_UNIT.ID)
                        .from(ORGANISATIONAL_UNIT))
                .and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name()));

       return dsl.select(AUTHORITATIVE_SOURCE.ID,
                AUTHORITATIVE_SOURCE.PARENT_ID)
                .from(AUTHORITATIVE_SOURCE)
                .where(missingOrgUnit)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.AUTHORITATIVE_SOURCE, r.value1()))
                        .entityB(mkRef(EntityKind.ORG_UNIT, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());

    }


    public List<OrphanRelationship> findOrphanAuthoritativeSourceByApp() {
        Condition missingApplication = AUTHORITATIVE_SOURCE.APPLICATION_ID
                .notIn(DSL.select(APPLICATION.ID)
                        .from(APPLICATION));


        return dsl.select(AUTHORITATIVE_SOURCE.ID,
                AUTHORITATIVE_SOURCE.APPLICATION_ID)
                .from(AUTHORITATIVE_SOURCE)
                .where(missingApplication)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.AUTHORITATIVE_SOURCE, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }


    public List<OrphanRelationship> findOrphanAuthoritiveSourceByDataType() {
        Condition missingDataType = AUTHORITATIVE_SOURCE.DATA_TYPE
                .notIn(DSL.select(DATA_TYPE.CODE)
                        .from(DATA_TYPE));


        return dsl.select(AUTHORITATIVE_SOURCE.ID,
                DATA_TYPE.ID,
                AUTHORITATIVE_SOURCE.DATA_TYPE)
                .from(AUTHORITATIVE_SOURCE)
                .leftJoin(DATA_TYPE)
                    .on(AUTHORITATIVE_SOURCE.DATA_TYPE.eq(DATA_TYPE.CODE))
                .where(missingDataType)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.AUTHORITATIVE_SOURCE, r.value1()))
                        .entityB(mkRef(EntityKind.DATA_TYPE, r.value2() != null ? r.value2() : -1, r.value3()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }

}
