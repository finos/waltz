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

package com.khartec.waltz.jobs;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import com.khartec.waltz.schema.tables.records.LogicalFlowRecord;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import org.jooq.DSLContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;

public class WaltzUtilities {


    public static Long getOrCreateMeasurableCategory(DSLContext dsl, String externalId, String name) {

        Long categoryId = dsl
                .select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(externalId))
                .fetchOne(MEASURABLE_CATEGORY.ID);


        if (categoryId != null) {
            return categoryId;
        } else {
            MeasurableCategoryRecord measurableCategoryRecord = dsl.newRecord(MEASURABLE_CATEGORY);
            measurableCategoryRecord.setName(name);
            measurableCategoryRecord.setDescription(name);
            measurableCategoryRecord.setExternalId(externalId);
            measurableCategoryRecord.setRatingSchemeId(1L);
            measurableCategoryRecord.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
            measurableCategoryRecord.setLastUpdatedBy("admin");
            measurableCategoryRecord.store();
            return measurableCategoryRecord.getId();
        }
    }


    public static String toId(String t) {
        return StringUtilities.mkSafe(t).toLowerCase();
    }


    public static String toId(String... ts) {
        return Stream.of(ts)
                .map(WaltzUtilities::toId)
                .collect(Collectors.joining("_"));
    }


    public static List<Long> getActiveAppIds(DSLContext dsl) {
        return dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()))
                .fetch(APPLICATION.ID);
    }



    public static LogicalFlowDecoratorRecord mkLogicalFlowDecoratorRecord(long flowId, long dtId, String provenance) {
        LogicalFlowDecoratorRecord decorator = new LogicalFlowDecoratorRecord();
        decorator.setLogicalFlowId(flowId);
        decorator.setDecoratorEntityKind(EntityKind.DATA_TYPE.name());
        decorator.setDecoratorEntityId(dtId);
        decorator.setProvenance(provenance);
        decorator.setLastUpdatedAt(nowUtcTimestamp());
        decorator.setLastUpdatedBy("admin");
        decorator.setRating(AuthoritativenessRating.NO_OPINION.name());
        return decorator;
    }



    public static LogicalFlowRecord mkLogicalFlowRecord(long sourceAppId, long targetAppId, String provenance) {
        LogicalFlowRecord record = new LogicalFlowRecord();
        record.setSourceEntityId(sourceAppId);
        record.setTargetEntityId(targetAppId);
        record.setSourceEntityKind(EntityKind.APPLICATION.name());
        record.setTargetEntityKind(EntityKind.APPLICATION.name());
        record.setCreatedBy("admin");
        record.setCreatedAt(nowUtcTimestamp());
        record.setLastUpdatedBy("admin");
        record.setLastUpdatedAt(nowUtcTimestamp());
        record.setProvenance(provenance);
        record.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE.name());
        record.setIsRemoved(false);
        return record;
    }

}
