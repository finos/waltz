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

package com.khartec.waltz.data.source_data_rating;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.model.source_data_rating.ImmutableSourceDataRating;
import com.khartec.waltz.model.source_data_rating.SourceDataRating;
import com.khartec.waltz.schema.tables.records.SourceDataRatingRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static com.khartec.waltz.schema.tables.SourceDataRating.SOURCE_DATA_RATING;


@Repository
public class SourceDataRatingDao {

    private static final RecordMapper<? super Record, SourceDataRating> MAPPER = r -> {
        SourceDataRatingRecord record = r.into(SOURCE_DATA_RATING);

        Optional<LocalDateTime> lastImportDateTime = Optional
                .ofNullable(record.getLastImport())
                .map(t -> t.toLocalDateTime());

        return ImmutableSourceDataRating.builder()
                .sourceName(record.getSourceName())
                .entityKind(EntityKind.valueOf(record.getEntityKind()))
                .authoritativeness(RagRating.valueOf(record.getAuthoritativeness()))
                .accuracy(RagRating.valueOf(record.getAccuracy()))
                .completeness(RagRating.valueOf(record.getCompleteness()))
                .lastImportDate(lastImportDateTime)
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public SourceDataRatingDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Collection<SourceDataRating> findAll() {
        return dsl.select(SOURCE_DATA_RATING.fields())
                .from(SOURCE_DATA_RATING)
                .fetch(MAPPER);
    }

}
