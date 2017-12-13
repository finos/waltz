/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
