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


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application_capability.ApplicationCapability;
import com.khartec.waltz.model.application_capability.GroupedApplications;
import com.khartec.waltz.model.application_capability.ImmutableApplicationCapability;
import com.khartec.waltz.model.application_capability.ImmutableGroupedApplications;
import com.khartec.waltz.model.capabilityrating.RagRating;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.AppCapabilityRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.AppCapability.APP_CAPABILITY;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.select;


@Deprecated
@Repository
public class AppCapabilityDao {

    private final DSLContext dsl;


    private final RecordMapper<Record, ApplicationCapability> TO_DOMAIN_MAPPER = r -> {
        AppCapabilityRecord record = r.into(APP_CAPABILITY);
        return ImmutableApplicationCapability.builder()
                .isPrimary(record.getIsPrimary())
                .capabilityId(record.getCapabilityId())
                .applicationId(record.getApplicationId())
                .rating(RagRating.valueOf(record.getRating()))
                .description(record.getDescription())
                .lastUpdatedAt(DateTimeUtilities.toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };

    private final BiFunction<DSLContext, ApplicationCapability, AppCapabilityRecord> TO_RECORD_MAPPER =
            (dsl, applicationCapability) -> {
                AppCapabilityRecord record = dsl.newRecord(APP_CAPABILITY);

                record.setApplicationId(applicationCapability.applicationId());
                record.setCapabilityId(applicationCapability.capabilityId());
                record.setRating(applicationCapability.rating().name());
                record.setDescription(applicationCapability.description());
                record.setIsPrimary(applicationCapability.isPrimary());

                record.setProvenance(applicationCapability.provenance());
                record.setLastUpdatedAt(Timestamp.valueOf(applicationCapability.lastUpdatedAt()));
                record.setLastUpdatedBy(applicationCapability.lastUpdatedBy());

                return record;
            };


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
                        .innerJoin(APPLICATION)
                            .on(APPLICATION.ID.eq(APP_CAPABILITY.APPLICATION_ID))
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


    public int removeCapabilityFromApp(long appId, Long capabilityId) {
        return dsl
                .deleteFrom(APP_CAPABILITY)
                .where(APP_CAPABILITY.APPLICATION_ID.eq(appId))
                .and(APP_CAPABILITY.CAPABILITY_ID.eq(capabilityId))
                .execute();

    }


    public List<ApplicationCapability> findByCapabilityIds(List<Long> capabilityIds) {
        return prepareSelect()
                .where(APP_CAPABILITY.CAPABILITY_ID.in(capabilityIds))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Collection<ApplicationCapability> findApplicationCapabilitiesForAppIdSelector(Select<Record1<Long>> selector) {
        return prepareSelect()
                .where(APP_CAPABILITY.APPLICATION_ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    private SelectJoinStep<Record> prepareSelect() {
        return dsl
                .select()
                .from(APP_CAPABILITY);
    }


    public Integer insert(ImmutableApplicationCapability applicationCapability) {
        AppCapabilityRecord record = TO_RECORD_MAPPER.apply(dsl, applicationCapability);
        return record.insert();
    }


    public Integer update(ImmutableApplicationCapability applicationCapability) {
        AppCapabilityRecord record = TO_RECORD_MAPPER.apply(dsl, applicationCapability);

        Condition condition = APP_CAPABILITY.APPLICATION_ID.eq(applicationCapability.applicationId())
                .and(APP_CAPABILITY.CAPABILITY_ID.eq(applicationCapability.capabilityId()));

        return dsl.executeUpdate(record, condition);
    }
}
