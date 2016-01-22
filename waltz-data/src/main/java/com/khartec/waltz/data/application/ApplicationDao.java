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

package com.khartec.waltz.data.application;


import com.khartec.waltz.model.application.*;
import com.khartec.waltz.model.capabilityrating.RagRating;
import com.khartec.waltz.model.tally.ImmutableLongTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.ApplicationRecord;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotEmptyString;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;


@Repository
public class ApplicationDao {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationDao.class);

    private final DSLContext dsl;

    public static final RecordMapper<Record, Application> applicationRecordMapper = record -> {
        ApplicationRecord appRecord = record.into(APPLICATION);
        return ImmutableApplication.builder()
                .name(appRecord.getName())
                .description(appRecord.getDescription())
                .assetCode(Optional.ofNullable(appRecord.getAssetCode()))
                .parentAssetCode(Optional.ofNullable(appRecord.getParentAssetCode()))
                .id(appRecord.getId())
                .organisationalUnitId(appRecord.getOrganisationalUnitId())
                .kind(readEnum(appRecord.getKind(), ApplicationKind.class, ApplicationKind.IN_HOUSE))
                .lifecyclePhase(readEnum(appRecord.getLifecyclePhase(), LifecyclePhase.class, LifecyclePhase.DEVELOPMENT))
                .overallRating(readEnum(appRecord.getOverallRating(), RagRating.class, RagRating.Z))
                .build();
    };


    @Autowired
    public ApplicationDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Application getById(long id) {
        return dsl.select()
                .from(APPLICATION)
                .where(APPLICATION.ID.eq(id))
                .fetchOne(applicationRecordMapper);
    }


    public List<Application> findByOrganisationalUnitId(long id) {
        return dsl.select()
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(id))
                .fetch(applicationRecordMapper);
    }



    public List<Application> findByOrganisationalUnitIds(List<Long> ids) {
        return dsl.select()
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.in(ids))
                .fetch(applicationRecordMapper);
    }


    public List<Application> getAll() {
        return dsl.select()
                .from(APPLICATION)
                .fetch(applicationRecordMapper);
    }

    public List<Application> findByIds(List<Long> ids) {
        return dsl.select()
                .from(APPLICATION)
                .where(APPLICATION.ID.in(ids))
                .fetch(applicationRecordMapper);

    }

    public List<Tally> countByOrganisationalUnit() {
        return dsl.select(APPLICATION.ORGANISATIONAL_UNIT_ID, DSL.count())
                .from(APPLICATION)
                .groupBy(APPLICATION.ORGANISATIONAL_UNIT_ID)
                .fetch(r -> ImmutableLongTally.builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());
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
                .fetch(applicationRecordMapper);
    }


    private static final String SEARCH_POSTGRES
            = "SELECT *, "
            + " ts_rank_cd(setweight(to_tsvector(name), 'A') "
            + "     || setweight(to_tsvector(description), 'D') "
            + "     || setweight(to_tsvector(coalesce(asset_code, '')), 'A') "
            + "     || setweight(to_tsvector(coalesce(parent_asset_code, '')), 'A'), "
            + "     plainto_tsquery(?)"
            + " ) AS rank"
            + " FROM application"
            + " WHERE setweight(to_tsvector(name), 'A') "
            + "     || setweight(to_tsvector(description), 'D') "
            + "     || setweight(to_tsvector(coalesce(asset_code, '')), 'A') "
            + "     || setweight(to_tsvector(coalesce(parent_asset_code, '')), 'A') "
            + "     @@ plainto_tsquery(?)"
            + " ORDER BY rank DESC"
            + " LIMIT 20";


    private static final String SEARCH_MARIADB
            = "SELECT * FROM application\n"
            + " WHERE\n"
            + "  MATCH(name, description, asset_code, parent_asset_code)\n"
            + "  AGAINST (?)\n"
            + " LIMIT 20";


    public List<Application> search(String query) {
        if (dsl.dialect() == SQLDialect.POSTGRES) {
            Result<Record> records = dsl.fetch(SEARCH_POSTGRES, query, query);
            return records.map(applicationRecordMapper);
        }
        if (dsl.dialect() == SQLDialect.MARIADB) {
            Result<Record> records = dsl.fetch(SEARCH_MARIADB, query);
            return records.map(applicationRecordMapper);
        }

        LOG.error("Could not find full text query for database dialect: " + dsl.dialect());
        return Collections.emptyList();
    }


    public AppRegistrationResponse registerApp(AppRegistrationRequest request) {

        checkNotEmptyString(request.name(), "Cannot register app with no name");

        LOG.info("Registering new application: "+request);

        ApplicationRecord record = dsl.newRecord(APPLICATION);

        record.setName(request.name());
        record.setDescription(request.description().orElse("TBC"));
        record.setAssetCode(request.assetCode().orElse(""));
        record.setParentAssetCode(request.parentAssetCode().orElse(""));
        record.setOrganisationalUnitId(request.organisationalUnitId());
        record.setKind(request.kind().name());
        record.setLifecyclePhase(request.lifecyclePhase().name());
        record.setOverallRating(request.overallRating().name());

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

        record.setId(application.id().get());
        record.setName(application.name());
        record.setDescription(application.description());
        record.setAssetCode(application.assetCode().orElse(""));
        record.setParentAssetCode(application.parentAssetCode().orElse(""));
        record.setOrganisationalUnitId(application.organisationalUnitId());
        record.setLifecyclePhase(application.lifecyclePhase().name());
        record.setKind(application.kind().name());
        record.setOverallRating(application.overallRating().name());

        return record.update();
    }

}
