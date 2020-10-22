/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.data.application;


import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.application.*;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.ApplicationRecord;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.schema.Tables.EXTERNAL_IDENTIFIER;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static java.util.Optional.ofNullable;


@Repository
public class ApplicationDao {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationDao.class);

    public static final RecordMapper<Record, Application> TO_DOMAIN_MAPPER = record -> {
        ApplicationRecord appRecord = record.into(APPLICATION);
        Application app = ImmutableApplication.builder()
                .name(appRecord.getName())
                .description(appRecord.getDescription())
                .assetCode(ofNullable(appRecord.getAssetCode()))
                .parentAssetCode(ofNullable(appRecord.getParentAssetCode()))
                .id(appRecord.getId())
                .isRemoved(appRecord.getIsRemoved())
                .organisationalUnitId(appRecord.getOrganisationalUnitId())
                .applicationKind(readEnum(appRecord.getKind(), ApplicationKind.class, (s) -> ApplicationKind.IN_HOUSE))
                .lifecyclePhase(readEnum(appRecord.getLifecyclePhase(), LifecyclePhase.class, (s) -> LifecyclePhase.DEVELOPMENT))
                .overallRating(readEnum(appRecord.getOverallRating(), RagRating.class, (s) -> RagRating.Z))
                .businessCriticality(readEnum(appRecord.getBusinessCriticality(), Criticality.class, c -> Criticality.UNKNOWN))
                .entityLifecycleStatus(readEnum(appRecord.getEntityLifecycleStatus(), EntityLifecycleStatus.class, s -> EntityLifecycleStatus.ACTIVE))
                .plannedRetirementDate(ofNullable(appRecord.getPlannedRetirementDate()).map(Timestamp::toLocalDateTime))
                .actualRetirementDate(ofNullable(appRecord.getActualRetirementDate()).map(Timestamp::toLocalDateTime))
                .commissionDate(ofNullable(appRecord.getCommissionDate()).map(Timestamp::toLocalDateTime))
                .provenance(appRecord.getProvenance())
                .build();

        return app;
    };


    public static final Condition IS_ACTIVE = APPLICATION.ENTITY_LIFECYCLE_STATUS
                                                          .eq(EntityLifecycleStatus.ACTIVE.name());


    private final DSLContext dsl;


    @Autowired
    public ApplicationDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Application getById(long id) {
        return dsl.select()
                .from(APPLICATION)
                .where(APPLICATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<Application> findAll() {
        return dsl.select()
                .from(APPLICATION)
                .where(IS_ACTIVE)
                .fetch(TO_DOMAIN_MAPPER);
    }

    public List<Application> findByIds(Collection<Long> ids) {
        return dsl.select()
                .from(APPLICATION)
                .where(APPLICATION.ID.in(ids))
                .fetch(TO_DOMAIN_MAPPER);

    }


    public List<Tally<Long>> countByOrganisationalUnit() {
        return JooqUtilities.calculateLongTallies(
                dsl,
                APPLICATION,
                APPLICATION.ORGANISATIONAL_UNIT_ID,
                IS_ACTIVE);
    }


    public List<Tally<String>> countByApplicationKind(Select<Record1<Long>> selector) {
        return JooqUtilities.calculateStringTallies(
                dsl,
                APPLICATION,
                APPLICATION.KIND,
                IS_ACTIVE.and(APPLICATION.ID.in(selector)));
    }


    /**
     * Given an appId will find all app records with:
     * <ul>
     *     <li>The same asset code (i.e. self/sharing the code)</li>
     *     <li>Same parent code (i.e. siblings)</li>
     *     <li>Any children (direct)</li>
     *     <li>The parent (direct)</li>
     * </ul>
     * @param appId
     * @return
     */
    public List<Application> findRelatedByApplicationId(long appId) {

        com.khartec.waltz.schema.tables.Application self = APPLICATION.as("self");
        com.khartec.waltz.schema.tables.Application rel = APPLICATION.as("rel");

        return dsl.select(rel.fields())
                .from(rel)
                .innerJoin(self)
                .on(rel.ASSET_CODE.eq(self.ASSET_CODE)) // shared code
                .or(rel.PARENT_ASSET_CODE.eq(self.PARENT_ASSET_CODE).and(self.PARENT_ASSET_CODE.ne(""))) // same parent
                .or(rel.PARENT_ASSET_CODE.eq(self.ASSET_CODE)) //  parent
                .or(rel.ASSET_CODE.eq(self.PARENT_ASSET_CODE).and(self.PARENT_ASSET_CODE.ne(""))) // child
                .where(self.ID.eq(appId))
                .and(rel.ENTITY_LIFECYCLE_STATUS
                        .eq(EntityLifecycleStatus.ACTIVE.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public AppRegistrationResponse registerApp(AppRegistrationRequest request) {

        checkNotEmpty(request.name(), "Cannot register app with no name");

        LOG.info("Registering new application: "+request);

        ApplicationRecord record = dsl.newRecord(APPLICATION);

        record.setName(request.name());
        record.setDescription(request.description().orElse("TBC"));
        record.setAssetCode(request.assetCode().orElse(""));
        record.setParentAssetCode(request.parentAssetCode().orElse(""));
        record.setOrganisationalUnitId(request.organisationalUnitId());
        record.setKind(request.applicationKind().name());
        record.setLifecyclePhase(request.lifecyclePhase().name());
        record.setOverallRating(request.overallRating().name());
        record.setUpdatedAt(Timestamp.from(Instant.now()));
        record.setBusinessCriticality(request.businessCriticality().name());
        record.setProvenance(request.provenance().orElse("waltz"));

        try {
            int count = record.insert();

            if (count == 1) {
                return ImmutableAppRegistrationResponse.builder()
                        .id(record.getId())
                        .message("created")
                        .originalRequest(request)
                        .build();
            } else {
                return ImmutableAppRegistrationResponse.builder()
                        .message("Failed")
                        .originalRequest(request)
                        .build();
            }

        } catch (DataAccessException dae) {
            return ImmutableAppRegistrationResponse.builder()
                    .message("Could not create app because: " + dae.getMessage())
                    .originalRequest(request)
                    .build();
        }
    }


    public int update(Application application) {

        checkNotNull(application, "application must not be null");

        LOG.info("Updating application: "+application);

        ApplicationRecord record = dsl.newRecord(APPLICATION);

        record.setName(application.name());
        record.setDescription(application.description());
        record.setAssetCode(application.assetCode().orElse(""));
        record.setParentAssetCode(application.parentAssetCode().orElse(""));
        record.setOrganisationalUnitId(application.organisationalUnitId());
        record.setLifecyclePhase(application.lifecyclePhase().name());
        record.setKind(application.applicationKind().name());
        record.setOverallRating(application.overallRating().name());
        record.setProvenance(application.provenance());
        record.setBusinessCriticality(application.businessCriticality().name());
        record.setIsRemoved(application.isRemoved());

        Condition condition = APPLICATION.ID.eq(application.id().get());

        return dsl.executeUpdate(record, condition);
    }


    public List<Application> findByAppIdSelector(Select<Record1<Long>> selector) {
        return dsl.selectFrom(APPLICATION)
                .where(APPLICATION.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<Application> findByAssetCode(String externalId) {
        checkNotNull(externalId, "externalId cannot be null");

        return dsl
                .select(APPLICATION.fields())
                .from(APPLICATION)
                .leftJoin(EXTERNAL_IDENTIFIER)
                .on(EXTERNAL_IDENTIFIER.ENTITY_ID.eq(APPLICATION.ID)
                        .and(EXTERNAL_IDENTIFIER.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(APPLICATION.ASSET_CODE.eq(externalId)
                        .or(EXTERNAL_IDENTIFIER.EXTERNAL_ID.eq(externalId)))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
