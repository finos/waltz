package com.khartec.waltz.data.app_group;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.ApplicationGroupOuEntry.APPLICATION_GROUP_OU_ENTRY;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

@Repository
public class AppGroupOrganisationalUnitDao {
    private final DSLContext dsl;

    @Autowired
    public AppGroupOrganisationalUnitDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<EntityReference> getEntriesForGroup(long groupId) {
        SelectConditionStep<Record3<Long, String, String>> q = dsl.select(ORGANISATIONAL_UNIT.ID, ORGANISATIONAL_UNIT.NAME, ORGANISATIONAL_UNIT.DESCRIPTION)
                .from(ORGANISATIONAL_UNIT)
                .where(
                        ORGANISATIONAL_UNIT.ID.in(
                                DSL.select(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID)
                                        .from(APPLICATION_GROUP_OU_ENTRY)
                                        .where(APPLICATION_GROUP_OU_ENTRY.GROUP_ID.eq(groupId))
                        )
                );
        return q
                .fetch(r -> ImmutableEntityReference.builder()
                        .id(r.getValue(ORGANISATIONAL_UNIT.ID))
                        .name(r.getValue(ORGANISATIONAL_UNIT.NAME))
                        .description(r.getValue(ORGANISATIONAL_UNIT.DESCRIPTION))
                        .kind(EntityKind.APPLICATION)
                        .build()
                );
    }

    public int removeOrgUnit(long groupId, long orgUnitId) {
        return dsl.delete(APPLICATION_GROUP_OU_ENTRY)
                .where(APPLICATION_GROUP_OU_ENTRY.GROUP_ID.eq(groupId))
                .and(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID.eq(orgUnitId))
                .execute();
    }

    public int addOrgUnit(long groupId, long orgUnitId) {
        return dsl.insertInto(APPLICATION_GROUP_OU_ENTRY)
                .set(APPLICATION_GROUP_OU_ENTRY.GROUP_ID, groupId)
                .set(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID, orgUnitId)
                .onDuplicateKeyIgnore()
                .execute();
    }
}
