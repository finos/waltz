/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.app_capability;


import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.applicationcapability.ApplicationCapability;
import com.khartec.waltz.model.applicationcapability.GroupedApplications;
import com.khartec.waltz.model.applicationcapability.ImmutableApplicationCapability;
import com.khartec.waltz.model.applicationcapability.ImmutableGroupedApplications;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.AppCapabilityRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.select;


@Repository
public class AppCapabilityDao {

    private final DSLContext dsl;


    private final RecordMapper<Record, ApplicationCapability> TO_DOMAIN_MAPPER =
            r -> ImmutableApplicationCapability.builder()
                    .isPrimary(r.getValue(APP_CAPABILITY.IS_PRIMARY))
                    .capabilityId(r.getValue(APP_CAPABILITY.CAPABILITY_ID))
                    .applicationId(r.getValue(APP_CAPABILITY.APPLICATION_ID))
                    .build();

    @Autowired
    public AppCapabilityDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<ApplicationCapability> findCapabilitiesForApp(long appId) {
        return prepareSelect()
                .where(APP_CAPABILITY.APPLICATION_ID.eq(appId))
                .fetch(TO_DOMAIN_MAPPER);

    }


    public List<ApplicationCapability> findAssociatedApplicationCapabilities(long capabilityId) {
        return prepareSelect()
                .where(APP_CAPABILITY.APPLICATION_ID.in(
                        select(APP_CAPABILITY.APPLICATION_ID)
                        .from(APP_CAPABILITY)
                        .where(APP_CAPABILITY.CAPABILITY_ID.eq(capabilityId))))
                .and(APP_CAPABILITY.CAPABILITY_ID.ne(capabilityId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public GroupedApplications findGroupedApplicationsByCapability(long capabilityId) {

        ImmutableGroupedApplications.Builder builder = ImmutableGroupedApplications.builder();

        ArrayList<Field<?>> projection = newArrayList(APPLICATION.fields());
        projection.add(APP_CAPABILITY.IS_PRIMARY);

        dsl.select(projection)
                .from(APPLICATION)
                .innerJoin(APP_CAPABILITY)
                .on(APP_CAPABILITY.APPLICATION_ID.eq(APPLICATION.ID))
                .where(APP_CAPABILITY.CAPABILITY_ID.eq(capabilityId))
                .forEach(r -> {
                    Application app = ApplicationDao.TO_DOMAIN_MAPPER.map(r);
                    if (r.getValue(APP_CAPABILITY.IS_PRIMARY)) {
                        builder.addPrimaryApps(app);
                    } else {
                        builder.addSecondaryApps(app);
                    }
                });

        return builder.build();
    }


    public List<Tally<Long>> tallyByCapabilityId() {
        return dsl.select(APP_CAPABILITY.CAPABILITY_ID, count(APP_CAPABILITY.APPLICATION_ID))
                .from(APP_CAPABILITY)
                .groupBy(APP_CAPABILITY.CAPABILITY_ID)
                .fetch(r -> ImmutableTally.<Long>builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());
    }


    public int[] addCapabilitiesToApp(Long appId, List<Long> capabilityIds) {

        List<InsertSetMoreStep<AppCapabilityRecord>> inserts = capabilityIds
                .stream()
                .map(capId -> dsl
                        .insertInto(APP_CAPABILITY)
                        .set(APP_CAPABILITY.APPLICATION_ID, appId)
                        .set(APP_CAPABILITY.CAPABILITY_ID, capId))
                .collect(Collectors.toList());

        return dsl
                .batch(inserts)
                .execute();
    }


    public int setIsPrimary(long id, long capabilityId, boolean isPrimary) {
        return dsl.update(APP_CAPABILITY)
                .set(APP_CAPABILITY.IS_PRIMARY, isPrimary)
                .where(APP_CAPABILITY.APPLICATION_ID.eq(id))
                .and(APP_CAPABILITY.CAPABILITY_ID.eq(capabilityId))
                .execute();

    }


    public int[] removeCapabilitiesFromApp(long appId, List<Long> capabilityIds) {
        List<DeleteConditionStep<AppCapabilityRecord>> deletes = capabilityIds
                .stream()
                .map(capId -> dsl
                        .deleteFrom(APP_CAPABILITY)
                        .where(APP_CAPABILITY.APPLICATION_ID.eq(appId))
                        .and(APP_CAPABILITY.CAPABILITY_ID.eq(capId)))
                .collect(Collectors.toList());

        return dsl.batch(deletes).execute();
    }


    public List<ApplicationCapability> findByCapabilityIds(List<Long> capabilityIds) {
        return prepareSelect()
                .where(APP_CAPABILITY.APPLICATION_ID.in(
                        select(APP_CAPABILITY.APPLICATION_ID)
                                .from(APP_CAPABILITY)
                                .where(APP_CAPABILITY.CAPABILITY_ID.in(capabilityIds))))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<ApplicationCapability> findApplicationCapabilitiesForAppIdSelector(Select<Record1<Long>> selector) {
        return prepareSelect()
                .where(APP_CAPABILITY.APPLICATION_ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    private SelectJoinStep<Record> prepareSelect() {
        return dsl
                .select(APP_CAPABILITY.fields())
                .from(APP_CAPABILITY);
    }

}
